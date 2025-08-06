package com.thindie.weathers.navigation

class Router(private val backStack: BackStack) : EventSink {
    val currentScreen = backStack.currentScreen
    val currentFlow = backStack.currentFlow

    override fun sendEvent(event: Event) {
        when (event) {
            is Screen -> backStack.push(event)
            is BackPress -> backStack.pop()
            is Segment -> {
                backStack.push(event)
                sendEvent(event.startScreen)
            }
        }
    }
}
