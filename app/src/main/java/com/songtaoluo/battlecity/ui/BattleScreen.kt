package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.game.GameEngine

@Composable
fun BattleScreen(
    engine: GameEngine,
    audioController: AndroidAudioController,
    onExit: () -> Unit,
) {
    BattleContent(engine, audioController, onExit)
}
