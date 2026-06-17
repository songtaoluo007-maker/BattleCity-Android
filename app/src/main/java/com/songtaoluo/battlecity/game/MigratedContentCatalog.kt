package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.ScenarioData

object MigratedContentCatalog {
    private const val KURSK_CAMPAIGN_ID = "kursk-1943"

    fun isCampaignPlayable(campaignId: String): Boolean =
        scenariosForCampaign(campaignId).isNotEmpty()

    fun scenariosForCampaign(campaignId: String): List<ScenarioData> = when (campaignId) {
        KURSK_CAMPAIGN_ID -> ScenarioCatalog.all
        else -> emptyList()
    }
}
