package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.TeamSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EngineObjectiveFlowTest {
    @Test
    fun sovietDefenseWinsWhenHoldTimerExpires() {
        val scenario = ScenarioCatalog.kurskSovietDefense.copy(
            enemyBudget = 0,
            maxActiveEnemies = 0,
            enemySpawns = emptyList(),
        )
        val engine = GameEngine(Random(31), scenario)

        engine.update(150f, null)

        assertTrue(engine.victory)
        assertFalse(engine.gameOver)
        assertEquals(0L, engine.objectiveRemainingMs)
        assertTrue(engine.combatMessage.contains("坚守成功"))
    }

    @Test
    fun enemyShellDestroyingDefenseBaseEndsMission() {
        val scenario = ScenarioCatalog.kurskSovietDefense.copy(
            enemyBudget = 0,
            maxActiveEnemies = 0,
            enemySpawns = emptyList(),
        )
        val engine = GameEngine(Random(32), scenario)
        val baseRow = engine.tiles.indexOfFirst { row -> row.contains(com.songtaoluo.battlecity.model.TileType.BASE) }
        val baseColumn = engine.tiles[baseRow].indexOf(com.songtaoluo.battlecity.model.TileType.BASE)
        engine.bullets += Bullet(
            ownerId = 999,
            team = TeamSide.ENEMY,
            faction = Faction.GERMAN,
            position = Vec2(
                baseColumn * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                baseRow * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
            ),
            direction = Direction.DOWN,
            speed = 0f,
            power = 2,
            penetration = 100,
        )

        engine.update(0f, null)

        assertTrue(engine.baseDestroyed)
        assertTrue(engine.gameOver)
        assertFalse(engine.victory)
        assertTrue(engine.combatMessage.contains("基地失守"))
    }
}
