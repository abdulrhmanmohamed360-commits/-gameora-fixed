package com.gameora.app.data.model

/** موديل اللعبة — يُقرأ من مجموعة games في Firestore */
data class Game(
    val id: String = "",
    val name: String = "",             // مثال: "Free Fire", "PUBG Mobile"
    val nameAr: String = "",
    val iconUrl: String = "",
    val bannerUrl: String = "",
    val productsCount: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
