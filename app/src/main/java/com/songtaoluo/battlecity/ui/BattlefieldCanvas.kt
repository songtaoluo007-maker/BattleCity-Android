package com.songtaoluo.battlecity.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.songtaoluo.battlecity.game.GameConstants
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.Tank
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.min

@Composable
internal fun BattlefieldCanvas(engine: GameEngine, frame: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        frame
        val availableWidth = (size.width - 300f).coerceAtLeast(220f)
        val availableHeight = (size.height - 120f).coerceAtLeast(220f)
        val boardScale = min(
            availableWidth / GameConstants.BOARD_SIZE,
            availableHeight / GameConstants.BOARD_SIZE,
        ).coerceAtMost(1.25f)
        val renderedSize = GameConstants.BOARD_SIZE * boardScale
        val left = (size.width - renderedSize) / 2f
        val top = 70f + (availableHeight - renderedSize) / 2f

        translate(left = left, top = top) {
            scale(scaleX = boardScale, scaleY = boardScale, pivot = Offset.Zero) {
                drawRect(Color(0xFF293526), size = Size(GameConstants.BOARD_SIZE, GameConstants.BOARD_SIZE))
                engine.tiles.forEachIndexed { row, values ->
                    values.forEachIndexed { column, tile -> drawBattleTile(tile, column, row) }
                }
                if (engine.player.alive) drawTank(engine.player)
                engine.enemies.filter { it.alive }.forEach { drawTank(it) }
                engine.bullets.forEach { bullet ->
                    drawCircle(
                        color = if (bullet.team == TeamSide.ENEMY) Color(0xFFFF7043) else Color(0xFFFFD54F),
                        radius = 4f,
                        center = Offset(bullet.position.x, bullet.position.y),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBattleTile(tile: TileType, column: Int, row: Int) {
    val tileSize = GameConstants.TILE_SIZE
    val topLeft = Offset(column * tileSize, row * tileSize)
    val color = when (tile) {
        TileType.EMPTY -> Color(0xFF293526)
        TileType.BRICK -> Color(0xFF8A5138)
        TileType.STEEL -> Color(0xFF777C82)
        TileType.WATER -> Color(0xFF285A76)
        TileType.FOREST -> Color(0xFF214D2E)
        TileType.BASE -> Color(0xFFD7B04B)
        TileType.OBJECTIVE -> Color(0xFFB33A32)
        TileType.VILLAGE -> Color(0xFF8C7658)
        TileType.MINE -> Color(0xFF3A3028)
    }
    drawRect(color, topLeft, Size(tileSize, tileSize))
    drawRect(
        color = Color(0x22000000),
        topLeft = topLeft,
        size = Size(tileSize, tileSize),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
    )
    when (tile) {
        TileType.WATER -> {
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 10f), topLeft + Offset(tileSize - 4f, 10f), 2f)
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 22f), topLeft + Offset(tileSize - 4f, 22f), 2f)
        }
        TileType.OBJECTIVE -> drawCircle(Color(0xFFFFD54F), 8f, topLeft + Offset(16f, 16f))
        TileType.BASE -> drawCircle(Color(0xFF493D1B), 8f, topLeft + Offset(16f, 16f))
        else -> Unit
    }
}

private fun DrawScope.drawTank(tank: Tank) {
    val spec = VehicleCatalog.get(tank.vehicleId)
    val body = Color(AndroidColor.parseColor(spec.bodyColor))
    val dark = Color(AndroidColor.parseColor(spec.darkColor))
    val trim = Color(AndroidColor.parseColor(spec.trimColor))
    val center = Offset(tank.position.x, tank.position.y)
    val half = GameConstants.TANK_SIZE / 2f

    drawRect(
        dark,
        Offset(center.x - half - 3f, center.y - half),
        Size(GameConstants.TANK_SIZE + 6f, GameConstants.TANK_SIZE),
    )
    drawRect(
        body,
        Offset(center.x - half, center.y - half),
        Size(GameConstants.TANK_SIZE, GameConstants.TANK_SIZE),
    )
    val barrel = when (tank.direction) {
        Direction.UP -> Offset(center.x, center.y - half - 13f)
        Direction.DOWN -> Offset(center.x, center.y + half + 13f)
        Direction.LEFT -> Offset(center.x - half - 13f, center.y)
        Direction.RIGHT -> Offset(center.x + half + 13f, center.y)
    }
    drawLine(dark, center, barrel, strokeWidth = 6f, cap = StrokeCap.Round)
    drawCircle(trim, 6f, center)

    val hpRatio = tank.hp.toFloat() / tank.maxHp.coerceAtLeast(1)
    drawRect(Color(0xAA111111), Offset(center.x - half, center.y - half - 8f), Size(GameConstants.TANK_SIZE, 4f))
    drawRect(
        if (hpRatio > 0.5f) Color(0xFF7EC850) else Color(0xFFD95C4F),
        Offset(center.x - half, center.y - half - 8f),
        Size(GameConstants.TANK_SIZE * hpRatio.coerceIn(0f, 1f), 4f),
    )
}
