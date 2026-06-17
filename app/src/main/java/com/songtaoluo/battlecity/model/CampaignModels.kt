package com.songtaoluo.battlecity.model

data class CampaignData(
    val id: String,
    val name: String,
    val subtitle: String,
    val year: String,
    val description: String,
    val scenarioCount: Int,
    val initiallyUnlocked: Boolean,
    val difficulty: String,
    val iconColor: String,
    val alliedFaction: String,
    val axisFaction: String,
    val order: Int,
    val prerequisiteCampaignId: String?,
)
