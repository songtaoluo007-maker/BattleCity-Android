package com.songtaoluo.battlecity.ui

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
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.ScenarioData
import com.songtaoluo.battlecity.model.VehicleId
import com.songtaoluo.battlecity.model.VehicleSpec
import com.songtaoluo.battlecity.ui.art.OriginalArtCatalog
import com.songtaoluo.battlecity.ui.art.OriginalArtNames
import com.songtaoluo.battlecity.ui.art.PreferredOriginalArtImage

@Composable
internal fun GarageScreen(
    scenario: ScenarioData,
    selectedVehicleId: VehicleId?,
    credits: Int,
    ownedVehicles: Set<VehicleId>,
    onBack: () -> Unit,
    onSelect: (VehicleId) -> Unit,
    onPurchase: (VehicleId) -> Unit,
    onContinue: () -> Unit,
) {
    val vehicles = scenario.allyVehicles.map(VehicleCatalog::get)
    val selectedIsOwned = selectedVehicleId != null && selectedVehicleId in ownedVehicles

    FrontEndLayout {
        Text("装甲车库", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("${scenario.faction.displayName} · ${scenario.operation}", color = Color(0xFFFFD54F))
        Text("可用军费 $credits", color = Color(0xFFE8D9A7))
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(vehicles, key = { it.id }) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    selected = selectedVehicleId == vehicle.id,
                    owned = vehicle.id in ownedVehicles,
                    credits = credits,
                    onSelect = onSelect,
                    onPurchase = onPurchase,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack) { Text("返回阵营") }
            Button(onClick = onContinue, enabled = selectedIsOwned) { Text("进入作战简报") }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleSpec,
    selected: Boolean,
    owned: Boolean,
    credits: Int,
    onSelect: (VehicleId) -> Unit,
    onPurchase: (VehicleId) -> Unit,
) {
    Card(
        modifier = Modifier.width(290.dp).height(360.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected && owned -> Color(0xFF344233)
                owned -> Color(0xEE1A1F1A)
                else -> Color(0xEE20201E)
            },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                vehicle.displayName,
                color = if (owned) Color.White else Color(0xFF9A9C96),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(vehicle.role.wireValue, color = Color(0xFFFFD54F))
            PreferredOriginalArtImage(
                resourceStem = OriginalArtNames.vehicle(vehicle.id),
                region = OriginalArtCatalog.vehicle(vehicle.id),
                contentDescription = vehicle.displayName,
                modifier = Modifier.fillMaxWidth().height(92.dp).background(Color(0xFF111411)),
                alpha = if (owned) 1f else 0.45f,
            )
            Text(vehicle.history, color = Color(0xFFD6D8D1), modifier = Modifier.weight(1f))
            Text("HP ${vehicle.hp}  速度 ${vehicle.speed.toInt()}  穿深 ${vehicle.penetration}", color = Color(0xFFB8BCAE))
            Text("装甲 ${vehicle.armorFront}/${vehicle.armorSide}/${vehicle.armorRear}", color = Color(0xFFB8BCAE))
            if (owned) {
                Button(onClick = { onSelect(vehicle.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selected) "已选择" else "选择车辆")
                }
            } else {
                Text("未解锁 · 价格 ${vehicle.price}", color = Color(0xFFFFAB91))
                Button(
                    onClick = { onPurchase(vehicle.id) },
                    enabled = credits >= vehicle.price,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (credits >= vehicle.price) "购买车辆" else "军费不足")
                }
            }
        }
    }
}
