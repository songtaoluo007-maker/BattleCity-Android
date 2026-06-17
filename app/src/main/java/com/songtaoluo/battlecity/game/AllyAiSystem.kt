package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs

object AllyAiSystem {
    fun update(
        ally: Tank,
        player: Tank,
        enemies: List<Tank>,
        order: SquadOrder,
        deltaSeconds: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
        objective: Vec2,
        fire: (Tank) -> Unit,
    ) {
        if (!ally.alive || !player.alive) return

        ally.blockedMs = (ally.blockedMs - deltaSeconds * 1000f).coerceAtLeast(0f)
        val target = nearestEnemy(ally, enemies)
        if (target != null &&
            EnemyAiSystem.distanceBetween(ally, target) <= ally.visionRange &&
            EnemyAiSystem.hasClearLineOfFire(ally, target, tiles)
        ) {
            val direction = EnemyAiSystem.firingDirection(ally, target)
            if (direction != null) {
                ally.direction = direction
                fire(ally)
                if (order == SquadOrder.HOLD) return
            }
        }

        val destination = when (order) {
            SquadOrder.HOLD -> null
            SquadOrder.ASSAULT -> target?.position ?: objective
            SquadOrder.FOCUS_FIRE -> target?.position ?: formationPoint(ally, player)
            SquadOrder.FOLLOW -> formationPoint(ally, player)
        } ?: return

        if (distanceToPoint(ally, destination) < 28f) return

        val distance = MovementSystem.effectiveSpeed(ally, tiles) * deltaSeconds
        val directions = preferredDirections(ally, destination)
        val moved = directions.any { direction ->
            if (ally.blockedMs > 0f && ally.blockedDirection == direction) {
                false
            } else {
                val success = MovementSystem.tryMove(ally, direction, distance, tiles, blockers)
                if (success) ally.direction = direction
                success
            }
        }

        if (!moved) {
            val fallback = listOf(Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN)
                .firstOrNull { direction ->
                    val success = MovementSystem.tryMove(ally, direction, distance, tiles, blockers)
                    if (success) ally.direction = direction
                    success
                }
            if (fallback == null) ally.direction = ally.direction.opposite()
        }
    }

    fun formationPoint(ally: Tank, player: Tank): Vec2 {
        val slot = if (ally.id % 2 == 0) -1f else 1f
        val sideOffset = 58f * slot
        val trail = 82f
        return when (player.direction) {
            Direction.UP -> Vec2(player.position.x + sideOffset, player.position.y + trail)
            Direction.DOWN -> Vec2(player.position.x + sideOffset, player.position.y - trail)
            Direction.LEFT -> Vec2(player.position.x + trail, player.position.y + sideOffset)
            Direction.RIGHT -> Vec2(player.position.x - trail, player.position.y + sideOffset)
        }.also { point ->
            val margin = GameConstants.TILE_SIZE + GameConstants.TANK_SIZE / 2f
            point.x = point.x.coerceIn(margin, GameConstants.BOARD_SIZE - margin)
            point.y = point.y.coerceIn(margin, GameConstants.BOARD_SIZE - margin)
        }
    }

    fun nearestEnemy(source: Tank, enemies: List<Tank>): Tank? =
        enemies.asSequence()
            .filter { it.alive }
            .minByOrNull { EnemyAiSystem.distanceBetween(source, it) }

    private fun preferredDirections(source: Tank, destination: Vec2): List<Direction> {
        val dx = destination.x - source.position.x
        val dy = destination.y - source.position.y
        val horizontal = if (dx >= 0f) Direction.RIGHT else Direction.LEFT
        val vertical = if (dy >= 0f) Direction.DOWN else Direction.UP
        return if (abs(dx) > abs(dy)) {
            listOf(horizontal, vertical, vertical.opposite(), horizontal.opposite())
        } else {
            listOf(vertical, horizontal, horizontal.opposite(), vertical.opposite())
        }
    }

    private fun distanceToPoint(tank: Tank, point: Vec2): Float {
        val dx = tank.position.x - point.x
        val dy = tank.position.y - point.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
