package com.gameora.app.ui.screens.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.DisplayPrice
import com.gameora.app.data.model.Game
import com.gameora.app.data.model.Product
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.CurrencyRepository
import com.gameora.app.data.repository.GameRepository
import com.gameora.app.data.repository.ProductRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val products: List<Product> = emptyList(),
    val displayPrices: Map<String, DisplayPrice> = emptyMap(),
    val errorMessage: String? = null
)

class GameDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository = ServiceLocator.gameRepository,
    private val productRepository: ProductRepository = ServiceLocator.productRepository,
    private val currencyRepository: CurrencyRepository = ServiceLocator.currencyRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val gameId: String = checkNotNull(savedStateHandle["gameId"])

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = GameDetailUiState(isLoading = true)

            val gameResult = gameRepository.fetchGameById(gameId)
            val productsResult = productRepository.fetchProductsByGame(gameId)
            val userCurrency = authRepository.fetchCurrentUserProfile()
                .getOrNull()?.currencyCode?.ifBlank { "USD" } ?: "USD"

            val products = productsResult.getOrDefault(emptyList())
            val prices = mutableMapOf<String, DisplayPrice>()
            products.forEach { product ->
                currencyRepository.convertForDisplay(
                    product.originalPrice, product.originalCurrency, userCurrency
                ).onSuccess { prices[product.id] = it }
            }

            _uiState.value = GameDetailUiState(
                isLoading = false,
                game = gameResult.getOrNull(),
                products = products,
                displayPrices = prices,
                errorMessage = productsResult.exceptionOrNull()?.message
            )
        }
    }
}
