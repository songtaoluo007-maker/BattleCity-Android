package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

internal object EnemyAiController {
    fun update(
        enemy: Tank,
        target: Tank,
        deltaSeconds: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
        fire: (Tank) -> Unit,
        canSeeTarget: Boolean,
    ) {
        if (!enemy.alive || !target.alive) return

        val firingDirection = firingDirection(enemy, target)
        if (canSeeTarget &&
            firingDirection != null &&
            distanceBetween(enemy, target) <= enemy.visionRange &&
            hasClearLineOfFire(enemy, target, tiles)
        ) {
            enemy.direction = firingDirection
            fire(enemy)
            return
        }

        val movementDistance = MovementSystem.effectiveSpeed(enemy, tiles) * deltaSeconds
        val moved = preferredDirections(enemy, target).any { direction ->
            if (enemy.blockedMs > 0f && enemy.blockedDirection == direction) {
                false
            } else {
                tryMove(enemy, direction, movementDistance, tiles, blockers)
            }
        }

        if (!moved) {
            val wallDirection = shootableWallDirection(enemy, tiles)
            if (wallDirection != null) {
                enemy.direction = wallDirection
                fire(enemy)
                return
            }

            val fallback = fallbackDirections(enemy).firstOrNull { direction ->
                tryMove(enemy, direction, movementDistance, tiles, blockers)
            }
            if (fallback == null) enemy.direction = enemy.direction.opposite()
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
                first > last || (first..last).none { row ->
                    tiles[row][startColumn].blocksProjectileLine()
                }
            }

            Direction.LEFT,
            Direction.RIGHT,
            -> {
                val first = minOf(startColumn, targetColumn) + 1
                val last = maxOf(startColumn, targetColumn) - 1
                first > last || (first..last).none { column ->
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

    private fun tryMove(
        enemy: Tank,
        direction: Direction,
        distance: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
    ): Boolean {
        val moved = MovementSystem.tryMove(enemy, direction, distance, tiles, blockers)
        if (moved) enemy.direction = direction
        return moved
    }

    private fun shootableWallDirection(enemy: Tank, tiles: List<List<TileType>>): Direction? {
        val directions = listOf(
            enemy.direction,
            Direction.UP,
            Direction.DOWN,
            Direction.LEFT,
            Direction.RIGHT,
        ).distinct()
        return directions.firstOrNull { direction ->
            val tile = tileAhead(enemy, direction, tiles)
            tile == TileType.BRICK || tile == TileType.VILLAGE
        }
    }

    private fun tileAhead(
        tank: Tank,
        direction: Direction,
        tiles: List<List<TileType>>,
    ): TileType? {
        val probe = GameConstants.TANK_SIZE / 2f + GameConstants.WALL_CLEARANCE + 3f
        val x = tank.position.x + when (direction) {
            Direction.LEFT -> -probe
            Direction.RIGHT -> probe
            else -> 0f
        }
        val y = tank.position.y + when (direction) {
            Direction.UP -> -probe
            Direction.DOWN -> probe
            else -> 0f
        }
        val column = floor(x / GameConstants.TILE_SIZE).toInt()
        val row = floor(y / GameConstants.TILE_SIZE).toInt()
        return if (row in tiles.indices && column in tiles[row].indices) tiles[row][column] else null
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
