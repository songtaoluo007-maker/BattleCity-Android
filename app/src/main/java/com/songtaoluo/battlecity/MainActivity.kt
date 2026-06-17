package com.songtaoluo.battlecity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.Direction
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.Tank
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                BattleCityApp()
            }
        }
    }
}

@Composable
private fun BattleCityApp() {
    val engine = remember { GameEngine() }
    var inputDirection by remember { mutableStateOf<Direction?>(null) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var frameVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var previousFrame = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (previousFrame != 0L && viewport != IntSize.Zero) {
                    val delta = ((frameNanos - previousFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                    engine.update(delta, viewport.width.toFloat(), viewport.height.toFloat(), inputDirection)
                    frameVersion++
                }
                previousFrame = frameNanos
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10150F))
            .onSizeChanged { viewport = it },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            frameVersion
            drawRect(Color(0xFF293526), size = size)

            val grid = 64f
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

            drawTank(engine.player, Color(0xFF4CAF50))
            if (engine.enemy.alive) drawTank(engine.enemy, Color(0xFFD85A4F))

            engine.bullets.forEach { bullet ->
                drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(bullet.position.x, bullet.position.y))
            }
        }

        Text(
            text = "KURSK 1943   SCORE ${engine.score}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
        )

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
            Button(onClick = engine::fire, modifier = Modifier.size(width = 112.dp, height = 58.dp)) {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTank(tank: Tank, color: Color) {
    val center = Offset(tank.position.x, tank.position.y)
    drawRect(
        color = color,
        topLeft = Offset(center.x - 27f, center.y - 27f),
        size = Size(54f, 54f),
    )
    val barrelEnd = when (tank.direction) {
        Direction.UP -> Offset(center.x, center.y - 46f)
        Direction.DOWN -> Offset(center.x, center.y + 46f)
        Direction.LEFT -> Offset(center.x - 46f, center.y)
        Direction.RIGHT -> Offset(center.x + 46f, center.y)
    }
    drawLine(Color(0xFF202020), center, barrelEnd, strokeWidth = 10f, cap = StrokeCap.Round)
    drawCircle(Color(0xFF303030), radius = 12f, center = center)
}
