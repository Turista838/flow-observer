package dev.goncaloramalho.flowobserver

internal object FlowObserverLog {
    fun log(tag: String, message: String) {
        val settings = FlowObserver.settings
        if (!settings.enabled) return

        val logger = settings.logger
        if (logger != null) {
            logger.log(tag, message)
            return
        }

        // Default Log.i on Android when no custom logger is configured.
        try {
            val logClass = Class.forName("android.util.Log")
            logClass
                .getMethod("i", String::class.java, String::class.java)
                .invoke(null, tag, message)
        } catch (_: Throwable) {
            println("I/$tag: $message")
        }
    }
}
