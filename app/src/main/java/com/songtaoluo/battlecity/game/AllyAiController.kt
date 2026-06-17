package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs
import kotlin.math.sqrt

internal object AllyAiController {
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

        val target = nearestEnemy(ally, enemies)
        if (target != null &&
            EnemyAiSystem.distanceBetween(ally, target) <= ally.visionRange &&
            EnemyAiSystem.hasClearLineOfFire(ally, target, tiles)
        ) {
            EnemyAiSystem.firingDirection(ally, target)?.let { direction ->
                ally.direction = direction
                fire(ally)
                if (order == SquadOrder.HOLD) return
            }
        }

        val destination = movementTarget(ally, player, target, order, objective) ?: return
        val stoppingDistance = if (order == SquadOrder.ASSAULT) 82f else 28f
        if (distanceToPoint(ally, destination) < stoppingDistance) return

        val distance = MovementSystem.effectiveSpeed(ally, tiles) * deltaSeconds
        val moved = preferredDirections(ally, destination).any { direction ->
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
        val sideOffset = GameConstants.FORMATION_OFFSET * slot
        val trail = GameConstants.FORMATION_TRAIL
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

    fun isOnFormationSide(ally: Tank, player: Tank): Boolean {
        val tolerance = GameConstants.TILE_SIZE
        return when (player.direction) {
            Direction.UP -> ally.position.y > player.position.y + tolerance
            Direction.DOWN -> ally.position.y < player.position.y - tolerance
            Direction.LEFT -> ally.position.x > player.position.x + tolerance
            Direction.RIGHT -> ally.position.x < player.position.x - tolerance
        }
    }

    fun nearestEnemy(source: Tank, enemies: List<Tank>): Tank? =
        enemies.asSequence()
            .filter { it.alive && it.isSpotted }
            .minByOrNull { EnemyAiSystem.distanceBetween(source, it) }

    private fun movementTarget(
        ally: Tank,
        player: Tank,
        target: Tank?,
        order: SquadOrder,
        objective: Vec2,
    ): Vec2? = when (order) {
        SquadOrder.HOLD -> null
        SquadOrder.ASSAULT -> target?.position ?: objective
        SquadOrder.FOCUS_FIRE -> when {
            target != null && EnemyAiSystem.distanceBetween(ally, target) > 82f -> target.position
            isOnFormationSide(ally, player) -> formationPoint(ally, player)
            else -> null
        }
        SquadOrder.FOLLOW -> if (isOnFormationSide(ally, player)) formationPoint(ally, player) else null
    }

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
        return sqrt(dx * dx + dy * dy)
    }
}
