package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.MutableStateFlow

internal class LoggingMutableStateFlow<T>(
    private val inner: MutableStateFlow<T>,
    private val tag: String,
    private val subscriptionLogging: SubscriptionLogging,
) : MutableStateFlow<T> by inner {

    override var value: T
        get() = inner.value
        set(value) {
            val previous = inner.value
            inner.value = value
            if (previous != value) {
                logChange(previous, value)
            }
        }

    override fun compareAndSet(expect: T, update: T): Boolean {
        val success = inner.compareAndSet(expect, update)
        if (success && expect != update) {
            logChange(expect, update)
        }
        return success
    }

    override suspend fun emit(value: T) {
        val previous = inner.value
        inner.emit(value)
        if (previous != value) {
            logChange(previous, value)
        }
    }

    override fun tryEmit(value: T): Boolean {
        val previous = inner.value
        val success = inner.tryEmit(value)
        if (success && previous != value) {
            logChange(previous, value)
        }
        return success
    }

    private fun logChange(previous: T, current: T) {
        FlowObserverLog.logIfAllowed(
            tag = tag,
            message = "change { previousState: $previous, currentState: $current }",
            subscriptionLogging = subscriptionLogging,
            subscriptionCount = inner.subscriptionCount.value,
        )
    }
}
