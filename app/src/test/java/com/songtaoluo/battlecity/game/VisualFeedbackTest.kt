package com.songtaoluo.battlecity.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualFeedbackTest {
    @Test
    fun temporaryVisualExpiresAfterItsDuration() {
        val visual = ImpactEffectSystem.spark(Vec2(40f, 60f))
        val visuals = mutableListOf(visual)

        ImpactEffectSystem.update(visuals, 0.11f)
        assertTrue(visual.progress in 0.49f..0.51f)
        assertEquals(1, visuals.size)

        ImpactEffectSystem.update(visuals, 0.12f)
        assertTrue(visuals.isEmpty())
    }

    @Test
    fun visualKeepsItsOriginalPosition() {
        val source = Vec2(10f, 20f)
        val visual = ImpactEffectSystem.hitFlash(source)
        source.x = 999f

        assertEquals(10f, visual.position.x, 0.001f)
        assertEquals(20f, visual.position.y, 0.001f)
    }
}
