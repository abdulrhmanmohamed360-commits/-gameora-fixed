package com.gameora.app.ui.screens.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.DisplayPrice
import com.gameora.app.data.model.Product
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.CurrencyRepository
import com.gameora.app.data.repository.OrderRepository
import com.gameora.app.data.repository.ProductRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val displayPrice: DisplayPrice? = null,
    val isPurchasing: Boolean = false,
    val purchaseSuccessOrderId: String? = null,
    val errorMessage: String? = null
)

class ProductDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository = ServiceLocator.productRepository,
    private val currencyRepository: CurrencyRepository = ServiceLocator.currencyRepository,
    private val orderRepository: OrderRepository = ServiceLocator.orderRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState(isLoading = true)
            val productResult = productRepository.fetchProductById(productId)
            val product = productResult.getOrNull()

            if (product == null) {
                _uiState.value = ProductDetailUiState(
                    isLoading = false,
                    errorMessage = "المنتج غير موجود أو تم حذفه"
                )
                return@launch
            }

            val userCurrency = authRepository.fetchCurrentUserProfile()
                .getOrNull()?.currencyCode?.ifBlank { "USD" } ?: "USD"

            val displayPrice = currencyRepository.convertForDisplay(
                product.originalPrice, product.originalCurrency, userCurrency
            ).getOrNull()

            _uiState.value = ProductDetailUiState(
                isLoading = false,
                product = product,
                displayPrice = displayPrice ?: DisplayPrice(product.originalPrice, product.originalCurrency)
            )
        }
    }

    /**
     * الشراء الفعلي: لا يتم أي خصم أو إنشاء طلب من داخل هذا الكود.
     * كل ما يحدث هنا هو استدعاء Cloud Function "createOrder" على الـ Backend،
     * وهي المسؤولة عن التحقق النهائي من السعر والرصيد وإنشاء الطلب.
     */
    fun purchase(quantity: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, errorMessage = null)
            orderRepository.createOrder(productId, quantity).fold(
                onSuccess = { orderId ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        purchaseSuccessOrderId = orderId
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        errorMessage = e.message ?: "فشلت عملية الشراء، حاول مرة أخرى"
                    )
                }
            )
        }
    }
}
