package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.TileType

object TileMapParser {
    fun parse(rows: List<String>): List<List<TileType>> {
        require(rows.size == GameConstants.BOARD_TILES)
        return rows.map { row ->
            require(row.length == GameConstants.BOARD_TILES)
            row.map { symbol -> fromSymbol(symbol) }
        }
    }

    fun fromSymbol(symbol: Char): TileType = when (symbol) {
        '.' -> TileType.EMPTY
        'B' -> TileType.BRICK
        'S' -> TileType.STEEL
        'W' -> TileType.WATER
        'F' -> TileType.FOREST
        'E' -> TileType.BASE
        'O' -> TileType.OBJECTIVE
        'H' -> TileType.VILLAGE
        'M' -> TileType.MINE
        else -> throw IllegalArgumentException("Unsupported map symbol")
    }
}
