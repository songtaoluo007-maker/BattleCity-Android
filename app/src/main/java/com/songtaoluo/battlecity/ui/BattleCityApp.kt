package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.songtaoluo.battlecity.audio.AndroidAudioController
import com.songtaoluo.battlecity.audio.MusicThemeResolver
import com.songtaoluo.battlecity.game.CampaignCatalog
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.MigratedContentCatalog
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.model.AppFlowState
import com.songtaoluo.battlecity.model.AppStage

@Composable
fun BattleCityApp() {
    var flow by remember { mutableStateOf(AppFlowState()) }
    val context = LocalContext.current
    val audioController = remember { AndroidAudioController(context) }

    val campaign = flow.campaignId?.let(CampaignCatalog::get)
    val scenarios = flow.campaignId
        ?.let(MigratedContentCatalog::scenariosForCampaign)
        .orEmpty()
    val scenario = flow.scenarioId?.let { id -> scenarios.firstOrNull { it.id == id } }
    val vehicle = flow.vehicleId?.let(VehicleCatalog::get)

    DisposableEffect(audioController) {
        onDispose { audioController.close() }
    }

    LaunchedEffect(flow.stage, scenario?.id) {
        when (flow.stage) {
            AppStage.CAMPAIGN,
            AppStage.FACTION,
            -> audioController.switchMusic(MusicThemeResolver.menuFor(null))

            AppStage.GARAGE,
            AppStage.BRIEFING,
            -> audioController.switchMusic(MusicThemeResolver.menuFor(scenario?.faction))

            AppStage.BATTLE -> scenario?.let { selected ->
                audioController.switchMusic(MusicThemeResolver.battleFor(selected))
            }
        }
    }

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
                    audioController = audioController,
                    onExit = { flow = flow.back() },
                )
            } else {
                CampaignSelectScreen { selected -> flow = flow.selectCampaign(selected.id) }
            }
        }
    }
}
