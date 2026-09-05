package com.gameora.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameora.app.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onShowAccountData: () -> Unit = {},
    onReportProblem: () -> Unit = {},
    onOrderCompleted: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var messageText by remember { mutableStateOf("") }

    val currentUid = remember {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                uiState.messages.lastIndex
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المحادثة",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                text = messageText,
                isSending = uiState.isSending,
                onTextChange = {
                    messageText = it
                },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (uiState.errorMessage != null) {
                ErrorBanner(
                    message = uiState.errorMessage!!,
                    onDismiss = {
                        viewModel.clearError()
                    }
                )
            }

            OrderActions(
                onShowAccountData = onShowAccountData,
                onReportProblem = onReportProblem,
                onOrderCompleted = onOrderCompleted
            )

            HorizontalDivider()

            if (uiState.isLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 12.dp
                    )
                ) {

                    items(
                        items = uiState.messages,
                        key = { message ->
                            message.id
                        }
                    ) { message ->

                        MessageBubble(
                            message = message,
                            isMine = message.senderId == currentUid
                        )
                    }
                }
            }
        }
    }
}

/**
 * أزرار مراحل البيع.
 *
 * ملاحظة:
 * في النسخة الحالية الأزرار تستدعي callbacks.
 * بعد ذلك سنربطها مباشرة بـ Cloud Functions.
 */
@Composable
private fun OrderActions(
    onShowAccountData: () -> Unit,
    onReportProblem: () -> Unit,
    onOrderCompleted: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "عملية البيع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = onShowAccountData,
                modifier = Modifier.weight(1f)
            ) {
                Text("عرض بيانات الحساب")
            }

            OutlinedButton(
                onClick = onReportProblem,
                modifier = Modifier.weight(1f)
            ) {
                Text("بلاغ")
            }
        }

        Button(
            onClick = onOrderCompleted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تأكيد استلام الحساب")
        }
    }
}

/**
 * فقاعة الرسالة.
 */
@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {

        Surface(
            shape = RoundedCornerShape(16.dp),
            color =
                if (isMine) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            modifier = Modifier.widthIn(
                max = 300.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
            ) {

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = if (message.isRead) {
                        "تمت القراءة"
                    } else {
                        "تم الإرسال"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * إدخال الرسالة وإرسالها.
 */
@Composable
private fun MessageInput(
    text: String,
    isSending: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {

    Surface(
        tonalElevation = 3.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("اكتب رسالة...")
                },
                maxLines = 4
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending
            ) {

                if (isSending) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال"
                    )
                }
            }
        }
    }
}

/**
 * رسالة الخطأ.
 */
@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            TextButton(
                onClick = onDismiss
            ) {
                Text("إغلاق")
            }
        }
    }
}