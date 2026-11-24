package com.example.data

import android.hardware.Sensor
import java.util.Locale

object FormatUtils {

  fun formatBytes(bytes: Long, decimals: Int = 2): String {
    if (bytes <= 0L) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
      bytes >= gb -> String.format(Locale.US, "%.${decimals}f GB", bytes / gb)
      bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
      bytes >= kb -> String.format(Locale.US, "%.0f KB", bytes / kb)
      else -> "$bytes B"
    }
  }

  fun formatKb(kb: Int): String {
    if (kb <= 0) return "0 KB"
    return if (kb >= 1024) {
      String.format(Locale.US, "%.1f MB", kb / 1024.0)
    } else {
      "$kb KB"
    }
  }

  fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0L) return "0 B/s"
    val kb = 1024.0
    val mb = kb * 1024.0
    return when {
      bytesPerSec >= mb -> String.format(Locale.US, "%.2f MB/s", bytesPerSec / mb)
      bytesPerSec >= kb -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / kb)
      else -> "$bytesPerSec B/s"
    }
  }

  fun formatUptime(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val days = totalSec / 86400L
    val hours = (totalSec % 86400L) / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (days > 0) {
      String.format(Locale.US, "%dd %02dh %02dm", days, hours, mins)
    } else {
      String.format(Locale.US, "%02dh %02dm %02ds", hours, mins, secs)
    }
  }

  fun sensorTypeLabel(typeCode: Int, isAr: Boolean): String =
    when (typeCode) {
      Sensor.TYPE_ACCELEROMETER -> if (isAr) "مقياس التسارع" else "Accelerometer"
      Sensor.TYPE_MAGNETIC_FIELD -> if (isAr) "المجال المغناطيسي" else "Magnetometer"
      Sensor.TYPE_GYROSCOPE -> if (isAr) "الجيروسكوب" else "Gyroscope"
      Sensor.TYPE_LIGHT -> if (isAr) "مستشعر الإضاءة" else "Ambient Light"
      Sensor.TYPE_PRESSURE -> if (isAr) "الضغط الجوي" else "Barometer"
      Sensor.TYPE_PROXIMITY -> if (isAr) "مستشعر التقارب" else "Proximity"
      Sensor.TYPE_GRAVITY -> if (isAr) "الجاذبية" else "Gravity"
      Sensor.TYPE_LINEAR_ACCELERATION -> if (isAr) "التسارع الخطي" else "Linear Accel"
      Sensor.TYPE_ROTATION_VECTOR -> if (isAr) "متجه الدوران" else "Rotation Vector"
      Sensor.TYPE_STEP_COUNTER -> if (isAr) "عداد الخطوات" else "Step Counter"
      Sensor.TYPE_STEP_DETECTOR -> if (isAr) "كاشف الخطوات" else "Step Detector"
      Sensor.TYPE_GAME_ROTATION_VECTOR -> if (isAr) "دوران الألعاب" else "Game Rotation"
      Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> if (isAr) "الدوران المغناطيسي" else "Geomagnetic Rot."
      Sensor.TYPE_AMBIENT_TEMPERATURE -> if (isAr) "الحرارة المحيطة" else "Ambient Temp"
      Sensor.TYPE_RELATIVE_HUMIDITY -> if (isAr) "الرطوبة النسبية" else "Humidity"
      Sensor.TYPE_HEART_RATE -> if (isAr) "نبض القلب" else "Heart Rate"
      else -> if (isAr) "مستشعر #$typeCode" else "Sensor #$typeCode"
    }

  fun formatTimestamp(timestampMs: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM • HH:mm:ss", Locale.US)
    return sdf.format(java.util.Date(timestampMs))
  }

  fun formatCurrencyAmount(usdAmount: Double, currency: CurrencyInfo): String {
    val converted = usdAmount * currency.rateFromUsd
    val formatted =
      when {
        converted >= 10000.0 -> String.format(Locale.US, "%,.0f", converted)
        converted >= 100.0 -> String.format(Locale.US, "%,.0f", converted)
        else -> String.format(Locale.US, "%,.2f", converted)
      }
    return "$formatted ${currency.code}"
  }
}
