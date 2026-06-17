package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.BattleSummary
import com.songtaoluo.battlecity.model.PlayerProfile
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.model.VehicleSpec

object ProgressionSystem {
    fun normalize(save: SaveData): SaveData {
        val achievements = AchievementCatalog.normalize(save.achievements)
        val profile = save.playerProfile.copy(
            callsign = save.playerProfile.callsign.ifBlank { save.playerName.ifBlank { "指挥官" } },
            title = AchievementCatalog.resolveTitle(achievements),
            totalKills = maxOf(save.playerProfile.totalKills, save.totalKills).coerceAtLeast(0),
            bestScore = maxOf(save.playerProfile.bestScore, save.bestScore).coerceAtLeast(0),
            bestOneMatchKills = save.playerProfile.bestOneMatchKills.coerceAtLeast(0),
        )
        return save.copy(
            playerName = save.playerName.ifBlank { "PLAYER" },
            bestScore = profile.bestScore,
            credits = save.credits.coerceAtLeast(0),
            ownedVehicles = save.ownedVehicles.ifEmpty {
                SaveData().ownedVehicles
            },
            totalKills = profile.totalKills,
            totalPlayTimeSec = save.totalPlayTimeSec.coerceAtLeast(0L),
            audioSettings = save.audioSettings.normalized(),
            achievements = achievements,
            playerProfile = profile,
        )
    }

    fun settleBattle(
        current: SaveData,
        summary: BattleSummary,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): SaveData {
        val before = normalize(current)
        val totalKills = before.totalKills + summary.destroyedEnemies.coerceAtLeast(0)
        val bestScore = maxOf(before.bestScore, summary.score.coerceAtLeast(0))
        val bestMatchKills = maxOf(
            before.playerProfile.bestOneMatchKills,
            summary.destroyedEnemies.coerceAtLeast(0),
        )
        val achievements = AchievementCatalog.mergeProgress(
            existing = before.achievements,
            oneMatchKills = summary.destroyedEnemies.coerceAtLeast(0),
            totalKills = totalKills,
            unlockedAtEpochMs = nowEpochMs,
        )
        val completedScenarios = if (summary.victory) {
            before.completedScenarios + summary.scenarioId
        } else {
            before.completedScenarios
        }
        val profile = PlayerProfile(
            callsign = before.playerProfile.callsign,
            avatarId = before.playerProfile.avatarId,
            title = AchievementCatalog.resolveTitle(achievements),
            totalKills = totalKills,
            bestScore = bestScore,
            bestOneMatchKills = bestMatchKills,
        )
        return before.copy(
            bestScore = bestScore,
            credits = before.credits + summary.earnedCredits.coerceAtLeast(0),
            completedScenarios = completedScenarios,
            totalKills = totalKills,
            totalPlayTimeSec = before.totalPlayTimeSec + summary.elapsedSeconds.coerceAtLeast(0L),
            achievements = achievements,
            playerProfile = profile,
        )
    }

    fun markCampaignCompleted(current: SaveData, campaignId: String): SaveData {
        if (campaignId.isBlank()) return normalize(current)
        val save = normalize(current)
        return save.copy(completedCampaigns = save.completedCampaigns + campaignId)
    }

    fun updateProfile(
        current: SaveData,
        callsign: String,
        avatarId: String = current.playerProfile.avatarId,
    ): SaveData {
        val normalized = normalize(current)
        val safeCallsign = callsign.trim().take(20).ifBlank { "指挥官" }
        return normalized.copy(
            playerName = safeCallsign,
            playerProfile = normalized.playerProfile.copy(
                callsign = safeCallsign,
                avatarId = avatarId.ifBlank { "commander-default" },
            ),
        )
    }
}

sealed interface PurchaseResult {
    data class Success(val save: SaveData) : PurchaseResult
    data object AlreadyOwned : PurchaseResult
    data object InsufficientCredits : PurchaseResult
}

object VehiclePurchaseSystem {
    fun purchase(current: SaveData, vehicle: VehicleSpec): PurchaseResult {
        val save = ProgressionSystem.normalize(current)
        if (vehicle.id in save.ownedVehicles) return PurchaseResult.AlreadyOwned
        if (save.credits < vehicle.price) return PurchaseResult.InsufficientCredits
        return PurchaseResult.Success(
            save.copy(
                credits = save.credits - vehicle.price,
                ownedVehicles = save.ownedVehicles + vehicle.id,
            ),
        )
    }
}
