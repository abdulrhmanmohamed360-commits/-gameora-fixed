package com.gameora.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.gameora.app.ui.screens.account.AccountScreen
import com.gameora.app.ui.screens.account.EditProfileScreen
import com.gameora.app.ui.screens.auth.LoginScreen
import com.gameora.app.ui.screens.auth.RegisterScreen
import com.gameora.app.ui.screens.chat.ChatScreen
import com.gameora.app.ui.screens.home.HomeScreen
import com.gameora.app.ui.screens.orders.OrderDetailScreen
import com.gameora.app.ui.screens.orders.OrdersScreen
import com.gameora.app.ui.screens.payment.PaymentMethodScreen
import com.gameora.app.ui.screens.payment.PaymentSuccessScreen
import com.gameora.app.ui.screens.payment.PaymentViewModel
import com.gameora.app.ui.screens.store.CreateProductScreen
import com.gameora.app.ui.screens.store.GameDetailScreen
import com.gameora.app.ui.screens.store.ProductDetailScreen
import com.gameora.app.ui.screens.store.StoreScreen
import com.gameora.app.ui.screens.wallet.WalletScreen
import com.gameora.app.ui.screens.wallet.WalletTopUpScreen
import com.gameora.app.util.ServiceLocator

private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Store.route,
    Screen.Orders.route,
    Screen.Wallet.route,
    Screen.Account.route
)

@Composable
fun GameoraNavGraph() {

    val navController = rememberNavController()

    val isLoggedIn =
        ServiceLocator.authRepository.currentUid != null

    val startDestination =
        if (isLoggedIn) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

    val backStackEntry by
        navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    val showBottomBar =
        currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GameoraBottomNavBar(navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {

            // =================================================
            // LOGIN
            // =================================================

            composable(Screen.Login.route) {

                LoginScreen(
                    onLoginSuccess = {

                        navController.navigate(
                            Screen.Home.route
                        ) {

                            popUpTo(
                                Screen.Login.route
                            ) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    },

                    onNavigateToRegister = {

                        navController.navigate(
                            Screen.Register.route
                        )
                    }
                )
            }


            // =================================================
            // REGISTER
            // =================================================

            composable(Screen.Register.route) {

                RegisterScreen(

                    onRegisterSuccess = {

                        navController.navigate(
                            Screen.Home.route
                        ) {

                            popUpTo(
                                Screen.Register.route
                            ) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    },

                    onNavigateToLogin = {

                        navController.popBackStack()
                    }
                )
            }


            // =================================================
            // HOME
            // =================================================

            composable(Screen.Home.route) {

                HomeScreen(

                    onGameClick = { gameId ->

                        navController.navigate(
                            Screen.GameDetail.createRoute(
                                gameId
                            )
                        )
                    }
                )
            }


            // =================================================
            // STORE
            // =================================================

            composable(Screen.Store.route) {

                StoreScreen(

                    onProductClick = { productId ->

                        navController.navigate(
                            Screen.ProductDetail.createRoute(
                                productId
                            )
                        )
                    },

                    onCreateProductClick = {

                        navController.navigate(
                            Screen.CreateProduct.route
                        )
                    }
                )
            }


            // =================================================
            // ORDERS
            // =================================================

            composable(Screen.Orders.route) {

                OrdersScreen(

                    onOrderClick = { orderId ->

                        navController.navigate(
                            Screen.OrderDetail.createRoute(
                                orderId
                            )
                        )
                    }
                )
            }


            // =================================================
            // WALLET
            // =================================================

            composable(Screen.Wallet.route) {

                WalletScreen()
            }


            // =================================================
            // WALLET HOME
            // =================================================

            composable(Screen.WalletHome.route) {

                WalletScreen()
            }


            // =================================================
            // WALLET TOP UP
            // =================================================

            composable(Screen.WalletTopUp.route) {

                WalletTopUpScreen(

                    currencyCode = "EGP",

                    onBack = {

                        navController.popBackStack()
                    },

                    onContinue = { amount, currencyCode ->

                        navController.navigate(
                            "wallet/payment-method/$amount/$currencyCode"
                        )
                    }
                )
            }


            // =================================================
            // PAYMENT METHOD
            // =================================================

            composable(
                route = "wallet/payment-method/{amount}/{currencyCode}"
            ) { backStackEntry ->

                val amount =
                    backStackEntry
                        .arguments
                        ?.getString("amount")
                        ?.toDoubleOrNull()
                        ?: 0.0

                val currencyCode =
                    backStackEntry
                        .arguments
                        ?.getString("currencyCode")
                        ?: "EGP"

                PaymentMethodScreen(

                    amount = amount,

                    currencyCode = currencyCode,

                    onBack = {

                        navController.popBackStack()
                    },

                    onContinue = { method, selectedAmount, selectedCurrency ->

                        navController.navigate(
                            "wallet/payment-processing/" +
                                "$selectedAmount/" +
                                "$selectedCurrency/" +
                                method.name
                        )
                    }
                )
            }


            // =================================================
            // PAYMENT PROCESSING
            // =================================================

            composable(
                route =
                    "wallet/payment-processing/" +
                        "{amount}/{currencyCode}/{paymentMethod}"
            ) { backStackEntry ->

                val amount =
                    backStackEntry
                        .arguments
                        ?.getString("amount")
                        ?.toDoubleOrNull()
                        ?: 0.0

                val currencyCode =
                    backStackEntry
                        .arguments
                        ?.getString("currencyCode")
                        ?: "EGP"

                val paymentMethodName =
                    backStackEntry
                        .arguments
                        ?.getString("paymentMethod")

                val paymentMethod =
                    runCatching {
                        com.gameora.app.ui.screens.payment
                            .PaymentMethod
                            .valueOf(
                                paymentMethodName
                                    ?: ""
                            )
                    }.getOrNull()


                if (paymentMethod == null) {

                    navController.popBackStack()

                } else {

                    PaymentProcessingRoute(
                        amount = amount,
                        currencyCode = currencyCode,
                        paymentMethod = paymentMethod,
                        onSuccess = { transactionId ->

                            navController.navigate(
                                "wallet/payment-success/" +
                                    "$amount/" +
                                    "$currencyCode" +
                                    if (!transactionId.isNullOrBlank()) {
                                        "/$transactionId"
                                    } else {
                                        ""
                                    }
                            ) {

                                popUpTo(
                                    "wallet/payment-method/{amount}/{currencyCode}"
                                ) {
                                    inclusive = true
                                }
                            }
                        },

                        onBack = {

                            navController.popBackStack()
                        }
                    )
                }
            }


            // =================================================
            // PAYMENT SUCCESS
            // =================================================

            composable(
                route =
                    "wallet/payment-success/" +
                        "{amount}/{currencyCode}"
            ) { backStackEntry ->

                val amount =
                    backStackEntry
                        .arguments
                        ?.getString("amount")
                        ?.toDoubleOrNull()
                        ?: 0.0

                val currencyCode =
                    backStackEntry
                        .arguments
                        ?.getString("currencyCode")
                        ?: "EGP"

                PaymentSuccessScreen(

                    amount = amount,

                    currencyCode = currencyCode,

                    transactionId = null,

                    onDone = {

                        navController.navigate(
                            Screen.Wallet.route
                        ) {

                            popUpTo(
                                Screen.Wallet.route
                            ) {
                                inclusive = false
                            }

                            launchSingleTop = true
                        }
                    },

                    onViewWallet = {

                        navController.navigate(
                            Screen.Wallet.route
                        ) {

                            popUpTo(
                                Screen.Wallet.route
                            ) {
                                inclusive = false
                            }

                            launchSingleTop = true
                        }
                    }
                )
            }


            // =================================================
            // WALLET TRANSFER
            // =================================================

            composable(Screen.WalletTransfer.route) {

                // سيتم ربط WalletTransferScreen هنا
                // مع الـ Backend بعد مراجعة توقيع الشاشة.
            }


            // =================================================
            // WALLET WITHDRAW
            // =================================================

            composable(Screen.WalletWithdraw.route) {

                // سيتم ربط WalletWithdrawScreen هنا
                // مع الـ Backend بعد مراجعة توقيع الشاشة.
            }


            // =================================================
            // ACCOUNT
            // =================================================

            composable(Screen.Account.route) {

                AccountScreen(

                    onEditProfile = {

                        navController.navigate(
                            Screen.EditProfile.route
                        )
                    },

                    onLoggedOut = {

                        navController.navigate(
                            Screen.Login.route
                        ) {

                            popUpTo(
                                navController.graph.startDestinationId
                            ) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                )
            }


            // =================================================
            // GAME DETAILS
            // =================================================

            composable(Screen.GameDetail.route) {

                GameDetailScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onProductClick = { productId ->

                        navController.navigate(
                            Screen.ProductDetail.createRoute(
                                productId
                            )
                        )
                    }
                )
            }


            // =================================================
            // PRODUCT DETAILS
            // =================================================

            composable(Screen.ProductDetail.route) {

                ProductDetailScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onPurchaseComplete = { orderId ->

                        navController.navigate(
                            Screen.OrderDetail.createRoute(
                                orderId
                            )
                        ) {

                            popUpTo(
                                Screen.Store.route
                            )
                        }
                    }
                )
            }


            // =================================================
            // CREATE PRODUCT
            // =================================================

            composable(Screen.CreateProduct.route) {

                CreateProductScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onProductCreated = {

                        navController.popBackStack()
                    }
                )
            }


            // =================================================
            // ORDER DETAILS
            // =================================================

            composable(Screen.OrderDetail.route) {

                OrderDetailScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onOpenChat = { orderId ->

                        navController.navigate(
                            Screen.Chat.createRoute(
                                orderId
                            )
                        )
                    }
                )
            }


            // =================================================
            // CHAT
            // =================================================

            composable(Screen.Chat.route) {

                ChatScreen(

                    onBack = {

                        navController.popBackStack()
                    },

                    onShowAccountData = {
                        // سيتم ربط بيانات الحساب بالـ Backend
                    },

                    onReportProblem = {
                        // سيتم ربط نظام البلاغات بالـ Backend
                    },

                    onOrderCompleted = {
                        // سيتم تنفيذ إكمال الطلب عبر Cloud Function
                    }
                )
            }


            // =================================================
            // EDIT PROFILE
            // =================================================

            composable(Screen.EditProfile.route) {

                EditProfileScreen(

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


// =============================================================
// Payment Processing Route
// =============================================================

@Composable
private fun PaymentProcessingRoute(
    amount: Double,
    currencyCode: String,
    paymentMethod: com.gameora.app.ui.screens.payment.PaymentMethod,
    onSuccess: (String?) -> Unit,
    onBack: () -> Unit
) {

    val viewModel =
        androidx.lifecycle.viewmodel.compose.viewModel<PaymentViewModel>()

    val uiState by viewModel.uiState.collectAsState()


    androidx.compose.runtime.LaunchedEffect(
        amount,
        currencyCode,
        paymentMethod
    ) {

        if (
            !uiState.isProcessing &&
            !uiState.isSuccess &&
            uiState.errorMessage == null
        ) {

            viewModel.startPayment(
                amount = amount,
                currencyCode = currencyCode,
                paymentMethod = paymentMethod
            )
        }
    }


    when {

        uiState.isSuccess -> {

            onSuccess(null)
        }

        uiState.errorMessage != null -> {

            androidx.compose.foundation.layout.Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {

                androidx.compose.material3.Text(
                    text = uiState.errorMessage ?: "حدث خطأ أثناء عملية الدفع",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(top = 16.dp)
                )

                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.startPayment(
                            amount = amount,
                            currencyCode = currencyCode,
                            paymentMethod = paymentMethod
                        )
                    }
                ) {
                    androidx.compose.material3.Text("إعادة المحاولة")
                }

                androidx.compose.material3.TextButton(
                    onClick = onBack
                ) {
                    androidx.compose.material3.Text("رجوع")
                }
            }
        }

        else -> {

            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {

                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}
