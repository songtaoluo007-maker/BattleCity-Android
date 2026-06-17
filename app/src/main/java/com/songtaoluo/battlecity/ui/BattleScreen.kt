package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.model.BattleSummary

@Composable
fun BattleScreen(
    engine: GameEngine,
    campaignId: String,
    audioController: AndroidAudioController,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onFinished: (BattleSummary) -> Unit,
) {
    BattleContent(
        engine = engine,
        campaignId = campaignId,
        audioController = audioController,
        onRestart = onRestart,
        onExit = onExit,
        onFinished = onFinished,
    )
}
