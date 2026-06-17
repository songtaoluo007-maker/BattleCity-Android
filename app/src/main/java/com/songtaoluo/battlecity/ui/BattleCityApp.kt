package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.songtaoluo.battlecity.game.CampaignCatalog
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.MigratedContentCatalog
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.VehicleId

enum class AppStage {
    CAMPAIGN,
    FACTION,
    GARAGE,
    BRIEFING,
    BATTLE,
}

@Composable
fun BattleCityApp() {
    var stage by remember { mutableStateOf(AppStage.CAMPAIGN) }
    var campaignId by remember { mutableStateOf<String?>(null) }
    var scenarioId by remember { mutableStateOf<String?>(null) }
    var vehicleId by remember { mutableStateOf<VehicleId?>(null) }

    val campaign = campaignId?.let(CampaignCatalog::get)
    val scenarios = campaignId?.let(MigratedContentCatalog::scenariosForCampaign).orEmpty()
    val scenario = scenarioId?.let { id -> scenarios.firstOrNull { it.id == id } }
    val vehicle = vehicleId?.let(VehicleCatalog::get)

    when (stage) {
        AppStage.CAMPAIGN -> CampaignSelectScreen { selected ->
            campaignId = selected.id
            scenarioId = null
            vehicleId = null
            stage = AppStage.FACTION
        }

        AppStage.FACTION -> {
            if (campaign == null || scenarios.isEmpty()) {
                stage = AppStage.CAMPAIGN
            } else {
                FactionSelectScreen(
                    campaign = campaign,
                    scenarios = scenarios,
                    onBack = { stage = AppStage.CAMPAIGN },
                    onSelect = { selected ->
                        scenarioId = selected.id
                        vehicleId = selected.allyVehicles.firstOrNull()
                        stage = AppStage.GARAGE
                    },
                )
            }
        }

        AppStage.GARAGE -> {
            if (scenario == null) {
                stage = AppStage.FACTION
            } else {
                GarageScreen(
                    scenario = scenario,
                    selectedVehicleId = vehicleId,
                    onBack = { stage = AppStage.FACTION },
                    onSelect = { vehicleId = it },
                    onContinue = { stage = AppStage.BRIEFING },
                )
            }
        }

        AppStage.BRIEFING -> {
            if (scenario == null || vehicle == null) {
                stage = AppStage.GARAGE
            } else {
                BriefingScreen(
                    scenario = scenario,
                    vehicle = vehicle,
                    onBack = { stage = AppStage.GARAGE },
                    onStart = { stage = AppStage.BATTLE },
                )
            }
        }

        AppStage.BATTLE -> {
            if (scenario == null || vehicle == null) {
                stage = AppStage.BRIEFING
            } else {
                val engine = remember(scenario.id, vehicle.id) {
                    GameEngine(
                        scenario = scenario,
                        selectedVehicleId = vehicle.id,
                    )
                }
                BattleScreen(
                    engine = engine,
                    onExit = { stage = AppStage.BRIEFING },
                )
            }
        }
    }
}
