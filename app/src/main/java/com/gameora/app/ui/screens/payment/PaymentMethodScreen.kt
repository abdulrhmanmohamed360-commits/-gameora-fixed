package com.gameora.app.ui.screens.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class PaymentMethod(
val title: String,
val description: String,
val icon: ImageVector
) {
VODAFONE_CASH(
title = "Vodafone Cash",
description = "الدفع عن طريق محفظة Vodafone Cash",
icon = Icons.Default.PhoneAndroid
),

ORANGE_CASH(
    title = "Orange Cash",
    description = "الدفع عن طريق محفظة Orange Cash",
    icon = Icons.Default.PhoneAndroid
),

ETISALAT_CASH(
    title = "Etisalat Cash",
    description = "الدفع عن طريق محفظة Etisalat Cash",
    icon = Icons.Default.PhoneAndroid
),

INSTAPAY(
    title = "InstaPay",
    description = "الدفع والتحويل عن طريق InstaPay",
    icon = Icons.Default.AccountBalance
),

BANK_CARD(
    title = "بطاقة بنكية",
    description = "Visa أو Mastercard",
    icon = Icons.Default.CreditCard
)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
amount: Double,
currencyCode: String,
onBack: () -> Unit,
onContinue: (PaymentMethod, Double, String) -> Unit
) {
var selectedMethod by remember {
mutableStateOf<PaymentMethod?>(null)
}

Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "اختيار طريقة الدفع",
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "شحن المحفظة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "اختر الطريقة المناسبة لإتمام عملية الدفع",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المبلغ المطلوب",
                        style =
                            MaterialTheme.typography.labelLarge
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = formatPaymentAmount(
                            amount,
                            currencyCode
                        ),
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        PaymentMethod.entries.forEach { method ->

            PaymentMethodCard(
                method = method,
                selected = selectedMethod == method,
                onClick = {
                    selectedMethod = method
                }
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                selectedMethod?.let { method ->
                    onContinue(
                        method,
                        amount,
                        currencyCode
                    )
                }
            },
            enabled = selectedMethod != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "متابعة الدفع",
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("إلغاء")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}

}

@Composable
private fun PaymentMethodCard(
method: PaymentMethod,
selected: Boolean,
onClick: () -> Unit
) {
Card(
modifier = Modifier
.fillMaxWidth()
.clickable(
onClick = onClick
),
shape = RoundedCornerShape(18.dp),
colors = CardDefaults.cardColors(
containerColor =
if (selected) {
MaterialTheme.colorScheme.primaryContainer
} else {
MaterialTheme.colorScheme.surfaceVariant
}
),
border =
if (selected) {
androidx.compose.foundation.BorderStroke(
width = 2.dp,
color = MaterialTheme.colorScheme.primary
)
} else {
null
}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Icon(
                imageVector = method.icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(12.dp)
                    .size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(
            modifier = Modifier.size(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = method.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = method.description,
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

}

private fun formatPaymentAmount(
amount: Double,
currencyCode: String
): String {
return "%,.2f %s".format(
amount,
currencyCode.ifBlank { "USD" }
)
}
