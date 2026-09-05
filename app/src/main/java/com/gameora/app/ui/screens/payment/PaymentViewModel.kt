package com.gameora.app.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.repository.WalletRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)

class PaymentViewModel(
    private val walletRepository: WalletRepository =
        ServiceLocator.walletRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(PaymentUiState())

    val uiState: StateFlow<PaymentUiState> =
        _uiState.asStateFlow()

    fun startPayment(
        amount: Double,
        currencyCode: String,
        paymentMethod: PaymentMethod
    ) {
        if (amount <= 0.0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "المبلغ غير صحيح",
                message = null
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = PaymentUiState(
                isProcessing = true
            )

            val paymentMethodToken =
                when (paymentMethod) {
                    PaymentMethod.VODAFONE_CASH ->
                        "VODAFONE_CASH"

                    PaymentMethod.ORANGE_CASH ->
                        "ORANGE_CASH"

                    PaymentMethod.ETISALAT_CASH ->
                        "ETISALAT_CASH"

                    PaymentMethod.INSTAPAY ->
                        "INSTAPAY"

                    PaymentMethod.BANK_CARD ->
                        "BANK_CARD"
                }

            walletRepository
                .requestDeposit(
                    amount = amount,
                    currencyCode = currencyCode,
                    paymentMethodToken = paymentMethodToken
                )
                .fold(
                    onSuccess = {
                        _uiState.value =
                            PaymentUiState(
                                isProcessing = false,
                                isSuccess = true,
                                message =
                                    "تم إرسال طلب الدفع بنجاح"
                            )
                    },

                    onFailure = { exception ->

                        _uiState.value =
                            PaymentUiState(
                                isProcessing = false,
                                errorMessage =
                                    exception.message
                                        ?: "تعذر إنشاء عملية الدفع"
                            )
                    }
                )
        }
    }

    fun clearState() {
        _uiState.value = PaymentUiState()
    }
}
