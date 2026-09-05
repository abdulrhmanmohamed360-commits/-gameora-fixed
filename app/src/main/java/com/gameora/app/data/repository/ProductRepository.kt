package com.gameora.app.data.repository

import android.net.Uri
import com.gameora.app.data.model.Product
import com.gameora.app.data.model.ProductCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val functions: FirebaseFunctions =
        FirebaseFunctions.getInstance("us-central1")
) {

    private fun requireUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً")
    }

    /**
     * جلب المنتجات النشطة من Backend/Firestore.
     * الفلترة (لعبة/تصنيف) والترتيب والبحث تتم بعد الجلب حتى لا نحتاج
     * فهارس Firestore مركّبة إضافية في هذه المرحلة.
     */
    suspend fun fetchProducts(): Result<List<Product>> = runCatching {
        requireUid()

        firestore.collection("products")
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    suspend fun fetchProductById(productId: String): Result<Product?> = runCatching {
        requireUid()

        if (productId.isBlank()) {
            throw IllegalArgumentException("معرف المنتج غير صحيح")
        }

        firestore.collection("products")
            .document(productId)
            .get()
            .await()
            .toObject(Product::class.java)
    }

    /**
     * منتجات لعبة معيّنة فقط (تُستخدم في شاشة تفاصيل اللعبة).
     */
    suspend fun fetchProductsByGame(gameId: String): Result<List<Product>> = runCatching {
        requireUid()

        if (gameId.isBlank()) {
            return@runCatching emptyList()
        }

        firestore.collection("products")
            .whereEqualTo("active", true)
            .whereEqualTo("gameId", gameId)
            .get()
            .await()
            .toObjects(Product::class.java)
            .sortedByDescending { it.createdAt }
    }

    /**
     * بحث داخل المتجر باسم اللعبة أو عنوان الإعلان أو اسم البائع.
     * البحث الفعلي (contains) يتم على البيانات الحقيقية بعد جلبها —
     * لا توجد أي بيانات ثابتة/تجريبية داخل الكود.
     *
     * gameNamesById اختياري: خريطة (gameId -> اسم اللعبة) حتى يشمل
     * البحث اسم اللعبة أيضًا، وليس فقط معرفها.
     */
    suspend fun searchProducts(
        query: String,
        gameNamesById: Map<String, String> = emptyMap()
    ): Result<List<Product>> = runCatching {
        val all = fetchProducts().getOrThrow()

        if (query.isBlank()) return@runCatching all

        val q = query.trim()

        all.filter { product ->
            product.title.contains(q, ignoreCase = true) ||
                product.sellerName.contains(q, ignoreCase = true) ||
                (gameNamesById[product.gameId]?.contains(q, ignoreCase = true) == true)
        }
    }

    /**
     * رفع صور الحساب إلى Firebase Storage وإرجاع روابطها.
     * لا يتم تخزين أي صورة كـ Base64 داخل Firestore.
     */
    private suspend fun uploadImages(
        uid: String,
        imageUris: List<Uri>
    ): List<String> {
        val folderId = UUID.randomUUID().toString()

        return imageUris.mapIndexed { index, uri ->
            val ref = storage.reference
                .child("product_images")
                .child(uid)
                .child(folderId)
                .child("image_$index.jpg")

            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }
    }

    /**
     * إنشاء إعلان حساب للبيع.
     *
     * - يرفع الصور إلى Storage أولاً.
     * - ثم ينشئ المنتج عبر Cloud Function واحدة تستقبل البيانات العامة
     *   وبيانات الدخول الحساسة معًا، والسيرفر هو المسؤول عن تخزين
     *   البيانات الحساسة بشكل منفصل تمامًا عن مستند المنتج العام
     *   (راجع functions/src/products.ts).
     * - لا نحتفظ بكلمة السر أو بيانات الدخول في أي مكان على الجهاز
     *   بعد إرسالها.
     */
    suspend fun createProduct(
        sellerName: String,
        gameId: String,
        title: String,
        description: String,
        imageUris: List<Uri>,
        price: Double,
        currencyCode: String,
        category: ProductCategory,
        stock: Int,
        accountUsername: String,
        accountPassword: String,
        accountEmail: String,
        accountEmailPassword: String,
        accountLevel: String,
        accountRank: String,
        accountCoins: String,
        accountServer: String
    ): Result<String> = runCatching {

        val uid = requireUid()

        if (gameId.isBlank()) {
            throw IllegalArgumentException("من فضلك اختر اللعبة")
        }

        if (title.isBlank()) {
            throw IllegalArgumentException("عنوان الإعلان مطلوب")
        }

        if (imageUris.isEmpty()) {
            throw IllegalArgumentException("من فضلك اختر صورة واحدة على الأقل")
        }

        if (price <= 0.0) {
            throw IllegalArgumentException("السعر غير صحيح")
        }

        if (stock < 1) {
            throw IllegalArgumentException("الكمية يجب أن تكون 1 على الأقل")
        }

        if (accountUsername.isBlank()) {
            throw IllegalArgumentException("اسم المستخدم / ID الحساب مطلوب")
        }

        if (accountPassword.isBlank()) {
            throw IllegalArgumentException("كلمة سر الحساب مطلوبة")
        }

        // 1) رفع الصور إلى Storage والحصول على روابطها
        val imageUrls = uploadImages(uid, imageUris)

        // 2) إنشاء المنتج عبر Cloud Function
        val data = hashMapOf<String, Any?>(
            "title" to title.trim(),
            "description" to description.trim(),
            "price" to price,
            "currencyCode" to currencyCode.trim().uppercase(),
            "gameId" to gameId,
            "sellerName" to sellerName.trim(),
            "imageUrls" to imageUrls,
            "category" to category.name,
            "stock" to stock,
            "accountLevel" to accountLevel.trim(),
            "accountRank" to accountRank.trim(),
            "accountCoins" to accountCoins.trim(),
            "accountServer" to accountServer.trim(),

            // بيانات دخول حساسة — يفصلها الـ Backend عن مستند المنتج العام
            // ولا يعيدها أبدًا ضمن بيانات المتجر.
            "accountUsername" to accountUsername.trim(),
            "accountPassword" to accountPassword,
            "accountEmail" to accountEmail.trim(),
            "accountEmailPassword" to accountEmailPassword
        )

        val result = functions
            .getHttpsCallable("createProduct")
            .call(data)
            .await()

        @Suppress("UNCHECKED_CAST")
        val response = result.data as? Map<String, Any?>
            ?: throw IllegalStateException("استجابة غير صالحة من الخادم")

        response["productId"]?.toString()
            ?: throw IllegalStateException("لم يتم إرجاع معرف المنتج")
    }

    /**
     * تعديل المنتج عبر Backend فقط.
     */
    suspend fun updateProduct(
        productId: String,
        title: String,
        description: String,
        price: Double,
        currencyCode: String
    ): Result<Unit> = runCatching {

        requireUid()

        if (productId.isBlank()) {
            throw IllegalArgumentException("معرف المنتج غير صحيح")
        }

        if (title.isBlank()) {
            throw IllegalArgumentException("اسم المنتج مطلوب")
        }

        if (price <= 0.0) {
            throw IllegalArgumentException("السعر غير صحيح")
        }

        functions
            .getHttpsCallable("updateProduct")
            .call(
                hashMapOf(
                    "productId" to productId,
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "price" to price,
                    "currencyCode" to currencyCode.trim().uppercase()
                )
            )
            .await()

        Unit
    }

    /**
     * تعطيل المنتج بدلاً من حذفه نهائياً.
     */
    suspend fun deactivateProduct(
        productId: String
    ): Result<Unit> = runCatching {

        requireUid()

        if (productId.isBlank()) {
            throw IllegalArgumentException("معرف المنتج غير صحيح")
        }

        functions
            .getHttpsCallable("deactivateProduct")
            .call(
                hashMapOf(
                    "productId" to productId
                )
            )
            .await()

        Unit
    }
}
