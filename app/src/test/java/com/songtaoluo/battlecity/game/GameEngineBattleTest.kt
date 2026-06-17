package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineBattleTest {
    @Test
    fun battleStartsWithThreeScenarioSpawnUnits() {
        val engine = GameEngine()

        assertEquals(3, engine.enemies.size)
        assertEquals(18, engine.enemiesLeft)
        assertEquals(3, engine.enemies.map { it.id }.distinct().size)
    }

    @Test
    fun battleStartsWithTwoScenarioAllies() {
        val engine = GameEngine()

        assertEquals(2, engine.allies.size)
        assertEquals(2, engine.alliesAlive)
        assertEquals(
            setOf(VehicleId.PZ_IV_H, VehicleId.TIGER_I),
            engine.allies.map { it.vehicleId }.toSet(),
        )
    }

    @Test
    fun firstWaveUsesAllThreeKurskEnemyVehicles() {
        val vehicleIds = GameEngine().enemies.map { it.vehicleId }.toSet()

        assertEquals(
            setOf(VehicleId.T34_76, VehicleId.T70, VehicleId.SU_152),
            vehicleIds,
        )
    }

    @Test
    fun reinforcementSpawnsUntilActiveLimit() {
        val engine = GameEngine()

        engine.update(0.6f, null)
        engine.update(0.6f, null)

        assertTrue(engine.enemies.size >= 4)
        assertTrue(engine.enemies.size <= engine.scenario.maxActiveEnemies)
        assertEquals(18, engine.enemiesLeft)
    }

    @Test
    fun playerStartsAtScenarioSpawnPoint() {
        val engine = GameEngine()
        val expectedX = engine.scenario.playerSpawn.x * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f
        val expectedY = engine.scenario.playerSpawn.y * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f

        assertEquals(expectedX, engine.player.position.x, 0.001f)
        assertEquals(expectedY, engine.player.position.y, 0.001f)
    }

    @Test
    fun squadOrderCanBeChangedAtRuntime() {
        val engine = GameEngine()

        engine.setSquadOrder(SquadOrder.ASSAULT)

        assertEquals(SquadOrder.ASSAULT, engine.squadOrder)
        assertTrue(engine.combatMessage.contains("突击"))
    }
}
