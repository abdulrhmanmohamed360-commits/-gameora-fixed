package com.gameora.app.ui.screens.store

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameora.app.data.model.Game
import com.gameora.app.data.model.ProductCategory
import com.gameora.app.data.repository.AuthRepository
import com.gameora.app.data.repository.GameRepository
import com.gameora.app.data.repository.ProductRepository
import com.gameora.app.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateProductUiState(
    val games: List<Game> = emptyList(),
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

class CreateProductViewModel(
    private val productRepository: ProductRepository = ServiceLocator.productRepository,
    private val gameRepository: GameRepository = ServiceLocator.gameRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProductUiState())
    val uiState: StateFlow<CreateProductUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {

            gameRepository.fetchActiveGames()
                .onSuccess { games ->

                    _uiState.value = _uiState.value.copy(
                        games = games,
                        errorMessage = null
                    )
                }
                .onFailure { error ->

                    _uiState.value = _uiState.value.copy(
                        errorMessage =
                            error.message ?: "فشل تحميل الألعاب"
                    )
                }
        }
    }

    /**
     * نشر حساب لعبة للبيع.
     *
     * بيانات الدخول لا يتم عرضها للمستخدمين من خلال الإعلان.
     * سيتم ربط تخزينها الآمن بالـRepository / Backend لاحقًا.
     */
    fun submit(
        gameId: String,

        title: String,

        description: String,

        imageUris: List<Uri>,

        price: Double,

        currencyCode: String,

        category: ProductCategory,

        stock: Int,

        accountUsername: String,

        accountPassword: String,

        accountEmail: String,

        accountEmailPassword: String,

        accountLevel: String,

        accountRank: String,

        accountCoins: String,

        accountServer: String
    ) {

        // -----------------------------
        // التحقق من تسجيل الدخول
        // -----------------------------

        val sellerId = authRepository.currentUid

        if (sellerId == null) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "يجب تسجيل الدخول أولًا"
            )

            return
        }

        // -----------------------------
        // التحقق من الصور
        // -----------------------------

        if (imageUris.isEmpty()) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "من فضلك اختر صورة واحدة على الأقل للحساب"
            )

            return
        }

        // -----------------------------
        // التحقق من اللعبة
        // -----------------------------

        if (gameId.isBlank()) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "من فضلك اختر اللعبة"
            )

            return
        }

        // -----------------------------
        // التحقق من عنوان الإعلان
        // -----------------------------

        if (title.isBlank()) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "اكتب عنوان الإعلان"
            )

            return
        }

        // -----------------------------
        // التحقق من بيانات الدخول
        // -----------------------------

        if (accountUsername.isBlank()) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "اكتب اسم المستخدم أو ID الحساب"
            )

            return
        }

        if (accountPassword.isBlank()) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "اكتب كلمة سر الحساب"
            )

            return
        }

        // -----------------------------
        // التحقق من السعر
        // -----------------------------

        if (price <= 0) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "السعر يجب أن يكون أكبر من صفر"
            )

            return
        }

        // -----------------------------
        // منع كمية غير صحيحة
        // -----------------------------

        if (stock < 1) {

            _uiState.value = _uiState.value.copy(
                errorMessage = "الكمية يجب أن تكون 1 على الأقل"
            )

            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null,
                success = false
            )

            try {

                val sellerName =
                    authRepository
                        .fetchCurrentUserProfile()
                        .getOrNull()
                        ?.displayName
                        ?: ""

                /*
                 * ملاحظة:
                 *
                 * sellerId لا يُرسل من التطبيق؛ الـCloud Function تحدده من
                 * جلسة المستخدم المسجّل دخوله (request.auth.uid) حتى لا
                 * يستطيع أحد انتحال هوية بائع آخر.
                 *
                 * لا نحفظ كلمة السر داخل واجهة المستخدم بعد الإرسال
                 * ولا نعرضها داخل بيانات المنتج العامة.
                 */

                productRepository.createProduct(
                    sellerName = sellerName,
                    gameId = gameId,
                    title = title,
                    description = description,
                    imageUris = imageUris,
                    price = price,
                    currencyCode = currencyCode,
                    category = category,
                    stock = stock,

                    // بيانات الحساب
                    accountUsername = accountUsername,
                    accountPassword = accountPassword,
                    accountEmail = accountEmail,
                    accountEmailPassword = accountEmailPassword,
                    accountLevel = accountLevel,
                    accountRank = accountRank,
                    accountCoins = accountCoins,
                    accountServer = accountServer

                ).fold(

                    onSuccess = {

                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            success = true,
                            errorMessage = null
                        )
                    },

                    onFailure = { error ->

                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            success = false,
                            errorMessage =
                                error.message
                                    ?: "فشل نشر الحساب، حاول مرة أخرى"
                        )
                    }
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    success = false,
                    errorMessage =
                        e.message
                            ?: "حدث خطأ أثناء نشر الحساب"
                )
            }
        }
    }

    fun clearError() {

        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun resetSuccess() {

        _uiState.value = _uiState.value.copy(
            success = false
        )
    }
}
