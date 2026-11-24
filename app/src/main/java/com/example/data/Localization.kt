package com.example.data

import android.app.ActivityManager
import android.os.BatteryManager
import android.os.PowerManager

class AppStrings(val lang: AppLanguage) {
  private val isAr: Boolean get() = lang == AppLanguage.AR

  val appName: String get() = "Cominitech"
  val subtitle: String get() = if (isAr) "Task Manager • CPU-Z" else "Task Manager • CPU-Z"

  val tabPerformance: String get() = if (isAr) "الأداء" else "Perf"
  val tabProcesses: String get() = if (isAr) "العمليات" else "Tasks"
  val tabHardware: String get() = if (isAr) "العتاد" else "CPU-Z"
  val tabPhones: String get() = if (isAr) "الهواتف" else "Phones"
  val tabSensors: String get() = if (isAr) "المستشعرات" else "Sensors"

  val liveLabel: String get() = if (isAr) "مباشر" else "LIVE"
  val pausedLabel: String get() = if (isAr) "متوقف مؤقتاً" else "PAUSED"

  // Performance / RAM
  val ramHeroTitle: String get() = if (isAr) "استهلاك الذاكرة العشوائية (RAM)" else "Memory (RAM) Usage"
  val ramConsumed: String get() = if (isAr) "المستهلك" else "Used"
  val ramAvailable: String get() = if (isAr) "المتاح" else "Available"
  val ramTotal: String get() = if (isAr) "الإجمالي" else "Total"
  val ramActive: String get() = if (isAr) "نشط" else "Active"
  val ramCached: String get() = if (isAr) "ذاكرة مؤقتة" else "Cached"
  val ramBuffers: String get() = if (isAr) "مخازن مؤقتة" else "Buffers"
  val ramSwapZram: String get() = if (isAr) "ZRAM / تبادل" else "ZRAM / Swap"
  val ramThreshold: String get() = if (isAr) "حد التنبيه" else "Alert Limit"
  val ramLowWarning: String get() = if (isAr) "تجاوز حد الذاكرة (70%)" else "RAM Threshold Exceeded (70%)"
  val ramHealthy: String get() = if (isAr) "حالة الذاكرة ضمن الحد الآمن (<70%)" else "RAM Within Safe Limit (<70%)"
  val ramHistoryTitle: String get() = if (isAr) "منحنى استهلاك الذاكرة الحي" else "Live RAM Utilization"
  val optimizeRamButton: String get() = if (isAr) "تحرير الذاكرة" else "Optimize RAM"
  val appSelfMemory: String get() = if (isAr) "ذاكرة التطبيق الحالي (PSS)" else "Current App Footprint (PSS)"
  val jvmHeap: String get() = if (isAr) "كومة JVM" else "JVM Heap"

  // Thresholds & Thermal Alerts
  val alertsCardTitle: String get() = if (isAr) "نظام التنبيهات والحدود الذكية" else "Smart Thresholds & Thermal Alerts"
  val ramLimitBadge: String get() = if (isAr) "حد الرام: 70%" else "RAM Limit: 70%"
  val storageLimitBadge: String get() = if (isAr) "حد الذاكرة: 90%" else "Storage Limit: 90%"
  val thermalLimitBadge: String get() = if (isAr) "تنبيه الحرارة نشط" else "Thermal Guard Active"
  val alertHighThermalTitle: String get() = if (isAr) "تحذير: ارتفاع حرارة الجهاز!" else "Warning: High Device Temperature!"
  val alertCpuHotLabel: String get() = if (isAr) "حرارة المعالج (CPU) مرتفعة" else "High CPU Temperature"
  val alertPhoneHotLabel: String get() = if (isAr) "حرارة الهاتف / البطارية مرتفعة" else "High Phone / Battery Temperature"
  val alertRamHighLabel: String get() = if (isAr) "استهلاك الرام تجاوز 70%" else "RAM Usage Exceeded 70%"
  val alertStorageHighLabel: String get() = if (isAr) "استهلاك الذاكرة الداخلية تجاوز 90%" else "Storage Usage Exceeded 90%"
  val alertAllNormal: String
    get() =
      if (isAr)
        "جميع القيم آمنة: الرام < 70% • الذاكرة < 90% • الحرارة مستقرة"
      else
        "All vitals normal: RAM < 70% • Storage < 90% • Thermals nominal"
  val alertDialogConfirm: String get() = if (isAr) "فهمت" else "Got it"
  val testAlertAction: String get() = if (isAr) "فحص التنبيه" else "Test Alert"
  val cpuTempLabel: String get() = if (isAr) "حرارة CPU" else "CPU Temp"
  val phoneTempLabel: String get() = if (isAr) "حرارة الهاتف" else "Phone Temp"

  // CPU
  val cpuHeroTitle: String get() = if (isAr) "المعالج المركزي (CPU)" else "Processor (CPU)"
  val cpuLoadLabel: String get() = if (isAr) "الحمل الحي" else "CPU Load"
  val cpuClockScaleLabel: String get() = if (isAr) "تردد الأنوية النشط" else "Active Clock Scale"
  val cpuAvgClock: String get() = if (isAr) "متوسط التردد" else "Avg Clock"
  val cpuMaxClock: String get() = if (isAr) "الحد الأقصى" else "Max Clock"
  val cpuActiveCores: String get() = if (isAr) "الأنوية النشطة" else "Active Cores"
  val cpuThreads: String get() = if (isAr) "الخيوط" else "Threads"
  val cpuCoresBreakdown: String get() = if (isAr) "تردد كل نواة (MHz)" else "Per-Core Frequencies (MHz)"
  val coreLabel: String get() = if (isAr) "نواة" else "Core"
  val offlineLabel: String get() = if (isAr) "خاملة" else "Offline"

  // Storage, Battery (iPhone-style Health), Network
  val storageTitle: String get() = if (isAr) "الذاكرة الداخلية (التخزين)" else "Internal Storage"
  val storageAlertBadge: String get() = if (isAr) "تجاوز 90%" else "Over 90%"
  val batteryTitle: String get() = if (isAr) "البطارية وصحتها" else "Battery & Health"
  val batteryHealthSectionTitle: String get() = if (isAr) "صحة البطارية (مثل الآيفون)" else "Battery Health (iPhone Style)"
  val batteryMaxCapacityLabel: String get() = if (isAr) "السعة القصوى (Health)" else "Maximum Capacity"
  val batteryConditionLabel: String get() = if (isAr) "حالة البطارية" else "Condition"
  val batteryDesignCapLabel: String get() = if (isAr) "السعة التصميمية" else "Design Capacity"
  val batteryCurrentChargeLabel: String get() = if (isAr) "الشحنة الحالية" else "Current Charge"
  val batteryEstFullLabel: String get() = if (isAr) "السعة الفعلية المقدرة" else "Est. Full Cap"
  val batteryCyclesLabel: String get() = if (isAr) "دورات الشحن" else "Cycle Count"
  val batteryPowerLabel: String get() = if (isAr) "القدرة" else "Power"
  val batteryTechLabel: String get() = if (isAr) "التقنية" else "Chemistry"
  val batterySourceLabel: String get() = if (isAr) "المصدر" else "Source"
  val batteryThermalStateLabel: String get() = if (isAr) "الحالة الحرارية" else "Thermal State"
  val batteryHealthNote: String
    get() =
      if (isAr)
        "يتم حساب السعة القصوى من قراءات وحدة التحكم والشحن الفعلية مقارنة بالسعة التصميمية للبطارية."
      else
        "Maximum capacity is computed from kernel fuel-gauge counters compared against factory design capacity."

  val networkTitle: String get() = if (isAr) "حركة الشبكة الحية" else "Live Network I/O"
  val downloadSpeed: String get() = if (isAr) "تنزيل" else "RX"
  val uploadSpeed: String get() = if (isAr) "رفع" else "TX"
  val totalTraffic: String get() = if (isAr) "الإجمالي منذ التشغيل" else "Session Total"
  val uptimeLabel: String get() = if (isAr) "مدة التشغيل" else "Uptime"
  val tempLabel: String get() = if (isAr) "درجة الحرارة" else "Temperature"
  val voltageLabel: String get() = if (isAr) "الجهد" else "Voltage"
  val currentLabel: String get() = if (isAr) "التيار" else "Current"
  val chargingLabel: String get() = if (isAr) "قيد الشحن" else "Charging"
  val dischargingLabel: String get() = if (isAr) "تفريغ" else "Discharging"

  // Processes / Task Manager
  val searchAppsPlaceholder: String get() = if (isAr) "ابحث عن تطبيق أو حزمة..." else "Search app or package..."
  val filterAll: String get() = if (isAr) "الكل" else "All"
  val filterRunning: String get() = if (isAr) "نشط الآن" else "Running"
  val filterUser: String get() = if (isAr) "المستخدم" else "User Apps"
  val filterSystem: String get() = if (isAr) "النظام" else "System"
  val runningProcessesSection: String get() = if (isAr) "العمليات الحية المرصودة" else "Visible Running Processes"
  val appsInventorySection: String get() = if (isAr) "تطبيقات الجهاز والموارد" else "Device Apps & Resources"
  val jvmThreadsSection: String get() = if (isAr) "خيوط التنفيذ الحية (Threads)" else "Active Process Threads"
  val endBackgroundAction: String get() = if (isAr) "إنهاء بالخلفية" else "Trim BG"
  val appDetailsAction: String get() = if (isAr) "إدارة" else "App Info"
  val pidLabel: String get() = "PID"
  val apkSizeLabel: String get() = if (isAr) "الحجم" else "APK"
  val noMatchingApps: String get() = if (isAr) "لا توجد تطبيقات مطابقة للبحث" else "No matching applications found"
  val androidSandboxNote: String
    get() =
      if (isAr)
        "يعرض أندرويد استهلاك RAM التفصيلي للعمليات المسموح بالوصول إليها نظامياً، ويمكنك فتح إعدادات أي تطبيق لإيقافه إجبارياً."
      else
        "Android sandboxing exposes live PSS memory for visible processes; open App Info to force-stop or inspect any app."

  // Hardware CPU-Z sections
  val sectionSocCpu: String get() = if (isAr) "المعالج والرقاقة (SoC & CPU)" else "SoC & CPU"
  val sectionGpu: String get() = if (isAr) "معالج الرسوميات (GPU)" else "Graphics (GPU)"
  val sectionDevice: String get() = if (isAr) "الجهاز واللوحة (Device)" else "Device & Board"
  val sectionDisplay: String get() = if (isAr) "الشاشة والعرض (Display)" else "Display"
  val sectionSystem: String get() = if (isAr) "النظام والنواة (System)" else "System & Kernel"
  val copiedToClipboard: String get() = if (isAr) "تم نسخ المواصفات" else "Copied to clipboard"
  val copySpecsLabel: String get() = if (isAr) "نسخ" else "Copy"

  // Spec Keys
  val keySocVendor: String get() = if (isAr) "الشركة المصنعة للرقاقة" else "SoC Manufacturer"
  val keySocModel: String get() = if (isAr) "طراز الرقاقة" else "SoC Model"
  val keyProcessor: String get() = if (isAr) "المعالج" else "Processor"
  val keyHardware: String get() = if (isAr) "العتاد" else "Hardware"
  val keyArch: String get() = if (isAr) "المعمارية" else "Architecture"
  val keyAbis: String get() = if (isAr) "مجموعات التعليمات (ABIs)" else "Supported ABIs"
  val keyCores: String get() = if (isAr) "عدد الأنوية" else "Cores"
  val keyClockRange: String get() = if (isAr) "نطاق التردد" else "Clock Range"
  val keyGovernor: String get() = if (isAr) "متحكم التردد" else "Governor"
  val keyBogomips: String get() = "BogoMIPS"
  val keyFeatures: String get() = if (isAr) "الخصائص" else "Instruction Features"

  val keyGpuVendor: String get() = if (isAr) "المصنّع" else "Vendor"
  val keyGpuRenderer: String get() = if (isAr) "المسرّع الرسومي" else "Renderer"
  val keyGlVersion: String get() = if (isAr) "إصدار OpenGL" else "OpenGL Version"
  val keyGlesApi: String get() = if (isAr) "دعم GLES" else "GLES API"

  val keyManufacturer: String get() = if (isAr) "الشركة" else "Manufacturer"
  val keyBrand: String get() = if (isAr) "العلامة التجارية" else "Brand"
  val keyModel: String get() = if (isAr) "الطراز" else "Model"
  val keyDevice: String get() = if (isAr) "اسم الجهاز" else "Device"
  val keyBoard: String get() = if (isAr) "اللوحة الأم" else "Board"
  val keyProduct: String get() = if (isAr) "المنتج" else "Product"
  val keyBootloader: String get() = if (isAr) "محمل الإقلاع" else "Bootloader"
  val keyRadio: String get() = if (isAr) "المودم" else "Radio / Baseband"

  val keyResolution: String get() = if (isAr) "الدقة" else "Resolution"
  val keyDensity: String get() = if (isAr) "الكثافة" else "Density"
  val keyRefreshRate: String get() = if (isAr) "معدل التحديث" else "Refresh Rate"
  val keyScreenSize: String get() = if (isAr) "القطر التقديري" else "Approx. Size"
  val keyHdr: String get() = if (isAr) "دعم HDR" else "HDR Support"

  val keyAndroidVer: String get() = if (isAr) "إصدار أندرويد" else "Android Version"
  val keyApiLevel: String get() = if (isAr) "مستوى API" else "API Level"
  val keySecPatch: String get() = if (isAr) "التحديث الأمني" else "Security Patch"
  val keyBuildId: String get() = if (isAr) "رقم البناء" else "Build ID"
  val keyKernel: String get() = if (isAr) "إصدار النواة" else "Kernel"
  val keyJavaVm: String get() = if (isAr) "آلة جافا" else "Java VM"
  val keyBuildTags: String get() = if (isAr) "وسوم البناء" else "Build Tags"

  // Sensors & Thermals
  val thermalSectionTitle: String get() = if (isAr) "مناطق الحرارة الحية" else "Live Thermal Zones"
  val sensorsSectionTitle: String get() = if (isAr) "المستشعرات الفيزيائية" else "Hardware Sensors"
  val noThermalZones: String
    get() =
      if (isAr)
        "قراءات حساسات النواة الحرارية مقيدة على هذا النظام؛ حرارة البطارية متاحة أعلاه."
      else
        "Kernel thermal sysfs nodes are restricted on this device; battery thermal telemetry is active above."
  val sensorVendorLabel: String get() = if (isAr) "المصنّع" else "Vendor"
  val sensorPowerLabel: String get() = if (isAr) "الطاقة" else "Power"
  val sensorRangeLabel: String get() = if (isAr) "المدى" else "Max Range"
  val searchSensorsPlaceholder: String get() = if (isAr) "ابحث في المستشعرات..." else "Search sensors..."

  fun importanceLabel(code: Int): String =
    when (code) {
      ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ->
        if (isAr) "واجهة" else "Foreground"
      ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE ->
        if (isAr) "خدمة نشطة" else "FG Service"
      ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE ->
        if (isAr) "مرئي" else "Visible"
      ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE ->
        if (isAr) "خدمة" else "Service"
      ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED ->
        if (isAr) "مخزّن مؤقتاً" else "Cached"
      else -> if (isAr) "خلفية" else "Background"
    }

  fun batteryHealthLabel(code: Int): String =
    when (code) {
      BatteryManager.BATTERY_HEALTH_GOOD -> if (isAr) "ممتازة (Good)" else "Good"
      BatteryManager.BATTERY_HEALTH_OVERHEAT -> if (isAr) "حرارة مرتفعة" else "Overheat"
      BatteryManager.BATTERY_HEALTH_DEAD -> if (isAr) "تالفة" else "Dead"
      BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> if (isAr) "جهد زائد" else "Over Voltage"
      BatteryManager.BATTERY_HEALTH_COLD -> if (isAr) "باردة" else "Cold"
      else -> if (isAr) "سليمة" else "Normal"
    }

  fun thermalStatusLabel(code: Int): String =
    when (code) {
      PowerManager.THERMAL_STATUS_NONE -> if (isAr) "طبيعية" else "Normal"
      PowerManager.THERMAL_STATUS_LIGHT -> if (isAr) "دافئة قليلاً" else "Light"
      PowerManager.THERMAL_STATUS_MODERATE -> if (isAr) "مرتفعة" else "Moderate"
      PowerManager.THERMAL_STATUS_SEVERE -> if (isAr) "مرتفعة جداً" else "Severe"
      PowerManager.THERMAL_STATUS_CRITICAL,
      PowerManager.THERMAL_STATUS_EMERGENCY,
      PowerManager.THERMAL_STATUS_SHUTDOWN -> if (isAr) "حرجة!" else "Critical!"
      else -> if (isAr) "طبيعية" else "Normal"
    }

  fun alertHeadline(kind: AlertKind): String =
    when (kind) {
      AlertKind.THERMAL_CPU -> alertCpuHotLabel
      AlertKind.THERMAL_PHONE -> alertPhoneHotLabel
      AlertKind.RAM_LIMIT -> alertRamHighLabel
      AlertKind.STORAGE_LIMIT -> alertStorageHighLabel
    }

  fun thermalDialogMessage(kind: AlertKind, value: String, limit: String): String =
    if (isAr) {
      when (kind) {
        AlertKind.THERMAL_CPU ->
          "تم رصد ارتفاع في درجة حرارة المعالج ($value) متجاوزة الحد الآمن ($limit). يُنصح بتقليل التطبيقات الثقيلة في الخلفية."
        AlertKind.THERMAL_PHONE ->
          "تم رصد ارتفاع في درجة حرارة الهاتف والبطارية ($value) متجاوزة الحد الآمن ($limit). يُنصح بإيقاف الشحن السريع أو إراحة الجهاز."
        AlertKind.RAM_LIMIT ->
          "استهلاك الرام وصل إلى $value (الحد التنبيهي 70%). يمكنك استخدام زر تحرير الذاكرة."
        AlertKind.STORAGE_LIMIT ->
          "امتلاء الذاكرة الداخلية وصل إلى $value (الحد التنبيهي 90%). يُنصح بتفريغ مساحة."
      }
    } else {
      when (kind) {
        AlertKind.THERMAL_CPU ->
          "CPU temperature reached $value, exceeding the $limit threshold. Consider closing heavy background tasks."
        AlertKind.THERMAL_PHONE ->
          "Phone/battery temperature reached $value, exceeding the $limit threshold. Pause fast charging or let the device cool."
        AlertKind.RAM_LIMIT ->
          "RAM usage reached $value (above 70% limit). Use Optimize RAM to trim background caches."
        AlertKind.STORAGE_LIMIT ->
          "Internal storage reached $value (above 90% limit). Free up space to keep system performance smooth."
      }
    }

  fun ramFreedMessage(freedMb: Float, trimmedCount: Int): String =
    if (isAr) {
      if (freedMb > 0.2f) {
        "تم تحرير %.1f MB وإرسال إشارة تنظيف لـ %d عملية بالخلفية".format(freedMb, trimmedCount)
      } else {
        "تم تنفيذ تحسين الذاكرة وإرسال إشارة تنظيف لـ %d عملية".format(trimmedCount)
      }
    } else {
      if (freedMb > 0.2f) {
        "Reclaimed %.1f MB and trimmed %d background packages".format(freedMb, trimmedCount)
      } else {
        "Memory trim completed across %d background packages".format(trimmedCount)
      }
    }

  fun bgTrimmedPackageMessage(appName: String): String =
    if (isAr) "تم إرسال أمر تقليص عمليات الخلفية لـ $appName"
    else "Requested background process trim for $appName"

  // Alert History Log
  val alertHistoryTitle: String get() = if (isAr) "سجل التنبيهات السابقة" else "Alert History Log"
  val alertHistoryClear: String get() = if (isAr) "مسح السجل" else "Clear Log"
  val alertHistoryEmpty: String
    get() =
      if (isAr)
        "لا توجد تنبيهات مسجلة حتى الآن. سيتم تسجيل أي ارتفاع في الحرارة أو استهلاك الذاكرة هنا مع الطابع الزمني."
      else
        "No past alerts logged yet. Any thermal spike or memory threshold breach will appear here with a timestamp."
  val alertHistoryCountSuffix: String get() = if (isAr) "تنبيه" else "logged"

  // Phones Catalog & World Currencies
  val phonesCatalogTab: String get() = if (isAr) "دليل الهواتف والأسعار" else "Phones & Prices"
  val phonesVsTab: String get() = if (isAr) "اختبار ضد هاتف" else "VS Benchmark"
  val searchPhonesPlaceholder: String get() = if (isAr) "ابحث عن أي هاتف أو معالج..." else "Search any phone or chipset..."
  val selectCurrencyTitle: String get() = if (isAr) "اختر عملة العرض" else "Select Currency"
  val searchCurrencyPlaceholder: String get() = if (isAr) "ابحث عن عملة (مثل MAD, SAR, EUR)..." else "Search currency (e.g. EUR, SAR, MAD)..."
  val liveRatesLabel: String get() = if (isAr) "أسعار صرف حية" else "Live FX"
  val offlineRatesLabel: String get() = if (isAr) "أسعار محفوظة" else "Saved FX"
  val priceUsdLabel: String get() = if (isAr) "السعر بالدولار" else "USD Price"
  val priceLocalLabel: String get() = if (isAr) "بالعملة المختارة" else "Converted"
  val compareAgainstThisBtn: String get() = if (isAr) "اختبار ضد هاتفي" else "Compare vs Mine"
  val addCustomPhoneBtn: String get() = if (isAr) "إضافة هاتف مخصص" else "Custom Phone"
  val customPhoneDialogTitle: String get() = if (isAr) "إدخال مواصفات أي هاتف للمقارنة" else "Enter Any Phone to Compare"
  val customModelLabel: String get() = if (isAr) "اسم الهاتف (مثال: iPhone 13 Pro)" else "Phone Model (e.g. iPhone 13 Pro)"
  val customChipsetLabel: String get() = if (isAr) "المعالج" else "Chipset"
  val customRamLabel: String get() = if (isAr) "الرام (GB)" else "RAM (GB)"
  val customBatteryLabel: String get() = if (isAr) "البطارية (mAh)" else "Battery (mAh)"
  val customHzLabel: String get() = if (isAr) "تردد الشاشة (Hz)" else "Refresh Rate (Hz)"
  val customPriceLabel: String get() = if (isAr) "السعر ($ USD)" else "Price ($ USD)"
  val saveAndCompareBtn: String get() = if (isAr) "حفظ ومقارنة" else "Save & Compare"
  val cancelBtn: String get() = if (isAr) "إلغاء" else "Cancel"

  // Phone vs Phone Benchmark
  val yourPhoneLabel: String get() = if (isAr) "هاتفك الحالي" else "Your Phone"
  val opponentPhoneLabel: String get() = if (isAr) "الهاتف المنافس" else "Target Phone"
  val pickOpponentLabel: String get() = if (isAr) "اختر الهاتف المنافس" else "Pick Target Phone"
  val runLiveBenchmarkBtn: String get() = if (isAr) "بدء اختبار الأداء الفعلي" else "Run Live Benchmark"
  val runningBenchmarkLabel: String get() = if (isAr) "جارٍ اختبار المعالج والرام والتخزين..." else "Benchmarking CPU, RAM & Storage..."
  val singleCoreLabel: String get() = if (isAr) "نواة واحدة" else "Single-Core"
  val multiCoreLabel: String get() = if (isAr) "تعدد الأنوية" else "Multi-Core"
  val ramBandwidthLabel: String get() = if (isAr) "سرعة الرام" else "RAM Bandwidth"
  val storageIoLabel: String get() = if (isAr) "سرعة التخزين" else "Storage I/O"
  val overallScoreLabel: String get() = if (isAr) "النتيجة الكلية" else "Overall Score"
  val winnerYourPhone: String get() = if (isAr) "هاتفك يتفوق في هذا الاختبار!" else "Your Phone Wins!"
  val winnerOpponent: String get() = if (isAr) "الهاتف المنافس يتفوق في الأداء" else "Target Phone Leads"
  val benchmarkPromptHint: String
    get() =
      if (isAr)
        "اضغط على بدء اختبار الأداء الفعلي لقياس سرعة معالجك ورامك وتخزينك الحقيقية ومقارنتها مباشرة."
      else
        "Tap Run Live Benchmark to measure your real CPU, RAM, and storage performance against the selected phone."
  val specChipset: String get() = if (isAr) "المعالج" else "Chipset"
  val specDisplay: String get() = if (isAr) "الشاشة" else "Display"
  val specCamera: String get() = if (isAr) "الكاميرا" else "Camera"
  val specBattery: String get() = if (isAr) "البطارية" else "Battery"
  val specStorage: String get() = if (isAr) "التخزين" else "Storage"
  val specRam: String get() = if (isAr) "الرام" else "RAM"
  val specCharging: String get() = if (isAr) "الشحن" else "Charging"
  val specProtection: String get() = if (isAr) "الحماية" else "Build / IP"
  val specOs: String get() = if (isAr) "النظام" else "OS"
  val compareWithMineBtn: String get() = if (isAr) "Compare with mine • قارن مع هاتفي" else "Compare with mine"
  val viewAllDescriptionBtn: String get() = if (isAr) "View all description • عرض كل التفاصيل" else "View all description"
  val prosTitle: String get() = if (isAr) "المميزات (الجيد)" else "Pros (The Good)"
  val consTitle: String get() = if (isAr) "العيوب (السيء)" else "Cons (The Bad)"
  val fullSpecsTitle: String get() = if (isAr) "المواصفات الكاملة" else "Full Specifications"
  val phoneOverviewTitle: String get() = if (isAr) "نظرة عامة" else "Overview"
  val phonesCountLabel: String get() = if (isAr) "هاتف" else "phones"
}
