package com.gameora.app.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.DisplayPrice
import com.gameora.app.data.model.Game
import com.gameora.app.data.model.Product
import com.gameora.app.data.model.ProductCategory
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.CurrencyRepository
import com.gameora.app.data.repository.GameRepository
import com.gameora.app.data.repository.ProductRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** خيارات ترتيب نتائج المتجر */
enum class StoreSortOption(val labelAr: String) {
    NEWEST("الأحدث"),
    PRICE_LOW_TO_HIGH("السعر: الأقل أولاً"),
    PRICE_HIGH_TO_LOW("السعر: الأعلى أولاً")
}

data class StoreUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val games: List<Game> = emptyList(),
    val selectedGameId: String? = null,
    val selectedCategory: ProductCategory? = null,
    val sortOption: StoreSortOption = StoreSortOption.NEWEST,
    val allProducts: List<Product> = emptyList(),
    val results: List<Product> = emptyList(),
    val displayPrices: Map<String, DisplayPrice> = emptyMap(), // productId -> السعر المحوَّل
    val userCurrency: String = "USD",
    val isSeller: Boolean = false,
    val errorMessage: String? = null
)

class StoreViewModel(
    private val productRepository: ProductRepository = ServiceLocator.productRepository,
    private val gameRepository: GameRepository = ServiceLocator.gameRepository,
    private val currencyRepository: CurrencyRepository = ServiceLocator.currencyRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val gameNamesById: Map<String, String>
        get() = _uiState.value.games.associate {
            it.id to it.nameAr.ifBlank { it.name }
        }

    init {
        loadUserCurrency()
        loadGames()
        loadProducts()
    }

    private fun loadUserCurrency() {
        viewModelScope.launch {
            authRepository.fetchCurrentUserProfile().onSuccess { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        userCurrency = user.currencyCode.ifBlank { "USD" },
                        isSeller = user.isSeller
                    )
                }
            }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            gameRepository.fetchActiveGames().onSuccess { games ->
                _uiState.value = _uiState.value.copy(games = games)
            }
        }
    }

    /** التحميل الأول لكل منتجات المتجر (تصفّح بدون الحاجة للبحث). */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            productRepository.fetchProducts().fold(
                onSuccess = { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allProducts = products
                    )
                    applyFiltersAndSort()
                    convertPricesForDisplay(products)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "تعذر تحميل المتجر، تحقق من اتصالك بالإنترنت"
                    )
                }
            )
        }
    }

    /** تحديث المتجر (Pull to refresh / زر تحديث) — يجلب أحدث البيانات من Backend. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            productRepository.fetchProducts().fold(
                onSuccess = { products ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        allProducts = products
                    )
                    applyFiltersAndSort()
                    convertPricesForDisplay(products)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = e.message ?: "تعذر تحديث المتجر، حاول مرة أخرى"
                    )
                }
            )
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        applyFiltersAndSort()
    }

    fun onGameFilterChanged(gameId: String?) {
        _uiState.value = _uiState.value.copy(selectedGameId = gameId)
        applyFiltersAndSort()
    }

    fun onCategoryFilterChanged(category: ProductCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFiltersAndSort()
    }

    fun onSortOptionChanged(option: StoreSortOption) {
        _uiState.value = _uiState.value.copy(sortOption = option)
        applyFiltersAndSort()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * يطبّق البحث + الفلاتر + الترتيب محليًا على القائمة المحمّلة فعليًا
     * من Backend — لا توجد بيانات ثابتة/تجريبية، فقط نتيجة معالجة القائمة
     * الحقيقية حسب اختيار المستخدم الحالي.
     */
    private fun applyFiltersAndSort() {
        val state = _uiState.value
        val q = state.query.trim()

        var filtered = state.allProducts

        if (q.isNotBlank()) {
            val names = gameNamesById
            filtered = filtered.filter { product ->
                product.title.contains(q, ignoreCase = true) ||
                    product.sellerName.contains(q, ignoreCase = true) ||
                    (names[product.gameId]?.contains(q, ignoreCase = true) == true)
            }
        }

        state.selectedGameId?.let { gameId ->
            filtered = filtered.filter { it.gameId == gameId }
        }

        state.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        filtered = when (state.sortOption) {
            StoreSortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
            StoreSortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.originalPrice }
            StoreSortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.originalPrice }
        }

        _uiState.value = state.copy(results = filtered)
    }

    /** تحويل كل الأسعار لعملة المستخدم — تحويل عرض فقط، لا يغيّر السعر الأصلي للبائع */
    private fun convertPricesForDisplay(products: List<Product>) {
        viewModelScope.launch {
            val userCurrency = _uiState.value.userCurrency
            val prices = mutableMapOf<String, DisplayPrice>()
            products.forEach { product ->
                currencyRepository.convertForDisplay(
                    amount = product.originalPrice,
                    fromCurrency = product.originalCurrency,
                    toCurrency = userCurrency
                ).onSuccess { prices[product.id] = it }
            }
            _uiState.value = _uiState.value.copy(displayPrices = prices)
        }
    }
}
