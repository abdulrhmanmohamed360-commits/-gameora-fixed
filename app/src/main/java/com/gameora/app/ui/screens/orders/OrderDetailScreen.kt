package com.gameora.app.ui.screens.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameora.app.data.model.Order
import com.gameora.app.data.model.OrderStatus
import com.gameora.app.ui.components.OrderStatusChip
import com.gameora.app.util.ServiceLocator
import java.util.concurrent.TimeUnit

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit = {},
    viewModel: OrderDetailViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order

    var showRejectDialog by remember {
        mutableStateOf(false)
    }

    var showDisputeDialog by remember {
        mutableStateOf(false)
    }

    val currentUid =
        ServiceLocator.authRepository.currentUid

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("تفاصيل الطلب")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }

    ) { padding ->

        when {

            // =================================================
            // Loading
            // =================================================

            uiState.isLoading -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }


            // =================================================
            // Error
            // =================================================

            order == null -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            uiState.errorMessage
                                ?: "الطلب غير موجود"
                        )

                        Spacer(
                            Modifier.height(12.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.loadOrder()
                            }
                        ) {

                            Text("إعادة المحاولة")
                        }
                    }
                }
            }


            // =================================================
            // Order
            // =================================================

            else -> {

                OrderContent(

                    order = order,

                    currentUid = currentUid,

                    remainingMillis =
                        uiState.remainingApprovalMillis,

                    isProcessing =
                        uiState.isProcessing,

                    errorMessage =
                        uiState.errorMessage,

                    successMessage =
                        uiState.successMessage,

                    onAccept = {
                        viewModel.acceptOrder()
                    },

                    onReject = {
                        showRejectDialog = true
                    },

                    onOpenChat = {

                        val chatId =
                            viewModel.getChatId()

                        if (!chatId.isNullOrBlank()) {
                            onOpenChat(chatId)
                        }
                    },

                    onStartDelivery = {
                        viewModel.startDelivery()
                    },

                    onDeliverAccount = {
                        viewModel.deliverAccount()
                    },

                    onConfirmAccount = {
                        viewModel.confirmAccount()
                    },

                    onOpenDispute = {
                        showDisputeDialog = true
                    },

                    modifier =
                        Modifier.padding(padding)
                )
            }
        }
    }


    // =========================================================
    // Reject Dialog
    // =========================================================

    if (showRejectDialog) {

        RejectOrderDialog(

            isProcessing =
                uiState.isProcessing,

            onDismiss = {
                showRejectDialog = false
            },

            onConfirm = { reason ->

                showRejectDialog = false

                viewModel.rejectOrder(reason)
            }
        )
    }


    // =========================================================
    // Dispute Dialog
    // =========================================================

    if (showDisputeDialog) {

        DisputeDialog(

            isProcessing =
                uiState.isProcessing,

            onDismiss = {
                showDisputeDialog = false
            },

            onConfirm = { reason ->

                showDisputeDialog = false

                viewModel.openDispute(reason)
            }
        )
    }
}


// =============================================================
// Order Content
// =============================================================

@Composable
private fun OrderContent(

    order: Order,

    currentUid: String?,

    remainingMillis: Long?,

    isProcessing: Boolean,

    errorMessage: String?,

    successMessage: String?,

    onAccept: () -> Unit,

    onReject: () -> Unit,

    onOpenChat: () -> Unit,

    onStartDelivery: () -> Unit,

    onDeliverAccount: () -> Unit,

    onConfirmAccount: () -> Unit,

    onOpenDispute: () -> Unit,

    modifier: Modifier = Modifier
) {

    val isSeller =
        currentUid == order.sellerId

    val isBuyer =
        currentUid == order.buyerId


    Column(

        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp)
    ) {


        // =====================================================
        // Product
        // =====================================================

        Text(

            text =
                order.productTitle,

            style =
                MaterialTheme.typography.headlineSmall,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(8.dp)
        )


        OrderStatusChip(
            status =
                order.status
        )


        Spacer(
            Modifier.height(20.dp)
        )


        DetailRow(
            "رقم الطلب",
            order.id
        )

        DetailRow(
            "الكمية",
            order.quantity.toString()
        )

        DetailRow(
            "السعر الأصلي",

            "%,.2f %s".format(
                order.originalPrice,
                order.originalCurrency
            )
        )

        DetailRow(
            "المبلغ المحجوز",

            "%,.2f %s".format(
                order.chargedAmount,
                order.chargedCurrency
            )
        )


        Spacer(
            Modifier.height(24.dp)
        )


        // =====================================================
        // Seller Approval Timer
        // =====================================================

        if (

            order.status ==
                OrderStatus.PENDING_SELLER_APPROVAL

            &&

            remainingMillis != null

        ) {

            ApprovalTimerCard(
                remainingMillis =
                    remainingMillis
            )

            Spacer(
                Modifier.height(16.dp)
            )
        }


        // =====================================================
        // Escrow
        // =====================================================

        EscrowCard(
            order = order
        )


        Spacer(
            Modifier.height(20.dp)
        )


        // =====================================================
        // Success
        // =====================================================

        successMessage?.let {

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = it,

                    modifier =
                        Modifier.padding(16.dp)
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )
        }


        // =====================================================
        // Error
        // =====================================================

        errorMessage?.let {

            Text(

                text = it,

                color =
                    MaterialTheme.colorScheme.error,

                modifier =
                    Modifier.padding(
                        bottom = 12.dp
                    )
            )
        }


        // =====================================================
        // Seller
        // =====================================================

        if (isSeller) {

            SellerActions(

                order = order,

                isProcessing =
                    isProcessing,

                onAccept =
                    onAccept,

                onReject =
                    onReject,

                onOpenChat =
                    onOpenChat,

                onStartDelivery =
                    onStartDelivery,

                onDeliverAccount =
                    onDeliverAccount
            )
        }


        // =====================================================
        // Buyer
        // =====================================================

        if (isBuyer) {

            BuyerActions(

                order = order,

                isProcessing =
                    isProcessing,

                onOpenChat =
                    onOpenChat,

                onConfirmAccount =
                    onConfirmAccount,

                onOpenDispute =
                    onOpenDispute
            )
        }
    }
}


// =============================================================
// Seller Actions
// =============================================================

@Composable
private fun SellerActions(

    order: Order,

    isProcessing: Boolean,

    onAccept: () -> Unit,

    onReject: () -> Unit,

    onOpenChat: () -> Unit,

    onStartDelivery: () -> Unit,

    onDeliverAccount: () -> Unit
) {

    Text(

        "إدارة البيع",

        style =
            MaterialTheme.typography.titleLarge,

        fontWeight =
            FontWeight.Bold
    )


    Spacer(
        Modifier.height(12.dp)
    )


    // =========================================================
    // Pending Approval
    // =========================================================

    if (
        order.status ==
            OrderStatus.PENDING_SELLER_APPROVAL
    ) {

        Button(

            onClick =
                onAccept,

            enabled =
                !isProcessing,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)

        ) {

            Text(
                "✅ قبول طلب البيع"
            )
        }


        Spacer(
            Modifier.height(10.dp)
        )


        OutlinedButton(

            onClick =
                onReject,

            enabled =
                !isProcessing,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)

        ) {

            Text(
                "❌ رفض الطلب"
            )
        }
    }


    // =========================================================
    // Accepted / Chat
    // =========================================================

    if (

        order.status ==
            OrderStatus.SELLER_ACCEPTED

        ||

        order.status ==
            OrderStatus.CHAT_ACTIVE

    ) {

        OutlinedButton(

            onClick =
                onOpenChat,

            enabled =
                !isProcessing,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Icon(
                Icons.Filled.Chat,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                "فتح المحادثة"
            )
        }


        Spacer(
            Modifier.height(10.dp)
        )


        Button(

            onClick =
                onStartDelivery,

            enabled =
                !isProcessing,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(
                "بدء تسليم الحساب"
            )
        }
    }


    // =========================================================
    // Deliver Account
    // =========================================================

    if (
        order.status ==
            OrderStatus.CHAT_ACTIVE
    ) {

        Spacer(
            Modifier.height(10.dp)
        )


        Button(

            onClick =
                onDeliverAccount,

            enabled =
                !isProcessing,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(
                "🔐 تسليم بيانات الحساب"
            )
        }
    }


    // =========================================================
    // Account Delivered
    // =========================================================

    if (

        order.status ==
            OrderStatus.ACCOUNT_DELIVERED

        ||

        order.status ==
            OrderStatus.BUYER_TESTING

    ) {

        OutlinedButton(

            onClick =
                onOpenChat,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Icon(
                Icons.Filled.Chat,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                "💬 متابعة المحادثة"
            )
        }
    }
}


// =============================================================
// Buyer Actions
// =============================================================

@Composable
private fun BuyerActions(

    order: Order,

    isProcessing: Boolean,

    onOpenChat: () -> Unit,

    onConfirmAccount: () -> Unit,

    onOpenDispute: () -> Unit
) {

    Text(

        "عملية الشراء",

        style =
            MaterialTheme.typography.titleLarge,

        fontWeight =
            FontWeight.Bold
    )


    Spacer(
        Modifier.height(12.dp)
    )


    // =========================================================
    // Waiting Seller
    // =========================================================

    if (

        order.status ==
            OrderStatus.PENDING_SELLER_APPROVAL

    ) {

        Text(
            "ننتظر موافقة البائع. " +
                "سيتم إعادة المبلغ تلقائيًا " +
                "إذا انتهت المهلة بدون موافقة."
        )
    }


    // =========================================================
    // Chat
    // =========================================================

    if (

        order.status ==
            OrderStatus.SELLER_ACCEPTED

        ||

        order.status ==
            OrderStatus.CHAT_ACTIVE

        ||

        order.status ==
            OrderStatus.ACCOUNT_DELIVERED

        ||

        order.status ==
            OrderStatus.BUYER_TESTING

    ) {

        Button(

            onClick =
                onOpenChat,

            enabled =
                !isProcessing,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)

        ) {

            Icon(
                Icons.Filled.Chat,
                contentDescription = null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                "💬 فتح المحادثة"
            )
        }
    }


    // =========================================================
    // Confirm Account
    // =========================================================

    if (

        order.status ==
            OrderStatus.ACCOUNT_DELIVERED

        ||

        order.status ==
            OrderStatus.BUYER_TESTING

    ) {

        Spacer(
            Modifier.height(12.dp)
        )


        Button(

            onClick =
                onConfirmAccount,

            enabled =
                !isProcessing,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)

        ) {

            Text(
                "✅ الحساب يعمل - تأكيد الاستلام"
            )
        }


        Spacer(
            Modifier.height(10.dp)
        )


        OutlinedButton(

            onClick =
                onOpenDispute,

            enabled =
                !isProcessing,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(
                "🚨 توجد مشكلة - فتح بلاغ"
            )
        }
    }
}


// =============================================================
// Approval Timer
// =============================================================

@Composable
private fun ApprovalTimerCard(
    remainingMillis: Long
) {

    val safeMillis =
        remainingMillis.coerceAtLeast(0L)


    val totalSeconds =
        TimeUnit.MILLISECONDS
            .toSeconds(safeMillis)


    val hours =
        totalSeconds / 3600


    val minutes =
        (totalSeconds % 3600) / 60


    val seconds =
        totalSeconds % 60


    val timeText =

        if (hours > 0) {

            "%02d:%02d:%02d".format(
                hours,
                minutes,
                seconds
            )

        } else {

            "%02d:%02d".format(
                minutes,
                seconds
            )
        }


    Card(

        modifier =
            Modifier.fillMaxWidth()

    ) {

        Column(

            modifier =
                Modifier.padding(16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {

            Text(
                "⏱️ وقت انتظار موافقة البائع",
                style =
                    MaterialTheme.typography.titleMedium
            )


            Spacer(
                Modifier.height(8.dp)
            )


            Text(

                timeText,

                style =
                    MaterialTheme.typography.headlineMedium,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// =============================================================
// Detail Row
// =============================================================

@Composable
private fun DetailRow(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}


// =============================================================
// Escrow Card
// =============================================================

@Composable
private fun EscrowCard(
    order: Order
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                "🔒 حالة الأمانة (Escrow)",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(10.dp)
            )

            DetailRow(
                "حالة الأموال",
                if (order.fundsReleased) {
                    "تم تحريرها للبائع"
                } else if (order.fundsHeld) {
                    "محجوزة بأمان"
                } else {
                    "غير محجوزة"
                }
            )

            if (order.refunded) {
                DetailRow(
                    "استرداد المبلغ",
                    "تم رد المبلغ للمشتري"
                )
            }

            if (order.hasDispute) {
                DetailRow(
                    "النزاع",
                    "يوجد بلاغ مفتوح على هذا الطلب"
                )
            }
        }
    }
}


// =============================================================
// Reject Order Dialog
// =============================================================

@Composable
private fun RejectOrderDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {

    var reason by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("رفض الطلب")
        },

        text = {
            Column {
                Text(
                    "من فضلك اذكر سبب رفض الطلب. " +
                        "سيتم إعادة المبلغ للمشتري تلقائيًا."
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الرفض") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = !isProcessing && reason.isNotBlank()
            ) {
                Text("تأكيد الرفض")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("إلغاء")
            }
        }
    )
}


// =============================================================
// Dispute Dialog
// =============================================================

@Composable
private fun DisputeDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {

    var reason by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("فتح بلاغ")
        },

        text = {
            Column {
                Text(
                    "اشرح المشكلة التي واجهتها مع هذا الطلب. " +
                        "سيتم مراجعة البلاغ من فريق Gameora."
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("تفاصيل المشكلة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = !isProcessing && reason.isNotBlank()
            ) {
                Text("إرسال البلاغ")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("إلغاء")
            }
        }
    )
}