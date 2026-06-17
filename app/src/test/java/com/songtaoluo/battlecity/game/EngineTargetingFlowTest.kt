package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.SupportSkillType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EngineTargetingFlowTest {
    @Test
    fun artilleryDoesNotSpendPointsUntilBoardTap() {
        val engine = GameEngine(Random(10))

        assertTrue(engine.useSupportSkill(SupportSkillType.ARTILLERY_BARRAGE))
        assertEquals(TargetingMode.ARTILLERY, engine.targetingMode)
        assertEquals(3, engine.commandPoints)
        assertEquals(0f, engine.supportCooldownMs(SupportSkillType.ARTILLERY_BARRAGE), 0.001f)

        val emptyX = GameConstants.BOARD_SIZE - GameConstants.TILE_SIZE
        val emptyY = GameConstants.BOARD_SIZE - GameConstants.TILE_SIZE
        assertTrue(engine.handleBoardTap(emptyX, emptyY))
        assertEquals(TargetingMode.NONE, engine.targetingMode)
        assertEquals(0, engine.commandPoints)
        assertTrue(engine.supportCooldownMs(SupportSkillType.ARTILLERY_BARRAGE) > 0f)
    }

    @Test
    fun focusFireRequiresVisibleEnemyHit() {
        val engine = GameEngine(Random(11))
        engine.enemies.forEach { it.isSpotted = false }

        engine.setSquadOrder(SquadOrder.FOCUS_FIRE)
        val first = engine.enemies.first()
        assertFalse(engine.handleBoardTap(first.position.x, first.position.y))
        assertEquals(TargetingMode.FOCUS_FIRE, engine.targetingMode)
        assertNull(engine.selectedFocusTargetId)

        first.isSpotted = true
        assertTrue(engine.handleBoardTap(first.position.x, first.position.y))
        assertEquals(first.id, engine.selectedFocusTargetId)
        assertEquals(TargetingMode.NONE, engine.targetingMode)
    }

    @Test
    fun changingAwayFromFocusClearsSelection() {
        val engine = GameEngine(Random(12))
        val target = engine.enemies.first().apply { isSpotted = true }
        engine.setSquadOrder(SquadOrder.FOCUS_FIRE)
        engine.handleBoardTap(target.position.x, target.position.y)

        engine.setSquadOrder(SquadOrder.FOLLOW)

        assertNull(engine.selectedFocusTargetId)
        assertEquals(TargetingMode.NONE, engine.targetingMode)
    }

    @Test
    fun cancellingSelectionDoesNotSpendResources() {
        val engine = GameEngine(Random(13))
        engine.useSupportSkill(SupportSkillType.ARTILLERY_BARRAGE)

        engine.cancelTargeting()

        assertEquals(TargetingMode.NONE, engine.targetingMode)
        assertEquals(3, engine.commandPoints)
        assertEquals(0f, engine.supportCooldownMs(SupportSkillType.ARTILLERY_BARRAGE), 0.001f)
    }
}
