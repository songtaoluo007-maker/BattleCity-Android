package com.songtaoluo.battlecity.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppFlowTest {
    @Test
    fun navigationKeepsSelections() {
        var state = AppFlowState()
        state = state.selectCampaign("kursk-1943")
        state = state.selectScenario("kursk-1943-german-breakthrough", VehicleId.PZ_IV_H)
        state = state.selectVehicle(VehicleId.TIGER_I)
        state = state.showBriefing().startBattle()

        assertEquals(AppStage.BATTLE, state.stage)
        assertEquals("kursk-1943", state.campaignId)
        assertEquals(VehicleId.TIGER_I, state.vehicleId)
        assertEquals(AppStage.BRIEFING, state.back().stage)
    }

    @Test
    fun newCampaignClearsScenarioAndVehicle() {
        val state = AppFlowState(
            stage = AppStage.BRIEFING,
            campaignId = "kursk-1943",
            scenarioId = "kursk-1943-german-breakthrough",
            vehicleId = VehicleId.TIGER_I,
        ).selectCampaign("moscow-1941")

        assertEquals(AppStage.FACTION, state.stage)
        assertNull(state.scenarioId)
        assertNull(state.vehicleId)
    }

    @Test
    fun backFromFactionReturnsCleanCampaignStage() {
        val state = AppFlowState().selectCampaign("kursk-1943").back()

        assertEquals(AppStage.CAMPAIGN, state.stage)
        assertNull(state.campaignId)
    }
}
