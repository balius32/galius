package com.balius.galius.core.mvi

/**
 * Pure state reducer: (State, Intent) → State.
 * Keep free of IO / Android framework calls.
 */
fun interface Reducer<State, Intent> {
    fun reduce(state: State, intent: Intent): State
}
