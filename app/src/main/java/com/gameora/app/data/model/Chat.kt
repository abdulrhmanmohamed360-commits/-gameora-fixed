package com.gameora.app.data.model

/**
 * محادثة مرتبطة بعملية بيع واحدة.
 *
 * كل Chat مرتبط بـ Order واحد فقط.
 * لا يتم إنشاء محادثة قبل موافقة البائع.
 */
data class Chat(
    val id: String = "",
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val productId: String = "",

    val isActive: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastMessageAt: Long = 0L
)

/**
 * رسالة داخل المحادثة.
 */
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val orderId: String = "",

    val senderId: String = "",
    val receiverId: String = "",

    val text: String = "",

    val createdAt: Long = 0L,

    val isRead: Boolean = false
)