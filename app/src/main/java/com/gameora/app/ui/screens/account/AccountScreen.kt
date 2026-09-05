package com.gameora.app.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameora.app.data.model.UserRole

@Composable
fun AccountScreen(
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        Text(
            text = "حسابي",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(22.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(22.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            val user = uiState.user

            if (user == null) {
                Text(
                    text = "تعذر تحميل بيانات الحساب",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = user.displayName.ifBlank {
                                "مستخدم Gameora"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(18.dp))

                        AccountInfo("البريد الإلكتروني", user.email)
                        AccountInfo("رقم الهاتف", user.phone ?: "")
                        AccountInfo(
                            "الدولة",
                            user.countryCode.ifBlank { "غير محددة" }
                        )
                        AccountInfo(
                            "العملة",
                            user.currencyCode.ifBlank { "غير محددة" }
                        )

                        AccountInfo(
                            "نوع الحساب",
                            when {
                                user.role == UserRole.ADMIN -> "مدير"
                                user.isSeller ||
                                    user.role == UserRole.SELLER -> "بائع"
                                else -> "مشتري"
                            }
                        )

                        if (
                            user.isSeller ||
                            user.role == UserRole.SELLER
                        ) {
                            user.sellerRating?.let {
                                AccountInfo(
                                    "تقييم البائع",
                                    String.format("%.1f / 5", it)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "تعديل الملف الشخصي",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("تسجيل الخروج")
                }
            }
        }
    }
}

@Composable
private fun AccountInfo(
    title: String,
    value: String
) {
    if (value.isBlank()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
