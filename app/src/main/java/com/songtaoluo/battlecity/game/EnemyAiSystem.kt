package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

object EnemyAiSystem {
    fun update(
        enemy: Tank,
        target: Tank,
        deltaSeconds: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
        fire: (Tank) -> Unit,
    ) {
        if (!enemy.alive || !target.alive) return

        enemy.aiTimerMs = (enemy.aiTimerMs - deltaSeconds * 1000f).coerceAtLeast(0f)
        enemy.blockedMs = (enemy.blockedMs - deltaSeconds * 1000f).coerceAtLeast(0f)

        val firingDirection = firingDirection(enemy, target)
        if (firingDirection != null &&
            distanceBetween(enemy, target) <= enemy.visionRange &&
            hasClearLineOfFire(enemy, target, tiles)
        ) {
            enemy.direction = firingDirection
            fire(enemy)
            return
        }

        val movementDistance = MovementSystem.effectiveSpeed(enemy, tiles) * deltaSeconds
        val directions = preferredDirections(enemy, target)
        val moved = directions.any { direction ->
            if (enemy.blockedMs > 0f && enemy.blockedDirection == direction) {
                false
            } else {
                val success = MovementSystem.tryMove(
                    tank = enemy,
                    direction = direction,
                    distance = movementDistance,
                    tiles = tiles,
                    blockers = blockers,
                )
                if (success) enemy.direction = direction
                success
            }
        }

        if (!moved) {
            val fallback = fallbackDirections(enemy).firstOrNull { direction ->
                val success = MovementSystem.tryMove(
                    tank = enemy,
                    direction = direction,
                    distance = movementDistance,
                    tiles = tiles,
                    blockers = blockers,
                )
                if (success) enemy.direction = direction
                success
            }
            if (fallback == null) {
                enemy.direction = enemy.direction.opposite()
            }
        }
    }

    fun firingDirection(source: Tank, target: Tank): Direction? {
        val dx = target.position.x - source.position.x
        val dy = target.position.y - source.position.y
        return when {
            abs(dx) < GameConstants.AI_FIRE_ALIGNMENT -> if (dy >= 0f) Direction.DOWN else Direction.UP
            abs(dy) < GameConstants.AI_FIRE_ALIGNMENT -> if (dx >= 0f) Direction.RIGHT else Direction.LEFT
            else -> null
        }
    }

    fun preferredDirections(source: Tank, target: Tank): List<Direction> {
        val dx = target.position.x - source.position.x
        val dy = target.position.y - source.position.y
        val horizontal = if (dx >= 0f) Direction.RIGHT else Direction.LEFT
        val vertical = if (dy >= 0f) Direction.DOWN else Direction.UP
        return if (abs(dx) > abs(dy)) {
            listOf(horizontal, vertical, vertical.opposite(), horizontal.opposite())
        } else {
            listOf(vertical, horizontal, horizontal.opposite(), vertical.opposite())
        }
    }

    fun hasClearLineOfFire(
        source: Tank,
        target: Tank,
        tiles: List<List<TileType>>,
    ): Boolean {
        val direction = firingDirection(source, target) ?: return false
        val startColumn = tileCoordinate(source.position.x)
        val startRow = tileCoordinate(source.position.y)
        val targetColumn = tileCoordinate(target.position.x)
        val targetRow = tileCoordinate(target.position.y)

        return when (direction) {
            Direction.UP,
            Direction.DOWN,
            -> {
                val first = minOf(startRow, targetRow) + 1
                val last = maxOf(startRow, targetRow) - 1
                if (first > last) true else (first..last).none { row ->
                    tiles[row][startColumn].blocksProjectileLine()
                }
            }

            Direction.LEFT,
            Direction.RIGHT,
            -> {
                val first = minOf(startColumn, targetColumn) + 1
                val last = maxOf(startColumn, targetColumn) - 1
                if (first > last) true else (first..last).none { column ->
                    tiles[startRow][column].blocksProjectileLine()
                }
            }
        }
    }

    fun distanceBetween(a: Tank, b: Tank): Float {
        val dx = a.position.x - b.position.x
        val dy = a.position.y - b.position.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun fallbackDirections(enemy: Tank): List<Direction> {
        val directions = listOf(Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP)
        val offset = (enemy.id * 3) % directions.size
        return List(directions.size) { index -> directions[(index + offset) % directions.size] }
    }

    private fun tileCoordinate(value: Float): Int =
        floor(value / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)

    private fun TileType.blocksProjectileLine(): Boolean = when (this) {
        TileType.BRICK,
        TileType.STEEL,
        TileType.BASE,
        TileType.VILLAGE,
        -> true

        TileType.EMPTY,
        TileType.WATER,
        TileType.FOREST,
        TileType.OBJECTIVE,
        TileType.MINE,
        -> false
    }
}
