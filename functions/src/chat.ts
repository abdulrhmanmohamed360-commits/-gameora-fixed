import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";

const db = getFirestore();

function requireAuth(request: any): string {
  const uid = request.auth?.uid;

  if (!uid) {
    throw new HttpsError(
      "unauthenticated",
      "يجب تسجيل الدخول أولاً"
    );
  }

  return uid;
}

function cleanString(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

/**
 * موافقة البائع على الطلب وفتح المحادثة.
 */
export const acceptOrder = onCall(
  { region: "us-central1" },
  async (request) => {
    const uid = requireAuth(request);

    const orderId = cleanString(request.data?.orderId);

    if (!orderId) {
      throw new HttpsError(
        "invalid-argument",
        "معرف الطلب غير صحيح"
      );
    }

    const orderRef = db.collection("orders").doc(orderId);
    const orderSnap = await orderRef.get();

    if (!orderSnap.exists) {
      throw new HttpsError(
        "not-found",
        "الطلب غير موجود"
      );
    }

    const order = orderSnap.data();

    if (order?.sellerId !== uid) {
      throw new HttpsError(
        "permission-denied",
        "أنت لست بائع هذا الطلب"
      );
    }

    if (order?.status !== "PENDING") {
      throw new HttpsError(
        "failed-precondition",
        "الطلب ليس في انتظار موافقة البائع"
      );
    }

    const existingChat = await db
      .collection("chats")
      .whereEqualTo("orderId", orderId)
      .limit(1)
      .get();

    if (!existingChat.empty) {
      const chatId = existingChat.docs[0].id;

      await orderRef.update({
        status: "PROCESSING",
        updatedAt: FieldValue.serverTimestamp()
      });

      return {
        success: true,
        chatId
      };
    }

    const chatRef = db.collection("chats").doc();

    const batch = db.batch();

    batch.set(chatRef, {
      id: chatRef.id,
      orderId,
      buyerId: order.buyerId,
      sellerId: order.sellerId,
      status: "ACTIVE",
      lastMessage: "",
      lastMessageAt: FieldValue.serverTimestamp(),
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp()
    });

    batch.update(orderRef, {
      status: "PROCESSING",
      chatId: chatRef.id,
      updatedAt: FieldValue.serverTimestamp()
    });

    await batch.commit();

    return {
      success: true,
      chatId: chatRef.id
    };
  }
);

/**
 * رفض الطلب.
 *
 * يتم تغيير الحالة فقط هنا.
 * استرجاع الأموال يجب أن يتم من مسار مالي موثوق منفصل.
 */
export const rejectOrder = onCall(
  { region: "us-central1" },
  async (request) => {
    const uid = requireAuth(request);

    const orderId = cleanString(request.data?.orderId);

    if (!orderId) {
      throw new HttpsError(
        "invalid-argument",
        "معرف الطلب غير صحيح"
      );
    }

    const orderRef = db.collection("orders").doc(orderId);
    const orderSnap = await orderRef.get();

    if (!orderSnap.exists) {
      throw new HttpsError(
        "not-found",
        "الطلب غير موجود"
      );
    }

    const order = orderSnap.data();

    if (order?.sellerId !== uid) {
      throw new HttpsError(
        "permission-denied",
        "أنت لست بائع هذا الطلب"
      );
    }

    if (order?.status !== "PENDING") {
      throw new HttpsError(
        "failed-precondition",
        "لا يمكن رفض الطلب في حالته الحالية"
      );
    }

    await orderRef.update({
      status: "CANCELLED",
      rejectedBy: uid,
      rejectedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp()
    });

    return {
      success: true
    };
  }
);

/**
 * إرسال رسالة داخل محادثة.
 */
export const sendChatMessage = onCall(
  { region: "us-central1" },
  async (request) => {
    const uid = requireAuth(request);

    const chatId = cleanString(request.data?.chatId);
    const orderId = cleanString(request.data?.orderId);
    const text = cleanString(request.data?.text);

    if (!chatId || !orderId || !text) {
      throw new HttpsError(
        "invalid-argument",
        "بيانات الرسالة غير مكتملة"
      );
    }

    if (text.length > 2000) {
      throw new HttpsError(
        "invalid-argument",
        "الرسالة طويلة جداً"
      );
    }

    const chatRef = db.collection("chats").doc(chatId);
    const chatSnap = await chatRef.get();

    if (!chatSnap.exists) {
      throw new HttpsError(
        "not-found",
        "المحادثة غير موجودة"
      );
    }

    const chat = chatSnap.data();

    if (chat?.orderId !== orderId) {
      throw new HttpsError(
        "failed-precondition",
        "المحادثة غير مرتبطة بهذا الطلب"
      );
    }

    if (
      chat?.buyerId !== uid &&
      chat?.sellerId !== uid
    ) {
      throw new HttpsError(
        "permission-denied",
        "غير مصرح لك بهذه المحادثة"
      );
    }

    if (chat?.status !== "ACTIVE") {
      throw new HttpsError(
        "failed-precondition",
        "المحادثة غير نشطة"
      );
    }

    const messageRef = chatRef
      .collection("messages")
      .doc();

    const batch = db.batch();

    batch.set(messageRef, {
      id: messageRef.id,
      chatId,
      orderId,
      senderId: uid,
      text,
      isRead: false,
      createdAt: FieldValue.serverTimestamp()
    });

    batch.update(chatRef, {
      lastMessage: text,
      lastMessageSenderId: uid,
      lastMessageAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp()
    });

    await batch.commit();

    return {
      success: true,
      messageId: messageRef.id
    };
  }
);

/**
 * تحديد الرسالة كمقروءة.
 */
export const markMessageAsRead = onCall(
  { region: "us-central1" },
  async (request) => {
    const uid = requireAuth(request);

    const chatId = cleanString(request.data?.chatId);
    const messageId = cleanString(request.data?.messageId);

    if (!chatId || !messageId) {
      throw new HttpsError(
        "invalid-argument",
        "بيانات الرسالة غير صحيحة"
      );
    }

    const chatRef = db.collection("chats").doc(chatId);
    const chatSnap = await chatRef.get();

    if (!chatSnap.exists) {
      throw new HttpsError(
        "not-found",
        "المحادثة غير موجودة"
      );
    }

    const chat = chatSnap.data();

    if (
      chat?.buyerId !== uid &&
      chat?.sellerId !== uid
    ) {
      throw new HttpsError(
        "permission-denied",
        "غير مصرح لك بهذه المحادثة"
      );
    }

    const messageRef = chatRef
      .collection("messages")
      .doc(messageId);

    const messageSnap = await messageRef.get();

    if (!messageSnap.exists) {
      throw new HttpsError(
        "not-found",
        "الرسالة غير موجودة"
      );
    }

    const message = messageSnap.data();

    if (message?.chatId !== chatId) {
      throw new HttpsError(
        "failed-precondition",
        "الرسالة لا تنتمي لهذه المحادثة"
      );
    }

    if (message?.senderId === uid) {
      return {
        success: true
      };
    }

    await messageRef.update({
      isRead: true,
      readAt: FieldValue.serverTimestamp(),
      readBy: uid
    });

    return {
      success: true
    };
  }
);
