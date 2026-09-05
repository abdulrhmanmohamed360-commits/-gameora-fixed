package com.gameora.app.data.repository

import com.gameora.app.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Repository الخاص بالطلبات في Gameora.
 *
 * ⚠️ جميع العمليات الحساسة تتم عن طريق Cloud Functions:
 *
 * - إنشاء الطلب وحجز الأموال
 * - قبول/رفض البائع
 * - تسليم الحساب
 * - تأكيد المشتري
 * - فتح النزاع
 * - Refund
 * - تحرير الأموال للبائع
 *
 * التطبيق لا يملك صلاحية تنفيذ هذه العمليات مباشرة.
 */
class OrderRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance(),

    private val functions: FirebaseFunctions =
        FirebaseFunctions.getInstance("us-central1")
) {

    private val ordersCollection
        get() = firestore.collection("orders")


    // =========================================================
    // إنشاء الطلب
    // =========================================================

    /**
     * إنشاء طلب شراء.
     *
     * الـBackend يقوم بـ:
     * 1. التحقق من المنتج.
     * 2. التحقق من السعر.
     * 3. حجز أموال المشتري.
     * 4. إنشاء الطلب.
     * 5. إنشاء المحادثة.
     * 6. إرسال إشعار للبائع.
     * 7. إنشاء sellerApprovalDeadline.
     */
    suspend fun createOrder(
        productId: String,
        quantity: Int,
        sellerApprovalTimeoutMinutes: Int = 2
    ): Result<String> = runCatching {

        require(quantity > 0) {
            "الكمية يجب أن تكون أكبر من صفر"
        }

        require(
            sellerApprovalTimeoutMinutes in 1..1440
        ) {
            "مدة الانتظار يجب أن تكون بين دقيقة و24 ساعة"
        }

        val data = hashMapOf(
            "productId" to productId,
            "quantity" to quantity,
            "sellerApprovalTimeoutMinutes" to sellerApprovalTimeoutMinutes
        )

        val result = functions
            .getHttpsCallable("createOrder")
            .call(data)
            .await()

        (result.data as? Map<*, *>)
            ?.get("orderId") as? String
            ?: error("لم يتم استلام رقم الطلب من الخادم")
    }


    // =========================================================
    // جلب الطلب
    // =========================================================

    suspend fun fetchOrderById(
        orderId: String
    ): Result<Order?> = runCatching {

        ordersCollection
            .document(orderId)
            .get()
            .await()
            .toObject(Order::class.java)
    }


    // =========================================================
    // طلبات المشتري
    // =========================================================

    suspend fun fetchOrdersAsBuyer(
        uid: String
    ): Result<List<Order>> = runCatching {

        ordersCollection
            .whereEqualTo("buyerId", uid)
            .orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )
            .get()
            .await()
            .toObjects(Order::class.java)
    }


    // =========================================================
    // طلبات البائع
    // =========================================================

    suspend fun fetchOrdersAsSeller(
        uid: String
    ): Result<List<Order>> = runCatching {

        ordersCollection
            .whereEqualTo("sellerId", uid)
            .orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )
            .get()
            .await()
            .toObjects(Order::class.java)
    }


    // =========================================================
    // قبول البائع
    // =========================================================

    /**
     * البائع يوافق على بدء عملية البيع.
     *
     * Backend يتحقق أن:
     * - المستخدم هو sellerId.
     * - الطلب ما زال PENDING_SELLER_APPROVAL.
     * - المهلة لم تنتهِ.
     * - الأموال ما زالت معلقة.
     */
    suspend fun acceptOrder(
        orderId: String
    ): Result<Unit> = callFunction(
        "sellerAcceptOrder",
        mapOf(
            "orderId" to orderId
        )
    )


    // =========================================================
    // رفض البائع
    // =========================================================

    /**
     * البائع يرفض الطلب.
     *
     * Backend يقوم بإلغاء الطلب وإعادة الأموال للمشتري.
     */
    suspend fun rejectOrder(
        orderId: String,
        reason: String = ""
    ): Result<Unit> = callFunction(
        "sellerRejectOrder",
        mapOf(
            "orderId" to orderId,
            "reason" to reason
        )
    )


    // =========================================================
    // بدء التسليم
    // =========================================================

    /**
     * بدء عملية تسليم الحساب.
     */
    suspend fun startDelivery(
        orderId: String
    ): Result<Unit> = callFunction(
        "startAccountDelivery",
        mapOf(
            "orderId" to orderId
        )
    )


    // =========================================================
    // تسليم بيانات الحساب
    // =========================================================

    /**
     * البائع يؤكد أنه قام بتسليم بيانات الحساب.
     *
     * ⚠️ بيانات الحساب نفسها لا تمر هنا.
     *
     * يتم تخزينها بشكل منفصل على Backend،
     * والـBackend هو الذي يتحكم في صلاحية عرضها.
     */
    suspend fun deliverAccount(
        orderId: String
    ): Result<Unit> = callFunction(
        "deliverAccount",
        mapOf(
            "orderId" to orderId
        )
    )


    // =========================================================
    // تأكيد المشتري
    // =========================================================

    /**
     * المشتري يؤكد أن الحساب يعمل وأنه استلمه.
     *
     * Backend بعدها يستطيع تحرير الأموال للبائع
     * وفق قواعد النظام.
     */
    suspend fun confirmAccount(
        orderId: String
    ): Result<Unit> = callFunction(
        "confirmAccount",
        mapOf(
            "orderId" to orderId
        )
    )


    // =========================================================
    // فتح نزاع
    // =========================================================

    /**
     * فتح بلاغ على الطلب.
     *
     * مثال:
     * - كلمة السر لا تعمل.
     * - الحساب مختلف.
     * - البائع لم يسلم الحساب.
     */
    suspend fun openDispute(
        orderId: String,
        reason: String
    ): Result<String> = runCatching {

        require(reason.isNotBlank()) {
            "يجب كتابة سبب البلاغ"
        }

        val data = hashMapOf(
            "orderId" to orderId,
            "reason" to reason
        )

        val result = functions
            .getHttpsCallable("openDispute")
            .call(data)
            .await()

        (result.data as? Map<*, *>)
            ?.get("disputeId") as? String
            ?: error("لم يتم استلام رقم البلاغ")
    }


    // =========================================================
    // تحرير الأموال
    // =========================================================

    /**
     * تحرير الأموال للبائع.
     *
     * ⚠️ لا يتم استدعاؤها من واجهة المستخدم بشكل مباشر.
     *
     * الـBackend هو الذي يقرر متى يسمح بتحرير الأموال.
     */
    suspend fun releaseFunds(
        orderId: String
    ): Result<Unit> = callFunction(
        "releaseFunds",
        mapOf(
            "orderId" to orderId
        )
    )


    // =========================================================
    // Refund
    // =========================================================

    /**
     * إعادة الأموال للمشتري.
     *
     * تستخدم عند:
     * - انتهاء مهلة موافقة البائع.
     * - رفض البائع.
     * - قبول النزاع.
     */
    suspend fun refundOrder(
        orderId: String,
        reason: String
    ): Result<Unit> = callFunction(
        "refundOrder",
        mapOf(
            "orderId" to orderId,
            "reason" to reason
        )
    )


    // =========================================================
    // تحديث حالة الطلب
    // =========================================================

    /**
     * للاستخدام في الحالات التي يسمح بها Backend.
     *
     * لا يتم تغيير الحالة مباشرة في Firestore.
     */
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ): Result<Unit> = callFunction(
        "updateOrderStatus",
        mapOf(
            "orderId" to orderId,
            "newStatus" to newStatus
        )
    )


    // =========================================================
    // Helper
    // =========================================================

    /**
     * استدعاء Cloud Function وإرجاع Result<Unit>.
     */
    private suspend fun callFunction(
        functionName: String,
        data: Map<String, Any?>
    ): Result<Unit> = runCatching {

        functions
            .getHttpsCallable(functionName)
            .call(data)
            .await()

        Unit
    }
}