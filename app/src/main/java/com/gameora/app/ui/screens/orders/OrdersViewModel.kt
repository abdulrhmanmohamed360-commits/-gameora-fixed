package com.gameora.app.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.Order
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.OrderRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdersUiState(
    val isLoading: Boolean = true,
    val purchases: List<Order> = emptyList(),  // بصفتي مشتري
    val sales: List<Order> = emptyList(),      // بصفتي بائع
    val errorMessage: String? = null
)

class OrdersViewModel(
    private val orderRepository: OrderRepository = ServiceLocator.orderRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val purchases = orderRepository.fetchOrdersAsBuyer(uid)
            val sales = orderRepository.fetchOrdersAsSeller(uid)

            _uiState.value = OrdersUiState(
                isLoading = false,
                purchases = purchases.getOrDefault(emptyList()),
                sales = sales.getOrDefault(emptyList()),
                errorMessage = purchases.exceptionOrNull()?.message ?: sales.exceptionOrNull()?.message
            )
        }
    }
}
