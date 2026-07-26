package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.MutableSharedFlow

internal class LoggingMutableSharedFlow<T>(
    private val inner: MutableSharedFlow<T>,
    private val tag: String,
    private val subscriptionLogging: SubscriptionLogging,
) : MutableSharedFlow<T> by inner {

    override suspend fun emit(value: T) {
        logEvent(value)
        inner.emit(value)
    }

    override fun tryEmit(value: T): Boolean {
        val success = inner.tryEmit(value)
        if (success) {
            logEvent(value)
        }
        return success
    }

    private fun logEvent(value: T) {
        FlowObserverLog.logIfAllowed(
            tag = tag,
            message = "event { $value }",
            subscriptionLogging = subscriptionLogging,
            subscriptionCount = inner.subscriptionCount.value,
        )
    }
}
