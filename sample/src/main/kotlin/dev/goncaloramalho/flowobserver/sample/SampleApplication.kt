package dev.goncaloramalho.flowobserver.sample

import android.app.Application
import dev.goncaloramalho.flowobserver.FlowObserver
import dev.goncaloramalho.flowobserver.FlowObserverSettings

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FlowObserver.configure(
            FlowObserverSettings(enabled = true),
        )
    }
}
