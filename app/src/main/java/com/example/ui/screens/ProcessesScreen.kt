package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.AppStrings
import com.example.data.CominitechSnapshot
import com.example.data.FormatUtils
import com.example.data.InstalledAppItem
import com.example.data.JvmThreadItem
import com.example.data.ProcessFilter
import com.example.data.RunningProcessItem
import com.example.ui.TelemetryUiState
import com.example.ui.components.MetricStatPill
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricSmall

@Composable
fun ProcessesScreen(
  uiState: TelemetryUiState,
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onQueryChange: (String) -> Unit,
  onFilterChange: (ProcessFilter) -> Unit,
  onKillBackground: (packageName: String, appName: String) -> Unit,
  onOpenAppInfo: (packageName: String) -> Unit,
  onShowThreads: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val q = uiState.processQuery.trim().lowercase()

  val filteredApps =
    remember(snapshot.installedApps, q, uiState.processFilter) {
      snapshot.installedApps.filter { app ->
        val matchesQuery =
          q.isEmpty() ||
            app.appName.lowercase().contains(q) ||
            app.packageName.lowercase().contains(q)
        val matchesFilter =
          when (uiState.processFilter) {
            ProcessFilter.ALL -> true
            ProcessFilter.RUNNING -> app.isCurrentlyRunning
            ProcessFilter.USER_APPS -> !app.isSystemApp
            ProcessFilter.SYSTEM_APPS -> app.isSystemApp
          }
        matchesQuery && matchesFilter
      }
    }

  val filteredProcs =
    remember(snapshot.runningProcesses, q) {
      snapshot.runningProcesses.filter { proc ->
        q.isEmpty() || proc.processName.lowercase().contains(q)
      }
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("processes_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Summary & Threads Trigger
    item {
      TelemetryCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            MetricStatPill(
              label = strings.ramConsumed,
              value = FormatUtils.formatBytes(snapshot.ram.usedBytes, 2),
              accent = palette.cyan,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.runningProcessesSection,
              value = "${snapshot.runningProcesses.size}",
              accent = palette.emerald,
              modifier = Modifier.weight(1f),
            )
            MetricStatPill(
              label = strings.cpuThreads,
              value = "${snapshot.jvmThreads.size}",
              accent = palette.violet,
              modifier = Modifier.weight(0.8f),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = strings.androidSandboxNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
              onClick = { onShowThreads(true) },
              modifier = Modifier.testTag("view_threads_button"),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.DeveloperBoard,
                contentDescription = strings.jvmThreadsSection,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = strings.cpuThreads,
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
      }
    }

    // Search + Filter Chips
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = uiState.processQuery,
          onValueChange = onQueryChange,
          modifier = Modifier.fillMaxWidth().testTag("process_search_input"),
          placeholder = { Text(strings.searchAppsPlaceholder) },
          leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = strings.searchAppsPlaceholder)
          },
          trailingIcon = {
            if (uiState.processQuery.isNotEmpty()) {
              IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
        )

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val filters =
            listOf(
              ProcessFilter.ALL to strings.filterAll,
              ProcessFilter.RUNNING to strings.filterRunning,
              ProcessFilter.USER_APPS to strings.filterUser,
              ProcessFilter.SYSTEM_APPS to strings.filterSystem,
            )
          filters.forEach { (filter, label) ->
            FilterChip(
              selected = uiState.processFilter == filter,
              onClick = { onFilterChange(filter) },
              modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
              label = { Text(label, style = MaterialTheme.typography.labelMedium) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = palette.cyan.copy(alpha = 0.2f),
                  selectedLabelColor = palette.cyan,
                ),
            )
          }
        }
      }
    }

    // Visible Running Processes Section
    if (filteredProcs.isNotEmpty()) {
      item {
        Text(
          text = "${strings.runningProcessesSection} (${filteredProcs.size})",
          style = MaterialTheme.typography.titleSmall,
          color = palette.emerald,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      items(filteredProcs, key = { "proc_${it.pid}" }) { proc ->
        RunningProcessCard(proc = proc, strings = strings)
      }
    }

    // Device Apps Inventory Section
    item {
      Text(
        text = "${strings.appsInventorySection} (${filteredApps.size})",
        style = MaterialTheme.typography.titleSmall,
        color = palette.cyan,
        modifier = Modifier.padding(top = 6.dp),
      )
    }

    if (filteredApps.isEmpty()) {
      item {
        TelemetryCard(modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Apps,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp),
            )
            Text(
              text = strings.noMatchingApps,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(filteredApps, key = { "app_${it.packageName}" }) { app ->
        InstalledAppTaskCard(
          app = app,
          strings = strings,
          onKillBackground = { onKillBackground(app.packageName, app.appName) },
          onOpenAppInfo = { onOpenAppInfo(app.packageName) },
        )
      }
    }
  }

  if (uiState.showThreadsModal) {
    ThreadsDialog(
      threads = snapshot.jvmThreads,
      strings = strings,
      onDismiss = { onShowThreads(false) },
    )
  }
}

@Composable
private fun RunningProcessCard(
  proc: RunningProcessItem,
  strings: AppStrings,
) {
  val palette = LocalTelemetryPalette.current
  TelemetryCard(
    modifier = Modifier.fillMaxWidth().testTag("running_proc_${proc.pid}"),
    borderColor = palette.emerald.copy(alpha = 0.35f),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = proc.processName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text =
              "${strings.pidLabel}: ${proc.pid} • ${strings.importanceLabel(proc.importanceCode)}",
            style = MonoMetricSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = palette.cyan.copy(alpha = 0.16f),
        ) {
          Text(
            text = FormatUtils.formatKb(proc.totalPssKb),
            style = MonoMetricSmall,
            color = palette.cyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = "Dalvik PSS",
          value = FormatUtils.formatKb(proc.dalvikPssKb),
          accent = palette.violet,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = "Native PSS",
          value = FormatUtils.formatKb(proc.nativePssKb),
          accent = palette.amber,
          modifier = Modifier.weight(1f),
        )
        MetricStatPill(
          label = "Other PSS",
          value = FormatUtils.formatKb(proc.otherPssKb),
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun InstalledAppTaskCard(
  app: InstalledAppItem,
  strings: AppStrings,
  onKillBackground: () -> Unit,
  onOpenAppInfo: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val iconBitmap =
    remember(app.packageName) {
      runCatching { app.icon?.toBitmap(84, 84)?.asImageBitmap() }.getOrNull()
    }

  TelemetryCard(
    modifier = Modifier.fillMaxWidth().testTag("app_task_card_${app.packageName}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (iconBitmap != null) {
          Image(
            bitmap = iconBitmap,
            contentDescription = app.appName,
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)),
          )
        } else {
          Box(
            modifier =
              Modifier.size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Apps,
              contentDescription = app.appName,
              tint = palette.cyan,
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = app.appName,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
            )
            if (app.isCurrentlyRunning) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = palette.emerald.copy(alpha = 0.18f),
              ) {
                Text(
                  text = FormatUtils.formatKb(app.runningPssKb),
                  style = MonoMetricSmall,
                  color = palette.emerald,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
          }
          Text(
            text = "${app.packageName} • v${app.versionName}",
            style = MonoMetricSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            "${strings.apkSizeLabel}: ${FormatUtils.formatBytes(app.apkSizeBytes, 1)} • API ${app.targetSdk}",
          style = MonoMetricSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onKillBackground,
            modifier = Modifier.testTag("trim_app_${app.packageName}"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, palette.amber.copy(alpha = 0.6f)),
          ) {
            Icon(
              imageVector = Icons.Filled.CleaningServices,
              contentDescription = strings.endBackgroundAction,
              tint = palette.amber,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.endBackgroundAction,
              style = MaterialTheme.typography.labelSmall,
              color = palette.amber,
            )
          }

          OutlinedButton(
            onClick = onOpenAppInfo,
            modifier = Modifier.testTag("info_app_${app.packageName}"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = strings.appDetailsAction,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.appDetailsAction,
              style = MaterialTheme.typography.labelSmall,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ThreadsDialog(
  threads: List<JvmThreadItem>,
  strings: AppStrings,
  onDismiss: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "${strings.jvmThreadsSection} (${threads.size})",
        style = MaterialTheme.typography.titleMedium,
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().height(340.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        items(threads, key = { it.id }) { thread ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = thread.name,
                  style = MonoMetricSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "ID ${thread.id} • P${thread.priority}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = thread.state,
                style = MonoMetricSmall,
                color =
                  if (thread.state == "RUNNABLE") palette.emerald
                  else palette.cyan,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("OK")
      }
    },
  )
}
