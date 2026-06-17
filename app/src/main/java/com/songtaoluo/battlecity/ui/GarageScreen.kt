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
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.ScenarioData
import com.songtaoluo.battlecity.model.VehicleId
import com.songtaoluo.battlecity.model.VehicleSpec

@Composable
internal fun GarageScreen(
    scenario: ScenarioData,
    selectedVehicleId: VehicleId?,
    onBack: () -> Unit,
    onSelect: (VehicleId) -> Unit,
    onContinue: () -> Unit,
) {
    val vehicles = scenario.allyVehicles.map(VehicleCatalog::get)
    FrontEndLayout {
        Text("装甲车库", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("${scenario.faction.displayName} · ${scenario.operation}", color = Color(0xFFFFD54F))
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(vehicles, key = { it.id }) { vehicle ->
                VehicleCard(vehicle, selectedVehicleId == vehicle.id, onSelect)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack) { Text("返回阵营") }
            Button(onClick = onContinue, enabled = selectedVehicleId != null) { Text("进入作战简报") }
        }
    }
}

@Composable
private fun VehicleCard(vehicle: VehicleSpec, selected: Boolean, onSelect: (VehicleId) -> Unit) {
    Card(
        modifier = Modifier.width(290.dp).height(270.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF344233) else Color(0xEE1A1F1A),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(vehicle.displayName, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(vehicle.role.wireValue, color = Color(0xFFFFD54F))
            Text(vehicle.history, color = Color(0xFFD6D8D1), modifier = Modifier.weight(1f))
            Text("HP ${vehicle.hp}  速度 ${vehicle.speed.toInt()}  穿深 ${vehicle.penetration}", color = Color(0xFFB8BCAE))
            Text("装甲 ${vehicle.armorFront}/${vehicle.armorSide}/${vehicle.armorRear}", color = Color(0xFFB8BCAE))
            Button(onClick = { onSelect(vehicle.id) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (selected) "已选择" else "选择车辆")
            }
        }
    }
}
