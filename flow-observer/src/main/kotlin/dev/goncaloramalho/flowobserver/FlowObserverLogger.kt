package dev.goncaloramalho.flowobserver

/**
 * Receives formatted observer messages.
 *
 * Use a lambda to redirect output (for example to `Log.d` or Timber):
 * ```
 * FlowObserverLogger { tag, message -> Log.d(tag, message) }
 * ```
 */
fun interface FlowObserverLogger {
    /** Logs a single observer message under [tag]. */
    fun log(tag: String, message: String)
}
