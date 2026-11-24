package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.view.Display
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

class SystemTelemetryRepository(private val context: Context) {

  private val activityManager =
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  private val batteryManager =
    context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
  private val sensorManager =
    context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private val displayManager =
    context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
  private val powerManager =
    context.getSystemService(Context.POWER_SERVICE) as PowerManager
  private val packageManager = context.packageManager

  private var prevCpuTotal: Long = 0L
  private var prevCpuIdle: Long = 0L
  private var prevRxBytes: Long = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
  private var prevTxBytes: Long = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
  private var prevTrafficTimeMs: Long = SystemClock.elapsedRealtime()

  private var cachedHardwareSpecs: HardwareSpecsSnapshot? = null
  private var cachedSensors: List<SensorItem>? = null
  private var cachedAppMeta: List<CachedAppMeta>? = null
  private val cachedDesignCapacityMah: Int? by lazy { readDesignCapacityMah() }

  private data class CachedAppMeta(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val targetSdk: Int,
    val apkSizeBytes: Long,
    val isSystemApp: Boolean,
    val uid: Int,
    val icon: android.graphics.drawable.Drawable?,
  )

  fun captureSnapshot(includeApps: Boolean = true): CominitechSnapshot {
    val nowMs = System.currentTimeMillis()
    val thermals = readThermalZones()
    val ram = readRamTelemetry()
    val cpu = readCpuTelemetry(thermals)
    val storage = readStorageTelemetry()
    val battery = readBatteryTelemetry()
    val network = readNetworkTelemetry()
    val hardware = cachedHardwareSpecs ?: buildHardwareSpecs().also { cachedHardwareSpecs = it }
    val sensors = cachedSensors ?: readSensors().also { cachedSensors = it }
    val runningProcesses = readRunningProcesses()
    val installedApps =
      if (includeApps) buildInstalledApps(runningProcesses) else emptyList()
    val threads = readJvmThreads()

    return CominitechSnapshot(
      timestampMs = nowMs,
      ram = ram,
      cpu = cpu,
      storage = storage,
      battery = battery,
      network = network,
      hardware = hardware,
      thermalZones = thermals,
      sensors = sensors,
      runningProcesses = runningProcesses,
      installedApps = installedApps,
      jvmThreads = threads,
    )
  }

  fun trimBackgroundMemory(): Pair<Float, Int> {
    val beforeInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(beforeInfo)

    val apps = cachedAppMeta ?: loadInstalledAppMetadata().also { cachedAppMeta = it }
    var trimmed = 0
    for (app in apps) {
      if (app.packageName != context.packageName) {
        runCatching {
          activityManager.killBackgroundProcesses(app.packageName)
          trimmed++
        }
      }
    }
    Runtime.getRuntime().gc()

    val afterInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(afterInfo)
    val freedMb = ((afterInfo.availMem - beforeInfo.availMem).coerceAtLeast(0L)) / (1024f * 1024f)
    return freedMb to trimmed
  }

  fun killBackgroundPackage(packageName: String) {
    runCatching { activityManager.killBackgroundProcesses(packageName) }
  }

  private fun readRamTelemetry(): RamTelemetry {
    val memInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)

    val procMem = parseProcMemInfo()
    val total = memInfo.totalMem.coerceAtLeast(procMem["MemTotal"] ?: 1L)
    val avail = memInfo.availMem.coerceIn(0L, total)
    val used = (total - avail).coerceAtLeast(0L)
    val usedPercent = if (total > 0L) (used.toFloat() / total.toFloat()) * 100f else 0f

    val cached = procMem["Cached"] ?: 0L
    val buffers = procMem["Buffers"] ?: 0L
    val active = procMem["Active"] ?: (used - cached).coerceAtLeast(0L)
    val swapTotal = procMem["SwapTotal"] ?: 0L
    val swapFree = procMem["SwapFree"] ?: swapTotal
    val swapUsed = (swapTotal - swapFree).coerceAtLeast(0L)

    val selfMem = Debug.MemoryInfo()
    Debug.getMemoryInfo(selfMem)

    val rt = Runtime.getRuntime()
    val jvmUsed = (rt.totalMemory() - rt.freeMemory()).coerceAtLeast(0L)
    val jvmMax = rt.maxMemory().coerceAtLeast(1L)

    return RamTelemetry(
      totalBytes = total,
      availableBytes = avail,
      usedBytes = used,
      usedPercent = usedPercent,
      lowMemoryThresholdBytes = memInfo.threshold,
      isLowMemory = memInfo.lowMemory,
      cachedBytes = cached,
      buffersBytes = buffers,
      activeBytes = active,
      swapTotalBytes = swapTotal,
      swapUsedBytes = swapUsed,
      appPssKb = selfMem.totalPss,
      jvmUsedBytes = jvmUsed,
      jvmMaxBytes = jvmMax,
    )
  }

  private fun parseProcMemInfo(): Map<String, Long> {
    val map = mutableMapOf<String, Long>()
    runCatching {
      File("/proc/meminfo").forEachLine { line ->
        val parts = line.split(":", limit = 2)
        if (parts.size == 2) {
          val key = parts[0].trim()
          val kbStr = parts[1].trim().split(Regex("\\s+")).firstOrNull()
          val kb = kbStr?.toLongOrNull()
          if (kb != null) {
            map[key] = kb * 1024L
          }
        }
      }
    }
    return map
  }

  private fun readCpuTelemetry(thermals: List<ThermalZoneInfo>): CpuTelemetry {
    val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
    val cores = mutableListOf<CpuCoreInfo>()
    var activeCores = 0
    var sumCurMhz = 0
    var maxClockAll = 0

    for (i in 0 until coreCount) {
      val basePath = "/sys/devices/system/cpu/cpu$i"
      val online =
        if (i == 0) true
        else readIntFromFile("$basePath/online")?.let { it == 1 } ?: true

      val curKhz = readIntFromFile("$basePath/cpufreq/scaling_cur_freq") ?: 0
      val minKhz = readIntFromFile("$basePath/cpufreq/cpuinfo_min_freq") ?: 0
      val maxKhz = readIntFromFile("$basePath/cpufreq/cpuinfo_max_freq") ?: 0
      val governor =
        readStringFromFile("$basePath/cpufreq/scaling_governor")?.ifBlank { "schedutil" }
          ?: "schedutil"

      val curMhz = curKhz / 1000
      val minMhz = minKhz / 1000
      val maxMhz = maxKhz / 1000

      if (online) {
        activeCores++
        sumCurMhz += curMhz
      }
      if (maxMhz > maxClockAll) maxClockAll = maxMhz

      val util =
        if (!online || maxMhz <= 0) 0f
        else if (maxMhz > minMhz) {
          (((curMhz - minMhz).coerceAtLeast(0)).toFloat() / (maxMhz - minMhz).toFloat()) * 100f
        } else {
          (curMhz.toFloat() / maxMhz.toFloat()) * 100f
        }

      cores.add(
        CpuCoreInfo(
          index = i,
          currentMhz = curMhz,
          minMhz = minMhz,
          maxMhz = maxMhz,
          governor = governor,
          isOnline = online,
          utilizationPercent = util.coerceIn(0f, 100f),
        )
      )
    }

    val kernelStatLoad = readProcStatCpuLoad()
    val avgClock = if (activeCores > 0) sumCurMhz / activeCores else 0
    val clockScaleLoad =
      if (cores.isNotEmpty()) {
        val onlineCores = cores.filter { it.isOnline && it.maxMhz > 0 }
        if (onlineCores.isNotEmpty()) {
          onlineCores.map { it.utilizationPercent }.average().toFloat()
        } else 0f
      } else 0f

    val loadPercent = kernelStatLoad ?: clockScaleLoad
    val isKernelStat = kernelStatLoad != null

    val cpuZoneTemp =
      thermals
        .filter { z ->
          val n = z.name.lowercase()
          n.contains("cpu") || n.contains("soc") || n.contains("tsens") || n.contains("ap")
        }
        .maxOfOrNull { it.temperatureCelsius }
        ?: thermals.maxOfOrNull { it.temperatureCelsius }

    return CpuTelemetry(
      coreCount = coreCount,
      activeCoreCount = activeCores.coerceAtLeast(1),
      loadPercent = loadPercent.coerceIn(0f, 100f),
      loadSourceIsKernelStat = isKernelStat,
      averageClockMhz = avgClock,
      maxClockMhz = maxClockAll,
      cores = cores,
      elapsedProcessCpuMs = Process.getElapsedCpuTime(),
      activeThreadCount = Thread.activeCount(),
      peakCpuTempCelsius = cpuZoneTemp,
    )
  }

  private fun readProcStatCpuLoad(): Float? {
    return runCatching {
      val firstLine = File("/proc/stat").bufferedReader().use { it.readLine() } ?: return null
      if (!firstLine.startsWith("cpu ")) return null
      val tokens = firstLine.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
      if (tokens.size < 4) return null

      val idle = tokens[3] + (tokens.getOrNull(4) ?: 0L)
      val total = tokens.sum()
      val diffTotal = total - prevCpuTotal
      val diffIdle = idle - prevCpuIdle

      val usage =
        if (prevCpuTotal > 0L && diffTotal > 0L) {
          ((diffTotal - diffIdle).toFloat() / diffTotal.toFloat()) * 100f
        } else {
          null
        }

      prevCpuTotal = total
      prevCpuIdle = idle
      usage
    }.getOrNull()
  }

  private fun readStorageTelemetry(): StorageTelemetry {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.totalBytes.coerceAtLeast(1L)
    val avail = stat.availableBytes.coerceIn(0L, total)
    val used = (total - avail).coerceAtLeast(0L)
    val percent = (used.toFloat() / total.toFloat()) * 100f
    return StorageTelemetry(
      internalTotalBytes = total,
      internalAvailableBytes = avail,
      internalUsedBytes = used,
      internalUsedPercent = percent.coerceIn(0f, 100f),
    )
  }

  private fun readBatteryTelemetry(): BatteryTelemetry {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.coerceAtLeast(1) ?: 100
    val pct =
      if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100)
      else batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)

    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
      ?: BatteryManager.BATTERY_STATUS_UNKNOWN
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
    val chargeSource =
      when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> if (isCharging) "Charging" else "Battery"
      }

    val health =
      intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
    val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.ifBlank { "Li-ion" } ?: "Li-ion"

    val rawCurrentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    val currentMa =
      if (rawCurrentUa != Int.MIN_VALUE && rawCurrentUa != 0) {
        if (kotlin.math.abs(rawCurrentUa) > 10_000) rawCurrentUa / 1000 else rawCurrentUa
      } else {
        null
      }

    val powerWatts =
      if (currentMa != null && voltageMv > 0) {
        (voltageMv / 1000f) * (kotlin.math.abs(currentMa) / 1000f)
      } else null

    val rawChargeUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val currentChargeMah =
      if (rawChargeUah != Int.MIN_VALUE && rawChargeUah > 0) {
        if (rawChargeUah > 20_000) rawChargeUah / 1000 else rawChargeUah
      } else null

    val sysChargeFull =
      readIntFromFile("/sys/class/power_supply/battery/charge_full")?.let {
        if (it > 20_000) it / 1000 else it
      }
    val sysDesignCap =
      readIntFromFile("/sys/class/power_supply/battery/charge_full_design")?.let {
        if (it > 20_000) it / 1000 else it
      }

    val designMah = sysDesignCap ?: cachedDesignCapacityMah

    val estimatedFullMah =
      sysChargeFull
        ?: if (currentChargeMah != null && currentChargeMah > 0 && pct >= 15) {
          ((currentChargeMah.toFloat() / (pct / 100f))).toInt()
        } else null

    val healthPercent =
      if (designMah != null && designMah > 0 && estimatedFullMah != null && estimatedFullMah > 0) {
        ((estimatedFullMah.toFloat() / designMah.toFloat()) * 100f).coerceIn(30f, 100f)
      } else {
        when (health) {
          BatteryManager.BATTERY_HEALTH_GOOD -> null
          BatteryManager.BATTERY_HEALTH_DEAD -> 0f
          else -> null
        }
      }

    val apiCycleCount =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)?.takeIf { it >= 0 }
      } else null
    val sysCycleCount = readIntFromFile("/sys/class/power_supply/battery/cycle_count")?.takeIf { it >= 0 }
    val cycleCount = apiCycleCount ?: sysCycleCount

    val thermalStatus =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching { powerManager.currentThermalStatus }.getOrDefault(0)
      } else 0

    return BatteryTelemetry(
      levelPercent = pct,
      isCharging = isCharging,
      chargeSource = chargeSource,
      statusCode = status,
      healthCode = health,
      temperatureCelsius = tempTenths / 10f,
      voltageMv = voltageMv,
      currentMa = currentMa,
      powerWatts = powerWatts,
      technology = tech,
      designCapacityMah = designMah,
      currentChargeMah = currentChargeMah,
      estimatedFullMah = estimatedFullMah,
      healthPercent = healthPercent,
      cycleCount = cycleCount,
      systemThermalStatus = thermalStatus,
    )
  }

  private fun readDesignCapacityMah(): Int? {
    return runCatching {
      val cls = Class.forName("com.android.internal.os.PowerProfile")
      val ctor = cls.getConstructor(Context::class.java)
      val instance = ctor.newInstance(context)
      val method = cls.getMethod("getBatteryCapacity")
      val cap = (method.invoke(instance) as? Double)?.toInt()
      cap?.takeIf { it in 500..25000 }
    }.getOrNull()
  }

  private fun readNetworkTelemetry(): NetworkTelemetry {
    val activeNetwork = connectivityManager.activeNetwork
    val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
    val isConnected =
      caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val type =
      when {
        caps == null -> "Offline"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> if (isConnected) "Active" else "Offline"
      }

    val now = SystemClock.elapsedRealtime()
    val totalRx = TrafficStats.getTotalRxBytes().let { if (it >= 0L) it else 0L }
    val totalTx = TrafficStats.getTotalTxBytes().let { if (it >= 0L) it else 0L }

    val elapsedSec = ((now - prevTrafficTimeMs).coerceAtLeast(250L)) / 1000.0
    val rxRate = (((totalRx - prevRxBytes).coerceAtLeast(0L)) / elapsedSec).toLong()
    val txRate = (((totalTx - prevTxBytes).coerceAtLeast(0L)) / elapsedSec).toLong()

    prevRxBytes = totalRx
    prevTxBytes = totalTx
    prevTrafficTimeMs = now

    return NetworkTelemetry(
      connectionType = type,
      isConnected = isConnected,
      rxBytesPerSec = rxRate,
      txBytesPerSec = txRate,
      totalRxBytes = totalRx,
      totalTxBytes = totalTx,
    )
  }

  private fun buildHardwareSpecs(): HardwareSpecsSnapshot {
    val cpuInfoMap = parseProcCpuInfo()
    val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

    var minMhz = Int.MAX_VALUE
    var maxMhz = 0
    var gov = "schedutil"
    for (i in 0 until coreCount) {
      val minK = readIntFromFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq")
      val maxK = readIntFromFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
      if (minK != null && minK / 1000 < minMhz) minMhz = minK / 1000
      if (maxK != null && maxK / 1000 > maxMhz) maxMhz = maxK / 1000
      if (i == 0) {
        gov = readStringFromFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor") ?: gov
      }
    }
    val clockRange =
      if (maxMhz > 0 && minMhz != Int.MAX_VALUE) "$minMhz - $maxMhz MHz"
      else if (maxMhz > 0) "Up to $maxMhz MHz"
      else "Dynamic"

    val socVendor =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MANUFACTURER.ifBlank { Build.HARDWARE }
      } else {
        Build.HARDWARE
      }
    val socModel =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MODEL.ifBlank { Build.BOARD }
      } else {
        cpuInfoMap["Hardware"] ?: Build.BOARD
      }

    val cpuSpecs =
      CpuHardwareSpecs(
        socManufacturer = socVendor,
        socModel = socModel,
        processorName = cpuInfoMap["Processor"] ?: cpuInfoMap["model name"] ?: Build.HARDWARE,
        hardwareName = cpuInfoMap["Hardware"] ?: Build.HARDWARE,
        architecture = System.getProperty("os.arch") ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "ARM64",
        abis = Build.SUPPORTED_ABIS.joinToString(", "),
        coreCount = coreCount,
        features = cpuInfoMap["Features"] ?: cpuInfoMap["flags"] ?: "NEON / AES / SHA",
        bogomips = cpuInfoMap["BogoMIPS"] ?: cpuInfoMap["bogomips"] ?: "-",
        clockRangeMhz = clockRange,
        activeGovernor = gov,
      )

    val gpu = readGpuSpecs()

    val deviceSpecs =
      DeviceSpecs(
        manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
        brand = Build.BRAND.replaceFirstChar { it.uppercase() },
        model = Build.MODEL,
        device = Build.DEVICE,
        board = Build.BOARD,
        product = Build.PRODUCT,
        bootloader = Build.BOOTLOADER.ifBlank { "Unknown" },
        radioVersion = runCatching { Build.getRadioVersion() }.getOrNull()?.ifBlank { "N/A" } ?: "N/A",
        fingerprint = Build.FINGERPRINT,
      )

    val dm = context.resources.displayMetrics
    val defaultDisplay: Display? = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
    val refreshHz = defaultDisplay?.refreshRate ?: 60f
    val isHdr =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && defaultDisplay != null) {
        @Suppress("DEPRECATION")
        defaultDisplay.isHdr
      } else false

    val widthInches = if (dm.xdpi > 0f) dm.widthPixels / dm.xdpi else 0f
    val heightInches = if (dm.ydpi > 0f) dm.heightPixels / dm.ydpi else 0f
    val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble().pow(2.0))
    val screenInchesStr =
      if (diagonalInches > 1.0 && diagonalInches < 40.0) "%.2f\"".format(diagonalInches) else "-"

    val displaySpecs =
      DisplaySpecs(
        resolutionPx = "${dm.widthPixels} × ${dm.heightPixels} px",
        densityDpi = dm.densityDpi,
        densityScale = dm.density,
        refreshRateHz = refreshHz,
        screenInches = screenInchesStr,
        isHdr = isHdr,
      )

    val kernelVer =
      readStringFromFile("/proc/version")?.substringBefore(" (")?.take(80)
        ?: System.getProperty("os.version")
        ?: "Linux"

    val systemSpecs =
      SystemOsSpecs(
        androidVersion = Build.VERSION.RELEASE ?: "-",
        apiLevel = Build.VERSION.SDK_INT,
        securityPatch = Build.VERSION.SECURITY_PATCH,
        buildId = Build.ID,
        kernelVersion = kernelVer,
        javaVmVersion = System.getProperty("java.vm.version") ?: "ART",
        buildTags = Build.TAGS ?: "release-keys",
        uptimeMs = SystemClock.elapsedRealtime(),
      )

    return HardwareSpecsSnapshot(
      cpu = cpuSpecs,
      gpu = gpu,
      device = deviceSpecs,
      display = displaySpecs,
      system = systemSpecs,
    )
  }

  private fun parseProcCpuInfo(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    runCatching {
      File("/proc/cpuinfo").forEachLine { line ->
        val idx = line.indexOf(':')
        if (idx > 0) {
          val key = line.substring(0, idx).trim()
          val value = line.substring(idx + 1).trim()
          if (value.isNotEmpty() && !map.containsKey(key)) {
            map[key] = value
          }
        }
      }
    }
    return map
  }

  private fun readGpuSpecs(): GpuSpecs {
    val glesConfig = activityManager.deviceConfigurationInfo.glEsVersion
    var vendor = Build.MANUFACTURER
    var renderer = Build.HARDWARE
    var glVersion = "OpenGL ES $glesConfig"

    runCatching {
      val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
      if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
        val version = IntArray(2)
        if (EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
          val configAttr =
            intArrayOf(
              EGL14.EGL_RENDERABLE_TYPE,
              EGL14.EGL_OPENGL_ES2_BIT,
              EGL14.EGL_SURFACE_TYPE,
              EGL14.EGL_PBUFFER_BIT,
              EGL14.EGL_RED_SIZE,
              8,
              EGL14.EGL_GREEN_SIZE,
              8,
              EGL14.EGL_BLUE_SIZE,
              8,
              EGL14.EGL_ALPHA_SIZE,
              8,
              EGL14.EGL_NONE,
            )
          val configs = arrayOfNulls<EGLConfig>(1)
          val numConfigs = IntArray(1)
          if (EGL14.eglChooseConfig(eglDisplay, configAttr, 0, configs, 0, 1, numConfigs, 0) &&
            numConfigs[0] > 0 &&
            configs[0] != null
          ) {
            val surfAttr = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
            val surface = EGL14.eglCreatePbufferSurface(eglDisplay, configs[0], surfAttr, 0)
            val ctxAttr = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val eglCtx =
              EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0)

            if (surface != EGL14.EGL_NO_SURFACE && eglCtx != EGL14.EGL_NO_CONTEXT) {
              EGL14.eglMakeCurrent(eglDisplay, surface, surface, eglCtx)
              GLES20.glGetString(GLES20.GL_VENDOR)?.takeIf { it.isNotBlank() }?.let { vendor = it }
              GLES20.glGetString(GLES20.GL_RENDERER)?.takeIf { it.isNotBlank() }?.let {
                renderer = it
              }
              GLES20.glGetString(GLES20.GL_VERSION)?.takeIf { it.isNotBlank() }?.let {
                glVersion = it
              }
              EGL14.eglMakeCurrent(
                eglDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT,
              )
            }
            if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(eglDisplay, surface)
            if (eglCtx != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(eglDisplay, eglCtx)
          }
          EGL14.eglTerminate(eglDisplay)
        }
      }
    }

    return GpuSpecs(
      vendor = vendor,
      renderer = renderer,
      glVersion = glVersion,
      glesApiVersion = "OpenGL ES $glesConfig",
    )
  }

  private fun readThermalZones(): List<ThermalZoneInfo> {
    val zones = mutableListOf<ThermalZoneInfo>()
    runCatching {
      val thermalDir = File("/sys/class/thermal")
      val files = thermalDir.listFiles { file -> file.name.startsWith("thermal_zone") } ?: return@runCatching
      for (dir in files.sortedBy { it.name }) {
        val type = readStringFromFile("${dir.absolutePath}/type") ?: dir.name
        val rawTemp = readIntFromFile("${dir.absolutePath}/temp") ?: continue
        val celsius = if (rawTemp > 1000) rawTemp / 1000f else if (rawTemp > 150) rawTemp / 10f else rawTemp.toFloat()
        if (celsius in -20f..140f) {
          zones.add(ThermalZoneInfo(name = type, temperatureCelsius = celsius))
        }
      }
    }
    return zones
  }

  private fun readSensors(): List<SensorItem> {
    return sensorManager.getSensorList(Sensor.TYPE_ALL).map { s ->
      SensorItem(
        name = s.name,
        vendor = s.vendor.ifBlank { "System" },
        typeCode = s.type,
        powerMa = s.power,
        maxRange = s.maximumRange,
        resolution = s.resolution,
      )
    }.sortedBy { it.name }
  }

  private fun readRunningProcesses(): List<RunningProcessItem> {
    val procs = runCatching { activityManager.runningAppProcesses }.getOrNull().orEmpty()
    if (procs.isEmpty()) return emptyList()

    val pids = procs.map { it.pid }.toIntArray()
    val memInfos = runCatching { activityManager.getProcessMemoryInfo(pids) }.getOrNull()

    return procs.mapIndexed { idx, proc ->
      val mem = memInfos?.getOrNull(idx)
      RunningProcessItem(
        pid = proc.pid,
        uid = proc.uid,
        processName = proc.processName,
        importanceCode = proc.importance,
        totalPssKb = mem?.totalPss ?: 0,
        dalvikPssKb = mem?.dalvikPss ?: 0,
        nativePssKb = mem?.nativePss ?: 0,
        otherPssKb = mem?.otherPss ?: 0,
        packages = proc.pkgList?.toList().orEmpty(),
      )
    }.sortedByDescending { it.totalPssKb }
  }

  private fun loadInstalledAppMetadata(): List<CachedAppMeta> {
    val launchIntent =
      Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolved =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
          launchIntent,
          PackageManager.ResolveInfoFlags.of(0L),
        )
      } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(launchIntent, 0)
      }

    val seenPackages = HashSet<String>()
    val result = mutableListOf<CachedAppMeta>()
    for (info in resolved) {
      val pkg = info.activityInfo?.packageName ?: continue
      if (!seenPackages.add(pkg)) continue
      val appInfo = info.activityInfo.applicationInfo ?: continue
      val label = info.loadLabel(packageManager)?.toString()?.ifBlank { pkg } ?: pkg
      val pkgInfo =
        runCatching {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L))
          } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(pkg, 0)
          }
        }.getOrNull()

      val apkFile = File(appInfo.sourceDir)
      val apkSize = if (apkFile.exists()) apkFile.length() else 0L
      val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
      val icon = runCatching { info.loadIcon(packageManager) }.getOrNull()

      result.add(
        CachedAppMeta(
          appName = label,
          packageName = pkg,
          versionName = pkgInfo?.versionName ?: "-",
          targetSdk = appInfo.targetSdkVersion,
          apkSizeBytes = apkSize,
          isSystemApp = isSystem,
          uid = appInfo.uid,
          icon = icon,
        )
      )
    }
    return result.sortedBy { it.appName.lowercase() }
  }

  private fun buildInstalledApps(runningProcs: List<RunningProcessItem>): List<InstalledAppItem> {
    val metas = cachedAppMeta ?: loadInstalledAppMetadata().also { cachedAppMeta = it }
    val runningByPkg = mutableMapOf<String, RunningProcessItem>()
    for (proc in runningProcs) {
      for (pkg in proc.packages) {
        val existing = runningByPkg[pkg]
        if (existing == null || proc.totalPssKb > existing.totalPssKb) {
          runningByPkg[pkg] = proc
        }
      }
      if (!runningByPkg.containsKey(proc.processName)) {
        runningByPkg[proc.processName] = proc
      }
    }

    return metas.map { meta ->
      val running = runningByPkg[meta.packageName]
      val rx = TrafficStats.getUidRxBytes(meta.uid).let { if (it >= 0L) it else 0L }
      val tx = TrafficStats.getUidTxBytes(meta.uid).let { if (it >= 0L) it else 0L }
      InstalledAppItem(
        appName = meta.appName,
        packageName = meta.packageName,
        versionName = meta.versionName,
        targetSdk = meta.targetSdk,
        apkSizeBytes = meta.apkSizeBytes,
        isSystemApp = meta.isSystemApp,
        isCurrentlyRunning = running != null,
        runningPssKb = running?.totalPssKb ?: 0,
        runningPid = running?.pid,
        uidRxBytes = rx,
        uidTxBytes = tx,
        icon = meta.icon,
      )
    }.sortedWith(
      compareByDescending<InstalledAppItem> { it.isCurrentlyRunning }
        .thenByDescending { it.runningPssKb }
        .thenBy { it.appName.lowercase() }
    )
  }

  private fun readJvmThreads(): List<JvmThreadItem> {
    return runCatching {
      Thread.getAllStackTraces().keys.map { t ->
        JvmThreadItem(
          id = t.id,
          name = t.name,
          state = t.state.name,
          priority = t.priority,
          isDaemon = t.isDaemon,
        )
      }.sortedBy { it.id }
    }.getOrDefault(emptyList())
  }

  private fun readIntFromFile(path: String): Int? =
    runCatching {
      val f = File(path)
      if (f.exists() && f.canRead()) f.readText().trim().toIntOrNull() else null
    }.getOrNull()

  private fun readStringFromFile(path: String): String? =
    runCatching {
      val f = File(path)
      if (f.exists() && f.canRead()) f.readText().trim() else null
    }.getOrNull()
}
