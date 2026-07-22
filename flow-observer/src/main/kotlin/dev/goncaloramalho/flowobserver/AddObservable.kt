package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Wraps this [MutableStateFlow] so state writes are logged (emit side).
 *
 * Prefer annotating the property with [ObserveFlow] and letting the compiler plugin inject this
 * call. Calling it manually is supported and idempotent with the plugin.
 */
fun <T> MutableStateFlow<T>.addObservable(
    tag: String,
    subscriptionLogging: SubscriptionLogging = SubscriptionLogging.Default,
): MutableStateFlow<T> {
    if (this is LoggingMutableStateFlow) return this
    return LoggingMutableStateFlow(this, tag, subscriptionLogging)
}

/**
 * Wraps this [MutableSharedFlow] so [emit] / [tryEmit] are logged (emit side).
 *
 * Prefer annotating the property with [ObserveFlow] and letting the compiler plugin inject this
 * call. Calling it manually is supported and idempotent with the plugin.
 */
fun <T> MutableSharedFlow<T>.addObservable(
    tag: String,
    subscriptionLogging: SubscriptionLogging = SubscriptionLogging.Default,
): MutableSharedFlow<T> {
    if (this is LoggingMutableSharedFlow) return this
    return LoggingMutableSharedFlow(this, tag, subscriptionLogging)
}
