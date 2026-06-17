package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyWallEscapeTest {
    @Test
    fun trappedUnitSelectsDestructibleRoute() {
        val tiles = MutableList(GameConstants.BOARD_TILES) {
            MutableList(GameConstants.BOARD_TILES) { TileType.EMPTY }
        }
        val enemy = createUnit(8, 8, TeamSide.ENEMY)
        val target = createUnit(8, 12, TeamSide.PLAYER)
        tiles[7][8] = TileType.STEEL
        tiles[8][7] = TileType.STEEL
        tiles[8][9] = TileType.STEEL
        tiles[9][8] = TileType.BRICK
        var actionTriggered = false

        EnemyAiSystem.update(
            enemy,
            target,
            0.016f,
            tiles,
            listOf(enemy, target),
        ) { actionTriggered = true }

        assertTrue(actionTriggered)
        assertEquals(Direction.DOWN, enemy.direction)
    }

    private fun createUnit(column: Int, row: Int, team: TeamSide): Tank {
        val id = if (team == TeamSide.ENEMY) VehicleId.T34_76 else VehicleId.PZ_IV_H
        return VehicleCatalog.get(id).createTank(
            id = if (team == TeamSide.ENEMY) 2 else 1,
            position = Vec2(
                column * GameConstants.TILE_SIZE + 16f,
                row * GameConstants.TILE_SIZE + 16f,
            ),
            team = team,
            kind = if (team == TeamSide.ENEMY) TankKind.BASIC else TankKind.PLAYER,
            direction = Direction.DOWN,
        )
    }
}
