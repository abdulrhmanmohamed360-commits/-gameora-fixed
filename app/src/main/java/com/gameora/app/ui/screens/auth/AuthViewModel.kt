package com.gameora.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.util.CountryCurrency
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val isLoggedIn: Boolean get() = authRepository.currentUid != null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "فشل تسجيل الدخول") }
            )
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        countryCode: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val currency = CountryCurrency.currencyFor(countryCode)
            val result = authRepository.register(email, password, displayName, countryCode, currency)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "فشل إنشاء الحساب") }
            )
        }
    }

    fun logout() = authRepository.logout()
}
