package com.songtaoluo.battlecity.game

import kotlin.math.floor

/**
 * Accumulates frame deltas without discarding sub-millisecond fractions.
 */
class BattleClock {
    private var elapsedExactMs: Double = 0.0

    val elapsedMs: Long
        get() = floor(elapsedExactMs).toLong()

    val elapsedSeconds: Double
        get() = elapsedExactMs / 1000.0

    fun advance(deltaSeconds: Float) {
        if (!deltaSeconds.isFinite() || deltaSeconds <= 0f) return
        elapsedExactMs += deltaSeconds.toDouble() * 1000.0
    }

    fun reset() {
        elapsedExactMs = 0.0
    }
}
