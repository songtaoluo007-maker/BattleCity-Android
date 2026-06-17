package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs
import kotlin.math.floor

object MovementSystem {
    fun tryMove(
        tank: Tank,
        direction: Direction,
        distance: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank> = emptyList(),
    ): Boolean {
        if (!tank.alive || distance <= 0f) return false

        val oldX = tank.position.x
        val oldY = tank.position.y

        when (direction) {
            Direction.UP -> tank.position.y -= distance
            Direction.DOWN -> tank.position.y += distance
            Direction.LEFT -> tank.position.x -= distance
            Direction.RIGHT -> tank.position.x += distance
        }
        clampToBoard(tank)

        val blockedByTerrain = collidesWithSolid(tank, tiles)
        val blockedByTank = blockers.any { other ->
            other.id != tank.id && other.alive && overlaps(tank, other)
        }
        if (blockedByTerrain || blockedByTank) {
            tank.position.x = oldX
            tank.position.y = oldY
            tank.blockedMs = GameConstants.AI_BLOCKED_MS
            tank.blockedDirection = direction
            return false
        }

        if (tank.blockedDirection != direction) {
            tank.blockedMs = (tank.blockedMs - 80f).coerceAtLeast(0f)
        }
        return tank.position.x != oldX || tank.position.y != oldY
    }

    fun effectiveSpeed(tank: Tank, tiles: List<List<TileType>>): Float =
        if (tileAtCenter(tank, tiles) == TileType.WATER) {
            tank.speed * GameConstants.WATER_SPEED_MULTIPLIER
        } else {
            tank.speed
        }

    fun tileAtCenter(tank: Tank, tiles: List<List<TileType>>): TileType {
        val column = floor(tank.position.x / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)
        val row = floor(tank.position.y / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)
        return tiles[row][column]
    }

    fun collidesWithSolid(tank: Tank, tiles: List<List<TileType>>): Boolean {
        val half = GameConstants.TANK_SIZE / 2f + GameConstants.WALL_CLEARANCE
        val left = floor((tank.position.x - half) / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)
        val right = floor((tank.position.x + half - 1f) / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)
        val top = floor((tank.position.y - half) / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)
        val bottom = floor((tank.position.y + half - 1f) / GameConstants.TILE_SIZE)
            .toInt()
            .coerceIn(0, GameConstants.BOARD_TILES - 1)

        for (row in top..bottom) {
            for (column in left..right) {
                if (tiles[row][column].blocksTankMovement()) return true
            }
        }
        return false
    }

    fun overlaps(a: Tank, b: Tank, clearance: Float = GameConstants.TANK_MIN_SPACING): Boolean {
        val minimumDistance = GameConstants.TANK_SIZE + clearance
        return abs(a.position.x - b.position.x) < minimumDistance &&
            abs(a.position.y - b.position.y) < minimumDistance
    }

    fun clampToBoard(tank: Tank) {
        val margin = GameConstants.TANK_SIZE / 2f + GameConstants.WALL_CLEARANCE
        tank.position.x = tank.position.x.coerceIn(margin, GameConstants.BOARD_SIZE - margin)
        tank.position.y = tank.position.y.coerceIn(margin, GameConstants.BOARD_SIZE - margin)
    }

    private fun TileType.blocksTankMovement(): Boolean = when (this) {
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
