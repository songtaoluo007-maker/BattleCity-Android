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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.songtaoluo.battlecity.game.GameConstants
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.PowerUp
import com.songtaoluo.battlecity.game.TacticalArea
import com.songtaoluo.battlecity.game.Tank
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.PowerUpType
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
                engine.reconArea?.let { drawTacticalArea(it, Color(0xFF64B5F6), 0.12f) }
                engine.smokeArea?.let { drawTacticalArea(it, Color.White, 0.2f) }
                engine.powerUps.forEach { drawPowerUp(it) }
                if (engine.player.alive) drawTank(engine.player)
                engine.allies.filter { it.alive }.forEach { drawTank(it) }
                engine.enemies.filter { it.alive && it.isSpotted }.forEach { drawTank(it) }
                engine.bullets.forEach { bullet ->
                    drawCircle(
                        color = if (bullet.team == TeamSide.ENEMY) Color(0xFFFF7043) else Color(0xFFFFD54F),
                        radius = 4f,
                        center = Offset(bullet.position.x, bullet.position.y),
                    )
                }
                engine.effects.forEach { effect -> drawImpactEffect(effect) }
            }
        }
    }
}

private fun DrawScope.drawTacticalArea(area: TacticalArea, color: Color, alpha: Float) {
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = area.radius,
        center = Offset(area.center.x, area.center.y),
    )
    drawCircle(
        color = color.copy(alpha = 0.55f),
        radius = area.radius,
        center = Offset(area.center.x, area.center.y),
        style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawPowerUp(powerUp: PowerUp) {
    val center = Offset(powerUp.position.x, powerUp.position.y)
    val color = when (powerUp.type) {
        PowerUpType.SHIELD -> Color(0xFF42A5F5)
        PowerUpType.SPEED -> Color(0xFF66BB6A)
        PowerUpType.FIRE -> Color(0xFFFF7043)
        PowerUpType.LIFE -> Color(0xFFEC407A)
        PowerUpType.FREEZE -> Color(0xFF80DEEA)
    }
    val half = powerUp.size / 2f
    drawRect(
        color = Color(0xCC101010),
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(powerUp.size, powerUp.size),
    )
    drawCircle(color, radius = half - 3f, center = center)
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 3f, center = center)
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
        style = Stroke(width = 1f),
    )
    when (tile) {
        TileType.WATER -> {
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 10f), topLeft + Offset(tileSize - 4f, 10f), 2f)
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 22f), topLeft + Offset(tileSize - 4f, 22f), 2f)
        }
        TileType.OBJECTIVE -> drawCircle(Color(0xFFFFD54F), 8f, topLeft + Offset(16f, 16f))
        TileType.BASE -> drawCircle(Color(0xFF493D1B), 8f, topLeft + Offset(16f, 16f))
        TileType.MINE -> {
            drawCircle(Color(0xFF17130F), 7f, topLeft + Offset(16f, 16f))
            drawLine(Color(0xFF9E8B74), topLeft + Offset(11f, 16f), topLeft + Offset(21f, 16f), 2f)
        }
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
    val teamColor = when (tank.team) {
        TeamSide.PLAYER -> Color(0xFF42A5F5)
        TeamSide.ALLY -> Color(0xFF4DD0E1)
        TeamSide.ENEMY -> Color(0xFFEF5350)
    }

    drawCircle(teamColor, half + 4f, center, style = Stroke(width = 2f))
    if (tank.shieldMs > 0f) {
        drawCircle(Color(0xFF90CAF9).copy(alpha = 0.75f), half + 8f, center, style = Stroke(width = 3f))
    }
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

    if (tank.trackBrokenMs > 0f) {
        drawLine(Color(0xFFFFC107), center + Offset(-8f, 8f), center + Offset(8f, -8f), 3f)
    }
    if (tank.apcrShots > 0) {
        drawCircle(Color(0xFFFFEB3B), 3f, center + Offset(half - 2f, -half + 2f))
    }

    val hpRatio = tank.hp.toFloat() / tank.maxHp.coerceAtLeast(1)
    drawRect(Color(0xAA111111), Offset(center.x - half, center.y - half - 8f), Size(GameConstants.TANK_SIZE, 4f))
    drawRect(
        if (hpRatio > 0.5f) Color(0xFF7EC850) else Color(0xFFD95C4F),
        Offset(center.x - half, center.y - half - 8f),
        Size(GameConstants.TANK_SIZE * hpRatio.coerceIn(0f, 1f), 4f),
    )
}
