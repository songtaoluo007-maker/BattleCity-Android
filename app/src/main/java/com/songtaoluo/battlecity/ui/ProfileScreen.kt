package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.AchievementCatalog
import com.songtaoluo.battlecity.model.AchievementDefinition
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.ui.art.OriginalArtCatalog
import com.songtaoluo.battlecity.ui.art.OriginalArtNames
import com.songtaoluo.battlecity.ui.art.PreferredOriginalArtImage

@Composable
internal fun ProfileScreen(
    saveData: SaveData,
    onSaveCallsign: (String) -> Unit,
    onBack: () -> Unit,
) {
    var callsign by remember(saveData.playerProfile.callsign) {
        mutableStateOf(saveData.playerProfile.callsign)
    }
    val states = saveData.achievements.associateBy { it.id }

    FrontEndLayout {
        Text("指挥官档案", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.width(290.dp).fillMaxHeight()
                    .background(Color(0xDD1A1F1A)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PreferredOriginalArtImage(
                    resourceStem = "command_hq_bg_portrait",
                    region = null,
                    contentDescription = "指挥部档案背景",
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    contentScale = ContentScale.Crop,
                )
                Text(saveData.playerProfile.title, color = Color(0xFFFFD54F), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = callsign,
                    onValueChange = { callsign = it.take(20) },
                    label = { Text("呼号") },
                    singleLine = true,
                )
                Button(onClick = { onSaveCallsign(callsign) }, modifier = Modifier.fillMaxWidth()) {
                    Text("保存呼号")
                }
                Text("最高得分 ${saveData.bestScore}", color = Color.White)
                Text("生涯击毁 ${saveData.totalKills}", color = Color.White)
                Text("单局最佳 ${saveData.playerProfile.bestOneMatchKills}", color = Color.White)
                Text("累计作战 ${formatPlayTime(saveData.totalPlayTimeSec)}", color = Color.White)
                Text("军费 ${saveData.credits}", color = Color(0xFFE8D9A7))
                Text("已解锁车辆 ${saveData.ownedVehicles.size}/24", color = Color(0xFFB8BCAE))
                Text("完成关卡 ${saveData.completedScenarios.size}", color = Color(0xFFB8BCAE))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("返回战役档案")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Faction.entries.forEach { faction ->
                    item(key = "header-${faction.wireValue}") {
                        Text(
                            faction.displayName,
                            color = Color(0xFFFFD54F),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    items(
                        items = AchievementCatalog.definitionsFor(faction),
                        key = AchievementDefinition::id,
                    ) { definition ->
                        AchievementCard(
                            definition = definition,
                            unlocked = states[definition.id]?.unlocked == true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(definition: AchievementDefinition, unlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) Color(0xFF344233) else Color(0xEE20231F),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreferredOriginalArtImage(
                resourceStem = OriginalArtNames.medal(definition.id, unlocked),
                region = OriginalArtCatalog.medal(definition.id, unlocked),
                contentDescription = definition.name,
                modifier = Modifier.size(54.dp),
                alpha = if (unlocked) 1f else 0.72f,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    definition.name,
                    color = if (unlocked) Color.White else Color(0xFF8A8D86),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(definition.category, color = Color(0xFFB8BCAE), style = MaterialTheme.typography.bodySmall)
                Text(definition.detail, color = Color(0xFFD6D8D1), style = MaterialTheme.typography.bodySmall)
            }
            Text(if (unlocked) "已获得" else "未解锁", color = if (unlocked) Color(0xFF9CCC65) else Color.Gray)
        }
    }
}

private fun formatPlayTime(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
