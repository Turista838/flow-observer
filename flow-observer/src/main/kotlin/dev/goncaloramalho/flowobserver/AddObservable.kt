package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Wraps this [StateFlow] so each collector logs state changes on the same subscription chain.
 *
 * Prefer annotating the property with [ObserveFlow] and letting the compiler plugin inject this
 * call. Calling it manually is supported and idempotent with the plugin (already-wrapped flows
 * are left as-is by the plugin).
 */
fun <T> StateFlow<T>.addObservable(tag: String): StateFlow<T> {
    if (this is LoggingStateFlow) return this
    return LoggingStateFlow(this, tag)
}

/**
 * Wraps this [SharedFlow] so each collector logs events on the same subscription chain.
 *
 * Prefer annotating the property with [ObserveFlow] and letting the compiler plugin inject this
 * call. Calling it manually is supported and idempotent with the plugin.
 */
fun <T> SharedFlow<T>.addObservable(tag: String): SharedFlow<T> {
    if (this is LoggingSharedFlow) return this
    return LoggingSharedFlow(this, tag)
}
