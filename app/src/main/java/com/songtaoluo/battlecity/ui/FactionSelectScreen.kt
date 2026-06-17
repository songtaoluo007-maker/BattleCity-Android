package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.model.CampaignData
import com.songtaoluo.battlecity.model.ScenarioData

@Composable
internal fun FactionSelectScreen(
    campaign: CampaignData,
    scenarios: List<ScenarioData>,
    onBack: () -> Unit,
    onSelect: (ScenarioData) -> Unit,
) {
    FrontEndLayout {
        Text(campaign.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("选择作战阵营与行动线", color = Color(0xFFD8D0A8))
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            scenarios.forEach { scenario -> ScenarioCard(scenario, onSelect) }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onBack) { Text("返回战役档案") }
    }
}

@Composable
private fun ScenarioCard(scenario: ScenarioData, onSelect: (ScenarioData) -> Unit) {
    Card(
        modifier = Modifier.width(330.dp).height(230.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE1A1F1A)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(scenario.faction.displayName, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(scenario.operation, color = Color(0xFFFFD54F))
            Text(scenario.historicalBackground, color = Color(0xFFD6D8D1), modifier = Modifier.weight(1f))
            Button(onClick = { onSelect(scenario) }, modifier = Modifier.fillMaxWidth()) {
                Text("选择该阵营")
            }
        }
    }
}
