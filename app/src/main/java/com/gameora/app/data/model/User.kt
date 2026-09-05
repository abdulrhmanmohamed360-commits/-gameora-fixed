package com.gameora.app.data.model

/**
 * بيانات مستخدم Gameora.
 *
 * البيانات الحساسة مثل كلمة المرور لا تُخزن هنا ولا في Firestore.
 * المصادقة تتم من خلال Firebase Authentication.
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val countryCode: String = "",
    val currencyCode: String = "",
    val isSeller: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
