package com.gameora.app.data.repository

import com.gameora.app.data.model.Wallet
import com.gameora.app.data.model.WalletTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * مستودع المحفظة.
 * القراءة (الرصيد وسجل المعاملات) مباشرة من Firestore — لكنها Read-only بقواعد الأمان.
 * أي إضافة/خصم/سحب فعلي يمر إجباريًا عبر Cloud Functions حتى لا تُعتمد أي قيمة من العميل مباشرة.
 */
class WalletRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1")
) {
    fun walletDocument(uid: String) = firestore.collection("wallets").document(uid)

    suspend fun fetchWallet(uid: String): Result<Wallet?> = runCatching {
        walletDocument(uid).get().await().toObject(Wallet::class.java)
    }

    suspend fun fetchTransactions(uid: String): Result<List<WalletTransaction>> = runCatching {
        firestore.collection("wallets").document(uid).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(WalletTransaction::class.java)
    }

    /** طلب إيداع رصيد — ينشئ عملية Pending يتم تأكيدها من الـ Backend بعد التحقق من الدفع */
    suspend fun requestDeposit(amount: Double, currencyCode: String, paymentMethodToken: String): Result<Unit> =
        runCatching {
            val data = hashMapOf(
                "amount" to amount,
                "currencyCode" to currencyCode,
                "paymentMethodToken" to paymentMethodToken
            )
            functions.getHttpsCallable("requestWalletDeposit").call(data).await()
            Unit
        }

    /** طلب سحب رصيد */
    suspend fun requestWithdrawal(amount: Double, currencyCode: String, payoutDetails: Map<String, String>): Result<Unit> =
        runCatching {
            val data = hashMapOf(
                "amount" to amount,
                "currencyCode" to currencyCode,
                "payoutDetails" to payoutDetails
            )
            functions.getHttpsCallable("requestWalletWithdrawal").call(data).await()
            Unit
        }
}
