package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AppStrings
import com.example.data.CominitechSnapshot
import com.example.data.FormatUtils
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale

@Composable
fun HardwareScreen(
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onShowToast: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val hw = snapshot.hardware
  val clipboard = LocalClipboardManager.current

  fun copySection(title: String, rows: List<Pair<String, String>>) {
    val text = buildString {
      appendLine("=== $title ===")
      rows.forEach { (k, v) -> appendLine("$k: $v") }
    }
    clipboard.setText(AnnotatedString(text))
    onShowToast(strings.copiedToClipboard)
  }

  val socRows =
    listOf(
      strings.keySocVendor to hw.cpu.socManufacturer,
      strings.keySocModel to hw.cpu.socModel,
      strings.keyProcessor to hw.cpu.processorName,
      strings.keyHardware to hw.cpu.hardwareName,
      strings.keyArch to hw.cpu.architecture,
      strings.keyAbis to hw.cpu.abis,
      strings.keyCores to "${hw.cpu.coreCount} Cores",
      strings.keyClockRange to hw.cpu.clockRangeMhz,
      strings.keyGovernor to hw.cpu.activeGovernor,
      strings.keyBogomips to hw.cpu.bogomips,
      strings.keyFeatures to hw.cpu.features,
    )

  val gpuRows =
    listOf(
      strings.keyGpuVendor to hw.gpu.vendor,
      strings.keyGpuRenderer to hw.gpu.renderer,
      strings.keyGlVersion to hw.gpu.glVersion,
      strings.keyGlesApi to hw.gpu.glesApiVersion,
    )

  val deviceRows =
    listOf(
      strings.keyManufacturer to hw.device.manufacturer,
      strings.keyBrand to hw.device.brand,
      strings.keyModel to hw.device.model,
      strings.keyDevice to hw.device.device,
      strings.keyBoard to hw.device.board,
      strings.keyProduct to hw.device.product,
      strings.keyBootloader to hw.device.bootloader,
      strings.keyRadio to hw.device.radioVersion,
    )

  val displayRows =
    listOf(
      strings.keyResolution to hw.display.resolutionPx,
      strings.keyDensity to "${hw.display.densityDpi} dpi (${hw.display.densityScale}x)",
      strings.keyRefreshRate to String.format(Locale.US, "%.0f Hz", hw.display.refreshRateHz),
      strings.keyScreenSize to hw.display.screenInches,
      strings.keyHdr to if (hw.display.isHdr) "Supported" else "SDR",
    )

  val systemRows =
    listOf(
      strings.keyAndroidVer to "Android ${hw.system.androidVersion}",
      strings.keyApiLevel to "API ${hw.system.apiLevel}",
      strings.keySecPatch to hw.system.securityPatch,
      strings.keyBuildId to hw.system.buildId,
      strings.keyKernel to hw.system.kernelVersion,
      strings.keyJavaVm to "ART ${hw.system.javaVmVersion}",
      strings.keyBuildTags to hw.system.buildTags,
      strings.uptimeLabel to FormatUtils.formatUptime(hw.system.uptimeMs),
    )

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("hardware_cpuz_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      HardwareSpecSectionCard(
        title = strings.sectionSocCpu,
        icon = Icons.Filled.Memory,
        accent = palette.cyan,
        rows = socRows,
        copyLabel = strings.copySpecsLabel,
        testTag = "cpuz_soc_card",
        onCopy = { copySection(strings.sectionSocCpu, socRows) },
      )
    }

    item {
      HardwareSpecSectionCard(
        title = strings.sectionGpu,
        icon = Icons.Filled.DeveloperBoard,
        accent = palette.emerald,
        rows = gpuRows,
        copyLabel = strings.copySpecsLabel,
        testTag = "cpuz_gpu_card",
        onCopy = { copySection(strings.sectionGpu, gpuRows) },
      )
    }

    item {
      HardwareSpecSectionCard(
        title = strings.sectionDevice,
        icon = Icons.Filled.PhoneAndroid,
        accent = palette.violet,
        rows = deviceRows,
        copyLabel = strings.copySpecsLabel,
        testTag = "cpuz_device_card",
        onCopy = { copySection(strings.sectionDevice, deviceRows) },
      )
    }

    item {
      HardwareSpecSectionCard(
        title = strings.sectionDisplay,
        icon = Icons.Filled.DisplaySettings,
        accent = palette.amber,
        rows = displayRows,
        copyLabel = strings.copySpecsLabel,
        testTag = "cpuz_display_card",
        onCopy = { copySection(strings.sectionDisplay, displayRows) },
      )
    }

    item {
      HardwareSpecSectionCard(
        title = strings.sectionSystem,
        icon = Icons.Filled.Android,
        accent = palette.blue,
        rows = systemRows,
        copyLabel = strings.copySpecsLabel,
        testTag = "cpuz_system_card",
        onCopy = { copySection(strings.sectionSystem, systemRows) },
      )
    }
  }
}

@Composable
private fun HardwareSpecSectionCard(
  title: String,
  icon: ImageVector,
  accent: Color,
  rows: List<Pair<String, String>>,
  copyLabel: String,
  testTag: String,
  onCopy: () -> Unit,
) {
  TelemetryCard(
    modifier = Modifier.fillMaxWidth().testTag(testTag),
    borderColor = accent.copy(alpha = 0.35f),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
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
            color = accent.copy(alpha = 0.16f),
          ) {
            Icon(
              imageVector = icon,
              contentDescription = title,
              tint = accent,
              modifier = Modifier.padding(7.dp).size(19.dp),
            )
          }
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        TextButton(
          onClick = onCopy,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = copyLabel,
            tint = accent,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = copyLabel,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
          )
        }
      }

      Column(modifier = Modifier.padding(top = 8.dp)) {
        rows.forEachIndexed { idx, (key, value) ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Text(
              text = key,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(0.44f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = value.ifBlank { "-" },
              style = MonoMetricSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.End,
              modifier = Modifier.weight(0.56f),
            )
          }
          if (idx != rows.lastIndex) {
            HorizontalDivider(
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
              thickness = 0.7.dp,
            )
          }
        }
      }
    }
  }
}
