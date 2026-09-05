package com.gameora.app.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.Order
import com.gameora.app.data.repository.OrderRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isProcessing: Boolean = false,
    val remainingApprovalMillis: Long? = null
)

class OrderDetailViewModel(
    private val repository: OrderRepository =
        ServiceLocator.orderRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(OrderDetailUiState())

    val uiState: StateFlow<OrderDetailUiState> =
        _uiState.asStateFlow()

    private var orderId: String? = null
    private var timerJob: Job? = null

    fun loadOrder(id: String? = orderId) {
        if (id.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "رقم الطلب غير موجود"
            )
            return
        }

        orderId = id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.fetchOrderById(id)
                .onSuccess { order ->
                    if (order == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            order = null,
                            errorMessage = "الطلب غير موجود"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            order = order,
                            errorMessage = null
                        )

                        startApprovalTimer(order)
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage =
                            error.message ?: "حدث خطأ أثناء تحميل الطلب"
                    )
                }
        }
    }

    private fun startApprovalTimer(order: Order) {
        timerJob?.cancel()

        val deadline = order.sellerApprovalDeadline

        if (deadline == null) {
            _uiState.value = _uiState.value.copy(
                remainingApprovalMillis = null
            )
            return
        }

        timerJob = viewModelScope.launch {
            while (true) {
                val remaining =
                    deadline - System.currentTimeMillis()

                _uiState.value = _uiState.value.copy(
                    remainingApprovalMillis =
                        remaining.coerceAtLeast(0L)
                )

                if (remaining <= 0L) {
                    break
                }

                delay(1000L)
            }
        }
    }

    fun getChatId(): String? {
        return _uiState.value.order
            ?.chatId
            ?.takeIf { it.isNotBlank() }
    }

    fun acceptOrder() {
        performAction("تم قبول الطلب") {
            repository.acceptOrder(requireOrderId())
        }
    }

    fun rejectOrder(reason: String) {
        performAction("تم رفض الطلب") {
            repository.rejectOrder(
                orderId = requireOrderId(),
                reason = reason
            )
        }
    }

    fun startDelivery() {
        performAction("بدأت عملية تسليم الحساب") {
            repository.startDelivery(
                requireOrderId()
            )
        }
    }

    fun deliverAccount() {
        performAction("تم تسليم بيانات الحساب") {
            repository.deliverAccount(
                requireOrderId()
            )
        }
    }

    fun confirmAccount() {
        performAction("تم تأكيد استلام الحساب") {
            repository.confirmAccount(
                requireOrderId()
            )
        }
    }

    fun openDispute(reason: String) {
        if (reason.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "يجب كتابة سبب البلاغ"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null,
                successMessage = null
            )

            repository.openDispute(
                orderId = requireOrderId(),
                reason = reason
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "تم فتح البلاغ بنجاح"
                    )

                    loadOrder()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage =
                            error.message ?: "تعذر فتح البلاغ"
                    )
                }
        }
    }

    private fun performAction(
        successMessage: String,
        action: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null,
                successMessage = null
            )

            action()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = successMessage
                    )

                    loadOrder()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage =
                            error.message
                                ?: "حدث خطأ أثناء تنفيذ العملية"
                    )
                }
        }
    }

    private fun requireOrderId(): String {
        return orderId
            ?: _uiState.value.order?.id
            ?: error("رقم الطلب غير موجود")
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}

