package dev.goncaloramalho.flowobserver

/**
 * Global entry point for Flow Observer configuration.
 *
 * Call [configure] once at app startup (for example in `Application.onCreate`).
 * [addObservable] wrappers read [settings] when emitting logs.
 */
object FlowObserver {

    /** Current settings; defaults until [configure] is called. */
    @Volatile
    var settings: FlowObserverSettings = FlowObserverSettings()
        private set

    /** Replaces the active [FlowObserverSettings]. */
    fun configure(settings: FlowObserverSettings) {
        this.settings = settings
    }
}
