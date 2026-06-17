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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.GameConstants
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.Tank
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import kotlinx.coroutines.isActive

@Composable
fun BattleScreen() {
    val engine = remember { GameEngine() }
    var inputDirection by remember { mutableStateOf<Direction?>(null) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var frameVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var previousFrame = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (previousFrame != 0L && viewport != IntSize.Zero) {
                    val deltaSeconds = ((frameNanos - previousFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                    engine.update(
                        deltaSeconds = deltaSeconds,
                        width = viewport.width.toFloat(),
                        height = viewport.height.toFloat(),
                        input = inputDirection,
                    )
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
            .background(Color(0xFF10150F))
            .onSizeChanged { viewport = it },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            renderVersion
            drawRect(Color(0xFF293526), size = size)

            val grid = GameConstants.TILE_SIZE
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0x223A4A35), Offset(x, 0f), Offset(x, size.height), 1f)
                x += grid
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0x223A4A35), Offset(0f, y), Offset(size.width, y), 1f)
                y += grid
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
        ) {
            Text(
                text = "KURSK 1943   SCORE ${engine.score}   军费 ${engine.credits}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${playerSpec.shortName}  穿深 ${playerSpec.penetration}  装填 ${playerSpec.reloadMs}ms  |  " +
                    "${enemySpec.shortName} HP ${engine.enemy.hp}/${engine.enemy.maxHp}",
                color = Color(0xFFD8D0A8),
                style = MaterialTheme.typography.bodyMedium,
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
