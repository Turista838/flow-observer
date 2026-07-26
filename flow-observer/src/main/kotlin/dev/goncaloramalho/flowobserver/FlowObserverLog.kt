package dev.goncaloramalho.flowobserver

internal object FlowObserverLog {
    fun shouldLog(
        subscriptionLogging: SubscriptionLogging,
        subscriptionCount: Int,
    ): Boolean {
        if (!FlowObserver.settings.enabled) return false

        val onlyWhenSubscribed = when (subscriptionLogging) {
            SubscriptionLogging.Default -> FlowObserver.settings.logOnlyWhenSubscribed
            SubscriptionLogging.OnlyWhenSubscribed -> true
            SubscriptionLogging.Always -> false
        }

        if (!onlyWhenSubscribed) return true
        return subscriptionCount > 0
    }

    fun log(tag: String, message: String) {
        val logger = FlowObserver.settings.logger
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

    fun logIfAllowed(
        tag: String,
        message: String,
        subscriptionLogging: SubscriptionLogging,
        subscriptionCount: Int,
    ) {
        if (!shouldLog(subscriptionLogging, subscriptionCount)) return
        log(tag, message)
    }
}
