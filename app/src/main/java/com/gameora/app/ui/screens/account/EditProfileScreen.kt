package com.gameora.app.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameora.app.util.CountryCurrency

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    var displayName by remember(user) {
        mutableStateOf(user?.displayName ?: "")
    }

    var selectedCountry by remember(user) {
        mutableStateOf(
            CountryCurrency.supportedCountries
                .firstOrNull { it.code == user?.countryCode }
                ?: CountryCurrency.supportedCountries.first()
        )
    }

    var countryMenuExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تعديل الملف الشخصي") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (uiState.isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            ) {

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = countryMenuExpanded,
                    onExpandedChange = { countryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = "${selectedCountry.nameAr} (${selectedCountry.currencyCode})",
                        onValueChange = {},
                        label = { Text("الدولة") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false }
                    ) {
                        CountryCurrency.supportedCountries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${country.nameAr} (${country.currencyCode})") },
                                onClick = {
                                    selectedCountry = country
                                    countryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                errorMessage?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = null

                        if (displayName.isBlank()) {
                            errorMessage = "من فضلك اكتب الاسم"
                            return@Button
                        }

                        isSaving = true

                        viewModel.updateProfile(
                            displayName = displayName,
                            countryCode = selectedCountry.code,
                            currencyCode = selectedCountry.currencyCode,
                            onResult = { success, message ->
                                isSaving = false
                                if (success) {
                                    onBack()
                                } else {
                                    errorMessage = message
                                }
                            }
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("حفظ التغييرات")
                    }
                }
            }
        }
    }
}
