package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.audio.AudioCue
import com.songtaoluo.battlecity.audio.BattleAudioObserver
import com.songtaoluo.battlecity.audio.MusicThemeResolver
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.TargetingMode
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.BattleSummary
import com.songtaoluo.battlecity.model.Direction
import kotlinx.coroutines.isActive
import kotlin.math.ceil

@Composable
internal fun BattleContent(
    engine: GameEngine,
    campaignId: String,
    audioController: AndroidAudioController,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onFinished: (BattleSummary) -> Unit,
) {
    var direction by remember(engine) { mutableStateOf<Direction?>(null) }
    var frame by remember(engine) { mutableIntStateOf(0) }
    var paused by remember(engine) { mutableStateOf(false) }
    var settled by remember(engine) { mutableStateOf(false) }
    val audioObserver = remember(engine) { BattleAudioObserver() }

    LaunchedEffect(engine, audioController, audioObserver) {
        var previous = 0L
        audioObserver.reset(engine)
        while (isActive) {
            withFrameNanos { now ->
                if (previous != 0L) {
                    val delta = ((now - previous) / 1_000_000_000f).coerceAtMost(0.05f)
                    if (!paused && !engine.victory && !engine.gameOver) {
                        engine.update(delta, direction)
                    }
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
                    if ((engine.victory || engine.gameOver) && !settled) {
                        settled = true
                        direction = null
                        onFinished(
                            BattleSummary(
                                scenarioId = engine.scenario.id,
                                campaignId = campaignId,
                                victory = engine.victory,
                                score = engine.score,
                                destroyedEnemies = engine.destroyedEnemies,
                                elapsedSeconds = engine.battleElapsedMs / 1000L,
                                earnedCredits = (engine.credits - 1600).coerceAtLeast(0),
                            ),
                        )
                    }
                    frame++
                }
                previous = now
            }
        }
    }

    LaunchedEffect(engine.targetingMode, paused) {
        if (engine.targetingMode != TargetingMode.NONE || paused) direction = null
    }

    val controlsEnabled = !paused && !engine.victory && !engine.gameOver

    Box(Modifier.fillMaxSize().background(Color(0xFF10150F))) {
        BattlefieldCanvas(engine, frame, Modifier.fillMaxSize())
        BattleHud(engine, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        DirectionPad(
            onDirection = { direction = it },
            enabled = controlsEnabled && engine.targetingMode == TargetingMode.NONE,
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
        )
        SquadControls(
            current = engine.squadOrder,
            onChange = engine::setSquadOrder,
            enabled = controlsEnabled,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
        )
        SupportControls(
            engine = engine,
            enabled = controlsEnabled,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp),
        )
        Button(
            onClick = engine::fire,
            enabled = controlsEnabled && engine.player.alive &&
                engine.targetingMode == TargetingMode.NONE,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(112.dp, 58.dp),
        ) { Text("FIRE") }
        OutlinedButton(
            onClick = { paused = true },
            enabled = controlsEnabled,
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
        ) { Text("暂停") }
        if (engine.targetingMode != TargetingMode.NONE && controlsEnabled) {
            TargetingPrompt(engine, Modifier.align(Alignment.TopStart).padding(16.dp))
        }
        if (paused && !engine.victory && !engine.gameOver) {
            BlockingOverlay {
                PausePanel(
                    onResume = { paused = false },
                    onRestart = onRestart,
                    onExit = onExit,
                )
            }
        }
        if (engine.victory || engine.gameOver) {
            BlockingOverlay {
                ResultArchivePanel(
                    engine = engine,
                    onRestart = onRestart,
                    onExit = onExit,
                )
            }
        }
    }
}

@Composable
private fun BlockingOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .pointerInput(Unit) { detectTapGestures(onTap = {}) },
        contentAlignment = Alignment.Center,
    ) {
        content()
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
            "SCORE ${engine.score}  本局军费 ${engine.credits}  指挥点 ${engine.commandPoints}  " +
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
private fun PausePanel(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.background(Color(0xEE111111)).padding(28.dp),
    ) {
        Text("战斗暂停", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onResume) { Text("继续作战") }
        OutlinedButton(onClick = onRestart) { Text("重新开始") }
        OutlinedButton(onClick = onExit) { Text("退出到简报") }
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
