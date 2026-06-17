package com.songtaoluo.battlecity.game

import kotlin.math.min

data class BoardViewport(
    val scale: Float,
    val left: Float,
    val top: Float,
    val size: Float,
) {
    fun toBoard(x: Float, y: Float): Vec2? {
        val inside = x >= left && y >= top && x <= left + size && y <= top + size
        if (!inside) return null
        return Vec2(
            ((x - left) / scale).coerceIn(0f, GameConstants.BOARD_SIZE),
            ((y - top) / scale).coerceIn(0f, GameConstants.BOARD_SIZE),
        )
    }
}

object BoardViewportCalculator {
    fun calculate(width: Float, height: Float): BoardViewport {
        val usableWidth = (width - 300f).coerceAtLeast(220f)
        val usableHeight = (height - 120f).coerceAtLeast(220f)
        val scale = min(
            usableWidth / GameConstants.BOARD_SIZE,
            usableHeight / GameConstants.BOARD_SIZE,
        ).coerceAtMost(1.25f)
        val boardSize = GameConstants.BOARD_SIZE * scale
        return BoardViewport(
            scale = scale,
            left = (width - boardSize) / 2f,
            top = 70f + (usableHeight - boardSize) / 2f,
            size = boardSize,
        )
    }
}
