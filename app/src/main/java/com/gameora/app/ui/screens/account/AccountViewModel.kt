package com.gameora.app.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.User
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = true,
    val user: User? = null
)

class AccountViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.fetchCurrentUserProfile().getOrNull()
            _uiState.value = AccountUiState(isLoading = false, user = user)
        }
    }

    fun logout() = authRepository.logout()

    /**
     * تحديث بيانات الملف الشخصي (الاسم والدولة والعملة).
     */
    fun updateProfile(
        displayName: String,
        countryCode: String,
        currencyCode: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {

            authRepository.updateProfile(
                displayName = displayName,
                countryCode = countryCode,
                currencyCode = currencyCode
            ).fold(
                onSuccess = {
                    val refreshedUser =
                        authRepository.fetchCurrentUserProfile().getOrNull()

                    _uiState.value = _uiState.value.copy(
                        user = refreshedUser
                    )

                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(
                        false,
                        error.message ?: "فشل تحديث البيانات"
                    )
                }
            )
        }
    }
}
