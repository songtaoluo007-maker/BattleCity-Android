package com.songtaoluo.battlecity.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.CampaignCatalog
import com.songtaoluo.battlecity.game.MigratedContentCatalog
import com.songtaoluo.battlecity.model.CampaignData

@Composable
internal fun CampaignSelectScreen(onSelect: (CampaignData) -> Unit) {
    FrontEndLayout {
        Text("欧洲装甲战争档案", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("选择战役。未迁移内容保留为档案，不加载占位关卡。", color = Color(0xFFD8D0A8))
        Spacer(Modifier.height(18.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(CampaignCatalog.all, key = { it.id }) { campaign ->
                CampaignCard(campaign, onSelect)
            }
        }
    }
}

@Composable
private fun CampaignCard(campaign: CampaignData, onSelect: (CampaignData) -> Unit) {
    val playable = MigratedContentCatalog.isCampaignPlayable(campaign.id)
    val accent = Color(AndroidColor.parseColor(campaign.iconColor))
    Card(
        modifier = Modifier.width(270.dp).height(250.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE1A1F1A)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.fillMaxWidth().height(8.dp).background(accent))
            Text(campaign.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(campaign.subtitle, color = Color(0xFFFFD54F))
            Text(campaign.description, color = Color(0xFFD6D8D1), modifier = Modifier.weight(1f))
            Text("难度 ${campaign.difficulty} · ${campaign.scenarioCount}条战线", color = Color(0xFFB8BCAE))
            Button(onClick = { onSelect(campaign) }, enabled = playable, modifier = Modifier.fillMaxWidth()) {
                Text(if (playable) "进入战役" else "内容迁移中")
            }
        }
    }
}
