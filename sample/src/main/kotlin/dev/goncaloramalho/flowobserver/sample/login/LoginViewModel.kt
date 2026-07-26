package dev.goncaloramalho.flowobserver.sample.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.goncaloramalho.flowobserver.ObserveFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val isLoading: Boolean = false,
)

class LoginViewModel : ViewModel() {

    @ObserveFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun login(onSuccess: (String) -> Unit) {
        val username = _uiState.value.username.trim()
        if (username.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(500)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess(username)
        }
    }
}
