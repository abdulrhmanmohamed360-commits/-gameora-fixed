package com.gameora.app.navigation

/**
 * جميع مسارات التنقل داخل تطبيق Gameora.
 *
 * ملاحظة:
 * - كل Route موجود هنا في مكان واحد.
 * - أي شاشة جديدة يتم إضافتها هنا قبل ربطها داخل NavGraph.
 */
sealed class Screen(val route: String) {

    // =========================================================
    // Authentication
    // =========================================================

    data object Login : Screen("auth/login")

    data object Register : Screen("auth/register")


    // =========================================================
    // Main / Bottom Navigation
    // =========================================================

    data object Home : Screen("home")

    data object Store : Screen("store")

    data object Orders : Screen("orders")

    data object Wallet : Screen("wallet")

    data object Account : Screen("account")


    // =========================================================
    // Games
    // =========================================================

    data object GameDetail : Screen("game/{gameId}") {

        fun createRoute(gameId: String): String {
            return "game/$gameId"
        }
    }


    // =========================================================
    // Products
    // =========================================================

    data object ProductDetail : Screen("product/{productId}") {

        fun createRoute(productId: String): String {
            return "product/$productId"
        }
    }

    data object CreateProduct : Screen("product/create")


    // =========================================================
    // Orders
    // =========================================================

    data object OrderDetail : Screen("order/{orderId}") {

        fun createRoute(orderId: String): String {
            return "order/$orderId"
        }
    }


    // =========================================================
    // Chat
    // =========================================================

    /**
     * المحادثة مرتبطة بالطلب.
     *
     * الوصول الحقيقي للمحادثة يجب أن يتم التحقق منه
     * من الـ Backend / Firestore Security Rules.
     */
    data object Chat : Screen("chat/{orderId}") {

        fun createRoute(orderId: String): String {
            return "chat/$orderId"
        }
    }


    // =========================================================
    // Account
    // =========================================================

    data object EditProfile : Screen("account/edit")


    // =========================================================
    // Wallet
    // =========================================================

    /**
     * الصفحة الرئيسية للمحفظة.
     */
    data object WalletHome : Screen("wallet/home")


    /**
     * صفحة شحن المحفظة.
     */
    data object WalletTopUp : Screen("wallet/topup")


    /**
     * صفحة التحويل بين مستخدمي Gameora.
     */
    data object WalletTransfer : Screen("wallet/transfer")


    /**
     * صفحة سحب الرصيد.
     */
    data object WalletWithdraw : Screen("wallet/withdraw")
}


/**
 * عناصر شريط التنقل السفلي.
 */
enum class BottomNavItem(
    val screen: Screen,
    val labelAr: String
) {

    HOME(
        screen = Screen.Home,
        labelAr = "الرئيسية"
    ),

    STORE(
        screen = Screen.Store,
        labelAr = "المتجر"
    ),

    ORDERS(
        screen = Screen.Orders,
        labelAr = "الطلبات"
    ),

    WALLET(
        screen = Screen.Wallet,
        labelAr = "المحفظة"
    ),

    ACCOUNT(
        screen = Screen.Account,
        labelAr = "الحساب"
    )
}
