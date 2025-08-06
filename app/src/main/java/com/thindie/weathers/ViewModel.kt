package com.thindie.weathers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

abstract class ViewModel<VS : ViewState, VI : ViewIntents>(
    val scope: CoroutineScope = CoroutineScope(context = Dispatchers.Default + SupervisorJob()),
) {
    internal val _state: MutableStateFlow<VS> = MutableStateFlow(startWith())
    val state =
        _state.stateIn(
            scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = startWith(),
        )

    val intents: VI = listen()

    abstract fun startWith(): VS

    abstract fun listen(): VI

    abstract fun execute()

    open fun onDestroy() {
        scope.cancel("ViewModel destroyed")
    }
}

abstract class ViewState

abstract class ViewIntents

class ViewIntent<T> private constructor() {
    val eventPayload =
        MutableSharedFlow<T>(
            extraBufferCapacity = 15,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val event =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 15,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    operator fun invoke() {
        event.tryEmit(Unit)
    }

    operator fun invoke(t: T) {
        eventPayload.tryEmit(t)
    }

    companion object {
        fun <T> intentPayload() = ViewIntent<T>()

        fun intent() = ViewIntent<Unit>()
    }
}

fun <VS : ViewState, VI : ViewIntents, T> ViewModel<VS, VI>.intent(
    intent: ViewIntent<T>,
    effect: (oldState: VS, newState: VS) -> Unit = { _, _ -> },
    update: (VS) -> VS = { it },
) {
    scope.launch {
        intent.event
            .collect {
                var stateBefore: VS? = null
                val newState =
                    _state.updateAndGet { current ->
                        stateBefore = current
                        update(current)
                    }
                effect(requireNotNull(stateBefore), newState)
            }
    }
}

fun <VS : ViewState, VI : ViewIntents, T> ViewModel<VS, VI>.intent1(
    intent: ViewIntent<T>,
    effect: (oldState: VS, newState: VS, T) -> Unit = { _, _, _ -> },
    update: (VS, T) -> VS = { it, _ -> it },
) {
    scope.launch {
        intent.eventPayload
            .filterNotNull()
            .catch { }
            .collect { payload: T ->
                var stateBefore: VS? = null
                val newState =
                    _state.updateAndGet { current ->
                        stateBefore = current
                        update(current, payload)
                    }
                effect(requireNotNull(stateBefore), newState, payload)
            }
    }
}

fun <VS : ViewState, VI : ViewIntents, T : Any?> ViewModel<VS, VI>.transition(
    flow: Flow<T>,
    effect: (oldState: VS, newState: VS, T?) -> Unit = { _, _, _ -> },
    update: (VS, T) -> VS,
) {
    scope.launch {
        flow
            .catch { }
            .collect { payload: T ->
                var stateBefore: VS? = null
                val newState =
                    _state.updateAndGet { current ->
                        stateBefore = current
                        update(current, payload)
                    }
                effect(requireNotNull(stateBefore), newState, payload)
            }
    }
}
