package com.gameora.app.data.repository

import com.gameora.app.data.model.DisplayPrice
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * مستودع العملات.
 *
 * أسعار الصرف تُخزَّن في مجموعة exchangeRates بواسطة الـ Backend فقط (مثلاً عبر
 * Cloud Function مجدولة تسحب أسعار محدّثة من مصدر خارجي كل ساعة)، والتطبيق يقرأها فقط.
 * هذا التحويل هنا "للعرض" في الشاشات فقط — التحويل المعتمد فعليًا عند الشراء
 * يُعاد حسابه ويُثبَّت داخل Cloud Function createOrder على السيرفر.
 */
class CurrencyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // كاش بسيط في الذاكرة لأسعار الصرف بالنسبة للدولار كعملة أساس، مثال: {"EGP": 48.5, "SAR": 3.75}
    private var ratesCache: Map<String, Double>? = null

    private suspend fun loadRates(): Map<String, Double> {
        ratesCache?.let { return it }
        val snapshot = firestore.collection("exchangeRates").document("latest").get().await()
        @Suppress("UNCHECKED_CAST")
        val rates = (snapshot.get("rates") as? Map<String, Double>) ?: emptyMap()
        ratesCache = rates
        return rates
    }

    /**
     * يحوّل مبلغًا من عملة البائع الأصلية إلى عملة عرض المشتري.
     * لا يُغيّر originalPrice في قاعدة البيانات أبدًا — هذه القيمة للعرض في الواجهة فقط.
     */
    suspend fun convertForDisplay(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<DisplayPrice> = runCatching {
        if (fromCurrency == toCurrency) return@runCatching DisplayPrice(amount, toCurrency)

        val rates = loadRates()
        val fromRate = rates[fromCurrency] ?: error("سعر صرف $fromCurrency غير متوفر")
        val toRate = rates[toCurrency] ?: error("سعر صرف $toCurrency غير متوفر")

        // التحويل عبر عملة أساس مشتركة (USD): amount / fromRate * toRate
        val amountInUsd = amount / fromRate
        val converted = amountInUsd * toRate

        DisplayPrice(amount = converted, currencyCode = toCurrency)
    }
}
