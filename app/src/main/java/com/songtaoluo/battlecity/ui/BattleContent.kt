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
import androidx.compose.material3.OutlinedButton
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
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.audio.AudioCue
import com.songtaoluo.battlecity.audio.BattleAudioObserver
import com.songtaoluo.battlecity.audio.MusicThemeResolver
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.TargetingMode
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.Direction
import kotlinx.coroutines.isActive
import kotlin.math.ceil

@Composable
internal fun BattleContent(
    engine: GameEngine,
    audioController: AndroidAudioController,
    onExit: () -> Unit,
) {
    var direction by remember(engine) { mutableStateOf<Direction?>(null) }
    var frame by remember(engine) { mutableIntStateOf(0) }
    val audioObserver = remember(engine) { BattleAudioObserver() }

    LaunchedEffect(engine, audioController, audioObserver) {
        var previous = 0L
        audioObserver.reset(engine)
        while (isActive) {
            withFrameNanos { now ->
                if (previous != 0L) {
                    val delta = ((now - previous) / 1_000_000_000f).coerceAtMost(0.05f)
                    engine.update(delta, direction)
                    audioObserver.collect(engine).forEach { cue ->
                        when (cue) {
                            AudioCue.VICTORY -> audioController.switchMusic(
                                MusicThemeResolver.resultFor(engine.scenario.faction, victory = true),
                            )
                            AudioCue.DEFEAT -> audioController.switchMusic(
                                MusicThemeResolver.resultFor(engine.scenario.faction, victory = false),
                            )
                            else -> audioController.play(cue)
                        }
                    }
                    frame++
                }
                previous = now
            }
        }
    }

    LaunchedEffect(engine.targetingMode) {
        if (engine.targetingMode != TargetingMode.NONE) direction = null
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF10150F))) {
        BattlefieldCanvas(engine, frame, Modifier.fillMaxSize())
        BattleHud(engine, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        DirectionPad(
            onDirection = { direction = it },
            enabled = engine.targetingMode == TargetingMode.NONE,
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
        )
        SquadControls(
            current = engine.squadOrder,
            onChange = engine::setSquadOrder,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
        )
        SupportControls(
            engine = engine,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp),
        )
        Button(
            onClick = engine::fire,
            enabled = engine.player.alive &&
                !engine.victory &&
                engine.targetingMode == TargetingMode.NONE,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(112.dp, 58.dp),
        ) { Text("FIRE") }
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
        ) { Text("退出战斗") }
        if (engine.targetingMode != TargetingMode.NONE) {
            TargetingPrompt(engine, Modifier.align(Alignment.TopStart).padding(16.dp))
        }
        if (engine.victory || engine.gameOver) {
            ResultPanel(engine, onExit, Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun BattleHud(engine: GameEngine, modifier: Modifier) {
    val spec = VehicleCatalog.get(engine.player.vehicleId)
    val focusName = engine.selectedFocusTarget?.let { VehicleCatalog.get(it.vehicleId).shortName } ?: "无"
    val timerText = if (engine.scenario.objective.holdMs > 0L) {
        val seconds = ceil(engine.objectiveRemainingMs / 1000.0).toInt()
        "  坚守 ${seconds}s"
    } else {
        ""
    }
    val baseText = if (engine.scenario.objective.holdMs > 0L) {
        if (engine.baseDestroyed) "  基地 已失守" else "  基地 安全"
    } else {
        ""
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(engine.scenario.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(
            "SCORE ${engine.score}  军费 ${engine.credits}  指挥点 ${engine.commandPoints}  " +
                "战果 ${engine.destroyedEnemies}/${engine.scenario.objective.requiredKills}$timerText",
            color = Color(0xFFE8D9A7),
        )
        Text(
            "${spec.shortName} HP ${engine.player.hp}/${engine.player.maxHp}  " +
                "友军 ${engine.alliesAlive}  敌军在场 ${engine.enemies.size}  剩余 ${engine.enemiesLeft}  " +
                "补给 ${engine.powerUps.size}  集火 $focusName$baseText",
            color = Color(0xFFD8D0A8),
        )
        Text(engine.combatMessage, color = Color(0xFFFFE082))
    }
}

@Composable
private fun TargetingPrompt(engine: GameEngine, modifier: Modifier) {
    val title = when (engine.targetingMode) {
        TargetingMode.ARTILLERY -> "炮击选点"
        TargetingMode.FOCUS_FIRE -> "集火选敌"
        TargetingMode.NONE -> return
    }
    val detail = when (engine.targetingMode) {
        TargetingMode.ARTILLERY -> "点击战场确定炮火覆盖中心"
        TargetingMode.FOCUS_FIRE -> "点击带红色识别环的可见敌车"
        TargetingMode.NONE -> ""
    }
    Column(
        modifier = modifier.background(Color(0xDD111111)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = Color(0xFFFFD54F), style = MaterialTheme.typography.titleMedium)
        Text(detail, color = Color.White, style = MaterialTheme.typography.bodySmall)
        Button(onClick = engine::cancelTargeting) { Text("取消") }
    }
}

@Composable
private fun DirectionPad(
    onDirection: (Direction?) -> Unit,
    enabled: Boolean,
    modifier: Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Button(onClick = { onDirection(Direction.UP) }, enabled = enabled) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onDirection(Direction.LEFT) }, enabled = enabled) { Text("←") }
            Button(onClick = { onDirection(null) }, enabled = enabled) { Text("■") }
            Button(onClick = { onDirection(Direction.RIGHT) }, enabled = enabled) { Text("→") }
        }
        Button(onClick = { onDirection(Direction.DOWN) }, enabled = enabled) { Text("↓") }
    }
}

@Composable
private fun ResultPanel(engine: GameEngine, onExit: () -> Unit, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
        Button(onClick = onExit) { Text("返回作战简报") }
    }
}
