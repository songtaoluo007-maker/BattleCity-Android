package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.SupportSkillType
import com.songtaoluo.battlecity.model.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EngineTacticalFlowTest {
    @Test
    fun smokeConsumesPointsAndStartsCooldown() {
        val engine = GameEngine(Random(1))

        val activated = engine.useSupportSkill(SupportSkillType.SMOKE_SCREEN)

        assertTrue(activated)
        assertEquals(2, engine.commandPoints)
        assertNotNull(engine.smokeArea)
        assertTrue(engine.supportCooldownMs(SupportSkillType.SMOKE_SCREEN) > 0f)
        assertFalse(engine.useSupportSkill(SupportSkillType.SMOKE_SCREEN))
    }

    @Test
    fun repairWithoutDamageDoesNotConsumePoints() {
        val engine = GameEngine(Random(2))

        val activated = engine.useSupportSkill(SupportSkillType.EMERGENCY_REPAIR)

        assertFalse(activated)
        assertEquals(3, engine.commandPoints)
    }

    @Test
    fun playerCollectsShieldSupply() {
        val engine = GameEngine(Random(3))
        engine.player.shieldMs = 0f
        engine.powerUps += PowerUp(
            PowerUpType.SHIELD,
            Vec2(engine.player.position.x, engine.player.position.y),
        )

        engine.update(0f, null)

        assertTrue(engine.powerUps.isEmpty())
        assertEquals(GameConstants.POWER_UP_SHIELD_MS, engine.player.shieldMs, 0.001f)
        assertEquals(4, engine.commandPoints)
    }

    @Test
    fun mineIsConsumedAndBreaksTracks() {
        val engine = GameEngine(Random(4))
        val column = (engine.player.position.x / GameConstants.TILE_SIZE).toInt()
        val row = (engine.player.position.y / GameConstants.TILE_SIZE).toInt()
        engine.tiles[row][column] = TileType.MINE

        engine.update(0f, null)

        assertEquals(TileType.EMPTY, engine.tiles[row][column])
        assertEquals(GameConstants.MINE_TRACK_MS, engine.player.trackBrokenMs, 0.001f)
    }
}
