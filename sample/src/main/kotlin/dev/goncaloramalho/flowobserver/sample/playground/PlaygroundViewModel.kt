package dev.goncaloramalho.flowobserver.sample.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.goncaloramalho.flowobserver.ObserveFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class PlaygroundUiState(
    val counter: Int = 0,
    val isTickerRunning: Boolean = false,
    val statusMessage: String = "Idle",
)

class PlaygroundViewModel : ViewModel() {

    @ObserveFlow
    private val _uiState = MutableStateFlow(PlaygroundUiState())
    val uiState: StateFlow<PlaygroundUiState> = _uiState.asStateFlow()

    @ObserveFlow(tag = "Playground.ticks")
    private val _ticks = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val ticks: SharedFlow<Long> = _ticks.asSharedFlow()

    @ObserveFlow(tag = "Playground.pulses")
    private val _pulses = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val pulses: SharedFlow<String> = _pulses.asSharedFlow()

    private var tickerJob: Job? = null
    private var pulseCount = 0

    fun increment() {
        _uiState.value = _uiState.value.copy(counter = _uiState.value.counter + 1)
    }

    fun decrement() {
        _uiState.value = _uiState.value.copy(counter = _uiState.value.counter - 1)
    }

    fun resetCounter() {
        _uiState.value = _uiState.value.copy(counter = 0)
    }

    fun startTicker() {
        if (_uiState.value.isTickerRunning) return

        _uiState.value = _uiState.value.copy(isTickerRunning = true, statusMessage = "Ticker running")
        var tickNumber = 0L
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(3_000)
                tickNumber++
                _ticks.emit(tickNumber)
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
        _uiState.value = _uiState.value.copy(
            isTickerRunning = false,
            statusMessage = "Ticker stopped",
        )
    }

    fun sendPulse() {
        pulseCount++
        viewModelScope.launch {
            _pulses.emit("Pulse #$pulseCount")
        }
    }

    fun burstPulses() {
        viewModelScope.launch {
            repeat(3) { index ->
                pulseCount++
                _pulses.emit("Burst pulse #$pulseCount (shot ${index + 1}/3)")
            }
        }
    }

    fun simulateLoad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusMessage = "Loading…")
            delay(2.seconds)
            _uiState.value = _uiState.value.copy(statusMessage = "Done")
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
