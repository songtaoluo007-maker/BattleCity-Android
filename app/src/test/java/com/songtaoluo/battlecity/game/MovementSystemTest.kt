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
    fun solidTerrainBlocksMovement() {
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
            assertFalse(moved)
            assertEquals(originalY, tank.position.y, 0.001f)
        }
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
    fun waterReducesTankSpeed() {
        val tiles = emptyBoard()
        tiles[6][5] = TileType.WATER
        val tank = tankAt(5, 6)
        val speed = MovementSystem.effectiveSpeed(tank, tiles)
        assertEquals(tank.speed * 0.52f, speed, 0.001f)
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
