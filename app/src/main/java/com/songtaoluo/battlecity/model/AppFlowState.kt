package com.songtaoluo.battlecity.model

enum class AppStage {
    CAMPAIGN,
    FACTION,
    GARAGE,
    BRIEFING,
    BATTLE,
}

data class AppFlowState(
    val stage: AppStage = AppStage.CAMPAIGN,
    val campaignId: String? = null,
    val scenarioId: String? = null,
    val vehicleId: VehicleId? = null,
) {
    fun selectCampaign(id: String): AppFlowState = copy(
        stage = AppStage.FACTION,
        campaignId = id,
        scenarioId = null,
        vehicleId = null,
    )

    fun selectScenario(id: String, defaultVehicleId: VehicleId?): AppFlowState = copy(
        stage = AppStage.GARAGE,
        scenarioId = id,
        vehicleId = defaultVehicleId,
    )

    fun selectVehicle(id: VehicleId): AppFlowState = copy(vehicleId = id)

    fun showBriefing(): AppFlowState = copy(stage = AppStage.BRIEFING)

    fun startBattle(): AppFlowState = copy(stage = AppStage.BATTLE)

    fun back(): AppFlowState = when (stage) {
        AppStage.CAMPAIGN -> this
        AppStage.FACTION -> copy(
            stage = AppStage.CAMPAIGN,
            campaignId = null,
            scenarioId = null,
            vehicleId = null,
        )
        AppStage.GARAGE -> copy(
            stage = AppStage.FACTION,
            scenarioId = null,
            vehicleId = null,
        )
        AppStage.BRIEFING -> copy(stage = AppStage.GARAGE)
        AppStage.BATTLE -> copy(stage = AppStage.BRIEFING)
    }
}
