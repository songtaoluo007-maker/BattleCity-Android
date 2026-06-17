package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.progression.GameSettings
import com.songtaoluo.battlecity.progression.PlayerProgress

@Composable
internal fun SettingsScreen(
    settings: GameSettings,
    progress: PlayerProgress,
    onSettingsChange: (GameSettings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    FrontEndLayout {
        Text("设置与战绩", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        ToggleRow("背景音乐", settings.musicEnabled) {
            onSettingsChange(settings.copy(musicEnabled = it))
        }
        Text("音乐音量 ${(settings.musicVolume * 100).toInt()}%", color = Color(0xFFD8D0A8))
        Slider(
            value = settings.musicVolume,
            onValueChange = { onSettingsChange(settings.copy(musicVolume = it).normalized()) },
            enabled = settings.musicEnabled,
            modifier = Modifier.fillMaxWidth(0.7f),
        )

        ToggleRow("战斗音效", settings.effectsEnabled) {
            onSettingsChange(settings.copy(effectsEnabled = it))
        }
        Text("音效音量 ${(settings.effectsVolume * 100).toInt()}%", color = Color(0xFFD8D0A8))
        Slider(
            value = settings.effectsVolume,
            onValueChange = { onSettingsChange(settings.copy(effectsVolume = it).normalized()) },
            enabled = settings.effectsEnabled,
            modifier = Modifier.fillMaxWidth(0.7f),
        )

        ToggleRow("震动反馈", settings.vibrationEnabled) {
            onSettingsChange(settings.copy(vibrationEnabled = it))
        }

        Spacer(Modifier.height(18.dp))
        Text("长期战绩", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(
            "总战斗 ${progress.totalBattles}  胜利 ${progress.totalVictories}  累计军费 ${progress.totalCredits}",
            color = Color(0xFFFFE082),
        )
        Text("已记录关卡 ${progress.scenarioRecords.size}", color = Color(0xFFD8D0A8))

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack) { Text("返回") }
            Button(onClick = onResetProgress, enabled = progress.totalBattles > 0) {
                Text("清空战绩")
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(0.7f),
    ) {
        Text(label, color = Color.White)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
