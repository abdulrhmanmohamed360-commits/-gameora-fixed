package com.gameora.app.data.model

/**
 * طلب شراء داخل Gameora.
 *
 * السعر والرصيد الحقيقيان لا يُعتبران مصدر ثقة من التطبيق.
 * الـ Backend هو المسؤول عن إنشاء الطلب وتأكيد الدفع وتغيير الحالة.
 */
data class Order(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val productId: String = "",
    val gameId: String? = null,

    val title: String = "",
    val description: String = "",

    val amount: Double = 0.0,
    val currencyCode: String = "",

    val status: OrderStatus = OrderStatus.PENDING,

    val paymentTransactionId: String? = null,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    REFUNDED,
    DISPUTED
}
