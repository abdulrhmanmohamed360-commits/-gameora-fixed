package com.gameora.app.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.Wallet
import com.gameora.app.data.model.WalletTransaction
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.WalletRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletUiState(
    val isLoading: Boolean = true,
    val wallet: Wallet? = null,
    val transactions: List<WalletTransaction> = emptyList(),
    val isProcessingAction: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
)

class WalletViewModel(
    private val walletRepository: WalletRepository = ServiceLocator.walletRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val walletResult = walletRepository.fetchWallet(uid)
            val txResult = walletRepository.fetchTransactions(uid)

            _uiState.value = WalletUiState(
                isLoading = false,
                wallet = walletResult.getOrNull(),
                transactions = txResult.getOrDefault(emptyList()),
                errorMessage = walletResult.exceptionOrNull()?.message
            )
        }
    }

    /** طلب إيداع — العملية النهائية والمعتمدة تتم في الـ Backend بعد تأكيد وسيلة الدفع */
    fun deposit(amount: Double, currencyCode: String, paymentMethodToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingAction = true, actionMessage = null)
            walletRepository.requestDeposit(amount, currencyCode, paymentMethodToken).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProcessingAction = false,
                        actionMessage = "تم إرسال طلب الإيداع بنجاح"
                    )
                    load()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessingAction = false,
                        errorMessage = e.message ?: "فشل طلب الإيداع"
                    )
                }
            )
        }
    }

    fun withdraw(amount: Double, currencyCode: String, payoutDetails: Map<String, String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingAction = true, actionMessage = null)
            walletRepository.requestWithdrawal(amount, currencyCode, payoutDetails).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProcessingAction = false,
                        actionMessage = "تم إرسال طلب السحب بنجاح، وسيتم مراجعته"
                    )
                    load()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessingAction = false,
                        errorMessage = e.message ?: "فشل طلب السحب"
                    )
                }
            )
        }
    }
}
