package com.songtaoluo.battlecity.model

enum class MedalTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
}

data class AudioSettings(
    val musicVolume: Float = 0.72f,
    val voiceVolume: Float = 0.82f,
    val combatVolume: Float = 0.78f,
    val soundEnabled: Boolean = true,
) {
    fun normalized(): AudioSettings = copy(
        musicVolume = musicVolume.coerceIn(0f, 1f),
        voiceVolume = voiceVolume.coerceIn(0f, 1f),
        combatVolume = combatVolume.coerceIn(0f, 1f),
    )
}

data class AchievementDefinition(
    val id: String,
    val name: String,
    val category: String,
    val detail: String,
    val symbol: String,
    val tier: MedalTier,
    val requiredMatchKills: Int = 0,
    val requiredTotalKills: Int = 0,
    val nation: Faction,
)

data class AchievementState(
    val id: String,
    val unlocked: Boolean = false,
    val unlockedAtEpochMs: Long = 0L,
)

data class PlayerProfile(
    val callsign: String = "指挥官",
    val avatarId: String = "commander-default",
    val title: String = "见习车长",
    val totalKills: Int = 0,
    val bestScore: Int = 0,
    val bestOneMatchKills: Int = 0,
)

data class SaveData(
    val playerName: String = "PLAYER",
    val bestScore: Int = 0,
    val credits: Int = 1600,
    val ownedVehicles: Set<VehicleId> = setOf(VehicleId.PZ_IV_H, VehicleId.T34_76),
    val completedScenarios: Set<String> = emptySet(),
    val completedCampaigns: Set<String> = emptySet(),
    val totalKills: Int = 0,
    val totalPlayTimeSec: Long = 0L,
    val audioSettings: AudioSettings = AudioSettings(),
    val achievements: List<AchievementState> = emptyList(),
    val playerProfile: PlayerProfile = PlayerProfile(),
)

data class BattleSummary(
    val scenarioId: String,
    val campaignId: String,
    val victory: Boolean,
    val score: Int,
    val destroyedEnemies: Int,
    val elapsedSeconds: Long,
    val earnedCredits: Int,
)
