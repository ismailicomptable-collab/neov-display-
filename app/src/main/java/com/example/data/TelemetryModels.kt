package com.example.data

import android.graphics.drawable.Drawable

enum class AppTab {
  PERFORMANCE,
  PROCESSES,
  HARDWARE,
  PHONES,
  SENSORS,
}

enum class AppLanguage {
  AR,
  EN,
}

enum class ProcessFilter {
  ALL,
  RUNNING,
  USER_APPS,
  SYSTEM_APPS,
}

enum class AlertKind {
  THERMAL_CPU,
  THERMAL_PHONE,
  RAM_LIMIT,
  STORAGE_LIMIT,
}

data class ActiveAlert(
  val kind: AlertKind,
  val currentValueText: String,
  val thresholdText: String,
)

data class AlertLogEntry(
  val id: Long,
  val kind: AlertKind,
  val currentValueText: String,
  val thresholdText: String,
  val timestampMs: Long,
)

data class AlertThresholds(
  val ramPercentLimit: Float = 70f,
  val storagePercentLimit: Float = 90f,
  val cpuTempLimitC: Float = 65f,
  val phoneTempLimitC: Float = 42f,
)

enum class PhoneDesignStyle {
  IPHONE_TITANIUM,
  GALAXY_ULTRA,
  CIRCULAR_CAMERA,
  VISOR_GAMING,
}

data class PhoneSpec(
  val id: String,
  val brand: String,
  val model: String,
  val designStyle: PhoneDesignStyle,
  val chipset: String,
  val cpuSummary: String,
  val ramGb: Int,
  val storageSummary: String,
  val displaySummary: String,
  val refreshRateHz: Int,
  val batteryMah: Int,
  val chargingSummary: String,
  val cameraSummary: String,
  val protectionSummary: String,
  val osVersion: String,
  val singleCoreScore: Int,
  val multiCoreScore: Int,
  val antutuScore: Int,
  val priceUsd: Double,
  val releaseYear: Int,
  val prosAr: List<String>,
  val prosEn: List<String>,
  val consAr: List<String>,
  val consEn: List<String>,
  val fullDescriptionAr: String,
  val fullDescriptionEn: String,
)

data class CurrencyInfo(
  val code: String,
  val nameAr: String,
  val nameEn: String,
  val symbol: String,
  val rateFromUsd: Double,
)

data class DeviceBenchmarkResult(
  val singleCoreScore: Int,
  val multiCoreScore: Int,
  val memoryBandwidthMbPerSec: Int,
  val storageSpeedMbPerSec: Int,
  val estimatedOverallScore: Int,
  val durationMs: Long,
)

data class RamTelemetry(
  val totalBytes: Long,
  val availableBytes: Long,
  val usedBytes: Long,
  val usedPercent: Float,
  val lowMemoryThresholdBytes: Long,
  val isLowMemory: Boolean,
  val cachedBytes: Long,
  val buffersBytes: Long,
  val activeBytes: Long,
  val swapTotalBytes: Long,
  val swapUsedBytes: Long,
  val appPssKb: Int,
  val jvmUsedBytes: Long,
  val jvmMaxBytes: Long,
)

data class CpuCoreInfo(
  val index: Int,
  val currentMhz: Int,
  val minMhz: Int,
  val maxMhz: Int,
  val governor: String,
  val isOnline: Boolean,
  val utilizationPercent: Float,
)

data class CpuTelemetry(
  val coreCount: Int,
  val activeCoreCount: Int,
  val loadPercent: Float,
  val loadSourceIsKernelStat: Boolean,
  val averageClockMhz: Int,
  val maxClockMhz: Int,
  val cores: List<CpuCoreInfo>,
  val elapsedProcessCpuMs: Long,
  val activeThreadCount: Int,
  val peakCpuTempCelsius: Float?,
)

data class StorageTelemetry(
  val internalTotalBytes: Long,
  val internalAvailableBytes: Long,
  val internalUsedBytes: Long,
  val internalUsedPercent: Float,
)

data class BatteryTelemetry(
  val levelPercent: Int,
  val isCharging: Boolean,
  val chargeSource: String,
  val statusCode: Int,
  val healthCode: Int,
  val temperatureCelsius: Float,
  val voltageMv: Int,
  val currentMa: Int?,
  val powerWatts: Float?,
  val technology: String,
  val designCapacityMah: Int?,
  val currentChargeMah: Int?,
  val estimatedFullMah: Int?,
  val healthPercent: Float?,
  val cycleCount: Int?,
  val systemThermalStatus: Int,
)

data class NetworkTelemetry(
  val connectionType: String,
  val isConnected: Boolean,
  val rxBytesPerSec: Long,
  val txBytesPerSec: Long,
  val totalRxBytes: Long,
  val totalTxBytes: Long,
)

data class GpuSpecs(
  val vendor: String,
  val renderer: String,
  val glVersion: String,
  val glesApiVersion: String,
)

data class CpuHardwareSpecs(
  val socManufacturer: String,
  val socModel: String,
  val processorName: String,
  val hardwareName: String,
  val architecture: String,
  val abis: String,
  val coreCount: Int,
  val features: String,
  val bogomips: String,
  val clockRangeMhz: String,
  val activeGovernor: String,
)

data class DeviceSpecs(
  val manufacturer: String,
  val brand: String,
  val model: String,
  val device: String,
  val board: String,
  val product: String,
  val bootloader: String,
  val radioVersion: String,
  val fingerprint: String,
)

data class DisplaySpecs(
  val resolutionPx: String,
  val densityDpi: Int,
  val densityScale: Float,
  val refreshRateHz: Float,
  val screenInches: String,
  val isHdr: Boolean,
)

data class SystemOsSpecs(
  val androidVersion: String,
  val apiLevel: Int,
  val securityPatch: String,
  val buildId: String,
  val kernelVersion: String,
  val javaVmVersion: String,
  val buildTags: String,
  val uptimeMs: Long,
)

data class HardwareSpecsSnapshot(
  val cpu: CpuHardwareSpecs,
  val gpu: GpuSpecs,
  val device: DeviceSpecs,
  val display: DisplaySpecs,
  val system: SystemOsSpecs,
)

data class ThermalZoneInfo(
  val name: String,
  val temperatureCelsius: Float,
)

data class SensorItem(
  val name: String,
  val vendor: String,
  val typeCode: Int,
  val powerMa: Float,
  val maxRange: Float,
  val resolution: Float,
)

data class RunningProcessItem(
  val pid: Int,
  val uid: Int,
  val processName: String,
  val importanceCode: Int,
  val totalPssKb: Int,
  val dalvikPssKb: Int,
  val nativePssKb: Int,
  val otherPssKb: Int,
  val packages: List<String>,
)

data class InstalledAppItem(
  val appName: String,
  val packageName: String,
  val versionName: String,
  val targetSdk: Int,
  val apkSizeBytes: Long,
  val isSystemApp: Boolean,
  val isCurrentlyRunning: Boolean,
  val runningPssKb: Int,
  val runningPid: Int?,
  val uidRxBytes: Long,
  val uidTxBytes: Long,
  val icon: Drawable?,
)

data class JvmThreadItem(
  val id: Long,
  val name: String,
  val state: String,
  val priority: Int,
  val isDaemon: Boolean,
)

data class CominitechSnapshot(
  val timestampMs: Long,
  val ram: RamTelemetry,
  val cpu: CpuTelemetry,
  val storage: StorageTelemetry,
  val battery: BatteryTelemetry,
  val network: NetworkTelemetry,
  val hardware: HardwareSpecsSnapshot,
  val thermalZones: List<ThermalZoneInfo>,
  val sensors: List<SensorItem>,
  val runningProcesses: List<RunningProcessItem>,
  val installedApps: List<InstalledAppItem>,
  val jvmThreads: List<JvmThreadItem>,
)
