package dev.goncaloramalho.flowobserver.sample.screenscoped

import androidx.lifecycle.ViewModel
import dev.goncaloramalho.flowobserver.ObserveFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreenScopedUiState(
    val taps: Int = 0,
)

/**
 * Owned by the Nav destination back-stack entry, not the Activity.
 * Cleared when you leave this screen; a new instance is created on re-entry.
 */
class ScreenScopedViewModel : ViewModel() {

    @ObserveFlow(tag = "ScreenScopedViewModel.uiState")
    private val _uiState = MutableStateFlow(ScreenScopedUiState())
    val uiState: StateFlow<ScreenScopedUiState> = _uiState.asStateFlow()

    fun tap() {
        _uiState.value = _uiState.value.copy(taps = _uiState.value.taps + 1)
    }

    fun reset() {
        _uiState.value = ScreenScopedUiState()
    }
}
