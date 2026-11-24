package com.example.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Currency
import java.util.Locale
import java.util.concurrent.TimeUnit

class CurrencyRepository(context: Context) {

  private val prefs = context.getSharedPreferences("cominitech_fx", Context.MODE_PRIVATE)
  private val client =
    OkHttpClient.Builder()
      .connectTimeout(6, TimeUnit.SECONDS)
      .readTimeout(6, TimeUnit.SECONDS)
      .build()

  private val popularPriority =
    listOf("USD", "EUR", "MAD", "SAR", "AED", "EGP", "KWD", "QAR", "GBP", "TRY", "CAD", "JPY", "CNY", "INR")

  fun getCachedOrDefaultCurrencies(): Pair<List<CurrencyInfo>, Boolean> {
    val cachedJson = prefs.getString("rates_json", null)
    val parsed = cachedJson?.let { parseRatesJson(it) }
    return if (!parsed.isNullOrEmpty()) {
      parsed to true
    } else {
      buildFallbackCurrencies() to false
    }
  }

  fun fetchLiveWorldCurrencies(): Pair<List<CurrencyInfo>, Boolean> {
    val request = Request.Builder().url("https://open.er-api.com/v6/latest/USD").build()
    val bodyStr =
      runCatching {
        client.newCall(request).execute().use { resp ->
          if (resp.isSuccessful) resp.body?.string() else null
        }
      }.getOrNull()

    if (!bodyStr.isNullOrBlank()) {
      val parsed = parseRatesJson(bodyStr)
      if (parsed.isNotEmpty()) {
        prefs.edit().putString("rates_json", bodyStr).apply()
        return parsed to true
      }
    }
    return getCachedOrDefaultCurrencies()
  }

  private fun parseRatesJson(rawJson: String): List<CurrencyInfo> {
    return runCatching {
      val root = JSONObject(rawJson)
      val ratesObj = root.optJSONObject("rates") ?: return emptyList()
      val list = mutableListOf<CurrencyInfo>()
      val keys = ratesObj.keys()
      val arLocale = Locale("ar")
      val enLocale = Locale.ENGLISH

      while (keys.hasNext()) {
        val code = keys.next().uppercase(Locale.US)
        val rate = ratesObj.optDouble(code, -1.0)
        if (rate <= 0.0) continue

        val javaCur = runCatching { Currency.getInstance(code) }.getOrNull()
        val nameAr = javaCur?.getDisplayName(arLocale) ?: code
        val nameEn = javaCur?.getDisplayName(enLocale) ?: code
        val symbol = javaCur?.getSymbol(Locale.US)?.takeIf { it.isNotBlank() } ?: code

        list.add(
          CurrencyInfo(
            code = code,
            nameAr = nameAr,
            nameEn = nameEn,
            symbol = symbol,
            rateFromUsd = rate,
          )
        )
      }
      sortCurrencies(list)
    }.getOrDefault(emptyList())
  }

  private fun buildFallbackCurrencies(): List<CurrencyInfo> {
    val fallbackRates =
      mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "MAD" to 9.85,
        "SAR" to 3.75,
        "AED" to 3.67,
        "EGP" to 49.5,
        "KWD" to 0.31,
        "QAR" to 3.64,
        "BHD" to 0.376,
        "OMR" to 0.385,
        "JOD" to 0.709,
        "DZD" to 133.5,
        "TND" to 3.12,
        "IQD" to 1310.0,
        "LBP" to 89500.0,
        "GBP" to 0.79,
        "CAD" to 1.39,
        "AUD" to 1.53,
        "CHF" to 0.88,
        "JPY" to 154.0,
        "CNY" to 7.23,
        "INR" to 84.4,
        "TRY" to 34.5,
        "KRW" to 1395.0,
        "BRL" to 5.78,
        "MXN" to 20.3,
        "ZAR" to 18.1,
        "NGN" to 1660.0,
        "RUB" to 98.0,
        "SEK" to 10.9,
        "NOK" to 11.0,
        "PLN" to 4.08,
        "MYR" to 4.46,
        "IDR" to 15850.0,
        "SGD" to 1.34,
        "PKR" to 278.0,
      )
    val arLocale = Locale("ar")
    val enLocale = Locale.ENGLISH
    val list =
      Currency.getAvailableCurrencies().mapNotNull { cur ->
        val code = cur.currencyCode
        val rate = fallbackRates[code] ?: return@mapNotNull null
        CurrencyInfo(
          code = code,
          nameAr = cur.getDisplayName(arLocale),
          nameEn = cur.getDisplayName(enLocale),
          symbol = cur.getSymbol(Locale.US).ifBlank { code },
          rateFromUsd = rate,
        )
      }
    return sortCurrencies(list)
  }

  private fun sortCurrencies(list: List<CurrencyInfo>): List<CurrencyInfo> {
    return list.sortedWith(
      compareBy<CurrencyInfo> { item ->
        val idx = popularPriority.indexOf(item.code)
        if (idx >= 0) idx else Int.MAX_VALUE
      }.thenBy { it.code }
    )
  }
}
