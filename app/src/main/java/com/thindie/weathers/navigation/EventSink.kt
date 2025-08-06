package com.thindie.weathers.navigation

interface EventSink {
    fun sendEvent(event: Event)
}
