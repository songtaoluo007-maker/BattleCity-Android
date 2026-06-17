package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.TileType
import kotlin.math.floor

data class TileImpact(
    val consumed: Boolean,
    val scoreDelta: Int = 0,
    val destroyedTile: Boolean = false,
    val baseHit: Boolean = false,
)

object ProjectileSystem {
    private const val STEEL_PENETRATION_THRESHOLD = 130

    fun resolveTileImpact(
        bullet: Bullet,
        tiles: MutableList<MutableList<TileType>>,
    ): TileImpact {
        val column = floor(bullet.position.x / GameConstants.TILE_SIZE).toInt()
        val row = floor(bullet.position.y / GameConstants.TILE_SIZE).toInt()

        if (row !in 0 until GameConstants.BOARD_TILES ||
            column !in 0 until GameConstants.BOARD_TILES
        ) {
            return TileImpact(consumed = true)
        }

        return when (tiles[row][column]) {
            TileType.BRICK,
            TileType.VILLAGE,
            -> {
                tiles[row][column] = TileType.EMPTY
                TileImpact(
                    consumed = true,
                    scoreDelta = 10,
                    destroyedTile = true,
                )
            }

            TileType.STEEL -> {
                val destroyed = bullet.penetration >= STEEL_PENETRATION_THRESHOLD
                if (destroyed) tiles[row][column] = TileType.EMPTY
                TileImpact(
                    consumed = true,
                    scoreDelta = if (destroyed) 20 else 0,
                    destroyedTile = destroyed,
                )
            }

            TileType.BASE -> TileImpact(
                consumed = true,
                baseHit = true,
            )

            TileType.EMPTY,
            TileType.WATER,
            TileType.FOREST,
            TileType.OBJECTIVE,
            TileType.MINE,
            -> TileImpact(consumed = false)
        }
    }
}
