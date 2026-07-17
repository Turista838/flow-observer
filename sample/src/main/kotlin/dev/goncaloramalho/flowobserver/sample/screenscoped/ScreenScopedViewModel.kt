package dev.goncaloramalho.flowobserver.sample.screenscoped

import androidx.lifecycle.ViewModel
import dev.goncaloramalho.flowobserver.ObserveFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreenScopedUiState(
    val taps: Int = 0,
    val label: String = "Fresh instance",
)

/**
 * Owned by the Nav destination back-stack entry, not the Activity.
 * Cleared when you leave this screen; a new instance is created on re-entry.
 */
class ScreenScopedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenScopedUiState())
    @ObserveFlow(tag = "ScreenScopedViewModel.uiState")
    val uiState: StateFlow<ScreenScopedUiState> = _uiState.asStateFlow()

    init {
        attachFlowObserver()
    }

    fun tap() {
        val next = _uiState.value.taps + 1
        _uiState.value = ScreenScopedUiState(
            taps = next,
            label = "Tap #$next on this instance",
        )
    }

    fun reset() {
        _uiState.value = ScreenScopedUiState(label = "Reset on this instance")
    }
}
