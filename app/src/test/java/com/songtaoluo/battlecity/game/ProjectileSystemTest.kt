package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectileSystemTest {
    @Test
    fun brickAndVillageAreDestroyed() {
        listOf(TileType.BRICK, TileType.VILLAGE).forEach { tileType ->
            val tiles = boardWith(tileType)
            val impact = ProjectileSystem.resolveTileImpact(bullet(penetration = 80), tiles)
            assertTrue(impact.consumed)
            assertTrue(impact.destroyedTile)
            assertEquals(10, impact.scoreDelta)
            assertEquals(TileType.EMPTY, tiles[5][5])
        }
    }

    @Test
    fun weakShellCannotDestroySteel() {
        val tiles = boardWith(TileType.STEEL)
        val impact = ProjectileSystem.resolveTileImpact(bullet(penetration = 80), tiles)
        assertTrue(impact.consumed)
        assertFalse(impact.destroyedTile)
        assertEquals(TileType.STEEL, tiles[5][5])
    }

    @Test
    fun highPenetrationShellDestroysSteel() {
        val tiles = boardWith(TileType.STEEL)
        val impact = ProjectileSystem.resolveTileImpact(bullet(penetration = 145), tiles)
        assertTrue(impact.consumed)
        assertTrue(impact.destroyedTile)
        assertEquals(20, impact.scoreDelta)
        assertEquals(TileType.EMPTY, tiles[5][5])
    }

    @Test
    fun waterAndForestDoNotConsumeShell() {
        listOf(TileType.WATER, TileType.FOREST).forEach { tileType ->
            val impact = ProjectileSystem.resolveTileImpact(
                bullet(penetration = 80),
                boardWith(tileType),
            )
            assertFalse(impact.consumed)
        }
    }

    @Test
    fun baseImpactIsReported() {
        val impact = ProjectileSystem.resolveTileImpact(
            bullet(penetration = 80),
            boardWith(TileType.BASE),
        )
        assertTrue(impact.consumed)
        assertTrue(impact.baseHit)
    }

    private fun boardWith(tileType: TileType): MutableList<MutableList<TileType>> =
        MutableList(GameConstants.BOARD_TILES) { row ->
            MutableList(GameConstants.BOARD_TILES) { column ->
                if (row == 5 && column == 5) tileType else TileType.EMPTY
            }
        }

    private fun bullet(penetration: Int): Bullet = Bullet(
        ownerId = 1,
        team = TeamSide.PLAYER,
        faction = Faction.GERMAN,
        position = Vec2(
            x = 5 * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
            y = 5 * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
        ),
        direction = Direction.UP,
        speed = 345f,
        power = 2,
        penetration = penetration,
    )
}
