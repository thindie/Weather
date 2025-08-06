package com.thindie.weathers

import android.app.Application
import com.thindie.weathers.navigation.BackStack

class WeatherApplication : Application() {
    val backStack: BackStack = BackStack()

    override fun onCreate() {
        super.onCreate()
    }
}
