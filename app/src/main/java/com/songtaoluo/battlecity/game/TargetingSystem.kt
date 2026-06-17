package com.songtaoluo.battlecity.game

import kotlin.math.sqrt

enum class TargetingMode {
    NONE,
    ARTILLERY,
    FOCUS_FIRE,
}

object TargetingSystem {
    const val ENEMY_SELECT_RADIUS = 30f

    fun findSpottedEnemyAt(
        boardX: Float,
        boardY: Float,
        enemies: List<Tank>,
        radius: Float = ENEMY_SELECT_RADIUS,
    ): Tank? = enemies.asSequence()
        .filter { it.alive && it.isSpotted }
        .map { enemy -> enemy to distance(enemy.position.x, enemy.position.y, boardX, boardY) }
        .filter { (_, distance) -> distance <= radius }
        .minByOrNull { (_, distance) -> distance }
        ?.first

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}
