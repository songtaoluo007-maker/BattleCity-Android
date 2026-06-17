package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovementSystemTest {
    @Test
    fun solidTerrainStopsAtLastLegalPosition() {
        val solidTypes = listOf(
            TileType.BRICK,
            TileType.STEEL,
            TileType.BASE,
            TileType.VILLAGE,
        )
        solidTypes.forEach { tileType ->
            val tiles = emptyBoard()
            tiles[5][5] = tileType
            val tank = tankAt(5, 6)
            val originalY = tank.position.y

            val moved = MovementSystem.tryMove(tank, Direction.UP, 20f, tiles)

            assertTrue(moved)
            assertTrue(tank.position.y < originalY)
            assertFalse(MovementSystem.collidesWithSolid(tank, tiles))
        }
    }

    @Test
    fun largeMovementCannotTunnelThroughWall() {
        val tiles = emptyBoard()
        tiles[5][5] = TileType.STEEL
        val tank = tankAt(5, 8)

        MovementSystem.tryMove(tank, Direction.UP, 140f, tiles)

        val wallBottom = 6 * GameConstants.TILE_SIZE
        val tankTop = tank.position.y - GameConstants.TANK_SIZE / 2f - GameConstants.WALL_CLEARANCE
        assertTrue(tankTop >= wallBottom - 0.001f)
        assertFalse(MovementSystem.collidesWithSolid(tank, tiles))
    }

    @Test
    fun softTerrainRemainsPassable() {
        val passableTypes = listOf(
            TileType.WATER,
            TileType.FOREST,
            TileType.OBJECTIVE,
            TileType.MINE,
        )
        passableTypes.forEach { tileType ->
            val tiles = emptyBoard()
            tiles[5][5] = tileType
            val tank = tankAt(5, 6)
            val moved = MovementSystem.tryMove(tank, Direction.UP, 20f, tiles)
            assertTrue(moved)
        }
    }

    @Test
    fun waterAndBrokenTracksStackTheirSpeedPenalties() {
        val tiles = emptyBoard()
        tiles[6][5] = TileType.WATER
        val tank = tankAt(5, 6).apply { trackBrokenMs = 1000f }

        val speed = MovementSystem.effectiveSpeed(tank, tiles)

        assertEquals(
            tank.speed * GameConstants.WATER_SPEED_MULTIPLIER * GameConstants.BROKEN_TRACK_SPEED_MULTIPLIER,
            speed,
            0.001f,
        )
    }

    @Test
    fun timedSpeedBoostAddsConfiguredBonus() {
        val tiles = emptyBoard()
        val tank = tankAt(5, 6).apply { speedBoostMs = 1000f }

        assertEquals(
            tank.speed + GameConstants.POWER_UP_SPEED_BONUS,
            MovementSystem.effectiveSpeed(tank, tiles),
            0.001f,
        )
    }

    @Test
    fun boardBoundaryKeepsTankInside() {
        val tiles = emptyBoard()
        val tank = tankAt(0, 0)
        MovementSystem.tryMove(tank, Direction.LEFT, 200f, tiles)
        MovementSystem.tryMove(tank, Direction.UP, 200f, tiles)
        val minimum = GameConstants.TANK_SIZE / 2f + GameConstants.WALL_CLEARANCE
        assertEquals(minimum, tank.position.x, 0.001f)
        assertEquals(minimum, tank.position.y, 0.001f)
    }

    private fun emptyBoard(): MutableList<MutableList<TileType>> =
        MutableList(GameConstants.BOARD_TILES) {
            MutableList(GameConstants.BOARD_TILES) { TileType.EMPTY }
        }

    private fun tankAt(column: Int, row: Int): Tank =
        VehicleCatalog.get(VehicleId.PZ_IV_H).createTank(
            id = 1,
            position = Vec2(
                x = column * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                y = row * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
            ),
            team = TeamSide.PLAYER,
            kind = TankKind.PLAYER,
            direction = Direction.UP,
        )
}
