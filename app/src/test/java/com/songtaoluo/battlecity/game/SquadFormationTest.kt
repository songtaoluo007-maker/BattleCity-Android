package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SquadFormationTest {
    @Test
    fun twoSlotsTrailOnOppositeSides() {
        val player = unit(1, VehicleId.PZ_IV_H, 200f, 200f, TeamSide.PLAYER)
        player.direction = Direction.UP
        val first = unit(100, VehicleId.PZ_IV_H, 100f, 100f, TeamSide.ALLY)
        val second = unit(101, VehicleId.TIGER_I, 100f, 100f, TeamSide.ALLY)

        val firstPoint = AllyAiSystem.formationPoint(first, player)
        val secondPoint = AllyAiSystem.formationPoint(second, player)

        assertTrue(firstPoint.x < player.position.x)
        assertTrue(secondPoint.x > player.position.x)
        assertTrue(firstPoint.y > player.position.y)
        assertEquals(firstPoint.y, secondPoint.y, 0.001f)
    }

    @Test
    fun holdKeepsCurrentPosition() {
        val tiles = MutableList(GameConstants.BOARD_TILES) {
            MutableList(GameConstants.BOARD_TILES) { TileType.EMPTY }
        }
        val player = unit(1, VehicleId.PZ_IV_H, 300f, 300f, TeamSide.PLAYER)
        val ally = unit(100, VehicleId.PZ_IV_H, 200f, 200f, TeamSide.ALLY)
        val opponent = unit(2, VehicleId.T34_76, 350f, 250f, TeamSide.ENEMY)
        val oldX = ally.position.x
        val oldY = ally.position.y

        AllyAiSystem.update(
            ally,
            player,
            listOf(opponent),
            SquadOrder.HOLD,
            0.5f,
            tiles,
            listOf(player, ally, opponent),
            Vec2(100f, 100f),
        ) {}

        assertEquals(oldX, ally.position.x, 0.001f)
        assertEquals(oldY, ally.position.y, 0.001f)
    }

    private fun unit(
        id: Int,
        vehicleId: VehicleId,
        x: Float,
        y: Float,
        team: TeamSide,
    ): Tank = VehicleCatalog.get(vehicleId).createTank(
        id = id,
        position = Vec2(x, y),
        team = team,
        kind = when (team) {
            TeamSide.PLAYER -> TankKind.PLAYER
            TeamSide.ALLY -> TankKind.ALLY
            TeamSide.ENEMY -> TankKind.BASIC
        },
        direction = Direction.UP,
    )
}
