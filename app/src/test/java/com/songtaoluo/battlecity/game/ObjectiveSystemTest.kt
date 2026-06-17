package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.ObjectiveState
import com.songtaoluo.battlecity.model.ObjectiveType
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectiveSystemTest {
    @Test
    fun breakthroughRequiresKillsAndFriendlyInTargetZone() {
        val objective = objective(ObjectiveType.BREAKTHROUGH, kills = 8, holdMs = 0)
        val outside = friendly(300f, 300f)
        val center = Vec2(100f, 100f)

        val killsOnly = ObjectiveSystem.evaluate(
            objective, 0L, 8, listOf(outside), center, true, false,
        )
        assertEquals(ObjectiveOutcome.ACTIVE, killsOnly.outcome)
        assertTrue(killsOnly.killsComplete)
        assertFalse(killsOnly.positionComplete)

        outside.position.x = 100f
        outside.position.y = 100f
        val complete = ObjectiveSystem.evaluate(
            objective, 0L, 8, listOf(outside), center, true, false,
        )
        assertEquals(ObjectiveOutcome.VICTORY, complete.outcome)
    }

    @Test
    fun surviveCanWinByTimeOrRequiredKills() {
        val objective = objective(ObjectiveType.SURVIVE, kills = 10, holdMs = 150_000)
        val friendly = friendly(100f, 100f)

        val byTime = ObjectiveSystem.evaluate(
            objective, 150_000L, 2, listOf(friendly), Vec2(100f, 100f), true, false,
        )
        val byKills = ObjectiveSystem.evaluate(
            objective, 20_000L, 10, listOf(friendly), Vec2(100f, 100f), true, false,
        )

        assertEquals(ObjectiveOutcome.VICTORY, byTime.outcome)
        assertEquals(ObjectiveOutcome.VICTORY, byKills.outcome)
        assertEquals(130_000L, byKills.remainingMs)
    }

    @Test
    fun defendedBaseDestructionCausesDefeat() {
        val objective = objective(ObjectiveType.SURVIVE, kills = 10, holdMs = 150_000)
        val result = ObjectiveSystem.evaluate(
            objective, 10_000L, 0, listOf(friendly(100f, 100f)), Vec2(100f, 100f), true, true,
        )

        assertEquals(ObjectiveOutcome.DEFEAT, result.outcome)
    }

    @Test
    fun playerLossOverridesCompletedRequirements() {
        val objective = objective(ObjectiveType.DESTROY, kills = 1, holdMs = 0)
        val result = ObjectiveSystem.evaluate(
            objective, 0L, 1, emptyList(), Vec2(100f, 100f), false, false,
        )

        assertEquals(ObjectiveOutcome.DEFEAT, result.outcome)
    }

    private fun objective(type: ObjectiveType, kills: Int, holdMs: Long): ObjectiveState =
        ObjectiveState(
            type = type,
            title = "test",
            detail = "test",
            targetX = 3,
            targetY = 3,
            radius = 40f,
            requiredKills = kills,
            holdMs = holdMs,
        )

    private fun friendly(x: Float, y: Float): Tank =
        VehicleCatalog.get(VehicleId.PZ_IV_H).createTank(
            id = 1,
            position = Vec2(x, y),
            team = TeamSide.PLAYER,
            kind = TankKind.PLAYER,
            direction = Direction.UP,
        )
}
