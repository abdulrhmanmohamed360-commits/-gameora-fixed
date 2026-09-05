package com.gameora.app.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun WalletTopUpScreen(
    currencyCode: String,
    onBack: () -> Unit,
    onContinue: (Double, String) -> Unit
) {

    var amountText by remember {
        mutableStateOf("")
    }

    val amount = amountText.toDoubleOrNull()

    val isValidAmount =
        amount != null && amount > 0.0

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "شحن المحفظة",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Icon(
                imageVector =
                    Icons.Default.AccountBalanceWallet,

                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),

                tint =
                    MaterialTheme.colorScheme.primary
            )

            Text(
                text = "أدخل المبلغ الذي تريد شحنه",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text =
                    "بعد إدخال المبلغ ستتمكن من اختيار طريقة الدفع المناسبة.",
                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(

                value = amountText,

                onValueChange = { value ->

                    // السماح بالأرقام والعلامة العشرية فقط
                    if (
                        value.isEmpty() ||
                        value.matches(
                            Regex("^\\d*\\.?\\d{0,2}$")
                        )
                    ) {
                        amountText = value
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("المبلغ")
                },

                placeholder = {
                    Text("مثال: 100")
                },

                suffix = {
                    Text(
                        currencyCode.ifBlank {
                            "USD"
                        }
                    )
                },

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),

                shape =
                    RoundedCornerShape(16.dp)
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(

                onClick = {

                    if (isValidAmount) {

                        onContinue(
                            amount!!,
                            currencyCode
                        )
                    }
                },

                enabled = isValidAmount,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "اختيار طريقة الدفع",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}
