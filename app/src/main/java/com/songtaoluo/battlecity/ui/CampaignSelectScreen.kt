package com.songtaoluo.battlecity.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.CampaignCatalog
import com.songtaoluo.battlecity.game.MigratedContentCatalog
import com.songtaoluo.battlecity.model.CampaignData
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.ui.art.OriginalArtCatalog
import com.songtaoluo.battlecity.ui.art.OriginalArtNames
import com.songtaoluo.battlecity.ui.art.PreferredOriginalArtImage

@Composable
internal fun CampaignSelectScreen(
    saveData: SaveData,
    onSelect: (CampaignData) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    FrontEndLayout {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("欧洲装甲战争档案", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("选择战役。未迁移内容保留为档案，不加载占位关卡。", color = Color(0xFFD8D0A8))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onProfile) {
                    Text("${saveData.playerProfile.title} · ${saveData.playerProfile.callsign}")
                }
                OutlinedButton(onClick = onSettings) { Text("设置") }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("军费 ${saveData.credits}", color = Color(0xFFFFD54F))
            Text("最高分 ${saveData.bestScore}", color = Color(0xFFD8D0A8))
            Text("生涯击毁 ${saveData.totalKills}", color = Color(0xFFD8D0A8))
            Text("车辆 ${saveData.ownedVehicles.size}/24", color = Color(0xFFD8D0A8))
            Text("完成关卡 ${saveData.completedScenarios.size}", color = Color(0xFFD8D0A8))
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(CampaignCatalog.all, key = { it.id }) { campaign ->
                CampaignCard(
                    campaign = campaign,
                    completed = campaign.id in saveData.completedCampaigns,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun CampaignCard(
    campaign: CampaignData,
    completed: Boolean,
    onSelect: (CampaignData) -> Unit,
) {
    val playable = MigratedContentCatalog.isCampaignPlayable(campaign.id)
    val accent = Color(AndroidColor.parseColor(campaign.iconColor))
    Card(
        modifier = Modifier.width(270.dp).height(330.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE1A1F1A)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.fillMaxWidth().height(6.dp).background(accent))
            PreferredOriginalArtImage(
                resourceStem = OriginalArtNames.campaign(campaign.id),
                region = OriginalArtCatalog.campaign(campaign.id),
                contentDescription = campaign.name,
                modifier = Modifier.fillMaxWidth().height(92.dp).background(Color.Black),
                contentScale = ContentScale.Crop,
            )
            Text(campaign.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(campaign.subtitle, color = Color(0xFFFFD54F))
            if (completed) Text("战役已完成", color = Color(0xFF9CCC65))
            Text(campaign.description, color = Color(0xFFD6D8D1), modifier = Modifier.weight(1f))
            Text("难度 ${campaign.difficulty} · ${campaign.scenarioCount}条战线", color = Color(0xFFB8BCAE))
            Button(onClick = { onSelect(campaign) }, enabled = playable, modifier = Modifier.fillMaxWidth()) {
                Text(if (playable) "进入战役" else "内容迁移中")
            }
        }
    }
}
