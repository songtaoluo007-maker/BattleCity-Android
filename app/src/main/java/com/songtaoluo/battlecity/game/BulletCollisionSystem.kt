package com.songtaoluo.battlecity.game

import kotlin.math.sqrt

data class BulletCollision(
    val position: Vec2,
)

object BulletCollisionSystem {
    fun resolve(bullets: MutableList<Bullet>): List<BulletCollision> {
        if (bullets.size < 2) return emptyList()

        val collisions = mutableListOf<BulletCollision>()
        for (firstIndex in 0 until bullets.lastIndex) {
            val first = bullets[firstIndex]
            if (!first.active) continue

            for (secondIndex in firstIndex + 1 until bullets.size) {
                val second = bullets[secondIndex]
                if (!second.active || first.team == second.team) continue

                val dx = first.position.x - second.position.x
                val dy = first.position.y - second.position.y
                val distance = sqrt(dx * dx + dy * dy)
                val collisionDistance = GameConstants.BULLET_COLLISION_RADIUS + 4f
                if (distance > collisionDistance) continue

                first.active = false
                second.active = false
                collisions += BulletCollision(
                    position = Vec2(
                        x = (first.position.x + second.position.x) / 2f,
                        y = (first.position.y + second.position.y) / 2f,
                    ),
                )
                break
            }
        }
        return collisions
    }
}
