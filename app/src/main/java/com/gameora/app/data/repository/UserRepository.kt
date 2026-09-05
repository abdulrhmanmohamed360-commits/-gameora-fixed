package com.gameora.app.data.repository

import com.gameora.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun requireUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً")
    }

    private fun userDocument(uid: String) =
        firestore.collection("users").document(uid)

    suspend fun getCurrentUser(): Result<User?> = runCatching {
        val uid = requireUid()

        userDocument(uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateProfile(
        displayName: String,
        phoneNumber: String,
        countryCode: String,
        currencyCode: String,
        photoUrl: String?
    ): Result<Unit> = runCatching {

        val uid = requireUid()

        val updates = hashMapOf<String, Any>(
            "displayName" to displayName.trim(),
            "phoneNumber" to phoneNumber.trim(),
            "countryCode" to countryCode.trim().uppercase(),
            "currencyCode" to currencyCode.trim().uppercase(),
            "updatedAt" to System.currentTimeMillis()
        )

        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }

        userDocument(uid)
            .update(updates)
            .await()
    }
}
