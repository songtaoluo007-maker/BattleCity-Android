package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.model.ScenarioData
import com.songtaoluo.battlecity.model.VehicleSpec

@Composable
internal fun BriefingScreen(
    scenario: ScenarioData,
    vehicle: VehicleSpec,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    FrontEndLayout {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("作战简报", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text(scenario.name, color = Color(0xFFFFD54F), style = MaterialTheme.typography.titleLarge)
            Text(scenario.historicalBackground, color = Color(0xFFD6D8D1))
            Text("任务", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(scenario.briefing, color = Color(0xFFD6D8D1))
            Text("目标：${scenario.objective.title}", color = Color(0xFFFFE082))
            Text(scenario.objective.detail, color = Color(0xFFD6D8D1))
            Text("座车：${vehicle.displayName}", color = Color.White)
            Text("敌军预算 ${scenario.enemyBudget} · 同时在场上限 ${scenario.maxActiveEnemies}", color = Color(0xFFB8BCAE))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack) { Text("返回车库") }
                Button(onClick = onStart) { Text("开始作战") }
            }
        }
    }
}
