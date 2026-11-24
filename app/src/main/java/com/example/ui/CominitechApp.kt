package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.AppLanguage
import com.example.data.AppStrings
import com.example.data.AppTab
import com.example.ui.screens.HardwareScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.PhonesScreen
import com.example.ui.screens.ProcessesScreen
import com.example.ui.screens.SensorsScreen
import com.example.ui.theme.CominitechTheme
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricSmall
import kotlinx.coroutines.launch

private data class NavTabItem(
  val tab: AppTab,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

@Composable
fun CominitechApp(viewModel: MainViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val strings = remember(uiState.language) { AppStrings(uiState.language) }
  val layoutDir =
    if (uiState.language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  if (uiState.selectedTab != AppTab.PERFORMANCE) {
    BackHandler { viewModel.selectTab(AppTab.PERFORMANCE) }
  }

  CominitechTheme(darkTheme = uiState.isDarkTheme) {
    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
      val palette = LocalTelemetryPalette.current
      val navItems =
        remember(strings) {
          listOf(
            NavTabItem(
              tab = AppTab.PERFORMANCE,
              label = strings.tabPerformance,
              selectedIcon = Icons.Filled.Speed,
              unselectedIcon = Icons.Outlined.Speed,
            ),
            NavTabItem(
              tab = AppTab.PROCESSES,
              label = strings.tabProcesses,
              selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
              unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt,
            ),
            NavTabItem(
              tab = AppTab.HARDWARE,
              label = strings.tabHardware,
              selectedIcon = Icons.Filled.DeveloperBoard,
              unselectedIcon = Icons.Outlined.DeveloperBoard,
            ),
            NavTabItem(
              tab = AppTab.PHONES,
              label = strings.tabPhones,
              selectedIcon = Icons.Filled.PhoneAndroid,
              unselectedIcon = Icons.Outlined.PhoneAndroid,
            ),
            NavTabItem(
              tab = AppTab.SENSORS,
              label = strings.tabSensors,
              selectedIcon = Icons.Filled.Sensors,
              unselectedIcon = Icons.Outlined.Sensors,
            ),
          )
        }

      BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
      ) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = MaterialTheme.colorScheme.background,
          contentWindowInsets = WindowInsets(0, 0, 0, 0),
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          topBar = {
            CominitechTopBar(
              strings = strings,
              uiState = uiState,
              onTogglePause = viewModel::togglePause,
              onToggleLanguage = viewModel::toggleLanguage,
              onToggleTheme = viewModel::toggleTheme,
            )
          },
          bottomBar = {
            if (!useRail) {
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                windowInsets = WindowInsets.navigationBars,
              ) {
                navItems.forEach { item ->
                  val selected = uiState.selectedTab == item.tab
                  NavigationBarItem(
                    selected = selected,
                    onClick = { viewModel.selectTab(item.tab) },
                    modifier = Modifier.testTag("nav_${item.tab.name.lowercase()}"),
                    icon = {
                      Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                      )
                    },
                    label = {
                      Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                      )
                    },
                    colors =
                      NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.cyan,
                        selectedTextColor = palette.cyan,
                        indicatorColor = palette.cyan.copy(alpha = 0.16f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      ),
                  )
                }
              }
            }
          },
        ) { innerPadding ->
          Row(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
          ) {
            if (useRail) {
              NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface,
              ) {
                Spacer(modifier = Modifier.weight(0.15f))
                navItems.forEach { item ->
                  val selected = uiState.selectedTab == item.tab
                  NavigationRailItem(
                    selected = selected,
                    onClick = { viewModel.selectTab(item.tab) },
                    modifier = Modifier.testTag("rail_${item.tab.name.lowercase()}"),
                    icon = {
                      Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                      )
                    },
                    label = {
                      Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                      )
                    },
                    colors =
                      NavigationRailItemDefaults.colors(
                        selectedIconColor = palette.cyan,
                        selectedTextColor = palette.cyan,
                        indicatorColor = palette.cyan.copy(alpha = 0.16f),
                      ),
                  )
                }
                Spacer(modifier = Modifier.weight(1f))
              }
            }

            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.TopCenter,
            ) {
              val snapshot = uiState.snapshot
              if (uiState.isLoading || snapshot == null) {
                Box(
                  modifier = Modifier.fillMaxSize(),
                  contentAlignment = Alignment.Center,
                ) {
                  CircularProgressIndicator(color = palette.cyan)
                }
              } else {
                Crossfade(
                  targetState = uiState.selectedTab,
                  label = "tab_crossfade",
                  modifier = Modifier.fillMaxSize().widthIn(max = 880.dp),
                ) { tab ->
                  when (tab) {
                    AppTab.PERFORMANCE ->
                      PerformanceScreen(
                        uiState = uiState,
                        snapshot = snapshot,
                        strings = strings,
                        onOptimizeRam = viewModel::optimizeRam,
                        onTestAlert = viewModel::triggerAlertPreview,
                        onClearAlertHistory = viewModel::clearAlertHistory,
                      )

                    AppTab.PROCESSES ->
                      ProcessesScreen(
                        uiState = uiState,
                        snapshot = snapshot,
                        strings = strings,
                        onQueryChange = viewModel::updateProcessQuery,
                        onFilterChange = viewModel::selectProcessFilter,
                        onKillBackground = viewModel::killBackgroundApp,
                        onOpenAppInfo = viewModel::openAppSettings,
                        onShowThreads = viewModel::setShowThreadsModal,
                      )

                    AppTab.HARDWARE ->
                      HardwareScreen(
                        snapshot = snapshot,
                        strings = strings,
                        onShowToast = { msg ->
                          scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                      )

                    AppTab.PHONES ->
                      PhonesScreen(
                        uiState = uiState,
                        snapshot = snapshot,
                        strings = strings,
                        onSubTabChange = viewModel::setPhonesSubTab,
                        onQueryChange = viewModel::updatePhoneQuery,
                        onBrandSelect = viewModel::selectBrand,
                        onCurrencySelect = viewModel::selectCurrency,
                        onCompareWithPhone = viewModel::compareWithPhone,
                        onAddCustomPhone = viewModel::addCustomPhoneAndCompare,
                        onRunBenchmark = viewModel::runLiveDeviceBenchmark,
                      )

                    AppTab.SENSORS ->
                      SensorsScreen(
                        uiState = uiState,
                        snapshot = snapshot,
                        strings = strings,
                        onQueryChange = viewModel::updateSensorQuery,
                      )
                  }
                }
              }
            }
          }
        }
      }

      uiState.pendingAlertDialog?.let { alert ->
        androidx.compose.material3.AlertDialog(
          onDismissRequest = viewModel::dismissPendingAlert,
          title = {
            Text(
              text = strings.alertHighThermalTitle,
              style = MaterialTheme.typography.titleMedium,
              color = palette.coral,
              fontWeight = FontWeight.Bold,
            )
          },
          text = {
            Text(
              text =
                strings.thermalDialogMessage(
                  kind = alert.kind,
                  value = alert.currentValueText,
                  limit = alert.thresholdText,
                ),
              style = MaterialTheme.typography.bodyMedium,
            )
          },
          confirmButton = {
            androidx.compose.material3.TextButton(
              onClick = viewModel::dismissPendingAlert,
              modifier = Modifier.testTag("dismiss_alert_dialog_button"),
            ) {
              Text(strings.alertDialogConfirm)
            }
          },
        )
      }
    }
  }
}

@Composable
private fun CominitechTopBar(
  strings: AppStrings,
  uiState: TelemetryUiState,
  onTogglePause: () -> Unit,
  onToggleLanguage: () -> Unit,
  onToggleTheme: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val pulseTransition = rememberInfiniteTransition(label = "live_pulse")
  val pulseAlpha by
    pulseTransition.animateFloat(
      initialValue = 0.35f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
      label = "pulse_alpha",
    )

  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
  ) {
    Column {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          Image(
            painter = painterResource(id = R.drawable.cominitech_icon_1795532922094),
            contentDescription = strings.appName,
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)),
          )
          Column {
            Text(
              text = strings.appName,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
            )
            Text(
              text = strings.subtitle,
              style = MonoMetricSmall,
              color = palette.cyan,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          // Live / Pause pill
          val statusColor = if (uiState.isPaused) palette.amber else palette.emerald
          Surface(
            modifier =
              Modifier.minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onTogglePause)
                .testTag("live_pause_button"),
            shape = RoundedCornerShape(20.dp),
            color = statusColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Box(
                modifier =
                  Modifier.size(8.dp)
                    .clip(CircleShape)
                    .background(
                      if (uiState.isPaused) statusColor else statusColor.copy(alpha = pulseAlpha)
                    )
              )
              Text(
                text = if (uiState.isPaused) strings.pausedLabel else strings.liveLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor,
              )
              Icon(
                imageVector = if (uiState.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (uiState.isPaused) strings.liveLabel else strings.pausedLabel,
                tint = statusColor,
                modifier = Modifier.size(14.dp),
              )
            }
          }

          // Language Switch (AR / EN)
          Surface(
            modifier =
              Modifier.minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggleLanguage)
                .testTag("language_toggle_button"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Text(
              text = if (uiState.language == AppLanguage.AR) "EN" else "AR",
              style = MonoMetricSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
          }

          // Theme Toggle
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.testTag("theme_toggle_button"),
          ) {
            Icon(
              imageVector = if (uiState.isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
              contentDescription = "Toggle Theme",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
      HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        thickness = 0.8.dp,
      )
    }
  }
}
