package com.songtaoluo.battlecity.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.songtaoluo.battlecity.progression.ProgressionSystem
import com.songtaoluo.battlecity.progression.SaveData
import com.songtaoluo.battlecity.progression.SharedPreferencesSaveRepository

@Composable
fun BattleCityApp() {
    val context = LocalContext.current
    val saveRepository = remember { SharedPreferencesSaveRepository(context) }
    var saveData by remember { mutableStateOf(saveRepository.load()) }
    var flow by remember { mutableStateOf(AppFlowState()) }
    var battleSession by remember { mutableIntStateOf(0) }
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

    LaunchedEffect(saveData.settings) {
        val settings = saveData.settings.normalized()
        audioController.setMusicEnabled(settings.musicEnabled)
        audioController.setEffectsEnabled(settings.effectsEnabled)
        audioController.setMusicVolume(settings.musicVolume)
        audioController.setEffectsVolume(settings.effectsVolume)
    }

    LaunchedEffect(flow.stage, scenario?.id, saveData.settings.musicEnabled) {
        if (!saveData.settings.musicEnabled) return@LaunchedEffect
        when (flow.stage) {
            AppStage.CAMPAIGN,
            AppStage.FACTION,
            AppStage.SETTINGS,
            -> audioController.switchMusic(MusicThemeResolver.menuFor(null))

            AppStage.GARAGE,
            AppStage.BRIEFING,
            -> audioController.switchMusic(MusicThemeResolver.menuFor(scenario?.faction))

            AppStage.BATTLE -> scenario?.let { selected ->
                audioController.switchMusic(MusicThemeResolver.battleFor(selected))
            }
        }
    }

    fun persist(updated: SaveData) {
        saveData = updated
        saveRepository.save(updated)
    }

    fun campaignScreen() = Unit

    when (flow.stage) {
        AppStage.CAMPAIGN -> CampaignSelectScreen(
            progress = saveData.progress,
            onSettings = { flow = flow.showSettings() },
            onSelect = { selected -> flow = flow.selectCampaign(selected.id) },
        )

        AppStage.SETTINGS -> SettingsScreen(
            settings = saveData.settings,
            progress = saveData.progress,
            onSettingsChange = { settings ->
                persist(saveData.copy(settings = settings.normalized()))
            },
            onResetProgress = {
                persist(saveData.copy(progress = com.songtaoluo.battlecity.progression.PlayerProgress()))
            },
            onBack = { flow = flow.back() },
        )

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
                flow = AppFlowState()
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
                flow = AppFlowState()
            }
        }

        AppStage.BRIEFING -> {
            if (scenario != null && vehicle != null) {
                BriefingScreen(
                    scenario = scenario,
                    vehicle = vehicle,
                    onBack = { flow = flow.back() },
                    onStart = {
                        battleSession += 1
                        flow = flow.startBattle()
                    },
                )
            } else {
                flow = AppFlowState()
            }
        }

        AppStage.BATTLE -> {
            if (scenario != null && vehicle != null) {
                val engine = remember(scenario.id, vehicle.id, battleSession) {
                    GameEngine(
                        scenario = scenario,
                        selectedVehicleId = vehicle.id,
                    )
                }
                BattleScreen(
                    engine = engine,
                    audioController = audioController,
                    onBattleFinished = { summary ->
                        persist(
                            saveData.copy(
                                progress = ProgressionSystem.applyBattle(saveData.progress, summary),
                            ),
                        )
                    },
                    onRestart = { battleSession += 1 },
                    onExit = { flow = flow.back() },
                )
            } else {
                flow = AppFlowState()
            }
        }
    }
}
