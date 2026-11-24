package com.example

import com.example.data.AppLanguage
import com.example.data.AppStrings
import com.example.data.CurrencyInfo
import com.example.data.FormatUtils
import com.example.data.PhonesCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun formatUtils_formatsBytesKbSpeedAndUptime() {
    assertEquals("0 B", FormatUtils.formatBytes(0L))
    assertEquals("1.00 GB", FormatUtils.formatBytes(1024L * 1024L * 1024L))
    assertEquals("2.0 MB", FormatUtils.formatKb(2048))
    assertEquals("1.00 MB/s", FormatUtils.formatSpeed(1024L * 1024L))
    assertEquals("01h 01m 01s", FormatUtils.formatUptime(3661_000L))
  }

  @Test
  fun formatUtils_formatsCurrencyAndTimestamp() {
    val mad = CurrencyInfo("MAD", "درهم مغربي", "Moroccan Dirham", "MAD", 10.0)
    assertEquals("10,000 MAD", FormatUtils.formatCurrencyAmount(1000.0, mad))
    val ts = FormatUtils.formatTimestamp(1_700_000_000_000L)
    assertTrue(ts.contains("•"))
  }

  @Test
  fun phonesCatalog_containsMultipleBrandsAndValidPrices() {
    assertTrue(PhonesCatalog.allPhones.size >= 50)
    assertEquals(PhonesCatalog.allPhones.size, PhonesCatalog.allPhones.map { it.id }.distinct().size)
    assertTrue(PhonesCatalog.brands.contains("Apple"))
    assertTrue(PhonesCatalog.brands.contains("Samsung"))
    assertTrue(PhonesCatalog.allPhones.all { it.priceUsd > 0.0 })
    assertTrue(
      PhonesCatalog.allPhones.all {
        it.prosAr.isNotEmpty() &&
          it.prosEn.isNotEmpty() &&
          it.consAr.isNotEmpty() &&
          it.consEn.isNotEmpty() &&
          it.fullDescriptionAr.isNotBlank() &&
          it.fullDescriptionEn.isNotBlank()
      }
    )
  }

  @Test
  fun localization_providesArabicAndEnglishLabels() {
    val ar = AppStrings(AppLanguage.AR)
    val en = AppStrings(AppLanguage.EN)
    assertEquals("Cominitech", ar.appName)
    assertEquals("Cominitech", en.appName)
    assertTrue(ar.ramHeroTitle.contains("RAM"))
    assertTrue(en.ramHeroTitle.contains("RAM"))
    assertTrue(ar.compareWithMineBtn.contains("Compare with mine"))
    assertTrue(ar.viewAllDescriptionBtn.contains("View all description"))
  }
}
