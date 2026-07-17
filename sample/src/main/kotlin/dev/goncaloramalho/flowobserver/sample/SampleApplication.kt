package dev.goncaloramalho.flowobserver.sample

import android.app.Application
import android.util.Log
import dev.goncaloramalho.flowobserver.FlowObserver
import dev.goncaloramalho.flowobserver.FlowObserverLogger
import dev.goncaloramalho.flowobserver.FlowObserverSettings

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FlowObserver.configure(
            FlowObserverSettings(
                enabled = BuildConfig.DEBUG,
                logger = FlowObserverLogger { tag, message -> Log.d(tag, message) },
            ),
        )
    }
}
