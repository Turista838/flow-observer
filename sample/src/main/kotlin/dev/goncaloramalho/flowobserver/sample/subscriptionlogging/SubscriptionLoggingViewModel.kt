package dev.goncaloramalho.flowobserver.sample.subscriptionlogging

import androidx.lifecycle.ViewModel
import dev.goncaloramalho.flowobserver.ObserveFlow
import dev.goncaloramalho.flowobserver.SubscriptionLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Demo for [SubscriptionLogging] vs [dev.goncaloramalho.flowobserver.FlowObserverSettings.logOnlyWhenSubscribed].
 *
 * - [defaultState] follows global settings (`SubscriptionLogging.Default`).
 * - [alwaysState] always logs writes (`SubscriptionLogging.Always`), ignoring settings.
 */
class SubscriptionLoggingViewModel : ViewModel() {

    @ObserveFlow(
        tag = DEFAULT_TAG,
        subscriptionLogging = SubscriptionLogging.Default,
    )
    private val _defaultState = MutableStateFlow(0)
    val defaultState: StateFlow<Int> = _defaultState.asStateFlow()
    val defaultSubscriptionCount: StateFlow<Int> = _defaultState.subscriptionCount

    @ObserveFlow(
        tag = ALWAYS_TAG,
        subscriptionLogging = SubscriptionLogging.Always,
    )
    private val _alwaysState = MutableStateFlow(0)
    val alwaysState: StateFlow<Int> = _alwaysState.asStateFlow()
    val alwaysSubscriptionCount: StateFlow<Int> = _alwaysState.subscriptionCount

    fun bumpDefault() {
        _defaultState.value = _defaultState.value + 1
    }

    fun bumpAlways() {
        _alwaysState.value = _alwaysState.value + 1
    }

    companion object {
        const val DEFAULT_TAG = "Demo.default"
        const val ALWAYS_TAG = "Demo.always"
    }
}
