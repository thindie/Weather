package com.thindie.weathers.navigation

import com.thindie.weathers.dependencies.Dependencies

interface Segment : Event, EventSink {
    val startScreen: Screen
}

abstract class Flow<PARAMS, ROUTE : Route>(
    val router: EventSink,
) : Segment {
    override fun sendEvent(event: Event) {
        if (filterEvent(event)) {
            val screen = mapScreen(event as ROUTE)
            router.sendEvent(screen)
        }
    }

    abstract fun mapScreen(route: ROUTE): Screen

    abstract fun filterEvent(route: Event): Boolean

    abstract fun start(params: PARAMS)

    open fun <Deps : Dependencies> dependencies(): Deps? = null
}

fun <PARAMS, R : Route> buildFlow(
    params: PARAMS,
    flow: Flow<PARAMS, R>,
): Flow<PARAMS, *> {
    return flow.also { it.start(params) }
}

fun <R : Route> buildFlow(flow: Flow<Unit, R>): Flow<Unit, R> {
    return flow.also { it.start(Unit) }
}
