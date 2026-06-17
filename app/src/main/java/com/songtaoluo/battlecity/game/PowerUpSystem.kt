package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.abs

data class PowerUpPickup(
    val powerUp: PowerUp,
    val collector: Tank,
)

object PowerUpSystem {
    private val types = PowerUpType.entries

    fun update(
        powerUps: MutableList<PowerUp>,
        deltaSeconds: Float,
        friendlies: List<Tank>,
    ): List<PowerUpPickup> {
        val deltaMs = deltaSeconds * 1000f
        powerUps.forEach { it.ttlMs = (it.ttlMs - deltaMs).coerceAtLeast(0f) }

        val pickups = mutableListOf<PowerUpPickup>()
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val powerUp = iterator.next()
            if (powerUp.ttlMs <= 0f) {
                iterator.remove()
                continue
            }
            val collector = friendlies.firstOrNull { tank ->
                tank.alive && intersects(powerUp, tank)
            }
            if (collector != null) {
                pickups += PowerUpPickup(powerUp, collector)
                iterator.remove()
            }
        }
        return pickups
    }

    fun create(position: Vec2, seed: Int): PowerUp = PowerUp(
        type = types[Math.floorMod(seed, types.size)],
        position = Vec2(
            position.x.coerceIn(12f, GameConstants.BOARD_SIZE - 36f),
            position.y.coerceIn(12f, GameConstants.BOARD_SIZE - 36f),
        ),
    )

    fun findOpenSpawn(
        tiles: List<List<TileType>>,
        tanks: List<Tank>,
        seed: Int,
    ): Vec2? {
        val candidates = mutableListOf<Vec2>()
        for (row in 1 until tiles.lastIndex) {
            for (column in 1 until tiles[row].lastIndex) {
                val tile = tiles[row][column]
                if (tile != TileType.EMPTY && tile != TileType.OBJECTIVE && tile != TileType.FOREST) continue
                val position = Vec2(
                    column * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                    row * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                )
                val occupied = tanks.any { tank ->
                    tank.alive && abs(tank.position.x - position.x) < GameConstants.TILE_SIZE * 1.5f &&
                        abs(tank.position.y - position.y) < GameConstants.TILE_SIZE * 1.5f
                }
                if (!occupied) candidates += position
            }
        }
        if (candidates.isEmpty()) return null
        return candidates[Math.floorMod(seed, candidates.size)]
    }

    private fun intersects(powerUp: PowerUp, tank: Tank): Boolean {
        val radius = powerUp.size / 2f + GameConstants.TANK_SIZE / 2f
        return abs(powerUp.position.x - tank.position.x) <= radius &&
            abs(powerUp.position.y - tank.position.y) <= radius
    }
}
