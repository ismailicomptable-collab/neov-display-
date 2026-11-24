package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ActiveAlert
import com.example.data.AlertKind
import com.example.data.AlertLogEntry
import com.example.data.AppStrings
import com.example.data.CominitechSnapshot
import com.example.data.FormatUtils
import com.example.ui.TelemetryUiState
import com.example.ui.components.CoreFrequencyCard
import com.example.ui.components.LiveSparklineChart
import com.example.ui.components.MemorySegment
import com.example.ui.components.MetricStatPill
import com.example.ui.components.SegmentedMemoryBar
import com.example.ui.components.TelemetryArcGauge
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale

@Composable
fun PerformanceScreen(
  uiState: TelemetryUiState,
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onOptimizeRam: () -> Unit,
  onTestAlert: () -> Unit,
  onClearAlertHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val ram = snapshot.ram
  val cpu = snapshot.cpu
  val storage = snapshot.storage
  val battery = snapshot.battery
  val network = snapshot.network
  val thresholds = uiState.thresholds

  val ramOverLimit = ram.usedPercent >= thresholds.ramPercentLimit
  val storageOverLimit = storage.internalUsedPercent >= thresholds.storagePercentLimit

  val ramAccent =
    when {
      ram.usedPercent >= 85f -> palette.coral
      ramOverLimit -> palette.amber
      else -> palette.cyan
    }

  val cpuAccent =
    when {
      cpu.loadPercent >= 80f -> palette.coral
      cpu.loadPercent >= 55f -> palette.amber
      else -> palette.emerald
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("performance_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 0. SMART THRESHOLDS & THERMAL GUARD CARD (RAM 70%, STORAGE 90%, CPU/PHONE TEMP)
    item {
      AlertsAndThresholdsCard(
        activeAlerts = uiState.activeAlerts,
        cpuTempC = cpu.peakCpuTempCelsius,
        phoneTempC = battery.temperatureCelsius,
        strings = strings,
        onTestAlert = onTestAlert,
      )
    }

    // 0b. ALERT HISTORY LOG CARD WITH TIMESTAMPS
    item {
      AlertHistoryCard(
        entries = uiState.alertHistory,
        strings = strings,
        onClear = onClearAlertHistory,
      )
    }

    // 1. HERO RAM CARD (70% Threshold + Task Manager Memory View)
    item {
      TelemetryCard(
        modifier = Modifier.fillMaxWidth().testTag("ram_hero_card"),
        borderColor = ramAccent.copy(alpha = 0.45f),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = ramAccent.copy(alpha = 0.16f),
              ) {
                Icon(
                  imageVector = Icons.Filled.Memory,
                  contentDescription = strings.ramHeroTitle,
                  tint = ramAccent,
                  modifier = Modifier.padding(8.dp).size(20.dp),
                )
              }
              Column {
                Text(
                  text = strings.ramHeroTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = if (ramOverLimit) strings.ramLowWarning else strings.ramHealthy,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ramOverLimit) palette.amber else palette.emerald,
                  )
                }
              }
            }

            Button(
              onClick = onOptimizeRam,
              modifier = Modifier.testTag("optimize_ram_button"),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = ramAccent,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.CleaningServices,
                contentDescription = strings.optimizeRamButton,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = strings.optimizeRamButton,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 520.dp
            if (isWide) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                TelemetryArcGauge(
                  percent = ram.usedPercent,
                  primaryLabel = strings.ramConsumed,
                  secondaryLabel =
                    "${FormatUtils.formatBytes(ram.usedBytes, 1)} / ${FormatUtils.formatBytes(ram.totalBytes, 1)}",
                  accentColor = ramAccent,
                  size = 154.dp,
                )
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = strings.ramHistoryTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  LiveSparklineChart(
                    values = uiState.ramHistory,
                    accentColor = ramAccent,
                    modifier = Modifier.fillMaxWidth().height(112.dp),
                  )
                }
              }
            } else {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                  TelemetryArcGauge(
                    percent = ram.usedPercent,
                    primaryLabel = strings.ramConsumed,
                    secondaryLabel = FormatUtils.formatBytes(ram.usedBytes, 2),
                    accentColor = ramAccent,
                    size = 144.dp,
                  )
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    MetricStatPill(
                      label = strings.ramAvailable,
                      value = FormatUtils.formatBytes(ram.availableBytes, 2),
                      accent = palette.emerald,
                      modifier = Modifier.fillMaxWidth(),
                    )
                    MetricStatPill(
                      label = "${strings.ramTotal} • ${strings.ramLimitBadge}",
                      value = FormatUtils.formatBytes(ram.totalBytes, 2),
                      accent = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                }
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = strings.ramHistoryTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  LiveSparklineChart(
                    values = uiState.ramHistory,
                    accentColor = ramAccent,
                    modifier = Modifier.fillMaxWidth().height(88.dp),
                  )
                }
              }
            }
          }

          val activeOnly = (ram.usedBytes - ram.cachedBytes).coerceAtLeast(0L)
          SegmentedMemoryBar(
            segments =
              listOf(
                MemorySegment(
                  label = strings.ramActive,
                  valueText = FormatUtils.formatBytes(activeOnly, 1),
                  bytes = activeOnly,
                  color = ramAccent,
                ),
                MemorySegment(
                  label = strings.ramCached,
                  valueText = FormatUtils.formatBytes(ram.cachedBytes, 1),
                  bytes = ram.cachedBytes,
                  color = palette.violet,
                ),
                MemorySegment(
                  label = strings.ramAvailable,
                  valueText = FormatUtils.formatBytes(ram.availableBytes, 1),
                  bytes = ram.availableBytes,
                  color = palette.emerald,
                ),
              ),
            totalBytes = ram.totalBytes,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricStatPill(
              label = strings.ramThreshold,
              value = "70% (${FormatUtils.formatBytes((ram.totalBytes * 0.70).toLong(), 1)})",
              accent = if (ramOverLimit) palette.amber else palette.cyan,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.appSelfMemory,
              value = FormatUtils.formatKb(ram.appPssKb),
              accent = palette.blue,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // 2. INTERNAL STORAGE (90% LIMIT) & IPHONE-STYLE BATTERY HEALTH + TEMP
    item {
      StorageCard(
        storage = storage,
        limitPercent = thresholds.storagePercentLimit,
        overLimit = storageOverLimit,
        strings = strings,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    item {
      BatteryHealthCard(
        battery = battery,
        peakCpuTempC = cpu.peakCpuTempCelsius,
        uptimeMs = snapshot.hardware.system.uptimeMs,
        strings = strings,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    // 3. CPU LIVE TELEMETRY & PER-CORE CLOCKS
    item {
      TelemetryCard(
        modifier = Modifier.fillMaxWidth().testTag("cpu_telemetry_card"),
        borderColor = cpuAccent.copy(alpha = 0.35f),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = cpuAccent.copy(alpha = 0.16f),
              ) {
                Icon(
                  imageVector = Icons.Filled.Speed,
                  contentDescription = strings.cpuHeroTitle,
                  tint = cpuAccent,
                  modifier = Modifier.padding(8.dp).size(20.dp),
                )
              }
              Column {
                Text(
                  text = strings.cpuHeroTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text =
                    "${snapshot.hardware.cpu.socManufacturer} ${snapshot.hardware.cpu.socModel}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = String.format(Locale.US, "%.0f%%", cpu.loadPercent),
                style = MonoMetricMedium,
                color = cpuAccent,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  if (cpu.loadSourceIsKernelStat) strings.cpuLoadLabel
                  else strings.cpuClockScaleLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          LiveSparklineChart(
            values = uiState.cpuHistory,
            accentColor = cpuAccent,
            modifier = Modifier.fillMaxWidth().height(84.dp),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricStatPill(
              label = strings.cpuAvgClock,
              value = if (cpu.averageClockMhz > 0) "${cpu.averageClockMhz} MHz" else "-",
              accent = palette.cyan,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.cpuTempLabel,
              value =
                cpu.peakCpuTempCelsius?.let { String.format(Locale.US, "%.1f°C", it) } ?: "-",
              accent =
                if ((cpu.peakCpuTempCelsius ?: 0f) >= thresholds.cpuTempLimitC) palette.coral
                else palette.amber,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.cpuActiveCores,
              value = "${cpu.activeCoreCount} / ${cpu.coreCount}",
              accent = palette.emerald,
              modifier = Modifier.weight(1f),
            )
          }

          Text(
            text = strings.cpuCoresBreakdown,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          val chunkedCores = cpu.cores.chunked(2)
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chunkedCores.forEach { pair ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                pair.forEach { core ->
                  CoreFrequencyCard(
                    core = core,
                    coreLabel = strings.coreLabel,
                    offlineLabel = strings.offlineLabel,
                    modifier = Modifier.weight(1f),
                  )
                }
                if (pair.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    // 4. NETWORK CARD
    item {
      NetworkCard(
        network = network,
        rxHistoryKb = uiState.rxHistoryKb,
        strings = strings,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun AlertsAndThresholdsCard(
  activeAlerts: List<ActiveAlert>,
  cpuTempC: Float?,
  phoneTempC: Float,
  strings: AppStrings,
  onTestAlert: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val hasAlerts = activeAlerts.isNotEmpty()
  val hasThermal =
    activeAlerts.any { it.kind == AlertKind.THERMAL_CPU || it.kind == AlertKind.THERMAL_PHONE }
  val border =
    when {
      hasThermal -> palette.coral.copy(alpha = 0.7f)
      hasAlerts -> palette.amber.copy(alpha = 0.65f)
      else -> palette.emerald.copy(alpha = 0.4f)
    }

  TelemetryCard(
    modifier = Modifier.fillMaxWidth().testTag("alerts_thresholds_card"),
    borderColor = border,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = if (hasAlerts) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = strings.alertsCardTitle,
            tint = if (hasThermal) palette.coral else if (hasAlerts) palette.amber else palette.emerald,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = strings.alertsCardTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        OutlinedButton(
          onClick = onTestAlert,
          modifier = Modifier.testTag("test_alert_button"),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = strings.testAlertAction,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = strings.testAlertAction,
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }

      // Live Threshold Pills: RAM 70%, Storage 90%, CPU & Phone Temps
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp),
          color = palette.cyan.copy(alpha = 0.14f),
        ) {
          Text(
            text = strings.ramLimitBadge,
            style = MaterialTheme.typography.labelSmall,
            color = palette.cyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
          )
        }
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp),
          color = palette.blue.copy(alpha = 0.14f),
        ) {
          Text(
            text = strings.storageLimitBadge,
            style = MaterialTheme.typography.labelSmall,
            color = palette.blue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.cpuTempLabel,
          value = cpuTempC?.let { String.format(Locale.US, "%.1f°C", it) } ?: "-",
          accent = if ((cpuTempC ?: 0f) >= 65f) palette.coral else palette.emerald,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.phoneTempLabel,
          value = String.format(Locale.US, "%.1f°C", phoneTempC),
          accent = if (phoneTempC >= 42f) palette.coral else palette.amber,
          modifier = Modifier.weight(1f),
        )
      }

      if (activeAlerts.isEmpty()) {
        Text(
          text = strings.alertAllNormal,
          style = MaterialTheme.typography.bodySmall,
          color = palette.emerald,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          activeAlerts.forEach { alert ->
            val alertColor =
              if (alert.kind == AlertKind.THERMAL_CPU || alert.kind == AlertKind.THERMAL_PHONE) {
                palette.coral
              } else {
                palette.amber
              }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = alertColor.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, alertColor.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.alertHeadline(alert.kind),
                  style = MaterialTheme.typography.labelMedium,
                  color = alertColor,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = "${alert.currentValueText} / ${alert.thresholdText}",
                  style = MonoMetricSmall,
                  color = alertColor,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AlertHistoryCard(
  entries: List<AlertLogEntry>,
  strings: AppStrings,
  onClear: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current

  TelemetryCard(
    modifier = Modifier.fillMaxWidth().testTag("alert_history_card"),
    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.History,
            contentDescription = strings.alertHistoryTitle,
            tint = palette.violet,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "${strings.alertHistoryTitle} (${entries.size})",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        if (entries.isNotEmpty()) {
          TextButton(
            onClick = onClear,
            modifier = Modifier.testTag("clear_alert_history_button"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteSweep,
              contentDescription = strings.alertHistoryClear,
              tint = palette.coral,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.alertHistoryClear,
              style = MaterialTheme.typography.labelSmall,
              color = palette.coral,
            )
          }
        }
      }

      if (entries.isEmpty()) {
        Text(
          text = strings.alertHistoryEmpty,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          entries.take(10).forEachIndexed { index, item ->
            val isThermal =
              item.kind == AlertKind.THERMAL_CPU || item.kind == AlertKind.THERMAL_PHONE
            val badgeColor = if (isThermal) palette.coral else palette.amber

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .testTag("alert_history_item_${item.id}"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.alertHeadline(item.kind),
                  style = MaterialTheme.typography.labelMedium,
                  color = badgeColor,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = FormatUtils.formatTimestamp(item.timestampMs),
                  style = MonoMetricSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
              ) {
                Text(
                  text = "${item.currentValueText} / ${item.thresholdText}",
                  style = MonoMetricSmall,
                  color = badgeColor,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
            if (index < entries.take(10).lastIndex) {
              HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 0.6.dp,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StorageCard(
  storage: com.example.data.StorageTelemetry,
  limitPercent: Float,
  overLimit: Boolean,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val barColor = if (overLimit) palette.coral else palette.blue

  TelemetryCard(
    modifier = modifier.testTag("storage_card"),
    borderColor = if (overLimit) palette.coral.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Storage,
            contentDescription = strings.storageTitle,
            tint = barColor,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = strings.storageTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color =
              if (overLimit) palette.coral.copy(alpha = 0.18f)
              else palette.blue.copy(alpha = 0.14f),
          ) {
            Text(
              text = "${limitPercent.toInt()}% MAX",
              style = MonoMetricSmall,
              color = if (overLimit) palette.coral else palette.blue,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
          Text(
            text = String.format(Locale.US, "%.1f%%", storage.internalUsedPercent),
            style = MonoMetricSmall,
            color = barColor,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Progress bar with 90% threshold marker
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(11.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
      ) {
        val fillFrac = (storage.internalUsedPercent / 100f).coerceIn(0f, 1f)
        Box(
          modifier =
            Modifier.fillMaxWidth(fillFrac)
              .height(11.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(barColor)
        )
        // 90% threshold indicator line
        Row(modifier = Modifier.fillMaxWidth()) {
          Spacer(modifier = Modifier.weight(0.90f))
          Box(
            modifier =
              Modifier.width(2.dp)
                .height(11.dp)
                .background(palette.amber)
          )
          Spacer(modifier = Modifier.weight(0.10f))
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text =
            "${strings.ramConsumed}: ${FormatUtils.formatBytes(storage.internalUsedBytes, 1)}",
          style = MonoMetricSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text =
            "${strings.ramAvailable}: ${FormatUtils.formatBytes(storage.internalAvailableBytes, 1)} / ${FormatUtils.formatBytes(storage.internalTotalBytes, 1)}",
          style = MonoMetricSmall,
          color = palette.emerald,
        )
      }
    }
  }
}

@Composable
private fun BatteryHealthCard(
  battery: com.example.data.BatteryTelemetry,
  peakCpuTempC: Float?,
  uptimeMs: Long,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val accent =
    when {
      battery.isCharging -> palette.emerald
      battery.levelPercent <= 20 -> palette.coral
      else -> palette.amber
    }

  val healthPct = battery.healthPercent
  val healthColor =
    when {
      healthPct == null -> palette.emerald
      healthPct >= 85f -> palette.emerald
      healthPct >= 75f -> palette.amber
      else -> palette.coral
    }

  val tempColor =
    when {
      battery.temperatureCelsius >= 42f -> palette.coral
      battery.temperatureCelsius >= 37f -> palette.amber
      else -> palette.cyan
    }

  TelemetryCard(
    modifier = modifier.testTag("battery_card"),
    borderColor = healthColor.copy(alpha = 0.4f),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = accent.copy(alpha = 0.16f),
          ) {
            Icon(
              imageVector =
                if (battery.isCharging) Icons.Filled.BatteryChargingFull
                else Icons.Filled.BatteryStd,
              contentDescription = strings.batteryTitle,
              tint = accent,
              modifier = Modifier.padding(7.dp).size(20.dp),
            )
          }
          Column {
            Text(
              text = strings.batteryTitle,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                "${battery.levelPercent}% • ${if (battery.isCharging) strings.chargingLabel else strings.dischargingLabel} (${battery.chargeSource})",
              style = MonoMetricSmall,
              color = accent,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = tempColor.copy(alpha = 0.16f),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Thermostat,
              contentDescription = strings.tempLabel,
              tint = tempColor,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = String.format(Locale.US, "%.1f°C", battery.temperatureCelsius),
              style = MonoMetricSmall,
              color = tempColor,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // iPhone-style Battery Health Highlight Box
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, healthColor.copy(alpha = 0.35f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = strings.batteryHealthSectionTitle,
                tint = healthColor,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = strings.batteryHealthSectionTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text =
                healthPct?.let { String.format(Locale.US, "%.0f%%", it) }
                  ?: strings.batteryHealthLabel(battery.healthCode),
              style = MonoMetricMedium,
              color = healthColor,
              fontWeight = FontWeight.Bold,
            )
          }

          if (healthPct != null) {
            LinearProgressIndicator(
              progress = { (healthPct / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
              color = healthColor,
              trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
              strokeCap = StrokeCap.Round,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricStatPill(
              label = strings.batteryConditionLabel,
              value = strings.batteryHealthLabel(battery.healthCode),
              accent = healthColor,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.batteryDesignCapLabel,
              value = battery.designCapacityMah?.let { "$it mAh" } ?: "-",
              accent = palette.cyan,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.batteryCyclesLabel,
              value = battery.cycleCount?.toString() ?: "-",
              accent = palette.violet,
              modifier = Modifier.weight(0.85f),
            )
          }

          Text(
            text = strings.batteryHealthNote,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Detailed Electrical & Thermal Telemetry Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.batteryCurrentChargeLabel,
          value = battery.currentChargeMah?.let { "$it mAh" } ?: "${battery.levelPercent}%",
          accent = palette.emerald,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.batteryEstFullLabel,
          value = battery.estimatedFullMah?.let { "$it mAh" } ?: "-",
          accent = palette.cyan,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.batteryThermalStateLabel,
          value = strings.thermalStatusLabel(battery.systemThermalStatus),
          accent = tempColor,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.voltageLabel,
          value = "${battery.voltageMv} mV",
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.currentLabel,
          value = battery.currentMa?.let { "$it mA" } ?: "-",
          accent = palette.amber,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.batteryPowerLabel,
          value = battery.powerWatts?.let { String.format(Locale.US, "%.2f W", it) } ?: "-",
          accent = palette.blue,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.batteryTechLabel,
          value = battery.technology,
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.cpuTempLabel,
          value = peakCpuTempC?.let { String.format(Locale.US, "%.1f°C", it) } ?: "-",
          accent = if ((peakCpuTempC ?: 0f) >= 65f) palette.coral else palette.emerald,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.uptimeLabel,
          value = FormatUtils.formatUptime(uptimeMs),
          accent = palette.violet,
          modifier = Modifier.weight(1.2f),
        )
      }
    }
  }
}

@Composable
private fun NetworkCard(
  network: com.example.data.NetworkTelemetry,
  rxHistoryKb: List<Float>,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val maxRx = (rxHistoryKb.maxOrNull() ?: 10f).coerceAtLeast(10f)

  TelemetryCard(modifier = modifier.testTag("network_card")) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.NetworkCheck,
            contentDescription = strings.networkTitle,
            tint = palette.cyan,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = strings.networkTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color =
            if (network.isConnected) palette.emerald.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.surfaceVariant,
        ) {
          Text(
            text = network.connectionType,
            style = MonoMetricSmall,
            color = if (network.isConnected) palette.emerald else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      LiveSparklineChart(
        values = rxHistoryKb,
        accentColor = palette.cyan,
        maxValue = maxRx,
        modifier = Modifier.fillMaxWidth().height(64.dp),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.downloadSpeed,
          value = FormatUtils.formatSpeed(network.rxBytesPerSec),
          accent = palette.cyan,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.uploadSpeed,
          value = FormatUtils.formatSpeed(network.txBytesPerSec),
          accent = palette.emerald,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = strings.totalTraffic,
          value = FormatUtils.formatBytes(network.totalRxBytes + network.totalTxBytes, 1),
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1.2f),
        )
      }
    }
  }
}
