package com.gameora.app.data.repository

import com.gameora.app.data.model.Game
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** مستودع الألعاب المتاحة على المنصة (Free Fire, PUBG Mobile...) — قراءة فقط من التطبيق */
class GameRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val gamesCollection get() = firestore.collection("games")

    suspend fun fetchActiveGames(): Result<List<Game>> = runCatching {
        gamesCollection
            // ملاحظة: خاصية Kotlin اسمها isActive تُخزَّن في Firestore
            // باسم الحقل "active" (تُحذف بادئة is تلقائيًا عند التسلسل) —
            // نفس القاعدة المتّبعة بالفعل في ProductRepository.
            .whereEqualTo("active", true)
            .orderBy("sortOrder")
            .get()
            .await()
            .toObjects(Game::class.java)
    }

    suspend fun fetchGameById(gameId: String): Result<Game?> = runCatching {
        gamesCollection.document(gameId).get().await().toObject(Game::class.java)
    }
}
