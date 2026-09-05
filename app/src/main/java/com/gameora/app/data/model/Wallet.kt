package com.gameora.app.data.model

/**
 * موديل المحفظة — يُقرأ من مستند wallets/{uid} في Firestore.
 * القراءة فقط من التطبيق. أي تعديل (إضافة/خصم/سحب) يمر إجباريًا عبر Cloud Functions
 * حتى لا يعتمد الرصيد الحقيقي على أي قيمة يرسلها العميل مباشرة.
 */
data class Wallet(
    val uid: String = "",
    val balance: Double = 0.0,
    val currencyCode: String = "",
    val updatedAt: Long = 0L
)

data class WalletTransaction(
    val id: String = "",
    val uid: String = "",
    val type: TransactionType = TransactionType.DEPOSIT,
    val amount: Double = 0.0,
    val currencyCode: String = "",
    val relatedOrderId: String? = null,
    val note: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val createdAt: Long = 0L
)

enum class TransactionType {
    DEPOSIT,      // إضافة رصيد
    WITHDRAWAL,   // سحب
    PURCHASE,     // خصم مقابل شراء
    SALE_PAYOUT,  // إيداع أرباح بيع لمنتج
    REFUND        // استرجاع
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
