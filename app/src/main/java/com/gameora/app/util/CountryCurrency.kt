package com.gameora.app.util

/**
 * تحديد العملة الافتراضية بناءً على دولة المستخدم عند التسجيل.
 * هذه قائمة مبدئية لأهم الدول العربية والمجاورة — يمكن للـ Backend لاحقًا
 * أن يكون المصدر الرسمي والمحدَّث لهذا الجدول بدل تثبيته داخل التطبيق.
 */
object CountryCurrency {

    data class CountryOption(val code: String, val nameAr: String, val currencyCode: String)

    val supportedCountries = listOf(
        CountryOption("EG", "مصر", "EGP"),
        CountryOption("SA", "السعودية", "SAR"),
        CountryOption("AE", "الإمارات", "AED"),
        CountryOption("KW", "الكويت", "KWD"),
        CountryOption("QA", "قطر", "QAR"),
        CountryOption("BH", "البحرين", "BHD"),
        CountryOption("OM", "عُمان", "OMR"),
        CountryOption("JO", "الأردن", "JOD"),
        CountryOption("IQ", "العراق", "IQD"),
        CountryOption("MA", "المغرب", "MAD"),
        CountryOption("DZ", "الجزائر", "DZD"),
        CountryOption("TN", "تونس", "TND"),
        CountryOption("US", "الولايات المتحدة", "USD"),
        CountryOption("GB", "المملكة المتحدة", "GBP")
        // يُفضَّل استكمال باقي دول العالم من الـ Backend بدل تضخيم التطبيق بقائمة ثابتة ضخمة
    )

    fun currencyFor(countryCode: String): String =
        supportedCountries.firstOrNull { it.code == countryCode }?.currencyCode ?: "USD"
}
