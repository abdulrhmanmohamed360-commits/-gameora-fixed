package com.gameora.app.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletWithdrawScreen(
    onBack: () -> Unit,
    onWithdraw: (
        Double,
        Map<String, String>
    ) -> Unit = { _, _ -> }
) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "سحب الرصيد",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("مبلغ السحب")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = method,
                onValueChange = {
                    method = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("طريقة السحب")
                },
                placeholder = {
                    Text("Vodafone Cash / InstaPay / بنك")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = account,
                onValueChange = {
                    account = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("رقم المحفظة أو الحساب")
                },
                singleLine = true
            )

            Button(
                onClick = {
                    val value =
                        amount.toDoubleOrNull()

                    if (
                        value != null &&
                        value > 0 &&
                        method.isNotBlank() &&
                        account.isNotBlank()
                    ) {
                        onWithdraw(
                            value,
                            mapOf(
                                "method" to method.trim(),
                                "account" to account.trim()
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    "طلب السحب",
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "ملاحظة: تنفيذ السحب وتغيير الرصيد يتم من الـ Backend فقط.",
                fontWeight = FontWeight.Medium
            )
        }
    }
}
