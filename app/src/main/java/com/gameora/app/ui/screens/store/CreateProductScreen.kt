package com.gameora.app.ui.screens.store

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gameora.app.data.model.ProductCategory
import com.gameora.app.util.CountryCurrency

@Composable
fun CreateProductScreen(
    onBack: () -> Unit,
    onProductCreated: () -> Unit,
    viewModel: CreateProductViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // -----------------------------
    // بيانات الإعلان
    // -----------------------------

    var selectedGameId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // -----------------------------
    // بيانات الحساب
    // -----------------------------

    var accountUsername by remember { mutableStateOf("") }
    var accountPassword by remember { mutableStateOf("") }

    var accountEmail by remember { mutableStateOf("") }
    var accountEmailPassword by remember { mutableStateOf("") }

    var accountLevel by remember { mutableStateOf("") }
    var accountRank by remember { mutableStateOf("") }
    var accountCoins by remember { mutableStateOf("") }
    var accountServer by remember { mutableStateOf("") }

    // -----------------------------
    // البيع
    // -----------------------------

    var price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("EGP") }
    var stock by remember { mutableStateOf("1") }

    var category by remember {
        mutableStateOf(ProductCategory.ACCOUNT)
    }

    var imageUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    // -----------------------------
    // Menus
    // -----------------------------

    var gameMenuExpanded by remember {
        mutableStateOf(false)
    }

    var currencyMenuExpanded by remember {
        mutableStateOf(false)
    }

    var categoryMenuExpanded by remember {
        mutableStateOf(false)
    }

    // -----------------------------
    // Image Picker
    // -----------------------------

    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris ->

            // نضيف على الصور المختارة سابقًا بدل استبدالها بالكامل
            imageUris = (imageUris + uris).distinct().take(6)
        }

    // -----------------------------
    // Success
    // -----------------------------

    LaunchedEffect(uiState.success) {

        if (uiState.success) {
            onProductCreated()
            viewModel.resetSuccess()
        }
    }

    // -----------------------------
    // Default Game
    // -----------------------------

    LaunchedEffect(uiState.games) {

        if (
            selectedGameId.isBlank() &&
            uiState.games.isNotEmpty()
        ) {

            selectedGameId =
                uiState.games.first().id
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("رفع حساب للبيع")
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

        Column(

            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            // =================================================
            // صور الحساب
            // =================================================

            Text(
                text = "صور الحساب (${imageUris.size}/6)",
                style = MaterialTheme.typography.titleMedium
            )

            if (imageUris.isEmpty()) {

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            imagePicker.launch("image/*")
                        },

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null
                        )

                        Text(
                            "اضغط لاختيار صورة أو أكثر للحساب"
                        )
                    }
                }

            } else {

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = imageUris,
                        key = { it.toString() }
                    ) { uri ->

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                        ) {

                            AsyncImage(
                                model = uri,
                                contentDescription = "صورة الحساب",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                            )

                            IconButton(
                                onClick = {
                                    imageUris = imageUris - uri
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "حذف الصورة",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (imageUris.size < 6) {

                        item {

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        imagePicker.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    Icons.Filled.Image,
                                    contentDescription = "إضافة صورة"
                                )
                            }
                        }
                    }
                }
            }

            // =================================================
            // اللعبة
            // =================================================

            ExposedDropdownMenuBox(

                expanded = gameMenuExpanded,

                onExpandedChange = {
                    gameMenuExpanded = it
                }
            ) {

                OutlinedTextField(

                    readOnly = true,

                    value =
                        uiState.games
                            .firstOrNull {
                                it.id == selectedGameId
                            }
                            ?.nameAr
                            ?.ifBlank {
                                uiState.games
                                    .firstOrNull {
                                        it.id == selectedGameId
                                    }
                                    ?.name
                                    ?: ""
                            }
                            ?: "اختر اللعبة",

                    onValueChange = {},

                    label = {
                        Text("اللعبة")
                    },

                    modifier =
                        Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                )

                ExposedDropdownMenu(

                    expanded = gameMenuExpanded,

                    onDismissRequest = {
                        gameMenuExpanded = false
                    }
                ) {

                    uiState.games.forEach { game ->

                        DropdownMenuItem(

                            text = {
                                Text(
                                    game.nameAr
                                        .ifBlank {
                                            game.name
                                        }
                                )
                            },

                            onClick = {

                                selectedGameId =
                                    game.id

                                gameMenuExpanded =
                                    false
                            }
                        )
                    }
                }
            }

            // =================================================
            // اسم الإعلان
            // =================================================

            OutlinedTextField(

                value = title,

                onValueChange = {
                    title = it
                },

                label = {
                    Text("عنوان الإعلان")
                },

                placeholder = {
                    Text("مثال: حساب Free Fire لفل 70")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            // =================================================
            // بيانات الدخول
            // =================================================

            Text(
                text = "بيانات الدخول",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "بيانات الدخول لن تظهر في الإعلان.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(

                value = accountUsername,

                onValueChange = {
                    accountUsername = it
                },

                label = {
                    Text("اسم المستخدم / ID")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountPassword,

                onValueChange = {
                    accountPassword = it
                },

                label = {
                    Text("كلمة سر الحساب")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountEmail,

                onValueChange = {
                    accountEmail = it
                },

                label = {
                    Text("إيميل الحساب")
                },

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email
                    ),

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountEmailPassword,

                onValueChange = {
                    accountEmailPassword = it
                },

                label = {
                    Text("كلمة سر الإيميل")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            // =================================================
            // مواصفات الحساب
            // =================================================

            Text(
                text = "مواصفات الحساب",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(

                value = accountLevel,

                onValueChange = {
                    accountLevel = it
                },

                label = {
                    Text("مستوى الحساب")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountRank,

                onValueChange = {
                    accountRank = it
                },

                label = {
                    Text("الرانك")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountCoins,

                onValueChange = {
                    accountCoins = it
                },

                label = {
                    Text("الجواهر / العملات")
                },

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = accountServer,

                onValueChange = {
                    accountServer = it
                },

                label = {
                    Text("السيرفر / المنطقة")
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            // =================================================
            // الوصف
            // =================================================

            OutlinedTextField(

                value = description,

                onValueChange = {
                    description = it
                },

                label = {
                    Text("تفاصيل الحساب")
                },

                placeholder = {
                    Text(
                        "اكتب تفاصيل الحساب والمميزات الموجودة فيه"
                    )
                },

                minLines = 4,

                modifier =
                    Modifier.fillMaxWidth()
            )

            // =================================================
            // السعر والعملة
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(

                    value = price,

                    onValueChange = {
                        price = it
                    },

                    label = {
                        Text("السعر")
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),

                    singleLine = true,

                    modifier =
                        Modifier.weight(1f)
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                ExposedDropdownMenuBox(

                    expanded =
                        currencyMenuExpanded,

                    onExpandedChange = {
                        currencyMenuExpanded =
                            it
                    },

                    modifier =
                        Modifier.weight(1f)
                ) {

                    OutlinedTextField(

                        readOnly = true,

                        value = currency,

                        onValueChange = {},

                        label = {
                            Text("العملة")
                        },

                        modifier =
                            Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                    )

                    ExposedDropdownMenu(

                        expanded =
                            currencyMenuExpanded,

                        onDismissRequest = {
                            currencyMenuExpanded =
                                false
                        }
                    ) {

                        CountryCurrency
                            .supportedCountries
                            .map {
                                it.currencyCode
                            }
                            .distinct()
                            .forEach { code ->

                                DropdownMenuItem(

                                    text = {
                                        Text(code)
                                    },

                                    onClick = {

                                        currency =
                                            code

                                        currencyMenuExpanded =
                                            false
                                    }
                                )
                            }
                    }
                }
            }

            // =================================================
            // النوع والكمية
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                ExposedDropdownMenuBox(

                    expanded =
                        categoryMenuExpanded,

                    onExpandedChange = {
                        categoryMenuExpanded =
                            it
                    },

                    modifier =
                        Modifier.weight(1f)
                ) {

                    OutlinedTextField(

                        readOnly = true,

                        value = category.name,

                        onValueChange = {},

                        label = {
                            Text("النوع")
                        },

                        modifier =
                            Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                    )

                    ExposedDropdownMenu(

                        expanded =
                            categoryMenuExpanded,

                        onDismissRequest = {
                            categoryMenuExpanded =
                                false
                        }
                    ) {

                        ProductCategory
                            .entries
                            .forEach { cat ->

                                DropdownMenuItem(

                                    text = {
                                        Text(cat.name)
                                    },

                                    onClick = {

                                        category =
                                            cat

                                        categoryMenuExpanded =
                                            false
                                    }
                                )
                            }
                    }
                }

                Spacer(
                    Modifier.width(8.dp)
                )

                OutlinedTextField(

                    value = stock,

                    onValueChange = {
                        stock = it
                    },

                    label = {
                        Text("الكمية")
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    singleLine = true,

                    modifier =
                        Modifier.weight(1f)
                )
            }

            // =================================================
            // رسالة الخطأ
            // =================================================

            uiState.errorMessage?.let { error ->

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // =================================================
            // زر النشر
            // =================================================

            Spacer(
                Modifier.height(8.dp)
            )

            Button(

                onClick = {

                    viewModel.submit(
                        gameId = selectedGameId,
                        title = title,
                        description = description,
                        imageUris = imageUris,
                        price = price.toDoubleOrNull() ?: 0.0,
                        currencyCode = currency,
                        category = category,
                        stock = stock.toIntOrNull() ?: 1,
                        accountUsername = accountUsername,
                        accountPassword = accountPassword,
                        accountEmail = accountEmail,
                        accountEmailPassword = accountEmailPassword,
                        accountLevel = accountLevel,
                        accountRank = accountRank,
                        accountCoins = accountCoins,
                        accountServer = accountServer
                    )
                },

                enabled = !uiState.isSubmitting,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)

            ) {

                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("نشر الإعلان")
                }
            }

            Spacer(
                Modifier.height(24.dp)
            )
        }
    }
}
