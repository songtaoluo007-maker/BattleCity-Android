package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplyRulesTest {
    @Test
    fun nearbyAllyCollectsSupply() {
        val player = unit(1, 100f, 100f, TeamSide.PLAYER)
        val ally = unit(100, 200f, 200f, TeamSide.ALLY)
        val supplies = mutableListOf(
            PowerUp(PowerUpType.SHIELD, Vec2(200f, 200f)),
        )

        val pickups = PowerUpSystem.update(supplies, 0f, listOf(player, ally))

        assertEquals(1, pickups.size)
        assertEquals(ally.id, pickups.first().collector.id)
        assertTrue(supplies.isEmpty())
    }

    @Test
    fun expiredSupplyDisappears() {
        val supplies = mutableListOf(
            PowerUp(PowerUpType.FIRE, Vec2(100f, 100f), ttlMs = 10f),
        )

        val pickups = PowerUpSystem.update(supplies, 0.02f, emptyList())

        assertTrue(pickups.isEmpty())
        assertTrue(supplies.isEmpty())
    }

    @Test
    fun seededCreationCoversEverySupplyType() {
        val generated = PowerUpType.entries.indices.map { seed ->
            PowerUpSystem.create(Vec2(50f, 50f), seed).type
        }

        assertEquals(PowerUpType.entries.toSet(), generated.toSet())
    }

    private fun unit(id: Int, x: Float, y: Float, team: TeamSide): Tank =
        VehicleCatalog.get(VehicleId.PZ_IV_H).createTank(
            id = id,
            position = Vec2(x, y),
            team = team,
            kind = if (team == TeamSide.PLAYER) TankKind.PLAYER else TankKind.ALLY,
            direction = Direction.UP,
        )
}
