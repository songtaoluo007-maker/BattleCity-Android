package com.songtaoluo.battlecity.game

import kotlin.math.abs

class GameEngine {
    val player = Tank(Vec2(180f, 360f), Direction.UP, speed = 190f)
    val enemy = Tank(Vec2(760f, 140f), Direction.DOWN, speed = 80f)
    val bullets = mutableListOf<Bullet>()

    var score: Int = 0
        private set

    private var fireCooldown = 0f

    fun update(deltaSeconds: Float, width: Float, height: Float, input: Direction?) {
        fireCooldown = (fireCooldown - deltaSeconds).coerceAtLeast(0f)
        input?.let {
            player.direction = it
            move(player, it, player.speed * deltaSeconds)
        }

        player.position.x = player.position.x.coerceIn(30f, width - 30f)
        player.position.y = player.position.y.coerceIn(30f, height - 30f)

        bullets.forEach { bullet ->
            when (bullet.direction) {
                Direction.UP -> bullet.position.y -= bullet.speed * deltaSeconds
                Direction.DOWN -> bullet.position.y += bullet.speed * deltaSeconds
                Direction.LEFT -> bullet.position.x -= bullet.speed * deltaSeconds
                Direction.RIGHT -> bullet.position.x += bullet.speed * deltaSeconds
            }

            if (bullet.position.x !in 0f..width || bullet.position.y !in 0f..height) {
                bullet.active = false
            }

            if (enemy.alive && abs(bullet.position.x - enemy.position.x) < 34f && abs(bullet.position.y - enemy.position.y) < 34f) {
                bullet.active = false
                enemy.alive = false
                score += 100
            }
        }
        bullets.removeAll { !it.active }
    }

    fun fire() {
        if (fireCooldown > 0f) return
        bullets += Bullet(Vec2(player.position.x, player.position.y), player.direction)
        fireCooldown = 0.22f
    }

    fun restartEnemy() {
        enemy.position.x = 760f
        enemy.position.y = 140f
        enemy.alive = true
    }

    private fun move(tank: Tank, direction: Direction, distance: Float) {
        when (direction) {
            Direction.UP -> tank.position.y -= distance
            Direction.DOWN -> tank.position.y += distance
            Direction.LEFT -> tank.position.x -= distance
            Direction.RIGHT -> tank.position.x += distance
        }
    }
}
