package com.gameora.app.util

import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.ChatRepository
import com.gameora.app.data.repository.CurrencyRepository
import com.gameora.app.data.repository.GameRepository
import com.gameora.app.data.repository.OrderRepository
import com.gameora.app.data.repository.ProductRepository
import com.gameora.app.data.repository.WalletRepository
import com.gameora.app.data.repository.UserRepository

/**
 * موفر بسيط لنسخ المستودعات المشتركة (Singleton) عبر التطبيق.
 * في مشروع أكبر يُفضَّل استبدال هذا بـ Hilt/Koin، لكن لهيكل البداية هذا كافٍ وواضح.
 */
object ServiceLocator {
    val authRepository by lazy { AuthRepository() }
    val gameRepository by lazy { GameRepository() }
    val productRepository by lazy { ProductRepository() }
    val orderRepository by lazy { OrderRepository() }
    val walletRepository by lazy { WalletRepository() }
    val userRepository by lazy { UserRepository() }
    val currencyRepository by lazy { CurrencyRepository() }
    val chatRepository by lazy { ChatRepository() }
}
