package dev.goncaloramalho.flowobserver.sample.whilesubscribed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class WhileSubscribedUiState(
    val tick: Int = 0,
)

/**
 * Activity-scoped ViewModel with a cold upstream shared via [SharingStarted.WhileSubscribed].
 * Not annotated with @ObserveFlow — emit-side observation targets MutableStateFlow / MutableSharedFlow.
 */
class WhileSubscribedViewModel : ViewModel() {

    private val upstream = flow {
        var tick = 0
        while (true) {
            tick++
            Log.d(UPSTREAM_TAG, "tick #$tick")
            emit(WhileSubscribedUiState(tick = tick))
            delay(1_000)
        }
    }

    val uiState: StateFlow<WhileSubscribedUiState> = upstream.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 2_000),
        initialValue = WhileSubscribedUiState(),
    )

    companion object {
        const val UPSTREAM_TAG = "WhileSubscribed.upstream"
    }
}
