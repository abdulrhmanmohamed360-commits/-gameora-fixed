import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { getAuth } from "firebase-admin/auth";

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

function validateAmount(amount: unknown): number {
  if (typeof amount !== "number" || !Number.isFinite(amount)) {
    throw new HttpsError(
      "invalid-argument",
      "المبلغ غير صحيح"
    );
  }

  if (amount <= 0) {
    throw new HttpsError(
      "invalid-argument",
      "يجب أن يكون المبلغ أكبر من صفر"
    );
  }

  if (amount > 1000000) {
    throw new HttpsError(
      "invalid-argument",
      "المبلغ أكبر من الحد المسموح"
    );
  }

  return Math.round(amount * 100) / 100;
}

/**
 * إنشاء طلب إيداع.
 *
 * مهم:
 * هذه الدالة لا تضيف الرصيد مباشرة.
 * يتم إنشاء Transaction بحالة PENDING فقط.
 * تأكيد الدفع وإضافة الرصيد يتم لاحقًا من Backend موثوق.
 */
export const requestWalletDeposit = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = requireAuth(request);

    const amount = validateAmount(request.data?.amount);

    const currencyCode =
      typeof request.data?.currencyCode === "string"
        ? request.data.currencyCode.trim().toUpperCase()
        : "";

    const paymentMethodToken =
      typeof request.data?.paymentMethodToken === "string"
        ? request.data.paymentMethodToken.trim()
        : "";

    if (!currencyCode) {
      throw new HttpsError(
        "invalid-argument",
        "عملة الدفع غير محددة"
      );
    }

    const allowedMethods = new Set([
      "VODAFONE_CASH",
      "ORANGE_CASH",
      "ETISALAT_CASH",
      "INSTAPAY",
      "BANK_CARD",
    ]);

    if (!allowedMethods.has(paymentMethodToken)) {
      throw new HttpsError(
        "invalid-argument",
        "طريقة الدفع غير مدعومة"
      );
    }

    const user = await getAuth().getUser(uid);

    const walletRef = db
      .collection("wallets")
      .doc(uid);

    const transactionRef = walletRef
      .collection("transactions")
      .doc();

    await db.runTransaction(async (transaction) => {
      const walletSnapshot =
        await transaction.get(walletRef);

      if (!walletSnapshot.exists) {
        transaction.set(walletRef, {
          uid,
          balance: 0,
          currencyCode,
          updatedAt: FieldValue.serverTimestamp(),
        });
      }

      transaction.set(transactionRef, {
        id: transactionRef.id,
        uid,
        type: "DEPOSIT",
        amount,
        currencyCode,
        relatedOrderId: null,
        note: `طلب إيداع عبر ${paymentMethodToken}`,
        paymentMethod: paymentMethodToken,
        status: "PENDING",
        createdAt: FieldValue.serverTimestamp(),
      });
    });

    return {
      success: true,
      transactionId: transactionRef.id,
      status: "PENDING",
      message: "تم إنشاء طلب الإيداع",
      email: user.email ?? null,
    };
  }
);

/**
 * إنشاء طلب سحب.
 *
 * لا يتم خصم الرصيد هنا بشكل مباشر.
 * الطلب يدخل PENDING ويقوم Backend بمعالجته بعد التحقق.
 */
export const requestWalletWithdrawal = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = requireAuth(request);

    const amount = validateAmount(request.data?.amount);

    const currencyCode =
      typeof request.data?.currencyCode === "string"
        ? request.data.currencyCode.trim().toUpperCase()
        : "";

    const payoutDetails =
      request.data?.payoutDetails;

    if (!currencyCode) {
      throw new HttpsError(
        "invalid-argument",
        "عملة السحب غير محددة"
      );
    }

    if (
      !payoutDetails ||
      typeof payoutDetails !== "object"
    ) {
      throw new HttpsError(
        "invalid-argument",
        "بيانات السحب غير صحيحة"
      );
    }

    const walletRef = db
      .collection("wallets")
      .doc(uid);

    const transactionRef = walletRef
      .collection("transactions")
      .doc();

    await db.runTransaction(async (transaction) => {
      const walletSnapshot =
        await transaction.get(walletRef);

      if (!walletSnapshot.exists) {
        throw new HttpsError(
          "failed-precondition",
          "المحفظة غير موجودة"
        );
      }

      const walletData =
        walletSnapshot.data() ?? {};

      const currentBalance =
        Number(walletData.balance ?? 0);

      if (currentBalance < amount) {
        throw new HttpsError(
          "failed-precondition",
          "الرصيد غير كافٍ"
        );
      }

      transaction.set(transactionRef, {
        id: transactionRef.id,
        uid,
        type: "WITHDRAWAL",
        amount,
        currencyCode,
        payoutDetails,
        relatedOrderId: null,
        note: "طلب سحب",
        status: "PENDING",
        createdAt: FieldValue.serverTimestamp(),
      });
    });

    return {
      success: true,
      transactionId: transactionRef.id,
      status: "PENDING",
      message: "تم إنشاء طلب السحب",
    };
  }
);
