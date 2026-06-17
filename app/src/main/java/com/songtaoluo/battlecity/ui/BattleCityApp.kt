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
import com.songtaoluo.battlecity.model.AppFlowState
import com.songtaoluo.battlecity.model.AppStage

@Composable
fun BattleCityApp() {
    var flow by remember { mutableStateOf(AppFlowState()) }

    val campaign = flow.campaignId?.let(CampaignCatalog::get)
    val scenarios = flow.campaignId
        ?.let(MigratedContentCatalog::scenariosForCampaign)
        .orEmpty()
    val scenario = flow.scenarioId?.let { id -> scenarios.firstOrNull { it.id == id } }
    val vehicle = flow.vehicleId?.let(VehicleCatalog::get)

    when (flow.stage) {
        AppStage.CAMPAIGN -> CampaignSelectScreen { selected ->
            flow = flow.selectCampaign(selected.id)
        }

        AppStage.FACTION -> {
            if (campaign != null && scenarios.isNotEmpty()) {
                FactionSelectScreen(
                    campaign = campaign,
                    scenarios = scenarios,
                    onBack = { flow = flow.back() },
                    onSelect = { selected ->
                        flow = flow.selectScenario(
                            id = selected.id,
                            defaultVehicleId = selected.allyVehicles.firstOrNull(),
                        )
                    },
                )
            } else {
                CampaignSelectScreen { selected -> flow = flow.selectCampaign(selected.id) }
            }
        }

        AppStage.GARAGE -> {
            if (scenario != null) {
                GarageScreen(
                    scenario = scenario,
                    selectedVehicleId = flow.vehicleId,
                    onBack = { flow = flow.back() },
                    onSelect = { flow = flow.selectVehicle(it) },
                    onContinue = { flow = flow.showBriefing() },
                )
            } else {
                CampaignSelectScreen { selected -> flow = flow.selectCampaign(selected.id) }
            }
        }

        AppStage.BRIEFING -> {
            if (scenario != null && vehicle != null) {
                BriefingScreen(
                    scenario = scenario,
                    vehicle = vehicle,
                    onBack = { flow = flow.back() },
                    onStart = { flow = flow.startBattle() },
                )
            } else {
                CampaignSelectScreen { selected -> flow = flow.selectCampaign(selected.id) }
            }
        }

        AppStage.BATTLE -> {
            if (scenario != null && vehicle != null) {
                val engine = remember(scenario.id, vehicle.id) {
                    GameEngine(
                        scenario = scenario,
                        selectedVehicleId = vehicle.id,
                    )
                }
                BattleScreen(
                    engine = engine,
                    onExit = { flow = flow.back() },
                )
            } else {
                CampaignSelectScreen { selected -> flow = flow.selectCampaign(selected.id) }
            }
        }
    }
}
