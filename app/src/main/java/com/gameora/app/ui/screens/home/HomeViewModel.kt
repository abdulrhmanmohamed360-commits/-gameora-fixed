package com.gameora.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.Game
import com.gameora.app.data.repository.GameRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val games: List<Game> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val gameRepository: GameRepository = ServiceLocator.gameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            gameRepository.fetchActiveGames().fold(
                onSuccess = { games ->
                    _uiState.value = HomeUiState(isLoading = false, games = games)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "تعذر تحميل الألعاب"
                    )
                }
            )
        }
    }
}
