package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveAlert
import com.example.data.AlertKind
import com.example.data.AlertLogEntry
import com.example.data.AlertThresholds
import com.example.data.AppLanguage
import com.example.data.AppStrings
import com.example.data.AppTab
import com.example.data.CominitechSnapshot
import com.example.data.CurrencyInfo
import com.example.data.CurrencyRepository
import com.example.data.DeviceBenchmarkEngine
import com.example.data.DeviceBenchmarkResult
import com.example.data.PhoneSpec
import com.example.data.PhonesCatalog
import com.example.data.ProcessFilter
import com.example.data.SystemTelemetryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class TelemetryUiState(
  val isLoading: Boolean = true,
  val snapshot: CominitechSnapshot? = null,
  val ramHistory: List<Float> = emptyList(),
  val cpuHistory: List<Float> = emptyList(),
  val rxHistoryKb: List<Float> = emptyList(),
  val txHistoryKb: List<Float> = emptyList(),
  val selectedTab: AppTab = AppTab.PERFORMANCE,
  val language: AppLanguage = AppLanguage.AR,
  val isDarkTheme: Boolean = true,
  val isPaused: Boolean = false,
  val processQuery: String = "",
  val processFilter: ProcessFilter = ProcessFilter.ALL,
  val sensorQuery: String = "",
  val showThreadsModal: Boolean = false,
  val thresholds: AlertThresholds = AlertThresholds(),
  val activeAlerts: List<ActiveAlert> = emptyList(),
  val alertHistory: List<AlertLogEntry> = emptyList(),
  val pendingAlertDialog: ActiveAlert? = null,
  // Phones Catalog & Currency & Benchmark
  val phonesSubTab: Int = 0, // 0 = Catalog, 1 = VS Benchmark
  val phonesCatalog: List<PhoneSpec> = PhonesCatalog.allPhones,
  val phoneQuery: String = "",
  val selectedBrand: String = "All",
  val currencies: List<CurrencyInfo> = emptyList(),
  val selectedCurrency: CurrencyInfo =
    CurrencyInfo(code = "USD", nameAr = "دولار أمريكي", nameEn = "US Dollar", symbol = "$", rateFromUsd = 1.0),
  val isCurrencyLive: Boolean = false,
  val selectedOpponentPhone: PhoneSpec = PhonesCatalog.allPhones.first(),
  val isRunningBenchmark: Boolean = false,
  val benchmarkProgress: Float = 0f,
  val benchmarkResult: DeviceBenchmarkResult? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs =
    application.getSharedPreferences("cominitech_prefs", Context.MODE_PRIVATE)
  private val repository = SystemTelemetryRepository(application)
  private val currencyRepository = CurrencyRepository(application)
  private val benchmarkEngine = DeviceBenchmarkEngine(application)

  private val initialLang =
    if (prefs.getString("lang", "AR") == "EN") AppLanguage.EN else AppLanguage.AR
  private val initialDark = prefs.getBoolean("dark_theme", true)
  private val savedCurrencyCode = prefs.getString("currency_code", "MAD") ?: "MAD"

  private val _uiState =
    MutableStateFlow(
      TelemetryUiState(
        language = initialLang,
        isDarkTheme = initialDark,
        alertHistory = loadSavedAlertHistory(),
      )
    )
  val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
  val events: SharedFlow<String> = _events.asSharedFlow()

  private val maxHistoryPoints = 60
  private var lastAlertDialogTimeMs = 0L
  private val lastLoggedByKindMs = mutableMapOf<AlertKind, Long>()

  init {
    loadCurrencies()
    startLiveTelemetryLoop()
  }

  private fun loadCurrencies() {
    val (cached, isLive) = currencyRepository.getCachedOrDefaultCurrencies()
    val preferred =
      cached.firstOrNull { it.code == savedCurrencyCode }
        ?: cached.firstOrNull { it.code == "USD" }
        ?: _uiState.value.selectedCurrency

    _uiState.update {
      it.copy(
        currencies = cached,
        selectedCurrency = preferred,
        isCurrencyLive = isLive,
      )
    }

    viewModelScope.launch {
      val (liveList, fetchedLive) =
        withContext(Dispatchers.IO) { currencyRepository.fetchLiveWorldCurrencies() }
      if (liveList.isNotEmpty()) {
        _uiState.update { cur ->
          val updatedSelected =
            liveList.firstOrNull { it.code == cur.selectedCurrency.code } ?: cur.selectedCurrency
          cur.copy(
            currencies = liveList,
            selectedCurrency = updatedSelected,
            isCurrencyLive = fetchedLive,
          )
        }
      }
    }
  }

  private fun startLiveTelemetryLoop() {
    viewModelScope.launch {
      var tick = 0
      while (isActive) {
        val state = _uiState.value
        if (!state.isPaused || state.snapshot == null) {
          val includeApps = tick == 0 || state.selectedTab == AppTab.PROCESSES || tick % 5 == 0
          val snap = withContext(Dispatchers.Default) {
            repository.captureSnapshot(includeApps = includeApps)
          }
          _uiState.update { cur ->
            val preservedApps =
              if (snap.installedApps.isNotEmpty()) snap.installedApps
              else cur.snapshot?.installedApps.orEmpty()
            val mergedSnap = snap.copy(installedApps = preservedApps)

            val alerts = evaluateAlerts(mergedSnap, cur.thresholds)
            val updatedLog = appendAlertsToHistory(cur.alertHistory, alerts)

            val thermalAlert =
              alerts.firstOrNull {
                it.kind == AlertKind.THERMAL_CPU || it.kind == AlertKind.THERMAL_PHONE
              }
            val nowElapsed = SystemClock.elapsedRealtime()
            val shouldPopupThermal =
              thermalAlert != null &&
                cur.pendingAlertDialog == null &&
                (lastAlertDialogTimeMs == 0L || nowElapsed - lastAlertDialogTimeMs > 60_000L)

            if (shouldPopupThermal) {
              lastAlertDialogTimeMs = nowElapsed
            }

            cur.copy(
              isLoading = false,
              snapshot = mergedSnap,
              ramHistory = appendPoint(cur.ramHistory, snap.ram.usedPercent),
              cpuHistory = appendPoint(cur.cpuHistory, snap.cpu.loadPercent),
              rxHistoryKb = appendPoint(cur.rxHistoryKb, snap.network.rxBytesPerSec / 1024f),
              txHistoryKb = appendPoint(cur.txHistoryKb, snap.network.txBytesPerSec / 1024f),
              activeAlerts = alerts,
              alertHistory = updatedLog,
              pendingAlertDialog =
                if (shouldPopupThermal) thermalAlert else cur.pendingAlertDialog,
            )
          }
          tick++
        }
        delay(1000L)
      }
    }
  }

  private fun evaluateAlerts(
    snap: CominitechSnapshot,
    thresholds: AlertThresholds,
  ): List<ActiveAlert> {
    val alerts = mutableListOf<ActiveAlert>()
    val cpuTemp = snap.cpu.peakCpuTempCelsius
    val sysThermalHigh =
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        snap.battery.systemThermalStatus >= PowerManager.THERMAL_STATUS_MODERATE

    if (cpuTemp != null && cpuTemp >= thresholds.cpuTempLimitC) {
      alerts.add(
        ActiveAlert(
          kind = AlertKind.THERMAL_CPU,
          currentValueText = String.format(Locale.US, "%.1f°C", cpuTemp),
          thresholdText = String.format(Locale.US, "%.0f°C", thresholds.cpuTempLimitC),
        )
      )
    }

    if (
      snap.battery.temperatureCelsius >= thresholds.phoneTempLimitC ||
        snap.battery.healthCode == BatteryManager.BATTERY_HEALTH_OVERHEAT ||
        sysThermalHigh
    ) {
      alerts.add(
        ActiveAlert(
          kind = AlertKind.THERMAL_PHONE,
          currentValueText = String.format(Locale.US, "%.1f°C", snap.battery.temperatureCelsius),
          thresholdText = String.format(Locale.US, "%.0f°C", thresholds.phoneTempLimitC),
        )
      )
    }

    if (snap.ram.usedPercent >= thresholds.ramPercentLimit) {
      alerts.add(
        ActiveAlert(
          kind = AlertKind.RAM_LIMIT,
          currentValueText = String.format(Locale.US, "%.1f%%", snap.ram.usedPercent),
          thresholdText = String.format(Locale.US, "%.0f%%", thresholds.ramPercentLimit),
        )
      )
    }

    if (snap.storage.internalUsedPercent >= thresholds.storagePercentLimit) {
      alerts.add(
        ActiveAlert(
          kind = AlertKind.STORAGE_LIMIT,
          currentValueText = String.format(Locale.US, "%.1f%%", snap.storage.internalUsedPercent),
          thresholdText = String.format(Locale.US, "%.0f%%", thresholds.storagePercentLimit),
        )
      )
    }

    return alerts
  }

  private fun appendAlertsToHistory(
    existing: List<AlertLogEntry>,
    active: List<ActiveAlert>,
  ): List<AlertLogEntry> {
    if (active.isEmpty()) return existing
    val nowWall = System.currentTimeMillis()
    val nowElapsed = SystemClock.elapsedRealtime()
    val added = mutableListOf<AlertLogEntry>()

    for (alert in active) {
      val prevElapsed = lastLoggedByKindMs[alert.kind] ?: 0L
      if (prevElapsed == 0L || nowElapsed - prevElapsed > 60_000L) {
        lastLoggedByKindMs[alert.kind] = nowElapsed
        added.add(
          AlertLogEntry(
            id = nowWall + added.size,
            kind = alert.kind,
            currentValueText = alert.currentValueText,
            thresholdText = alert.thresholdText,
            timestampMs = nowWall,
          )
        )
      }
    }

    if (added.isEmpty()) return existing
    val merged = (added + existing).take(50)
    saveAlertHistory(merged)
    return merged
  }

  private fun loadSavedAlertHistory(): List<AlertLogEntry> {
    return runCatching {
      val raw = prefs.getString("alert_history_json", null) ?: return emptyList()
      val arr = JSONArray(raw)
      val list = mutableListOf<AlertLogEntry>()
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val kind = AlertKind.valueOf(obj.getString("kind"))
        list.add(
          AlertLogEntry(
            id = obj.getLong("id"),
            kind = kind,
            currentValueText = obj.getString("val"),
            thresholdText = obj.getString("lim"),
            timestampMs = obj.getLong("ts"),
          )
        )
      }
      list
    }.getOrDefault(emptyList())
  }

  private fun saveAlertHistory(list: List<AlertLogEntry>) {
    runCatching {
      val arr = JSONArray()
      list.forEach { item ->
        arr.put(
          JSONObject().apply {
            put("id", item.id)
            put("kind", item.kind.name)
            put("val", item.currentValueText)
            put("lim", item.thresholdText)
            put("ts", item.timestampMs)
          }
        )
      }
      prefs.edit().putString("alert_history_json", arr.toString()).apply()
    }
  }

  fun clearAlertHistory() {
    prefs.edit().remove("alert_history_json").apply()
    lastLoggedByKindMs.clear()
    _uiState.update { it.copy(alertHistory = emptyList()) }
  }

  private fun appendPoint(list: List<Float>, value: Float): List<Float> {
    val next = list + value
    return if (next.size > maxHistoryPoints) next.takeLast(maxHistoryPoints) else next
  }

  fun selectTab(tab: AppTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }

  fun toggleLanguage() {
    _uiState.update { cur ->
      val next = if (cur.language == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
      prefs.edit().putString("lang", next.name).apply()
      cur.copy(language = next)
    }
  }

  fun toggleTheme() {
    _uiState.update { cur ->
      val next = !cur.isDarkTheme
      prefs.edit().putBoolean("dark_theme", next).apply()
      cur.copy(isDarkTheme = next)
    }
  }

  fun togglePause() {
    _uiState.update { it.copy(isPaused = !it.isPaused) }
  }

  fun updateProcessQuery(query: String) {
    _uiState.update { it.copy(processQuery = query) }
  }

  fun selectProcessFilter(filter: ProcessFilter) {
    _uiState.update { it.copy(processFilter = filter) }
  }

  fun updateSensorQuery(query: String) {
    _uiState.update { it.copy(sensorQuery = query) }
  }

  fun setShowThreadsModal(show: Boolean) {
    _uiState.update { it.copy(showThreadsModal = show) }
  }

  fun dismissPendingAlert() {
    _uiState.update { it.copy(pendingAlertDialog = null) }
  }

  fun triggerAlertPreview() {
    val cur = _uiState.value
    val existing = cur.activeAlerts.firstOrNull()
    val alertToLog =
      existing
        ?: run {
          val snap = cur.snapshot
          if (snap?.cpu?.peakCpuTempCelsius != null) {
            ActiveAlert(
              kind = AlertKind.THERMAL_CPU,
              currentValueText = String.format(Locale.US, "%.1f°C", snap.cpu.peakCpuTempCelsius),
              thresholdText = String.format(Locale.US, "%.0f°C", cur.thresholds.cpuTempLimitC),
            )
          } else {
            val phoneTemp = snap?.battery?.temperatureCelsius ?: 36.0f
            ActiveAlert(
              kind = AlertKind.THERMAL_PHONE,
              currentValueText = String.format(Locale.US, "%.1f°C", phoneTemp),
              thresholdText = String.format(Locale.US, "%.0f°C", cur.thresholds.phoneTempLimitC),
            )
          }
        }

    val entry =
      AlertLogEntry(
        id = System.currentTimeMillis(),
        kind = alertToLog.kind,
        currentValueText = alertToLog.currentValueText,
        thresholdText = alertToLog.thresholdText,
        timestampMs = System.currentTimeMillis(),
      )
    val updatedLog = (listOf(entry) + cur.alertHistory).take(50)
    saveAlertHistory(updatedLog)

    if (existing == null) {
      val strings = AppStrings(cur.language)
      _events.tryEmit(strings.alertAllNormal)
    }

    _uiState.update {
      it.copy(
        alertHistory = updatedLog,
        pendingAlertDialog = alertToLog,
      )
    }
  }

  // Phones Catalog & Benchmark Controls
  fun setPhonesSubTab(subTab: Int) {
    _uiState.update { it.copy(phonesSubTab = subTab) }
  }

  fun updatePhoneQuery(query: String) {
    _uiState.update { it.copy(phoneQuery = query) }
  }

  fun selectBrand(brand: String) {
    _uiState.update { it.copy(selectedBrand = brand) }
  }

  fun selectCurrency(currency: CurrencyInfo) {
    prefs.edit().putString("currency_code", currency.code).apply()
    _uiState.update { it.copy(selectedCurrency = currency) }
  }

  fun compareWithPhone(phone: PhoneSpec) {
    _uiState.update {
      it.copy(
        selectedOpponentPhone = phone,
        phonesSubTab = 1,
      )
    }
  }

  fun addCustomPhoneAndCompare(
    model: String,
    chipset: String,
    ramGb: Int,
    batteryMah: Int,
    refreshRateHz: Int,
    priceUsd: Double,
  ) {
    val cleanModel = model.trim().ifBlank { "Custom Phone" }
    val estimatedSingle = (ramGb * 160 + refreshRateHz * 4).coerceIn(800, 3200)
    val estimatedMulti = (estimatedSingle * 2.9).toInt().coerceIn(2200, 9800)
    val custom =
      PhoneSpec(
        id = "custom_${System.currentTimeMillis()}",
        brand = "Custom",
        model = cleanModel,
        designStyle = com.example.data.PhoneDesignStyle.GALAXY_ULTRA,
        chipset = chipset.trim().ifBlank { "Custom SoC" },
        cpuSummary = "8-core",
        ramGb = ramGb.coerceIn(1, 32),
        storageSummary = "128 / 256 GB",
        displaySummary = "OLED / AMOLED",
        refreshRateHz = refreshRateHz.coerceIn(60, 185),
        batteryMah = batteryMah.coerceIn(1500, 10000),
        chargingSummary = "Fast Charging",
        cameraSummary = "Multi-Lens Camera",
        protectionSummary = "Standard Build",
        osVersion = "Android / iOS",
        singleCoreScore = estimatedSingle,
        multiCoreScore = estimatedMulti,
        antutuScore = estimatedMulti * 240,
        priceUsd = priceUsd.coerceAtLeast(50.0),
        releaseYear = 2025,
        prosAr = listOf("مواصفات مخصصة مُدخلة من المستخدم"),
        prosEn = listOf("User-configured custom phone profile"),
        consAr = listOf("تقدير الأداء مبني على المواصفات المدخلة"),
        consEn = listOf("Scores estimated from entered hardware specs"),
        fullDescriptionAr = "هاتف مخصص أضفته للمقارنة المباشرة مع هاتفك الحالي.",
        fullDescriptionEn = "Custom smartphone added for direct comparison against your device.",
      )
    _uiState.update { cur ->
      cur.copy(
        phonesCatalog = listOf(custom) + cur.phonesCatalog,
        selectedOpponentPhone = custom,
        phonesSubTab = 1,
      )
    }
  }

  fun runLiveDeviceBenchmark() {
    if (_uiState.value.isRunningBenchmark) return
    viewModelScope.launch {
      _uiState.update { it.copy(isRunningBenchmark = true, benchmarkProgress = 0.05f) }
      val result =
        benchmarkEngine.runLiveBenchmark { progress ->
          _uiState.update { it.copy(benchmarkProgress = progress) }
        }
      _uiState.update {
        it.copy(
          isRunningBenchmark = false,
          benchmarkProgress = 1f,
          benchmarkResult = result,
        )
      }
    }
  }

  fun optimizeRam() {
    viewModelScope.launch {
      val (freedMb, trimmedCount) =
        withContext(Dispatchers.Default) { repository.trimBackgroundMemory() }
      val snap =
        withContext(Dispatchers.Default) {
          repository.captureSnapshot(includeApps = _uiState.value.selectedTab == AppTab.PROCESSES)
        }
      _uiState.update { cur ->
        val preservedApps =
          if (snap.installedApps.isNotEmpty()) snap.installedApps
          else cur.snapshot?.installedApps.orEmpty()
        val merged = snap.copy(installedApps = preservedApps)
        val alerts = evaluateAlerts(merged, cur.thresholds)
        cur.copy(
          snapshot = merged,
          ramHistory = appendPoint(cur.ramHistory, snap.ram.usedPercent),
          activeAlerts = alerts,
        )
      }
      val strings = AppStrings(_uiState.value.language)
      _events.tryEmit(strings.ramFreedMessage(freedMb, trimmedCount))
    }
  }

  fun killBackgroundApp(packageName: String, appName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.Default) {
        repository.killBackgroundPackage(packageName)
      }
      val snap = withContext(Dispatchers.Default) { repository.captureSnapshot(includeApps = true) }
      _uiState.update { it.copy(snapshot = snap) }
      val strings = AppStrings(_uiState.value.language)
      _events.tryEmit(strings.bgTrimmedPackageMessage(appName))
    }
  }

  fun openAppSettings(packageName: String) {
    val ctx = getApplication<Application>()
    val intent =
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    runCatching { ctx.startActivity(intent) }
  }
}
