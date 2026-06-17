package com.songtaoluo.battlecity.game

import org.junit.Assert.assertEquals
import org.junit.Test

class BattleClockTest {
    @Test
    fun sixtyFpsFramesDoNotLoseFractionalMilliseconds() {
        val clock = BattleClock()

        repeat(9_000) {
            clock.advance(1f / 60f)
        }

        assertEquals(150_000L, clock.elapsedMs)
    }

    @Test
    fun invalidAndNegativeDeltasAreIgnored() {
        val clock = BattleClock()

        clock.advance(-1f)
        clock.advance(Float.NaN)
        clock.advance(Float.POSITIVE_INFINITY)

        assertEquals(0L, clock.elapsedMs)
    }

    @Test
    fun resetClearsExactAndRoundedTime() {
        val clock = BattleClock()
        clock.advance(0.0166667f)

        clock.reset()

        assertEquals(0L, clock.elapsedMs)
        assertEquals(0.0, clock.elapsedSeconds, 0.0)
    }
}
