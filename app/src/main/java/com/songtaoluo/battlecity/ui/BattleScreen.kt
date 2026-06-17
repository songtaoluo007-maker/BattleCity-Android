package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import com.songtaoluo.battlecity.game.GameEngine

@Composable
fun BattleScreen(
    engine: GameEngine,
    onExit: () -> Unit,
) {
    BattleContent(engine, onExit)
}
