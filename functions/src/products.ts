import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";

const db = getFirestore();

const VALID_CATEGORIES = [
  "ACCOUNT",
  "TOPUP",
  "ITEM",
  "BOOSTING",
  "GIFT_CARD"
];

function requireAuth(request: any): string {
  if (!request.auth) {
    throw new HttpsError(
      "unauthenticated",
      "يجب تسجيل الدخول أولاً"
    );
  }

  return request.auth.uid;
}

function cleanString(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function validPrice(value: unknown): boolean {
  const price = Number(value);
  return Number.isFinite(price) && price > 0;
}

function cleanImageUrls(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .filter((item): item is string => typeof item === "string" && item.trim().length > 0)
    .map((item) => item.trim())
    .slice(0, 6);
}

/**
 * إنشاء منتج (حساب لعبة للبيع).
 *
 * ملاحظة مهمة جدًا:
 * بيانات الدخول الحساسة (accountPassword, accountEmailPassword) لا يتم
 * تخزينها أبدًا داخل مستند products/{id} العام الذي يقرأه المتجر.
 * يتم تخزينها في مجموعة منفصلة ومحمية productSecrets/{id}، والتي تمنعها
 * قواعد Firestore (firestore.rules) من القراءة عبر التطبيق تمامًا في
 * هذه المرحلة. تسليمها الآمن للمشتري سيُنفَّذ في مرحلة قادمة.
 */
export const createProduct = onCall(async (request) => {
  const uid = requireAuth(request);

  const title = cleanString(request.data?.title);
  const description = cleanString(request.data?.description);
  const currencyCode = cleanString(
    request.data?.currencyCode
  ).toUpperCase();

  const price = Number(request.data?.price);
  const gameId = cleanString(request.data?.gameId);
  const sellerName = cleanString(request.data?.sellerName);
  const imageUrls = cleanImageUrls(request.data?.imageUrls);

  const categoryInput = cleanString(request.data?.category).toUpperCase();
  const category = VALID_CATEGORIES.includes(categoryInput)
    ? categoryInput
    : "ACCOUNT";

  const stock = Number.isInteger(Number(request.data?.stock))
    ? Number(request.data?.stock)
    : 1;

  const accountLevel = cleanString(request.data?.accountLevel);
  const accountRank = cleanString(request.data?.accountRank);
  const accountCoins = cleanString(request.data?.accountCoins);
  const accountServer = cleanString(request.data?.accountServer);

  // بيانات الدخول (حساسة جزئيًا)
  const accountUsername = cleanString(request.data?.accountUsername);
  const accountPassword =
    typeof request.data?.accountPassword === "string"
      ? request.data.accountPassword
      : "";
  const accountEmail = cleanString(request.data?.accountEmail);
  const accountEmailPassword =
    typeof request.data?.accountEmailPassword === "string"
      ? request.data.accountEmailPassword
      : "";

  if (!title) {
    throw new HttpsError(
      "invalid-argument",
      "اسم المنتج مطلوب"
    );
  }

  if (title.length > 150) {
    throw new HttpsError(
      "invalid-argument",
      "اسم المنتج طويل جداً"
    );
  }

  if (description.length > 5000) {
    throw new HttpsError(
      "invalid-argument",
      "وصف المنتج طويل جداً"
    );
  }

  if (!validPrice(price)) {
    throw new HttpsError(
      "invalid-argument",
      "السعر غير صحيح"
    );
  }

  if (!/^[A-Z]{3}$/.test(currencyCode)) {
    throw new HttpsError(
      "invalid-argument",
      "رمز العملة غير صحيح"
    );
  }

  if (!gameId) {
    throw new HttpsError(
      "invalid-argument",
      "من فضلك اختر اللعبة"
    );
  }

  if (imageUrls.length === 0) {
    throw new HttpsError(
      "invalid-argument",
      "يجب رفع صورة واحدة على الأقل للحساب"
    );
  }

  if (stock < 1) {
    throw new HttpsError(
      "invalid-argument",
      "الكمية يجب أن تكون 1 على الأقل"
    );
  }

  if (!accountUsername) {
    throw new HttpsError(
      "invalid-argument",
      "اسم المستخدم / ID الحساب مطلوب"
    );
  }

  if (!accountPassword) {
    throw new HttpsError(
      "invalid-argument",
      "كلمة سر الحساب مطلوبة"
    );
  }

  // تأكد أن اللعبة المختارة موجودة فعلاً في Backend
  const gameSnap = await db.collection("games").doc(gameId).get();

  if (!gameSnap.exists) {
    throw new HttpsError(
      "invalid-argument",
      "اللعبة المختارة غير موجودة"
    );
  }

  const productRef = db.collection("products").doc();

  // -----------------------------------------------------------
  // 1) المستند العام — هذا فقط ما يظهر في المتجر وصفحة التفاصيل
  // -----------------------------------------------------------
  await productRef.set({
    id: productRef.id,
    sellerId: uid,
    sellerName,
    gameId,
    title,
    description,
    imageUrls,
    originalPrice: price,
    originalCurrency: currencyCode,
    category,
    stockAvailable: stock,
    active: true,
    createdAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),

    // مواصفات عامة غير سرية
    accountUsername,
    accountLevel,
    accountRank,
    accountCoins,
    accountServer
  });

  // -----------------------------------------------------------
  // 2) مستند منفصل ومحمي لبيانات الدخول الحساسة فقط
  //    (لا تتم قراءته من التطبيق في هذه المرحلة إطلاقًا)
  // -----------------------------------------------------------
  await db.collection("productSecrets").doc(productRef.id).set({
    productId: productRef.id,
    sellerId: uid,
    accountUsername,
    accountPassword,
    accountEmail,
    accountEmailPassword,
    createdAt: FieldValue.serverTimestamp()
  });

  return {
    productId: productRef.id
  };
});


/**
 * تعديل منتج
 */
export const updateProduct = onCall(async (request) => {
  const uid = requireAuth(request);

  const productId = cleanString(
    request.data?.productId
  );

  const title = cleanString(request.data?.title);
  const description = cleanString(request.data?.description);
  const currencyCode = cleanString(
    request.data?.currencyCode
  ).toUpperCase();

  const price = Number(request.data?.price);

  if (!productId) {
    throw new HttpsError(
      "invalid-argument",
      "معرف المنتج غير صحيح"
    );
  }

  if (!title) {
    throw new HttpsError(
      "invalid-argument",
      "اسم المنتج مطلوب"
    );
  }

  if (!validPrice(price)) {
    throw new HttpsError(
      "invalid-argument",
      "السعر غير صحيح"
    );
  }

  if (!/^[A-Z]{3}$/.test(currencyCode)) {
    throw new HttpsError(
      "invalid-argument",
      "رمز العملة غير صحيح"
    );
  }

  const productRef = db
    .collection("products")
    .doc(productId);

  const productSnap = await productRef.get();

  if (!productSnap.exists) {
    throw new HttpsError(
      "not-found",
      "المنتج غير موجود"
    );
  }

  const product = productSnap.data();

  if (product?.sellerId !== uid) {
    throw new HttpsError(
      "permission-denied",
      "هذا المنتج ليس ملكك"
    );
  }

  await productRef.update({
    title,
    description,
    originalPrice: price,
    originalCurrency: currencyCode,
    updatedAt: FieldValue.serverTimestamp()
  });

  return {
    success: true
  };
});


/**
 * تعطيل المنتج
 */
export const deactivateProduct = onCall(async (request) => {
  const uid = requireAuth(request);

  const productId = cleanString(
    request.data?.productId
  );

  if (!productId) {
    throw new HttpsError(
      "invalid-argument",
      "معرف المنتج غير صحيح"
    );
  }

  const productRef = db
    .collection("products")
    .doc(productId);

  const productSnap = await productRef.get();

  if (!productSnap.exists) {
    throw new HttpsError(
      "not-found",
      "المنتج غير موجود"
    );
  }

  const product = productSnap.data();

  if (product?.sellerId !== uid) {
    throw new HttpsError(
      "permission-denied",
      "هذا المنتج ليس ملكك"
    );
  }

  await productRef.update({
    active: false,
    updatedAt: FieldValue.serverTimestamp()
  });

  return {
    success: true
  };
});
