package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.ui.art.OriginalArtCatalog
import com.songtaoluo.battlecity.ui.art.OriginalArtNames
import com.songtaoluo.battlecity.ui.art.PreferredOriginalArtImage

@Composable
internal fun ResultArchivePanel(
    engine: GameEngine,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier.width(520.dp).height(360.dp),
        contentAlignment = Alignment.Center,
    ) {
        PreferredOriginalArtImage(
            resourceStem = OriginalArtNames.result(engine.victory),
            region = OriginalArtCatalog.result(engine.victory),
            contentDescription = "作战结算背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC111111))
                .padding(26.dp),
        ) {
            Text(
                if (engine.victory) "任务完成" else "任务结束",
                color = if (engine.victory) Color(0xFFFFD54F) else Color(0xFFFF6B5E),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                "战果 ${engine.destroyedEnemies}  友军存活 ${engine.alliesAlive}  得分 ${engine.score}",
                color = Color.White,
            )
            Text(
                "本局获得军费 ${(engine.credits - 1600).coerceAtLeast(0)}",
                color = Color(0xFFE8D9A7),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRestart) { Text("再次作战") }
                OutlinedButton(onClick = onExit) { Text("返回作战简报") }
            }
        }
    }
}
