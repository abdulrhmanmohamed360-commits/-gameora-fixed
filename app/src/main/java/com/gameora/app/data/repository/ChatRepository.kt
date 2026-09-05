package com.gameora.app.data.repository

import com.gameora.app.data.model.Chat
import com.gameora.app.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

/**
 * مستودع المحادثات في Gameora.
 *
 * قواعد مهمة:
 * - كل محادثة مرتبطة بطلب واحد.
 * - الـ Backend هو المسؤول عن فتح المحادثة بعد موافقة البائع.
 * - التطبيق لا يستطيع من نفسه تحويل الطلب إلى CHAT_ACTIVE.
 * - الرسائل محفوظة في Firestore وتظل موجودة عند خروج أي طرف من التطبيق.
 */
class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions =
        FirebaseFunctions.getInstance("us-central1")
) {

    private val chatsCollection
        get() = firestore.collection("chats")

    /**
     * جلب محادثة مرتبطة بطلب معين.
     */
    suspend fun fetchChatByOrderId(
        orderId: String
    ): Result<Chat?> = runCatching {

        val snapshot = chatsCollection
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .get()
            .await()

        snapshot.documents.firstOrNull()
            ?.toObject(Chat::class.java)
    }

    /**
     * جلب محادثة بواسطة ID.
     */
    suspend fun fetchChatById(
        chatId: String
    ): Result<Chat?> = runCatching {

        chatsCollection
            .document(chatId)
            .get()
            .await()
            .toObject(Chat::class.java)
    }

    /**
     * فتح المحادثة بعد موافقة البائع.
     *
     * لا يتم إنشاء Chat مباشرة من التطبيق.
     * Cloud Function تتحقق من:
     *
     * 1. المستخدم هو البائع الحقيقي.
     * 2. الطلب موجود.
     * 3. الطلب ينتظر موافقة البائع.
     * 4. الأموال ما زالت محجوزة.
     * 5. إنشاء Chat وربطه بالطلب.
     */
    suspend fun acceptOrder(
        orderId: String
    ): Result<String> = runCatching {

        val data = hashMapOf(
            "orderId" to orderId
        )

        val result = functions
            .getHttpsCallable("acceptOrder")
            .call(data)
            .await()

        val response = result.data as? Map<*, *>

        response?.get("chatId") as? String
            ?: error("لم يتم استلام معرف المحادثة من الخادم")
    }

    /**
     * رفض طلب البيع من البائع.
     *
     * الـ Backend هو المسؤول عن إعادة الأموال للمشتري
     * وتغيير حالة الطلب.
     */
    suspend fun rejectOrder(
        orderId: String
    ): Result<Unit> = runCatching {

        val data = hashMapOf(
            "orderId" to orderId
        )

        functions
            .getHttpsCallable("rejectOrder")
            .call(data)
            .await()

        Unit
    }

    /**
     * إرسال رسالة.
     *
     * نرسل الرسالة إلى Cloud Function بدل الكتابة المباشرة
     * حتى يتحقق الـ Backend من أن المستخدم طرف في الطلب.
     */
    suspend fun sendMessage(
        chatId: String,
        orderId: String,
        text: String
    ): Result<String> = runCatching {

        require(text.isNotBlank()) {
            "لا يمكن إرسال رسالة فارغة"
        }

        val data = hashMapOf(
            "chatId" to chatId,
            "orderId" to orderId,
            "text" to text.trim()
        )

        val result = functions
            .getHttpsCallable("sendChatMessage")
            .call(data)
            .await()

        val response = result.data as? Map<*, *>

        response?.get("messageId") as? String
            ?: error("لم يتم استلام معرف الرسالة من الخادم")
    }

    /**
     * مراقبة رسائل المحادثة لحظيًا.
     *
     * بمجرد وصول رسالة جديدة تظهر للطرف الآخر
     * بدون الحاجة إلى عمل Refresh.
     */
    fun observeMessages(
        chatId: String
    ): Flow<List<ChatMessage>> = callbackFlow {

        val registration = chatsCollection
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot
                    ?.toObjects(ChatMessage::class.java)
                    ?: emptyList()

                trySend(messages)
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * تحديد رسالة كمقروءة.
     *
     * يتم ذلك عن طريق Backend للتحقق من صلاحية المستخدم.
     */
    suspend fun markMessageAsRead(
        chatId: String,
        messageId: String
    ): Result<Unit> = runCatching {

        val data = hashMapOf(
            "chatId" to chatId,
            "messageId" to messageId
        )

        functions
            .getHttpsCallable("markMessageAsRead")
            .call(data)
            .await()

        Unit
    }

    /**
     * جلب كل محادثات المستخدم.
     *
     * الـ Backend/Firestore Rules يجب أن يسمح فقط
     * للمستخدم الذي يكون buyerId أو sellerId برؤية المحادثة.
     */
    suspend fun fetchMyChats(
        uid: String
    ): Result<List<Chat>> = runCatching {

        val buyerChats = chatsCollection
            .whereEqualTo("buyerId", uid)
            .get()
            .await()
            .toObjects(Chat::class.java)

        val sellerChats = chatsCollection
            .whereEqualTo("sellerId", uid)
            .get()
            .await()
            .toObjects(Chat::class.java)

        (buyerChats + sellerChats)
            .distinctBy { it.id }
            .sortedByDescending { it.lastMessageAt }
    }
}