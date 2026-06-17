package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.TeamSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectilePipelineTest {
    @Test
    fun engineConvertsProjectileCollisionIntoVisualFeedback() {
        val engine = GameEngine()
        engine.bullets.clear()
        engine.bullets += bullet(100f, 100f, TeamSide.PLAYER)
        engine.bullets += bullet(106f, 100f, TeamSide.ENEMY)

        engine.update(0f, null)

        assertTrue(engine.bullets.isEmpty())
        assertEquals(1, engine.effects.size)
        assertEquals(ImpactEffectKind.SPARK, engine.effects.first().kind)
    }

    private fun bullet(x: Float, y: Float, team: TeamSide): Bullet = Bullet(
        ownerId = if (team == TeamSide.PLAYER) 1 else 99,
        team = team,
        faction = if (team == TeamSide.PLAYER) Faction.GERMAN else Faction.SOVIET,
        position = Vec2(x, y),
        direction = Direction.UP,
        speed = 0f,
        power = 2,
        penetration = 80,
    )
}
