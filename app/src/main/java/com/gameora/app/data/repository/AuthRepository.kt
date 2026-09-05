package com.gameora.app.data.repository

import com.gameora.app.data.model.User
import com.gameora.app.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * مستودع المصادقة وبيانات المستخدم.
 * - إنشاء الحساب وتسجيل الدخول يتم عبر Firebase Authentication مباشرة (آمن بطبيعته).
 * - بيانات الملف الشخصي (الدولة/العملة/الدور) تُخزَّن في Firestore تحت users/{uid}.
 * - تغيير role إلى ADMIN أو seller verification يجب أن يتم فقط عبر Backend، وليس من هنا.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection get() = firestore.collection("users")

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        countryCode: String,
        currencyCode: String
    ): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("فشل إنشاء الحساب")

        val newUser = User(
            uid = uid,
            displayName = displayName,
            email = email,
            countryCode = countryCode,
            currencyCode = currencyCode,
            role = UserRole.BUYER,
            createdAt = System.currentTimeMillis()
        )
        // كتابة بيانات المستخدم الأساسية مسموحة له، لكن قواعد الأمان في Firestore
        // يجب أن تمنع تعديل حقول حساسة مثل role بعد الإنشاء (راجع firestore.rules)
        usersCollection.document(uid).set(newUser).await()
        newUser
    }

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    fun logout() = auth.signOut()

    suspend fun fetchCurrentUserProfile(): Result<User?> = runCatching {
        val uid = currentUid ?: return@runCatching null
        usersCollection.document(uid).get().await().toObject(User::class.java)
    }

    suspend fun updateProfile(displayName: String, countryCode: String, currencyCode: String): Result<Unit> =
        runCatching {
            val uid = currentUid ?: error("المستخدم غير مسجل الدخول")
            usersCollection.document(uid).update(
                mapOf(
                    "displayName" to displayName,
                    "countryCode" to countryCode,
                    "currencyCode" to currencyCode
                )
            ).await()
        }
}
