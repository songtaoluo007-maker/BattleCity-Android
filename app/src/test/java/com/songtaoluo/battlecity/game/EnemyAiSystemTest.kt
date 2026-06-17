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

class EnemyAiSystemTest {
    @Test
    fun enemyFacesTargetWhenAlignedVertically() {
        val enemy = tank(VehicleId.T34_76, 100f, 50f, TeamSide.ENEMY)
        val target = tank(VehicleId.PZ_IV_H, 105f, 180f, TeamSide.PLAYER)
        assertEquals(Direction.DOWN, EnemyAiSystem.firingDirection(enemy, target))
    }

    @Test
    fun enemyPrioritizesLargestDistanceAxis() {
        val enemy = tank(VehicleId.T34_76, 50f, 50f, TeamSide.ENEMY)
        val target = tank(VehicleId.PZ_IV_H, 200f, 90f, TeamSide.PLAYER)
        assertEquals(Direction.RIGHT, EnemyAiSystem.preferredDirections(enemy, target).first())
    }

    @Test
    fun brickBlocksLineOfFire() {
        val tiles = emptyBoard()
        tiles[3][3] = TileType.BRICK
        val enemy = tankAtTile(VehicleId.T34_76, 3, 1, TeamSide.ENEMY)
        val target = tankAtTile(VehicleId.PZ_IV_H, 3, 6, TeamSide.PLAYER)
        assertFalse(EnemyAiSystem.hasClearLineOfFire(enemy, target, tiles))
    }

    @Test
    fun clearAlignedTargetTriggersFireCallback() {
        val tiles = emptyBoard()
        val enemy = tankAtTile(VehicleId.T34_76, 3, 1, TeamSide.ENEMY)
        val target = tankAtTile(VehicleId.PZ_IV_H, 3, 4, TeamSide.PLAYER)
        var fired = false

        EnemyAiSystem.update(
            enemy = enemy,
            target = target,
            deltaSeconds = 0.016f,
            tiles = tiles,
            blockers = listOf(enemy, target),
            fire = { fired = true },
        )

        assertTrue(fired)
        assertEquals(Direction.DOWN, enemy.direction)
    }

    @Test
    fun tankBlockerPreventsOverlap() {
        val tiles = emptyBoard()
        val mover = tank(VehicleId.T34_76, 100f, 100f, TeamSide.ENEMY)
        val blocker = tank(VehicleId.PZ_IV_H, 100f, 126f, TeamSide.PLAYER)
        val oldY = mover.position.y

        val moved = MovementSystem.tryMove(
            tank = mover,
            direction = Direction.DOWN,
            distance = 8f,
            tiles = tiles,
            blockers = listOf(blocker),
        )

        assertFalse(moved)
        assertEquals(oldY, mover.position.y, 0.001f)
    }

    private fun emptyBoard(): MutableList<MutableList<TileType>> =
        MutableList(GameConstants.BOARD_TILES) {
            MutableList(GameConstants.BOARD_TILES) { TileType.EMPTY }
        }

    private fun tankAtTile(
        vehicleId: VehicleId,
        column: Int,
        row: Int,
        team: TeamSide,
    ): Tank = tank(
        vehicleId,
        column * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
        row * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
        team,
    )

    private fun tank(
        vehicleId: VehicleId,
        x: Float,
        y: Float,
        team: TeamSide,
    ): Tank = VehicleCatalog.get(vehicleId).createTank(
        id = if (team == TeamSide.PLAYER) 1 else 2,
        position = Vec2(x, y),
        team = team,
        kind = if (team == TeamSide.PLAYER) TankKind.PLAYER else TankKind.BASIC,
        direction = Direction.UP,
    )
}
