package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.MutableSharedFlow

internal class LoggingMutableSharedFlow<T>(
    private val inner: MutableSharedFlow<T>,
    private val tag: String,
) : MutableSharedFlow<T> by inner {

    override suspend fun emit(value: T) {
        FlowObserverLog.log(tag, "event { $value }")
        inner.emit(value)
    }

    override fun tryEmit(value: T): Boolean {
        val success = inner.tryEmit(value)
        if (success) {
            FlowObserverLog.log(tag, "event { $value }")
        }
        return success
    }
}
