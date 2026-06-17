package com.songtaoluo.battlecity.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.GameConstants
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.Tank
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TileType
import kotlinx.coroutines.isActive
import kotlin.math.min

@Composable
fun BattleScreen() {
    val engine = remember { GameEngine() }
    var inputDirection by remember { mutableStateOf<Direction?>(null) }
    var frameVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var previousFrame = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (previousFrame != 0L) {
                    val deltaSeconds = ((frameNanos - previousFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                    engine.update(deltaSeconds = deltaSeconds, input = inputDirection)
                    frameVersion++
                }
                previousFrame = frameNanos
            }
        }
    }

    val renderVersion = frameVersion
    val playerSpec = VehicleCatalog.get(engine.player.vehicleId)
    val enemySpec = VehicleCatalog.get(engine.enemy.vehicleId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10150F)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            renderVersion
            drawRect(Color(0xFF10150F), size = size)

            val availableWidth = (size.width - 320f).coerceAtLeast(220f)
            val availableHeight = (size.height - 120f).coerceAtLeast(220f)
            val boardScale = min(
                availableWidth / GameConstants.BOARD_SIZE,
                availableHeight / GameConstants.BOARD_SIZE,
            ).coerceAtMost(1.25f)
            val renderedBoardSize = GameConstants.BOARD_SIZE * boardScale
            val boardLeft = (size.width - renderedBoardSize) / 2f
            val boardTop = 72f + (availableHeight - renderedBoardSize) / 2f

            translate(left = boardLeft, top = boardTop) {
                scale(scaleX = boardScale, scaleY = boardScale, pivot = Offset.Zero) {
                    drawRect(
                        color = Color(0xFF293526),
                        size = Size(GameConstants.BOARD_SIZE, GameConstants.BOARD_SIZE),
                    )
                    engine.tiles.forEachIndexed { row, tiles ->
                        tiles.forEachIndexed { column, tile ->
                            drawBattleTile(tile, column, row)
                        }
                    }

                    drawTank(engine.player)
                    if (engine.enemy.alive) drawTank(engine.enemy)

                    engine.bullets.forEach { bullet ->
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = 4f,
                            center = Offset(bullet.position.x, bullet.position.y),
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
        ) {
            Text(
                text = engine.scenario.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "SCORE ${engine.score}   军费 ${engine.credits}   目标：${engine.scenario.objective.title}",
                color = Color(0xFFE8D9A7),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${playerSpec.shortName} 穿深 ${playerSpec.penetration} 装填 ${playerSpec.reloadMs}ms  |  " +
                    "${enemySpec.shortName} HP ${engine.enemy.hp}/${engine.enemy.maxHp}",
                color = Color(0xFFD8D0A8),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = engine.combatMessage,
                color = Color(0xFFFFE082),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        DirectionPad(
            onDirection = { inputDirection = it },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Button(
                onClick = engine::fire,
                modifier = Modifier.size(width = 112.dp, height = 58.dp),
            ) {
                Text("FIRE")
            }
            if (!engine.enemy.alive) {
                Button(onClick = engine::restartEnemy) {
                    Text("NEXT TARGET")
                }
            }
        }
    }
}

@Composable
private fun DirectionPad(onDirection: (Direction?) -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Button(onClick = { onDirection(Direction.UP) }) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onDirection(Direction.LEFT) }) { Text("←") }
            Button(onClick = { onDirection(null) }) { Text("■") }
            Button(onClick = { onDirection(Direction.RIGHT) }) { Text("→") }
        }
        Button(onClick = { onDirection(Direction.DOWN) }) { Text("↓") }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBattleTile(
    tile: TileType,
    column: Int,
    row: Int,
) {
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

    drawRect(color = color, topLeft = topLeft, size = Size(tileSize, tileSize))
    drawRect(
        color = Color(0x22000000),
        topLeft = topLeft,
        size = Size(tileSize, tileSize),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
    )

    when (tile) {
        TileType.BRICK -> {
            drawLine(Color(0x55804A34), topLeft + Offset(0f, tileSize / 2f), topLeft + Offset(tileSize, tileSize / 2f), 2f)
            drawLine(Color(0x55804A34), topLeft + Offset(tileSize / 2f, 0f), topLeft + Offset(tileSize / 2f, tileSize), 2f)
        }
        TileType.WATER -> {
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 10f), topLeft + Offset(tileSize - 4f, 10f), 2f)
            drawLine(Color(0x667EC8E3), topLeft + Offset(4f, 22f), topLeft + Offset(tileSize - 4f, 22f), 2f)
        }
        TileType.OBJECTIVE -> drawCircle(Color(0xFFFFD54F), 8f, topLeft + Offset(tileSize / 2f, tileSize / 2f))
        TileType.BASE -> drawCircle(Color(0xFF493D1B), 8f, topLeft + Offset(tileSize / 2f, tileSize / 2f))
        else -> Unit
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTank(tank: Tank) {
    val spec = VehicleCatalog.get(tank.vehicleId)
    val bodyColor = Color(AndroidColor.parseColor(spec.bodyColor))
    val darkColor = Color(AndroidColor.parseColor(spec.darkColor))
    val trimColor = Color(AndroidColor.parseColor(spec.trimColor))
    val center = Offset(tank.position.x, tank.position.y)
    val bodySize = GameConstants.TANK_SIZE
    val half = bodySize / 2f

    drawRect(
        color = darkColor,
        topLeft = Offset(center.x - half - 3f, center.y - half),
        size = Size(bodySize + 6f, bodySize),
    )
    drawRect(
        color = bodyColor,
        topLeft = Offset(center.x - half, center.y - half),
        size = Size(bodySize, bodySize),
    )

    val barrelEnd = when (tank.direction) {
        Direction.UP -> Offset(center.x, center.y - half - 13f)
        Direction.DOWN -> Offset(center.x, center.y + half + 13f)
        Direction.LEFT -> Offset(center.x - half - 13f, center.y)
        Direction.RIGHT -> Offset(center.x + half + 13f, center.y)
    }
    drawLine(darkColor, center, barrelEnd, strokeWidth = 6f, cap = StrokeCap.Round)
    drawCircle(trimColor, radius = 6f, center = center)

    val hpRatio = if (tank.maxHp == 0) 0f else tank.hp.toFloat() / tank.maxHp.toFloat()
    drawRect(
        color = Color(0xAA111111),
        topLeft = Offset(center.x - half, center.y - half - 8f),
        size = Size(bodySize, 4f),
    )
    drawRect(
        color = if (hpRatio > 0.5f) Color(0xFF7EC850) else Color(0xFFD95C4F),
        topLeft = Offset(center.x - half, center.y - half - 8f),
        size = Size(bodySize * hpRatio.coerceIn(0f, 1f), 4f),
    )
}
