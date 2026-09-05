import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";

const db = getFirestore();

export const createOrder = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "يجب تسجيل الدخول أولاً");
  }

  const productId = String(request.data?.productId ?? "").trim();

  if (!productId) {
    throw new HttpsError("invalid-argument", "معرف المنتج غير صحيح");
  }

  const buyerId = request.auth.uid;

  const productRef = db.collection("products").doc(productId);
  const productSnap = await productRef.get();

  if (!productSnap.exists) {
    throw new HttpsError("not-found", "المنتج غير موجود");
  }

  const product = productSnap.data();

  const sellerId = String(product?.sellerId ?? "");

  if (!sellerId) {
    throw new HttpsError("failed-precondition", "المنتج غير مرتبط ببائع");
  }

  if (sellerId === buyerId) {
    throw new HttpsError(
      "failed-precondition",
      "لا يمكنك شراء منتجك الخاص"
    );
  }

  const amount = Number(product?.price ?? 0);
  const currencyCode = String(product?.currencyCode ?? "");

  if (!Number.isFinite(amount) || amount <= 0 || !currencyCode) {
    throw new HttpsError(
      "failed-precondition",
      "بيانات سعر المنتج غير صحيحة"
    );
  }

  const orderRef = db.collection("orders").doc();

  const order = {
    id: orderRef.id,
    buyerId,
    sellerId,
    productId,
    gameId: product?.gameId ?? null,
    title: String(product?.title ?? ""),
    description: String(product?.description ?? ""),
    amount,
    currencyCode,
    status: "PENDING",
    paymentTransactionId: null,
    createdAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp()
  };

  await orderRef.set(order);

  return {
    orderId: orderRef.id
  };
});

export const completeOrder = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "يجب تسجيل الدخول أولاً");
  }

  const orderId = String(request.data?.orderId ?? "").trim();

  if (!orderId) {
    throw new HttpsError("invalid-argument", "معرف الطلب غير صحيح");
  }

  const orderRef = db.collection("orders").doc(orderId);
  const orderSnap = await orderRef.get();

  if (!orderSnap.exists) {
    throw new HttpsError("not-found", "الطلب غير موجود");
  }

  const order = orderSnap.data();

  if (order?.buyerId !== request.auth.uid) {
    throw new HttpsError("permission-denied", "غير مصرح لك بهذا الطلب");
  }

  if (order?.status !== "PROCESSING") {
    throw new HttpsError(
      "failed-precondition",
      "لا يمكن إتمام الطلب في حالته الحالية"
    );
  }

  await orderRef.update({
    status: "COMPLETED",
    updatedAt: FieldValue.serverTimestamp()
  });

  return { success: true };
});

export const disputeOrder = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "يجب تسجيل الدخول أولاً");
  }

  const orderId = String(request.data?.orderId ?? "").trim();
  const reason = String(request.data?.reason ?? "").trim();

  if (!orderId || !reason) {
    throw new HttpsError(
      "invalid-argument",
      "بيانات النزاع غير مكتملة"
    );
  }

  if (reason.length > 1000) {
    throw new HttpsError(
      "invalid-argument",
      "سبب النزاع طويل جداً"
    );
  }

  const orderRef = db.collection("orders").doc(orderId);
  const orderSnap = await orderRef.get();

  if (!orderSnap.exists) {
    throw new HttpsError("not-found", "الطلب غير موجود");
  }

  const order = orderSnap.data();

  const uid = request.auth.uid;

  if (order?.buyerId !== uid && order?.sellerId !== uid) {
    throw new HttpsError(
      "permission-denied",
      "غير مصرح لك بهذا الطلب"
    );
  }

  if (
    order?.status === "COMPLETED" ||
    order?.status === "CANCELLED" ||
    order?.status === "REFUNDED"
  ) {
    throw new HttpsError(
      "failed-precondition",
      "لا يمكن فتح نزاع على هذا الطلب"
    );
  }

  await orderRef.update({
    status: "DISPUTED",
    disputeReason: reason,
    disputeOpenedBy: uid,
    disputeOpenedAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp()
  });

  return { success: true };
});
