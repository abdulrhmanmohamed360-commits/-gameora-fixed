package com.gameora.app.data.model

import com.google.firebase.firestore.Exclude

/**
 * موديل المنتج/الحساب المعروض للبيع.
 *
 * مهم:
 * بيانات الدخول الحساسة مثل:
 * - accountPassword
 * - accountEmailPassword
 *
 * لا يتم تخزينها داخل Product.
 *
 * سيتم حفظها في مكان منفصل ومحمي على الـBackend،
 * ولن يتم إرجاعها مع بيانات المتجر.
 */
data class Product(

    val id: String = "",

    // اللعبة
    val gameId: String = "",

    // البائع
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerRating: Double? = null,

    // بيانات الإعلان
    val title: String = "",
    val description: String = "",

    /** روابط صور الحساب على Firebase Storage (وليس Base64) */
    val imageUrls: List<String> = emptyList(),

    // السعر الأصلي الذي أدخله البائع
    val originalPrice: Double = 0.0,
    val originalCurrency: String = "",

    // نوع المنتج
    val category: ProductCategory = ProductCategory.ACCOUNT,

    // الكمية
    val stockAvailable: Int = 1,

    // حالة الإعلان
    val isActive: Boolean = true,

    // وقت الإنشاء
    val createdAt: Long = 0L,

    // ==========================================
    // مواصفات الحساب العامة
    // ==========================================

    /** اسم المستخدم أو ID الحساب - غير سري */
    val accountUsername: String = "",

    /** مستوى الحساب */
    val accountLevel: String = "",

    /** الرانك */
    val accountRank: String = "",

    /** الجواهر / العملات */
    val accountCoins: String = "",

    /** السيرفر / المنطقة */
    val accountServer: String = ""
) {
    /**
     * أول صورة للعرض في الأماكن اللي بتحتاج صورة واحدة بس (مثل ProductCard).
     * غير مخزّنة في Firestore — محسوبة من imageUrls.
     */
    @get:Exclude
    val imageUrl: String
        get() = imageUrls.firstOrNull() ?: ""
}

/**
 * بيانات الدخول الحساسة للحساب.
 *
 * ⚠️ لا نضع هذا الموديل داخل Product.
 *
 * سيتم استخدامه مع Backend / Cloud Functions
 * بعد نجاح عملية الشراء فقط.
 */
data class AccountSecrets(

    val productId: String = "",

    val accountUsername: String = "",

    val accountPassword: String = "",

    val accountEmail: String = "",

    val accountEmailPassword: String = ""
)

/**
 * سعر مُحوَّل للعرض فقط.
 *
 * لا يستخدم كمصدر حقيقي عند إتمام الشراء.
 */
data class DisplayPrice(

    val amount: Double,

    val currencyCode: String
)

enum class ProductCategory {

    /** حساب لعبة */
    ACCOUNT,

    /** شحن عملة داخل اللعبة */
    TOPUP,

    /** عنصر / سكن */
    ITEM,

    /** خدمة رفع مستوى */
    BOOSTING,

    /** بطاقة هدايا */
    GIFT_CARD
}