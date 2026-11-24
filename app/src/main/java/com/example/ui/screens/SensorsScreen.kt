package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.AppStrings
import com.example.data.CominitechSnapshot
import com.example.data.FormatUtils
import com.example.data.SensorItem
import com.example.ui.TelemetryUiState
import com.example.ui.components.MetricStatPill
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale

@Composable
fun SensorsScreen(
  uiState: TelemetryUiState,
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val isAr = uiState.language == AppLanguage.AR
  val q = uiState.sensorQuery.trim().lowercase()

  val filteredSensors =
    remember(snapshot.sensors, q, isAr) {
      snapshot.sensors.filter { s ->
        q.isEmpty() ||
          s.name.lowercase().contains(q) ||
          s.vendor.lowercase().contains(q) ||
          FormatUtils.sensorTypeLabel(s.typeCode, isAr).lowercase().contains(q)
      }
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("sensors_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Thermal Section
    item {
      TelemetryCard(
        modifier = Modifier.fillMaxWidth().testTag("thermal_zones_card"),
        borderColor = palette.amber.copy(alpha = 0.4f),
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
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = strings.thermalSectionTitle,
                tint = palette.amber,
                modifier = Modifier.size(22.dp),
              )
              Text(
                text = strings.thermalSectionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text = String.format(Locale.US, "%.1f°C", snapshot.battery.temperatureCelsius),
              style = MonoMetricMedium,
              color =
                if (snapshot.battery.temperatureCelsius >= 42f) palette.coral else palette.emerald,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricStatPill(
              label = strings.batteryTitle,
              value = String.format(Locale.US, "%.1f°C", snapshot.battery.temperatureCelsius),
              accent = palette.amber,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.voltageLabel,
              value = "${snapshot.battery.voltageMv} mV",
              accent = palette.cyan,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.currentLabel,
              value = snapshot.battery.currentMa?.let { "$it mA" } ?: "-",
              accent = palette.emerald,
              modifier = Modifier.weight(1f),
            )
          }

          if (snapshot.thermalZones.isEmpty()) {
            Text(
              text = strings.noThermalZones,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          } else {
            val chunked = snapshot.thermalZones.take(12).chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              chunked.forEach { pair ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  pair.forEach { zone ->
                    val tempColor =
                      when {
                        zone.temperatureCelsius >= 65f -> palette.coral
                        zone.temperatureCelsius >= 45f -> palette.amber
                        else -> palette.emerald
                      }
                    Surface(
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          text = zone.name,
                          style = MonoMetricSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.weight(1f),
                        )
                        Text(
                          text = String.format(Locale.US, "%.1f°C", zone.temperatureCelsius),
                          style = MonoMetricSmall,
                          fontWeight = FontWeight.Bold,
                          color = tempColor,
                        )
                      }
                    }
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
    }

    // Sensors Header & Search
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "${strings.sensorsSectionTitle} (${filteredSensors.size})",
          style = MaterialTheme.typography.titleSmall,
          color = palette.cyan,
        )
        OutlinedTextField(
          value = uiState.sensorQuery,
          onValueChange = onQueryChange,
          modifier = Modifier.fillMaxWidth().testTag("sensor_search_input"),
          placeholder = { Text(strings.searchSensorsPlaceholder) },
          leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = strings.searchSensorsPlaceholder)
          },
          trailingIcon = {
            if (uiState.sensorQuery.isNotEmpty()) {
              IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
        )
      }
    }

    items(filteredSensors, key = { "${it.name}_${it.typeCode}" }) { sensor ->
      SensorRowCard(sensor = sensor, isAr = isAr, strings = strings)
    }
  }
}

@Composable
private fun SensorRowCard(
  sensor: SensorItem,
  isAr: Boolean,
  strings: AppStrings,
) {
  val palette = LocalTelemetryPalette.current
  TelemetryCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = palette.violet.copy(alpha = 0.16f),
        ) {
          Icon(
            imageVector = Icons.Filled.Sensors,
            contentDescription = sensor.name,
            tint = palette.violet,
            modifier = Modifier.padding(8.dp).size(18.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = sensor.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = FormatUtils.sensorTypeLabel(sensor.typeCode, isAr),
            style = MaterialTheme.typography.labelSmall,
            color = palette.cyan,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.sensorVendorLabel,
          value = sensor.vendor,
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1.2f),
        )
        MetricStatPill(
          label = strings.sensorPowerLabel,
          value = String.format(Locale.US, "%.2f mA", sensor.powerMa),
          accent = palette.emerald,
          modifier = Modifier.weight(0.9f),
        )
        MetricStatPill(
          label = strings.sensorRangeLabel,
          value = String.format(Locale.US, "%.1f", sensor.maxRange),
          accent = palette.amber,
          modifier = Modifier.weight(0.8f),
        )
      }
    }
  }
}
