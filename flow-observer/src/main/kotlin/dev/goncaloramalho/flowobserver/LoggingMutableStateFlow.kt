package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.MutableStateFlow

internal class LoggingMutableStateFlow<T>(
    private val inner: MutableStateFlow<T>,
    private val tag: String,
) : MutableStateFlow<T> by inner {

    override var value: T
        get() = inner.value
        set(value) {
            val previous = inner.value
            inner.value = value
            if (previous != value) {
                FlowObserverLog.log(
                    tag,
                    "change { previousState: $previous, currentState: $value }",
                )
            }
        }

    override fun compareAndSet(expect: T, update: T): Boolean {
        val success = inner.compareAndSet(expect, update)
        if (success && expect != update) {
            FlowObserverLog.log(
                tag,
                "change { previousState: $expect, currentState: $update }",
            )
        }
        return success
    }

    override suspend fun emit(value: T) {
        val previous = inner.value
        inner.emit(value)
        if (previous != value) {
            FlowObserverLog.log(
                tag,
                "change { previousState: $previous, currentState: $value }",
            )
        }
    }

    override fun tryEmit(value: T): Boolean {
        val previous = inner.value
        val success = inner.tryEmit(value)
        if (success && previous != value) {
            FlowObserverLog.log(
                tag,
                "change { previousState: $previous, currentState: $value }",
            )
        }
        return success
    }
}
