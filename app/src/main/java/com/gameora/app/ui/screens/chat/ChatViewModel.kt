package com.gameora.app.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.ChatMessage
import com.gameora.app.data.repository.ChatRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val chatId: String = "",
    val orderId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository = ServiceLocator.chatRepository
) : ViewModel() {

    private val orderId: String =
        checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(
        ChatUiState(orderId = orderId)
    )

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    init {
        loadChat()
    }

    /**
     * تحميل المحادثة المرتبطة بالطلب.
     */
    private fun loadChat() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            chatRepository
                .fetchChatByOrderId(orderId)
                .fold(

                    onSuccess = { chat ->

                        if (chat == null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "المحادثة لم تبدأ بعد"
                            )
                            return@fold
                        }

                        _uiState.value = _uiState.value.copy(
                            chatId = chat.id,
                            isLoading = false,
                            errorMessage = null
                        )

                        observeMessages(chat.id)
                    },

                    onFailure = { error ->

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage =
                                error.message
                                    ?: "فشل تحميل المحادثة"
                        )
                    }
                )
        }
    }

    /**
     * الاستماع للرسائل بشكل لحظي.
     */
    private fun observeMessages(chatId: String) {

        viewModelScope.launch {

            chatRepository
                .observeMessages(chatId)
                .collect { messages ->

                    _uiState.value =
                        _uiState.value.copy(
                            messages = messages
                        )

                    markUnreadMessagesAsRead(
                        chatId = chatId,
                        messages = messages
                    )
                }
        }
    }

    /**
     * إرسال رسالة.
     */
    fun sendMessage(text: String) {

        val cleanText = text.trim()

        if (cleanText.isBlank()) {
            return
        }

        val chatId = _uiState.value.chatId

        if (chatId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "المحادثة غير متاحة حاليًا"
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isSending = true,
                errorMessage = null
            )

            chatRepository
                .sendMessage(
                    chatId = chatId,
                    orderId = orderId,
                    text = cleanText
                )
                .fold(

                    onSuccess = {

                        _uiState.value =
                            _uiState.value.copy(
                                isSending = false
                            )
                    },

                    onFailure = { error ->

                        _uiState.value =
                            _uiState.value.copy(
                                isSending = false,
                                errorMessage =
                                    error.message
                                        ?: "فشل إرسال الرسالة"
                            )
                    }
                )
        }
    }

    /**
     * تحديد الرسائل الواردة كمقروءة.
     */
    private suspend fun markUnreadMessagesAsRead(
        chatId: String,
        messages: List<ChatMessage>
    ) {

        val currentUid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid
                ?: return

        messages
            .filter {
                it.receiverId == currentUid &&
                !it.isRead
            }
            .forEach { message ->

                chatRepository.markMessageAsRead(
                    chatId = chatId,
                    messageId = message.id
                )
            }
    }

    /**
     * إعادة تحميل المحادثة يدويًا.
     */
    fun reload() {
        loadChat()
    }

    fun clearError() {
        _uiState.value =
            _uiState.value.copy(
                errorMessage = null
            )
    }
}