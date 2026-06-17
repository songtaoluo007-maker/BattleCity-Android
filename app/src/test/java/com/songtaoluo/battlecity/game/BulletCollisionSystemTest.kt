package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.TeamSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulletCollisionSystemTest {
    @Test
    fun opposingProjectilesCancelEachOther() {
        val playerBullet = bullet(100f, 100f, TeamSide.PLAYER)
        val enemyBullet = bullet(106f, 100f, TeamSide.ENEMY)
        val bullets = mutableListOf(playerBullet, enemyBullet)

        val collisions = BulletCollisionSystem.resolve(bullets)

        assertEquals(1, collisions.size)
        assertFalse(playerBullet.active)
        assertFalse(enemyBullet.active)
        assertEquals(103f, collisions.first().position.x, 0.001f)
    }

    @Test
    fun sameSideProjectilesDoNotCollide() {
        val first = bullet(100f, 100f, TeamSide.PLAYER)
        val second = bullet(102f, 100f, TeamSide.PLAYER)

        val collisions = BulletCollisionSystem.resolve(mutableListOf(first, second))

        assertTrue(collisions.isEmpty())
        assertTrue(first.active)
        assertTrue(second.active)
    }

    @Test
    fun consumedProjectileCannotCancelMultipleTargets() {
        val playerBullet = bullet(100f, 100f, TeamSide.PLAYER)
        val firstEnemy = bullet(104f, 100f, TeamSide.ENEMY)
        val secondEnemy = bullet(106f, 100f, TeamSide.ENEMY)

        val collisions = BulletCollisionSystem.resolve(
            mutableListOf(playerBullet, firstEnemy, secondEnemy),
        )

        assertEquals(1, collisions.size)
        assertFalse(playerBullet.active)
        assertFalse(firstEnemy.active)
        assertTrue(secondEnemy.active)
    }

    @Test
    fun distantProjectilesRemainActive() {
        val first = bullet(20f, 20f, TeamSide.PLAYER)
        val second = bullet(100f, 100f, TeamSide.ENEMY)

        val collisions = BulletCollisionSystem.resolve(mutableListOf(first, second))

        assertTrue(collisions.isEmpty())
        assertTrue(first.active)
        assertTrue(second.active)
    }

    private fun bullet(x: Float, y: Float, team: TeamSide): Bullet = Bullet(
        ownerId = if (team == TeamSide.PLAYER) 1 else 2,
        team = team,
        faction = if (team == TeamSide.PLAYER) Faction.GERMAN else Faction.SOVIET,
        position = Vec2(x, y),
        direction = Direction.UP,
        speed = 300f,
        power = 2,
        penetration = 80,
    )
}
