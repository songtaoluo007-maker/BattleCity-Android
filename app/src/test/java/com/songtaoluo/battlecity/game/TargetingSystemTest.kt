package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetingSystemTest {
    @Test
    fun nearestSpottedEnemyInsideRadiusIsSelected() {
        val first = enemy(2, 100f, 100f)
        val second = enemy(3, 110f, 100f)

        val selected = TargetingSystem.findSpottedEnemyAt(
            boardX = 107f,
            boardY = 100f,
            enemies = listOf(first, second),
        )

        assertEquals(second.id, selected?.id)
    }

    @Test
    fun hiddenAndDestroyedEnemiesCannotBeSelected() {
        val hidden = enemy(2, 100f, 100f).apply { isSpotted = false }
        val destroyed = enemy(3, 100f, 100f).apply { alive = false }

        assertNull(TargetingSystem.findSpottedEnemyAt(100f, 100f, listOf(hidden, destroyed)))
    }

    @Test
    fun tapOutsideSelectionRadiusReturnsNull() {
        val target = enemy(2, 100f, 100f)

        assertNull(TargetingSystem.findSpottedEnemyAt(200f, 200f, listOf(target)))
    }

    private fun enemy(id: Int, x: Float, y: Float): Tank =
        VehicleCatalog.get(VehicleId.T34_76).createTank(
            id = id,
            position = Vec2(x, y),
            team = TeamSide.ENEMY,
            kind = TankKind.BASIC,
            direction = Direction.DOWN,
        ).apply {
            isSpotted = true
        }
}
