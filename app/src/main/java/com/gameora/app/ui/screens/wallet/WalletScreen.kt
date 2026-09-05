package com.gameora.app.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gameora.app.data.model.TransactionType
import com.gameora.app.data.model.Wallet
import com.gameora.app.data.model.WalletTransaction
import com.gameora.app.util.ServiceLocator

@Composable
fun WalletScreen(
    onTopUp: () -> Unit = {},
    onTransfer: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    var wallet by remember { mutableStateOf<Wallet?>(null) }
    var transactions by remember {
        mutableStateOf<List<WalletTransaction>>(emptyList())
    }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val uid = ServiceLocator.authRepository.currentUid

    suspend fun loadWallet() {
        if (uid == null) {
            loading = false
            error = "يجب تسجيل الدخول أولاً"
            return
        }

        loading = true
        error = null

        ServiceLocator.walletRepository
            .fetchWallet(uid)
            .onSuccess {
                wallet = it
            }
            .onFailure {
                error = it.message ?: "تعذر تحميل المحفظة"
            }

        ServiceLocator.walletRepository
            .fetchTransactions(uid)
            .onSuccess {
                transactions = it
            }
            .onFailure {
                error = it.message ?: "تعذر تحميل سجل العمليات"
            }

        loading = false
    }

    LaunchedEffect(uid) {
        loadWallet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "المحفظة",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            kotlinx.coroutines.MainScope().launch {
                                loadWallet()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "تحديث"
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "الرصيد المتاح",
                                    style =
                                        MaterialTheme.typography.labelLarge
                                )

                                Spacer(
                                    Modifier.height(6.dp)
                                )

                                Text(
                                    formatMoney(
                                        wallet?.balance ?: 0.0,
                                        wallet?.currencyCode ?: "EGP"
                                    ),
                                    style =
                                        MaterialTheme.typography.headlineMedium,
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )
                            }

                            Icon(
                                Icons.Default.Wallet,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = onTopUp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("شحن")
                    }

                    Button(
                        onClick = onTransfer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("تحويل")
                    }

                    Button(
                        onClick = onWithdraw,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("سحب")
                    }
                }
            }

            if (error != null) {
                item {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Text(
                    "آخر العمليات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "لا توجد عمليات حتى الآن",
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(
                transactions,
                key = { it.id }
            ) { transaction ->
                TransactionCard(transaction)
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: WalletTransaction
) {
    val icon = when (transaction.type) {
        TransactionType.DEPOSIT,
        TransactionType.SALE_PAYOUT,
        TransactionType.REFUND ->
            Icons.Default.ArrowDownward

        TransactionType.PURCHASE,
        TransactionType.WITHDRAWAL ->
            Icons.Default.ArrowUpward
    }

    val prefix = when (transaction.type) {
        TransactionType.DEPOSIT,
        TransactionType.SALE_PAYOUT,
        TransactionType.REFUND -> "+"

        TransactionType.PURCHASE,
        TransactionType.WITHDRAWAL -> "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    transaction.type.name,
                    fontWeight = FontWeight.Bold
                )

                if (transaction.note.isNotBlank()) {
                    Text(
                        transaction.note,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "$prefix${formatMoney(transaction.amount, transaction.currencyCode)}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatMoney(
    amount: Double,
    currency: String
): String {
    return "%,.2f %s".format(
        amount,
        currency.ifBlank { "EGP" }
    )
}
