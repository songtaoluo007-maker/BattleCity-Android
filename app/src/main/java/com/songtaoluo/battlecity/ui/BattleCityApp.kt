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
import com.songtaoluo.battlecity.game.ProgressionSystem
import com.songtaoluo.battlecity.game.PurchaseResult
import com.songtaoluo.battlecity.game.VehicleCatalog
import com.songtaoluo.battlecity.game.VehiclePurchaseSystem
import com.songtaoluo.battlecity.model.AppFlowState
import com.songtaoluo.battlecity.model.AppStage
import com.songtaoluo.battlecity.model.BattleSummary
import com.songtaoluo.battlecity.model.CampaignData
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.storage.AndroidSaveRepository

@Composable
fun BattleCityApp() {
    val context = LocalContext.current
    val saveRepository = remember { AndroidSaveRepository(context) }
    val audioController = remember { AndroidAudioController(context) }
    var saveData by remember { mutableStateOf(saveRepository.load()) }
    var flow by remember { mutableStateOf(AppFlowState()) }
    var battleSessionId by remember { mutableIntStateOf(0) }

    val campaign = flow.campaignId?.let(CampaignCatalog::get)
    val scenarios = flow.campaignId
        ?.let(MigratedContentCatalog::scenariosForCampaign)
        .orEmpty()
    val scenario = flow.scenarioId?.let { id -> scenarios.firstOrNull { it.id == id } }
    val vehicle = flow.vehicleId?.let(VehicleCatalog::get)

    fun persist(updated: SaveData) {
        val normalized = ProgressionSystem.normalize(updated)
        saveRepository.save(normalized)
        saveData = normalized
    }

    DisposableEffect(audioController) {
        onDispose { audioController.close() }
    }

    LaunchedEffect(flow.stage, scenario?.id, battleSessionId, saveData.audioSettings) {
        val settings = saveData.audioSettings
        audioController.setEnabled(settings.soundEnabled)
        audioController.setMusicVolume(settings.musicVolume)
        audioController.setEffectsVolume(settings.combatVolume)
        if (!settings.soundEnabled) return@LaunchedEffect

        when (flow.stage) {
            AppStage.CAMPAIGN,
            AppStage.FACTION,
            AppStage.PROFILE,
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

    val showCampaignHome: @Composable () -> Unit = {
        CampaignHome(
            saveData = saveData,
            onSelect = { selected -> flow = flow.selectCampaign(selected.id) },
            onProfile = { flow = flow.showProfile() },
            onSettings = { flow = flow.showSettings() },
        )
    }

    when (flow.stage) {
        AppStage.CAMPAIGN -> showCampaignHome()

        AppStage.FACTION -> {
            if (campaign != null && scenarios.isNotEmpty()) {
                FactionSelectScreen(
                    campaign = campaign,
                    scenarios = scenarios,
                    onBack = { flow = flow.back() },
                    onSelect = { selected ->
                        val defaultVehicle = selected.allyVehicles.firstOrNull {
                            it in saveData.ownedVehicles
                        } ?: selected.allyVehicles.firstOrNull()
                        flow = flow.selectScenario(
                            id = selected.id,
                            defaultVehicleId = defaultVehicle,
                        )
                    },
                )
            } else {
                showCampaignHome()
            }
        }

        AppStage.GARAGE -> {
            if (scenario != null) {
                GarageScreen(
                    scenario = scenario,
                    selectedVehicleId = flow.vehicleId,
                    credits = saveData.credits,
                    ownedVehicles = saveData.ownedVehicles,
                    onBack = { flow = flow.back() },
                    onSelect = { id ->
                        if (id in saveData.ownedVehicles) flow = flow.selectVehicle(id)
                    },
                    onPurchase = { id ->
                        when (val result = VehiclePurchaseSystem.purchase(saveData, VehicleCatalog.get(id))) {
                            is PurchaseResult.Success -> {
                                persist(result.save)
                                flow = flow.selectVehicle(id)
                            }
                            PurchaseResult.AlreadyOwned -> flow = flow.selectVehicle(id)
                            PurchaseResult.InsufficientCredits -> Unit
                        }
                    },
                    onContinue = { flow = flow.showBriefing() },
                )
            } else {
                showCampaignHome()
            }
        }

        AppStage.BRIEFING -> {
            if (scenario != null && vehicle != null && vehicle.id in saveData.ownedVehicles) {
                BriefingScreen(
                    scenario = scenario,
                    vehicle = vehicle,
                    onBack = { flow = flow.back() },
                    onStart = {
                        battleSessionId += 1
                        flow = flow.startBattle()
                    },
                )
            } else if (scenario != null) {
                GarageScreen(
                    scenario = scenario,
                    selectedVehicleId = flow.vehicleId,
                    credits = saveData.credits,
                    ownedVehicles = saveData.ownedVehicles,
                    onBack = { flow = flow.back() },
                    onSelect = { id -> flow = flow.selectVehicle(id) },
                    onPurchase = { id ->
                        val result = VehiclePurchaseSystem.purchase(saveData, VehicleCatalog.get(id))
                        if (result is PurchaseResult.Success) persist(result.save)
                    },
                    onContinue = { flow = flow.showBriefing() },
                )
            } else {
                showCampaignHome()
            }
        }

        AppStage.BATTLE -> {
            if (scenario != null && vehicle != null && vehicle.id in saveData.ownedVehicles) {
                val engine = remember(scenario.id, vehicle.id, battleSessionId) {
                    GameEngine(
                        scenario = scenario,
                        selectedVehicleId = vehicle.id,
                    )
                }
                BattleScreen(
                    engine = engine,
                    campaignId = flow.campaignId.orEmpty(),
                    audioController = audioController,
                    onRestart = { battleSessionId += 1 },
                    onExit = { flow = flow.back() },
                    onFinished = { summary ->
                        persist(settleAndCompleteCampaign(saveData, summary))
                    },
                )
            } else {
                showCampaignHome()
            }
        }

        AppStage.PROFILE -> ProfileScreen(
            saveData = saveData,
            onSaveCallsign = { callsign ->
                persist(ProgressionSystem.updateProfile(saveData, callsign))
            },
            onBack = { flow = flow.back() },
        )

        AppStage.SETTINGS -> SettingsScreen(
            settings = saveData.audioSettings,
            onUpdate = { settings -> persist(saveData.copy(audioSettings = settings.normalized())) },
            onResetProgress = {
                saveData = saveRepository.reset()
                battleSessionId += 1
                flow = AppFlowState()
            },
            onBack = { flow = flow.back() },
        )
    }
}

@Composable
private fun CampaignHome(
    saveData: SaveData,
    onSelect: (CampaignData) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    CampaignSelectScreen(
        saveData = saveData,
        onSelect = onSelect,
        onProfile = onProfile,
        onSettings = onSettings,
    )
}

private fun settleAndCompleteCampaign(current: SaveData, summary: BattleSummary): SaveData {
    var updated = ProgressionSystem.settleBattle(current, summary)
    if (!summary.victory || summary.campaignId.isBlank()) return updated

    val scenarioIds = MigratedContentCatalog.scenariosForCampaign(summary.campaignId)
        .mapTo(mutableSetOf()) { it.id }
    if (scenarioIds.isNotEmpty() && scenarioIds.all { it in updated.completedScenarios }) {
        updated = ProgressionSystem.markCampaignCompleted(updated, summary.campaignId)
    }
    return updated
}
