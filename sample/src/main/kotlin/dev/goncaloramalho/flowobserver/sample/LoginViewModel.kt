package dev.goncaloramalho.flowobserver.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.goncaloramalho.flowobserver.FlowObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
)

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    @FlowObserver
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    @FlowObserver(tag = "Login.events")
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun login() {
        if (_uiState.value.loading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            delay(500)
            _uiState.value = _uiState.value.copy(loading = false, loggedIn = true)
            _events.emit(LoginEvent.ShowMessage("Welcome, ${_uiState.value.username}!"))
            _events.emit(LoginEvent.NavigateHome)
        }
    }

    fun reset() {
        _uiState.value = LoginUiState()
    }
}
