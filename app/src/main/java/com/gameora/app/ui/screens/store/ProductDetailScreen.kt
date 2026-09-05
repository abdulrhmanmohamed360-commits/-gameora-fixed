package com.gameora.app.ui.screens.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gameora.app.ui.components.PriceTag

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onPurchaseComplete: (String) -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.purchaseSuccessOrderId) {
        uiState.purchaseSuccessOrderId?.let { orderId ->
            onPurchaseComplete(orderId)
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("تفاصيل الحساب")
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

    ) { paddingValues ->

        when {

            // =================================================
            // Loading
            // =================================================

            uiState.isLoading -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }

            // =================================================
            // Error / Product not found
            // =================================================

            uiState.product == null -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            uiState.errorMessage
                                ?: "الحساب غير موجود"
                        )

                        Spacer(
                            Modifier.height(12.dp)
                        )

                        OutlinedButton(
                            onClick = onBack
                        ) {

                            Text("رجوع")
                        }
                    }
                }
            }

            // =================================================
            // Product
            // =================================================

            else -> {

                val product =
                    uiState.product!!

                Column(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    // =================================================
                    // صور الحساب
                    // =================================================

                    if (product.imageUrls.size <= 1) {

                        AsyncImage(

                            model =
                                product.imageUrl,

                            contentDescription =
                                product.title,

                            contentScale =
                                ContentScale.Crop,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                        )

                    } else {

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            items(
                                items = product.imageUrls,
                                key = { it }
                            ) { url ->

                                AsyncImage(
                                    model = url,
                                    contentDescription = product.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .height(230.dp)
                                        .clip(RoundedCornerShape(0.dp))
                                )
                            }
                        }
                    }

                    Column(

                        modifier =
                            Modifier.padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        // =================================================
                        // العنوان
                        // =================================================

                        Text(

                            text =
                                product.title,

                            style =
                                MaterialTheme
                                    .typography
                                    .headlineMedium,

                            fontWeight =
                                FontWeight.Bold
                        )

                        // =================================================
                        // البائع
                        // =================================================

                        Text(

                            text =
                                buildString {

                                    append(
                                        "البائع: "
                                    )

                                    append(
                                        product.sellerName
                                    )

                                    product.sellerRating
                                        ?.let {

                                            append(
                                                " ⭐ "
                                            )

                                            append(
                                                "%.1f".format(
                                                    it
                                                )
                                            )
                                        }
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                                    .copy(
                                        alpha = 0.7f
                                    )
                        )

                        HorizontalDivider()

                        // =================================================
                        // مواصفات الحساب
                        // =================================================

                        Text(

                            text =
                                "🎮 مواصفات الحساب",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Bold
                        )

                        AccountInfoRow(
                            label = "اللعبة",
                            value = product.gameId
                        )

                        if (
                            product.accountUsername
                                .isNotBlank()
                        ) {

                            AccountInfoRow(
                                label = "ID / Username",
                                value =
                                    product.accountUsername
                            )
                        }

                        if (
                            product.accountLevel
                                .isNotBlank()
                        ) {

                            AccountInfoRow(
                                label = "المستوى",
                                value =
                                    product.accountLevel
                            )
                        }

                        if (
                            product.accountRank
                                .isNotBlank()
                        ) {

                            AccountInfoRow(
                                label = "الرانك",
                                value =
                                    product.accountRank
                            )
                        }

                        if (
                            product.accountCoins
                                .isNotBlank()
                        ) {

                            AccountInfoRow(
                                label = "الجواهر / العملات",
                                value =
                                    product.accountCoins
                            )
                        }

                        if (
                            product.accountServer
                                .isNotBlank()
                        ) {

                            AccountInfoRow(
                                label = "السيرفر",
                                value =
                                    product.accountServer
                            )
                        }

                        // =================================================
                        // بيانات الدخول
                        // =================================================

                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(
                                        16.dp
                                    ),

                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        6.dp
                                    )
                            ) {

                                Text(

                                    text =
                                        "🔐 بيانات الدخول",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    "بيانات الدخول مخفية حاليًا."
                                )

                                Text(
                                    "سيتم تسليم بيانات الحساب للمشتري فقط بعد نجاح عملية الشراء.",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }
                        }

                        // =================================================
                        // السعر
                        // =================================================

                        uiState.displayPrice?.let {

                            PriceTag(

                                displayPrice =
                                    it,

                                originalPrice =
                                    product.originalPrice,

                                originalCurrency =
                                    product.originalCurrency
                            )
                        }

                        // =================================================
                        // الوصف
                        // =================================================

                        Text(

                            text =
                                "📝 تفاصيل الحساب",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(

                            text =
                                product.description,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )

                        // =================================================
                        // حالة الحساب
                        // =================================================

                        Text(

                            text =
                                if (
                                    product.isActive &&
                                    product.stockAvailable > 0
                                ) {
                                    "🟢 الحساب متاح للبيع"
                                } else {
                                    "🔴 الحساب غير متاح"
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                        // =================================================
                        // Error
                        // =================================================

                        uiState.errorMessage?.let {

                            Text(

                                text = it,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        }

                        // =================================================
                        // Purchase
                        // =================================================

                        Button(

                            onClick = {
                                viewModel.purchase()
                            },

                            enabled =
                                !uiState.isPurchasing &&
                                product.isActive &&
                                product.stockAvailable > 0,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                        ) {

                            if (
                                uiState.isPurchasing
                            ) {

                                CircularProgressIndicator(

                                    modifier =
                                        Modifier.size(
                                            22.dp
                                        ),

                                    strokeWidth = 2.dp
                                )

                            } else {

                                Text(

                                    if (
                                        product.isActive &&
                                        product.stockAvailable > 0
                                    ) {
                                        "🛒 شراء الحساب"
                                    } else {
                                        "غير متوفر حاليًا"
                                    }
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * صف لعرض مواصفة من مواصفات الحساب.
 *
 * لا يستخدم لعرض كلمات السر أو أي بيانات دخول حساسة.
 */
@Composable
private fun AccountInfoRow(
    label: String,
    value: String
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(

            text = label,

            fontWeight =
                FontWeight.SemiBold
        )

        Text(
            text = value
        )
    }
}