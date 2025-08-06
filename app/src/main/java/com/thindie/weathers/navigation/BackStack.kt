package com.thindie.weathers.navigation

import androidx.compose.runtime.Stable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Stable
class BackStack {
    private val _backStack = MutableStateFlow<List<Event>?>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentScreen: Flow<Screen?> =
        _backStack
            .map { it?.filterIsInstance<Screen>() }
            .flatMapLatest { if (it?.isEmpty() == true) emptyFlow() else flowOf(it?.last()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFlow: Flow<Segment?> =
        _backStack
            .map { it?.filterIsInstance<Segment>() }
            .flatMapLatest { if (it?.isEmpty() == true) emptyFlow() else flowOf(it?.last()) }

    fun pop() {
        val currentStack = _backStack.value ?: emptyList()
        if (currentStack.isEmpty()) return

        val newStack = currentStack.dropLast(1)
        _backStack.value = newStack.dropLastWhile { it is Segment }.ifEmpty { null }
    }

    fun push(screen: Screen) {
        val currentStack = _backStack.value ?: emptyList()
        _backStack.value = currentStack + screen
    }

    fun push(segment: Segment) {
        val currentStack = _backStack.value ?: emptyList()
        _backStack.value = currentStack + segment
    }

    fun popTo(screen: Screen) {
        val currentStack = _backStack.value ?: emptyList()
        if (currentStack.isEmpty() == true) return
        val existing = currentStack.indexOfFirst { it is Screen && it.id == screen.id }
        if (existing == -1) return
        val newStack = currentStack.subList(0, existing + 1)
        _backStack.value = newStack.dropLastWhile { it is Segment }.ifEmpty { null }
    }

    fun empty(): Boolean {
        return _backStack.value.isNullOrEmpty()
    }
}
