package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.model.AudioSettings

@Composable
internal fun SettingsScreen(
    settings: AudioSettings,
    onUpdate: (AudioSettings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }

    FrontEndLayout {
        Text("系统设置", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Column(
            modifier = Modifier.width(560.dp).padding(top = 18.dp)
                .background(Color(0xDD1A1F1A)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("声音总开关", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("关闭后音乐和战斗音效全部静音", color = Color(0xFFB8BCAE))
                }
                Switch(
                    checked = settings.soundEnabled,
                    onCheckedChange = { onUpdate(settings.copy(soundEnabled = it)) },
                )
            }

            VolumeSlider("音乐音量", settings.musicVolume) {
                onUpdate(settings.copy(musicVolume = it))
            }
            VolumeSlider("语音音量", settings.voiceVolume) {
                onUpdate(settings.copy(voiceVolume = it))
            }
            VolumeSlider("战斗音效", settings.combatVolume) {
                onUpdate(settings.copy(combatVolume = it))
            }

            Text("存档管理", color = Color.White, style = MaterialTheme.typography.titleMedium)
            if (!confirmReset) {
                OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("重置全部进度")
                }
            } else {
                Text("将清除军费、车辆、战绩与全部勋章。此操作无法撤销。", color = Color(0xFFFF8A80))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onResetProgress()
                            confirmReset = false
                        },
                    ) { Text("确认重置") }
                    OutlinedButton(onClick = { confirmReset = false }) { Text("取消") }
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回战役档案")
            }
        }
    }
}

@Composable
private fun VolumeSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = Color.White)
            Text("${(value.coerceIn(0f, 1f) * 100).toInt()}%", color = Color(0xFFFFD54F))
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onChange,
            valueRange = 0f..1f,
        )
    }
}
