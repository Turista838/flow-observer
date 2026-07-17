package dev.goncaloramalho.flowobserver

object FlowObserver {

    @Volatile
    var settings: FlowObserverSettings = FlowObserverSettings()
        private set

    fun configure(settings: FlowObserverSettings) {
        this.settings = settings
    }
}
