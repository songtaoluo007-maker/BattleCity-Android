package com.songtaoluo.battlecity.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import kotlinx.coroutines.isActive

@Composable
internal fun BattleContent() {
    val engine = remember { GameEngine() }
    var direction by remember { mutableStateOf<Direction?>(null) }
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var previous = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (previous != 0L) {
                    val delta = ((now - previous) / 1_000_000_000f).coerceAtMost(0.05f)
                    engine.update(delta, direction)
                    frame++
                }
                previous = now
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF10150F))) {
        BattlefieldCanvas(engine, frame, Modifier.fillMaxSize())
        BattleHud(engine, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        DirectionPad({ direction = it }, Modifier.align(Alignment.BottomStart).padding(20.dp))
        SquadControls(
            current = engine.squadOrder,
            onChange = engine::setSquadOrder,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
        )
        Button(
            onClick = engine::fire,
            enabled = engine.player.alive && !engine.victory,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(112.dp, 58.dp),
        ) { Text("FIRE") }
        if (engine.victory || engine.gameOver) {
            ResultPanel(engine, Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun BattleHud(engine: GameEngine, modifier: Modifier) {
    val spec = VehicleCatalog.get(engine.player.vehicleId)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(engine.scenario.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(
            "SCORE ${engine.score}  军费 ${engine.credits}  战果 ${engine.destroyedEnemies}/${engine.scenario.objective.requiredKills}",
            color = Color(0xFFE8D9A7),
        )
        Text(
            "${spec.shortName} HP ${engine.player.hp}/${engine.player.maxHp}  " +
                "友军 ${engine.alliesAlive}  敌军在场 ${engine.enemies.size}  剩余 ${engine.enemiesLeft}",
            color = Color(0xFFD8D0A8),
        )
        Text(engine.combatMessage, color = Color(0xFFFFE082))
    }
}

@Composable
private fun DirectionPad(onDirection: (Direction?) -> Unit, modifier: Modifier) {
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

@Composable
private fun ResultPanel(engine: GameEngine, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(Color(0xDD111111)).padding(28.dp),
    ) {
        Text(
            if (engine.victory) "任务完成" else "任务失败",
            color = if (engine.victory) Color(0xFFFFD54F) else Color(0xFFFF6B5E),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            "战果 ${engine.destroyedEnemies}  友军存活 ${engine.alliesAlive}  得分 ${engine.score}",
            color = Color.White,
        )
    }
}
