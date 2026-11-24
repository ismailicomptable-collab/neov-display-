package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AppLanguage
import com.example.data.AppStrings
import com.example.data.CominitechSnapshot
import com.example.data.CurrencyInfo
import com.example.data.FormatUtils
import com.example.data.PhoneDesignStyle
import com.example.data.PhoneSpec
import com.example.ui.TelemetryUiState
import com.example.ui.components.MetricStatPill
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale
import kotlin.math.roundToInt

@DrawableRes
private fun phoneImageRes(style: PhoneDesignStyle): Int =
  when (style) {
    PhoneDesignStyle.IPHONE_TITANIUM -> R.drawable.img_phone_iphone_1795536239997
    PhoneDesignStyle.GALAXY_ULTRA -> R.drawable.img_phone_samsung_1795536239318
    PhoneDesignStyle.CIRCULAR_CAMERA -> R.drawable.img_phone_circle_1795536241729
    PhoneDesignStyle.VISOR_GAMING -> R.drawable.img_phone_fold_gaming_1795536241810
  }

@Composable
private fun PhoneThumbnail(
  phone: PhoneSpec,
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  Surface(
    modifier = modifier.size(width = width, height = height),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = BorderStroke(1.dp, palette.cyan.copy(alpha = 0.25f)),
  ) {
    Image(
      painter = painterResource(id = phoneImageRes(phone.designStyle)),
      contentDescription = phone.model,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
    )
  }
}

@Composable
fun PhonesScreen(
  uiState: TelemetryUiState,
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onSubTabChange: (Int) -> Unit,
  onQueryChange: (String) -> Unit,
  onBrandSelect: (String) -> Unit,
  onCurrencySelect: (CurrencyInfo) -> Unit,
  onCompareWithPhone: (PhoneSpec) -> Unit,
  onAddCustomPhone: (String, String, Int, Int, Int, Double) -> Unit,
  onRunBenchmark: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val isAr = uiState.language == AppLanguage.AR
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showCustomPhoneDialog by remember { mutableStateOf(false) }
  var showOpponentPickerDialog by remember { mutableStateOf(false) }
  var actionSheetPhone by remember { mutableStateOf<PhoneSpec?>(null) }
  var detailPhone by remember { mutableStateOf<PhoneSpec?>(null) }

  Column(modifier = modifier.fillMaxSize()) {
    // Sub-tabs: Phones Catalog & Prices vs VS Benchmark
    TabRow(
      selectedTabIndex = uiState.phonesSubTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = palette.cyan,
    ) {
      Tab(
        selected = uiState.phonesSubTab == 0,
        onClick = { onSubTabChange(0) },
        modifier = Modifier.testTag("phones_subtab_catalog"),
        text = {
          Text(
            text = strings.phonesCatalogTab,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
          )
        },
      )
      Tab(
        selected = uiState.phonesSubTab == 1,
        onClick = { onSubTabChange(1) },
        modifier = Modifier.testTag("phones_subtab_vs"),
        text = {
          Text(
            text = strings.phonesVsTab,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
          )
        },
      )
    }

    if (uiState.phonesSubTab == 0) {
      PhonesCatalogView(
        uiState = uiState,
        isAr = isAr,
        strings = strings,
        onQueryChange = onQueryChange,
        onBrandSelect = onBrandSelect,
        onOpenCurrencyPicker = { showCurrencyDialog = true },
        onOpenCustomPhone = { showCustomPhoneDialog = true },
        onPhoneClicked = { phone -> actionSheetPhone = phone },
      )
    } else {
      PhoneVsBenchmarkView(
        uiState = uiState,
        snapshot = snapshot,
        strings = strings,
        onOpenOpponentPicker = { showOpponentPickerDialog = true },
        onOpenCustomPhone = { showCustomPhoneDialog = true },
        onRunBenchmark = onRunBenchmark,
      )
    }
  }

  // Tap on any phone -> Action Dialog with "Compare with mine" & underneath "View all description"
  actionSheetPhone?.let { phone ->
    PhoneActionDialog(
      phone = phone,
      currency = uiState.selectedCurrency,
      strings = strings,
      onCompareWithMine = {
        actionSheetPhone = null
        onCompareWithPhone(phone)
      },
      onViewAllDescription = {
        actionSheetPhone = null
        detailPhone = phone
      },
      onDismiss = { actionSheetPhone = null },
    )
  }

  // Full Phone Description + All Good (Pros) & Bad (Cons) + Full Specs Dialog
  detailPhone?.let { phone ->
    PhoneFullDescriptionDialog(
      phone = phone,
      currency = uiState.selectedCurrency,
      isAr = isAr,
      strings = strings,
      onCompareWithMine = {
        detailPhone = null
        onCompareWithPhone(phone)
      },
      onDismiss = { detailPhone = null },
    )
  }

  if (showCurrencyDialog) {
    CurrencyPickerDialog(
      currencies = uiState.currencies,
      selectedCurrency = uiState.selectedCurrency,
      isAr = isAr,
      strings = strings,
      onSelect = { cur ->
        onCurrencySelect(cur)
        showCurrencyDialog = false
      },
      onDismiss = { showCurrencyDialog = false },
    )
  }

  if (showCustomPhoneDialog) {
    CustomPhoneDialog(
      strings = strings,
      onSave = { model, chipset, ram, bat, hz, price ->
        onAddCustomPhone(model, chipset, ram, bat, hz, price)
        showCustomPhoneDialog = false
      },
      onDismiss = { showCustomPhoneDialog = false },
    )
  }

  if (showOpponentPickerDialog) {
    OpponentPickerDialog(
      phones = uiState.phonesCatalog,
      selectedPhone = uiState.selectedOpponentPhone,
      currency = uiState.selectedCurrency,
      strings = strings,
      onSelect = { phone ->
        onCompareWithPhone(phone)
        showOpponentPickerDialog = false
      },
      onDismiss = { showOpponentPickerDialog = false },
    )
  }
}

@Composable
private fun PhonesCatalogView(
  uiState: TelemetryUiState,
  isAr: Boolean,
  strings: AppStrings,
  onQueryChange: (String) -> Unit,
  onBrandSelect: (String) -> Unit,
  onOpenCurrencyPicker: () -> Unit,
  onOpenCustomPhone: () -> Unit,
  onPhoneClicked: (PhoneSpec) -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val q = uiState.phoneQuery.trim().lowercase()
  val brands = remember(uiState.phonesCatalog) {
    listOf("All") + uiState.phonesCatalog.map { it.brand }.distinct()
  }

  val filteredPhones =
    remember(uiState.phonesCatalog, q, uiState.selectedBrand) {
      uiState.phonesCatalog.filter { p ->
        val matchesBrand = uiState.selectedBrand == "All" || p.brand == uiState.selectedBrand
        val matchesQuery =
          q.isEmpty() ||
            p.model.lowercase().contains(q) ||
            p.brand.lowercase().contains(q) ||
            p.chipset.lowercase().contains(q)
        matchesBrand && matchesQuery
      }
    }

  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("phones_catalog_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Currency Bar & Custom Phone Button
    item {
      TelemetryCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = palette.cyan.copy(alpha = 0.4f),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenCurrencyPicker)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("currency_picker_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.CurrencyExchange,
              contentDescription = strings.selectCurrencyTitle,
              tint = palette.cyan,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text =
                  "${uiState.selectedCurrency.code} (${if (isAr) uiState.selectedCurrency.nameAr else uiState.selectedCurrency.nameEn})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text =
                  "${if (uiState.isCurrencyLive) strings.liveRatesLabel else strings.offlineRatesLabel} • ${uiState.currencies.size}",
                style = MonoMetricSmall,
                color = if (uiState.isCurrencyLive) palette.emerald else palette.amber,
              )
            }
          }

          OutlinedButton(
            onClick = onOpenCustomPhone,
            modifier = Modifier.testTag("add_custom_phone_button"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = strings.addCustomPhoneBtn,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.addCustomPhoneBtn,
              style = MaterialTheme.typography.labelSmall,
            )
          }
        }
      }
    }

    // Search + Brand Chips + Count Badge
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = uiState.phoneQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).testTag("phone_search_input"),
            placeholder = { Text(strings.searchPhonesPlaceholder) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
              if (uiState.phoneQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                  Icon(Icons.Filled.Close, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
          )

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cyan.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, palette.cyan.copy(alpha = 0.35f)),
          ) {
            Text(
              text = "${filteredPhones.size}\n${strings.phonesCountLabel}",
              style = MonoMetricSmall,
              color = palette.cyan,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          brands.forEach { brand ->
            FilterChip(
              selected = uiState.selectedBrand == brand,
              onClick = { onBrandSelect(brand) },
              label = {
                Text(
                  text = if (brand == "All") strings.filterAll else brand,
                  style = MaterialTheme.typography.labelMedium,
                )
              },
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

    items(filteredPhones, key = { it.id }) { phone ->
      PhoneCatalogCard(
        phone = phone,
        currency = uiState.selectedCurrency,
        isAr = isAr,
        strings = strings,
        onClick = { onPhoneClicked(phone) },
      )
    }
  }
}

@Composable
private fun PhoneCatalogCard(
  phone: PhoneSpec,
  currency: CurrencyInfo,
  isAr: Boolean,
  strings: AppStrings,
  onClick: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val pros = if (isAr) phone.prosAr else phone.prosEn
  val cons = if (isAr) phone.consAr else phone.consEn

  TelemetryCard(
    modifier =
      Modifier.fillMaxWidth()
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("phone_card_${phone.id}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PhoneThumbnail(phone = phone, width = 76.dp, height = 96.dp)

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = phone.model,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${phone.brand} • ${phone.releaseYear} • ${phone.osVersion}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.cyan,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "$${String.format(Locale.US, "%,.0f", phone.priceUsd)}",
              style = MonoMetricMedium,
              color = palette.emerald,
              fontWeight = FontWeight.Bold,
            )
            if (currency.code != "USD") {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = palette.amber.copy(alpha = 0.15f),
              ) {
                Text(
                  text = FormatUtils.formatCurrencyAmount(phone.priceUsd, currency),
                  style = MonoMetricSmall,
                  color = palette.amber,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
          }

          Text(
            text = "${phone.displaySummary} (${phone.refreshRateHz} Hz)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricStatPill(
          label = strings.specChipset,
          value = phone.chipset,
          accent = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1.4f),
        )
        MetricStatPill(
          label = strings.specRam,
          value = "${phone.ramGb} GB",
          accent = palette.cyan,
          modifier = Modifier.weight(0.7f),
        )
        MetricStatPill(
          label = strings.specBattery,
          value = "${phone.batteryMah} mAh",
          accent = palette.emerald,
          modifier = Modifier.weight(0.9f),
        )
      }

      // Quick preview of Good (Pros) & Bad (Cons)
      if (pros.isNotEmpty() || cons.isNotEmpty()) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            pros.firstOrNull()?.let { good ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = strings.prosTitle,
                  tint = palette.emerald,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = good,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            cons.firstOrNull()?.let { bad ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.RemoveCircleOutline,
                  contentDescription = strings.consTitle,
                  tint = palette.coral,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = bad,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
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
private fun PhoneActionDialog(
  phone: PhoneSpec,
  currency: CurrencyInfo,
  strings: AppStrings,
  onCompareWithMine: () -> Unit,
  onViewAllDescription: () -> Unit,
  onDismiss: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        PhoneThumbnail(phone = phone, width = 56.dp, height = 72.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = phone.model,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${phone.brand} • ${phone.releaseYear}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.cyan,
          )
          Text(
            text =
              if (currency.code == "USD") "$${String.format(Locale.US, "%,.0f", phone.priceUsd)}"
              else "$${String.format(Locale.US, "%,.0f", phone.priceUsd)} • ${FormatUtils.formatCurrencyAmount(phone.priceUsd, currency)}",
            style = MonoMetricSmall,
            color = palette.emerald,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Primary action: Compare with mine
        Button(
          onClick = onCompareWithMine,
          modifier =
            Modifier.fillMaxWidth()
              .minimumInteractiveComponentSize()
              .testTag("dialog_compare_with_mine_${phone.id}"),
          shape = RoundedCornerShape(12.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = palette.cyan,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
            contentDescription = strings.compareWithMineBtn,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.compareWithMineBtn,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }

        // Underneath: View all description
        OutlinedButton(
          onClick = onViewAllDescription,
          modifier =
            Modifier.fillMaxWidth()
              .minimumInteractiveComponentSize()
              .testTag("dialog_view_all_desc_${phone.id}"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, palette.violet.copy(alpha = 0.6f)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.violet),
        ) {
          Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = strings.viewAllDescriptionBtn,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.viewAllDescriptionBtn,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancelBtn)
      }
    },
  )
}

@Composable
private fun PhoneFullDescriptionDialog(
  phone: PhoneSpec,
  currency: CurrencyInfo,
  isAr: Boolean,
  strings: AppStrings,
  onCompareWithMine: () -> Unit,
  onDismiss: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val pros = if (isAr) phone.prosAr else phone.prosEn
  val cons = if (isAr) phone.consAr else phone.consEn
  val overview = if (isAr) phone.fullDescriptionAr else phone.fullDescriptionEn

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier.fillMaxWidth(0.94f)
          .widthIn(max = 580.dp)
          .heightIn(max = 720.dp)
          .testTag("phone_full_description_dialog"),
      shape = RoundedCornerShape(22.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      border = BorderStroke(1.dp, palette.cyan.copy(alpha = 0.35f)),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header bar
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = phone.model,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("close_description_dialog"),
          ) {
            Icon(Icons.Filled.Close, contentDescription = strings.cancelBtn)
          }
        }

        HorizontalDivider(thickness = 0.5.dp)

        // Scrollable Body
        Column(
          modifier =
            Modifier.weight(1f, fill = false)
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // Top hero image & price summary
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            PhoneThumbnail(phone = phone, width = 104.dp, height = 132.dp)
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = "${phone.brand} • ${phone.releaseYear}",
                style = MaterialTheme.typography.labelMedium,
                color = palette.cyan,
              )
              Text(
                text = phone.osVersion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "$${String.format(Locale.US, "%,.0f", phone.priceUsd)} USD",
                style = MonoMetricMedium,
                color = palette.emerald,
                fontWeight = FontWeight.Bold,
              )
              if (currency.code != "USD") {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = palette.amber.copy(alpha = 0.16f),
                ) {
                  Text(
                    text = FormatUtils.formatCurrencyAmount(phone.priceUsd, currency),
                    style = MonoMetricSmall,
                    color = palette.amber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }

          // Overview
          if (overview.isNotBlank()) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = strings.phoneOverviewTitle,
                  style = MaterialTheme.typography.labelMedium,
                  color = palette.cyan,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = overview,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }

          // Good features (Pros)
          if (pros.isNotEmpty()) {
            Surface(
              modifier = Modifier.fillMaxWidth().testTag("phone_pros_section"),
              shape = RoundedCornerShape(14.dp),
              color = palette.emerald.copy(alpha = 0.1f),
              border = BorderStroke(1.dp, palette.emerald.copy(alpha = 0.35f)),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = strings.prosTitle,
                  style = MaterialTheme.typography.titleSmall,
                  color = palette.emerald,
                  fontWeight = FontWeight.Bold,
                )
                pros.forEach { pro ->
                  Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.CheckCircle,
                      contentDescription = null,
                      tint = palette.emerald,
                      modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    )
                    Text(
                      text = pro,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }

          // Bad features (Cons)
          if (cons.isNotEmpty()) {
            Surface(
              modifier = Modifier.fillMaxWidth().testTag("phone_cons_section"),
              shape = RoundedCornerShape(14.dp),
              color = palette.coral.copy(alpha = 0.1f),
              border = BorderStroke(1.dp, palette.coral.copy(alpha = 0.35f)),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = strings.consTitle,
                  style = MaterialTheme.typography.titleSmall,
                  color = palette.coral,
                  fontWeight = FontWeight.Bold,
                )
                cons.forEach { con ->
                  Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.RemoveCircleOutline,
                      contentDescription = null,
                      tint = palette.coral,
                      modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    )
                    Text(
                      text = con,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }

          // Full Specs Table
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = strings.fullSpecsTitle,
                style = MaterialTheme.typography.titleSmall,
                color = palette.cyan,
                fontWeight = FontWeight.Bold,
              )
              PhoneDetailSpecRow(strings.specChipset, phone.chipset)
              PhoneDetailSpecRow(strings.keyProcessor, phone.cpuSummary)
              PhoneDetailSpecRow(strings.specRam, "${phone.ramGb} GB")
              PhoneDetailSpecRow(strings.specStorage, phone.storageSummary)
              PhoneDetailSpecRow(strings.specDisplay, "${phone.displaySummary} (${phone.refreshRateHz} Hz)")
              PhoneDetailSpecRow(strings.specCamera, phone.cameraSummary)
              PhoneDetailSpecRow(strings.specBattery, "${phone.batteryMah} mAh")
              PhoneDetailSpecRow(strings.specCharging, phone.chargingSummary)
              PhoneDetailSpecRow(strings.specProtection, phone.protectionSummary)
              PhoneDetailSpecRow(strings.specOs, phone.osVersion)
              PhoneDetailSpecRow(strings.singleCoreLabel, "${phone.singleCoreScore}")
              PhoneDetailSpecRow(strings.multiCoreLabel, "${phone.multiCoreScore}")
              PhoneDetailSpecRow("AnTuTu", "~${String.format(Locale.US, "%,d", phone.antutuScore)}")
            }
          }
        }

        HorizontalDivider(thickness = 0.5.dp)

        // Footer CTA: Compare with mine
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text(strings.cancelBtn)
          }
          Button(
            onClick = onCompareWithMine,
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("full_desc_compare_button"),
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = palette.cyan,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.CompareArrows,
              contentDescription = strings.compareWithMineBtn,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.compareWithMineBtn,
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PhoneDetailSpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.38f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.weight(0.62f),
    )
  }
  HorizontalDivider(thickness = 0.4.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
}

@Composable
private fun PhoneVsBenchmarkView(
  uiState: TelemetryUiState,
  snapshot: CominitechSnapshot,
  strings: AppStrings,
  onOpenOpponentPicker: () -> Unit,
  onOpenCustomPhone: () -> Unit,
  onRunBenchmark: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current
  val myRamGb = ((snapshot.ram.totalBytes / (1024.0 * 1024.0 * 1024.0)).roundToInt()).coerceAtLeast(1)
  val myHz = snapshot.hardware.display.refreshRateHz.roundToInt()
  val myBatMah = snapshot.battery.designCapacityMah ?: 4500
  val opponent = uiState.selectedOpponentPhone
  val bench = uiState.benchmarkResult

  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("phones_vs_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // VS Header Card
    item {
      TelemetryCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = palette.cyan.copy(alpha = 0.45f),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Your Phone Box
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              color = palette.cyan.copy(alpha = 0.14f),
              border = BorderStroke(1.dp, palette.cyan.copy(alpha = 0.45f)),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = strings.yourPhoneLabel,
                  style = MaterialTheme.typography.labelSmall,
                  color = palette.cyan,
                )
                Text(
                  text = "${snapshot.hardware.device.brand} ${snapshot.hardware.device.model}",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${snapshot.hardware.cpu.socModel} • $myRamGb GB",
                  style = MonoMetricSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            Text(
              text = "VS",
              style = MonoMetricMedium,
              fontWeight = FontWeight.Bold,
              color = palette.amber,
            )

            // Opponent Phone Box (Clickable to change, with phone photo)
            Surface(
              modifier =
                Modifier.weight(1f)
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable(onClick = onOpenOpponentPicker)
                  .testTag("pick_opponent_card"),
              shape = RoundedCornerShape(14.dp),
              color = palette.violet.copy(alpha = 0.14f),
              border = BorderStroke(1.dp, palette.violet.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                PhoneThumbnail(phone = opponent, width = 38.dp, height = 48.dp)
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Text(
                    text = strings.opponentPhoneLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.violet,
                  )
                  Text(
                    text = opponent.model,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "${opponent.chipset} • ${opponent.ramGb} GB",
                    style = MonoMetricSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = onOpenOpponentPicker,
              modifier = Modifier.weight(1f).testTag("pick_opponent_button"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.pickOpponentLabel, style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = onOpenCustomPhone,
              modifier = Modifier.weight(0.9f),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.addCustomPhoneBtn, style = MaterialTheme.typography.labelMedium)
            }
          }

          // Run Live Benchmark CTA
          Button(
            onClick = onRunBenchmark,
            enabled = !uiState.isRunningBenchmark,
            modifier = Modifier.fillMaxWidth().testTag("run_benchmark_button"),
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = palette.emerald,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ),
          ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = strings.runLiveBenchmarkBtn)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text =
                if (uiState.isRunningBenchmark) strings.runningBenchmarkLabel
                else strings.runLiveBenchmarkBtn,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }

          if (uiState.isRunningBenchmark) {
            LinearProgressIndicator(
              progress = { uiState.benchmarkProgress.coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
              color = palette.emerald,
              strokeCap = StrokeCap.Round,
            )
          } else if (bench == null) {
            Text(
              text = strings.benchmarkPromptHint,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // Live Benchmark Results & Winner Verdict
    if (bench != null) {
      val userWins = bench.estimatedOverallScore >= opponent.antutuScore
      val verdictColor = if (userWins) palette.emerald else palette.violet

      item {
        TelemetryCard(
          modifier = Modifier.fillMaxWidth().testTag("benchmark_verdict_card"),
          borderColor = verdictColor.copy(alpha = 0.55f),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = verdictColor,
                modifier = Modifier.size(22.dp),
              )
              Text(
                text = if (userWins) strings.winnerYourPhone else strings.winnerOpponent,
                style = MaterialTheme.typography.titleMedium,
                color = verdictColor,
                fontWeight = FontWeight.Bold,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              MetricStatPill(
                label = strings.ramBandwidthLabel,
                value = "${bench.memoryBandwidthMbPerSec} MB/s",
                accent = palette.cyan,
                modifier = Modifier.weight(1f),
              )
              MetricStatPill(
                label = strings.storageIoLabel,
                value = "${bench.storageSpeedMbPerSec} MB/s",
                accent = palette.amber,
                modifier = Modifier.weight(1f),
              )
            }

            ComparisonBarRow(
              label = strings.singleCoreLabel,
              leftValue = bench.singleCoreScore,
              rightValue = opponent.singleCoreScore,
              leftText = "${bench.singleCoreScore}",
              rightText = "${opponent.singleCoreScore}",
              leftColor = palette.cyan,
              rightColor = palette.violet,
            )
            ComparisonBarRow(
              label = strings.multiCoreLabel,
              leftValue = bench.multiCoreScore,
              rightValue = opponent.multiCoreScore,
              leftText = "${bench.multiCoreScore}",
              rightText = "${opponent.multiCoreScore}",
              leftColor = palette.cyan,
              rightColor = palette.violet,
            )
            ComparisonBarRow(
              label = strings.overallScoreLabel,
              leftValue = bench.estimatedOverallScore,
              rightValue = opponent.antutuScore,
              leftText = String.format(Locale.US, "%,d", bench.estimatedOverallScore),
              rightText = String.format(Locale.US, "%,d", opponent.antutuScore),
              leftColor = palette.emerald,
              rightColor = palette.violet,
            )
          }
        }
      }
    }

    // Hardware Spec Face-Off Card (Always Visible)
    item {
      TelemetryCard(modifier = Modifier.fillMaxWidth().testTag("spec_faceoff_card")) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ComparisonBarRow(
            label = strings.specRam,
            leftValue = myRamGb,
            rightValue = opponent.ramGb,
            leftText = "$myRamGb GB",
            rightText = "${opponent.ramGb} GB",
            leftColor = palette.cyan,
            rightColor = palette.violet,
          )
          ComparisonBarRow(
            label = strings.keyRefreshRate,
            leftValue = myHz,
            rightValue = opponent.refreshRateHz,
            leftText = "$myHz Hz",
            rightText = "${opponent.refreshRateHz} Hz",
            leftColor = palette.cyan,
            rightColor = palette.violet,
          )
          ComparisonBarRow(
            label = strings.specBattery,
            leftValue = myBatMah,
            rightValue = opponent.batteryMah,
            leftText = "$myBatMah mAh",
            rightText = "${opponent.batteryMah} mAh",
            leftColor = palette.emerald,
            rightColor = palette.violet,
          )
          ComparisonBarRow(
            label = strings.keyCores,
            leftValue = snapshot.cpu.coreCount,
            rightValue = 8,
            leftText = "${snapshot.cpu.coreCount} Cores",
            rightText = opponent.cpuSummary.take(12),
            leftColor = palette.cyan,
            rightColor = palette.violet,
          )
        }
      }
    }
  }
}

@Composable
private fun ComparisonBarRow(
  label: String,
  leftValue: Int,
  rightValue: Int,
  leftText: String,
  rightText: String,
  leftColor: Color,
  rightColor: Color,
) {
  val maxVal = maxOf(leftValue, rightValue, 1).toFloat()
  val leftFrac by
    animateFloatAsState(
      targetValue = (leftValue / maxVal).coerceIn(0.08f, 1f),
      animationSpec = spring(),
      label = "left_$label",
    )
  val rightFrac by
    animateFloatAsState(
      targetValue = (rightValue / maxVal).coerceIn(0.08f, 1f),
      animationSpec = spring(),
      label = "right_$label",
    )

  Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(text = leftText, style = MonoMetricSmall, color = leftColor, fontWeight = FontWeight.Bold)
      Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
      Text(text = rightText, style = MonoMetricSmall, color = rightColor, fontWeight = FontWeight.Bold)
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier =
          Modifier.weight(1f)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.CenterEnd,
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth(leftFrac)
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(leftColor)
        )
      }
      Box(
        modifier =
          Modifier.weight(1f)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.CenterStart,
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth(rightFrac)
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(rightColor)
        )
      }
    }
  }
}

@Composable
private fun CurrencyPickerDialog(
  currencies: List<CurrencyInfo>,
  selectedCurrency: CurrencyInfo,
  isAr: Boolean,
  strings: AppStrings,
  onSelect: (CurrencyInfo) -> Unit,
  onDismiss: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  val q = query.trim().lowercase()
  val filtered =
    remember(currencies, q) {
      currencies.filter { c ->
        q.isEmpty() ||
          c.code.lowercase().contains(q) ||
          c.nameAr.lowercase().contains(q) ||
          c.nameEn.lowercase().contains(q)
      }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.selectCurrencyTitle, style = MaterialTheme.typography.titleMedium) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(strings.searchCurrencyPlaceholder) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("currency_search_input"),
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(filtered, key = { it.code }) { item ->
            val isSelected = item.code == selectedCurrency.code
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .clickable { onSelect(item) }
                  .padding(horizontal = 8.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${item.code} • ${if (isAr) item.nameAr else item.nameEn}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Text(
                text = String.format(Locale.US, "1$ = %.2f", item.rateFromUsd),
                style = MonoMetricSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            HorizontalDivider(thickness = 0.5.dp)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancelBtn) }
    },
  )
}

@Composable
private fun OpponentPickerDialog(
  phones: List<PhoneSpec>,
  selectedPhone: PhoneSpec,
  currency: CurrencyInfo,
  strings: AppStrings,
  onSelect: (PhoneSpec) -> Unit,
  onDismiss: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  val q = query.trim().lowercase()
  val filtered =
    remember(phones, q) {
      phones.filter { p ->
        q.isEmpty() ||
          p.model.lowercase().contains(q) ||
          p.brand.lowercase().contains(q) ||
          p.chipset.lowercase().contains(q)
      }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.pickOpponentLabel, style = MaterialTheme.typography.titleMedium) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(strings.searchPhonesPlaceholder) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
          items(filtered, key = { it.id }) { phone ->
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .clickable { onSelect(phone) }
                  .padding(horizontal = 8.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              PhoneThumbnail(phone = phone, width = 38.dp, height = 48.dp)
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = phone.model,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (phone.id == selectedPhone.id) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                  text = "${phone.chipset} • ${phone.ramGb} GB",
                  style = MonoMetricSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = FormatUtils.formatCurrencyAmount(phone.priceUsd, currency),
                style = MonoMetricSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            HorizontalDivider(thickness = 0.5.dp)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancelBtn) }
    },
  )
}

@Composable
private fun CustomPhoneDialog(
  strings: AppStrings,
  onSave: (String, String, Int, Int, Int, Double) -> Unit,
  onDismiss: () -> Unit,
) {
  var model by remember { mutableStateOf("") }
  var chipset by remember { mutableStateOf("") }
  var ramText by remember { mutableStateOf("8") }
  var batteryText by remember { mutableStateOf("5000") }
  var hzText by remember { mutableStateOf("120") }
  var priceText by remember { mutableStateOf("699") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.customPhoneDialogTitle, style = MaterialTheme.typography.titleMedium) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text(strings.customModelLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = chipset,
          onValueChange = { chipset = it },
          label = { Text(strings.customChipsetLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = ramText,
            onValueChange = { ramText = it.filter(Char::isDigit) },
            label = { Text(strings.customRamLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = hzText,
            onValueChange = { hzText = it.filter(Char::isDigit) },
            label = { Text(strings.customHzLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = batteryText,
            onValueChange = { batteryText = it.filter(Char::isDigit) },
            label = { Text(strings.customBatteryLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text(strings.customPriceLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            model,
            chipset,
            ramText.toIntOrNull() ?: 8,
            batteryText.toIntOrNull() ?: 5000,
            hzText.toIntOrNull() ?: 120,
            priceText.toDoubleOrNull() ?: 699.0,
          )
        }
      ) {
        Text(strings.saveAndCompareBtn)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancelBtn) }
    },
  )
}
