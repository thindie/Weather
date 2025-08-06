package com.thindie.weathers.feature.main

import com.thindie.weathers.navigation.Event
import com.thindie.weathers.navigation.EventSink
import com.thindie.weathers.navigation.Flow
import com.thindie.weathers.navigation.Route
import com.thindie.weathers.navigation.Screen
import com.thindie.weathers.navigation.buildFlow
import com.thindie.weathers.navigation.produceScreen

interface AppFlow {
    companion object {
        operator fun invoke(eventSink: EventSink): Flow<Unit, AppFlowEvents> {
            return buildFlow(AppFlowInstance(eventSink = eventSink))
        }
    }

    private data class AppFlowInstance(
        val eventSink: EventSink,
    ) : Flow<Unit, AppFlowEvents>(router = eventSink), AppFlow {
        override val startScreen: Screen =
            produceScreen {  }

        override fun mapScreen(route: AppFlowEvents): Screen {
            return when (route) {
                AppFlowEvents.Profile -> startScreen
                AppFlowEvents.Settings -> produceScreen { }
                AppFlowEvents.Home -> produceScreen {  }
            }
        }

        override fun filterEvent(route: Event): Boolean {
            return route is AppFlowEvents
        }

        override fun start(params: Unit) {
            router.sendEvent(this)
        }
    }

    sealed interface AppFlowEvents : Route {
        data object Profile : AppFlowEvents
        data object Settings : AppFlowEvents
        data object Home : AppFlowEvents
    }
}
