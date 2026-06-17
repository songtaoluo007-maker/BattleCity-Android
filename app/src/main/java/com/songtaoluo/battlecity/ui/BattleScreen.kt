package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.progression.BattleSummary

@Composable
fun BattleScreen(
    engine: GameEngine,
    audioController: AndroidAudioController,
    onBattleFinished: (BattleSummary) -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    BattleContent(
        engine = engine,
        audioController = audioController,
        onBattleFinished = onBattleFinished,
        onRestart = onRestart,
        onExit = onExit,
    )
}
