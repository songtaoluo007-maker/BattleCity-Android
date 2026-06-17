package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.BattleSummary
import com.songtaoluo.battlecity.model.MedalTier
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionSystemTest {
    @Test
    fun catalogContainsAllTwentyOriginalAchievements() {
        assertEquals(20, AchievementCatalog.all.size)
        assertEquals(5, AchievementCatalog.all.count { it.nation.wireValue == "german" })
        assertEquals(5, AchievementCatalog.all.count { it.nation.wireValue == "soviet" })
        assertEquals(5, AchievementCatalog.all.count { it.nation.wireValue == "british" })
        assertEquals(5, AchievementCatalog.all.count { it.nation.wireValue == "american" })
    }

    @Test
    fun victorySettlementUpdatesCareerAndUnlocksMatchAchievements() {
        val initial = SaveData(
            credits = 1600,
            achievements = AchievementCatalog.defaultStates(),
        )
        val settled = ProgressionSystem.settleBattle(
            current = initial,
            summary = BattleSummary(
                scenarioId = "kursk-1943-german-breakthrough",
                campaignId = "kursk-1943",
                victory = true,
                score = 4200,
                destroyedEnemies = 8,
                elapsedSeconds = 130,
                earnedCredits = 420,
            ),
            nowEpochMs = 1234L,
        )

        assertEquals(2020, settled.credits)
        assertEquals(8, settled.totalKills)
        assertEquals(4200, settled.bestScore)
        assertEquals(8, settled.playerProfile.bestOneMatchKills)
        assertTrue("kursk-1943-german-breakthrough" in settled.completedScenarios)
        assertTrue("kursk-1943" in settled.completedCampaigns)
        assertTrue(settled.achievements.first { it.id == "ace_gold_8" }.unlocked)
        assertFalse(settled.achievements.first { it.id == "ace_platinum_12" }.unlocked)
        assertEquals(AchievementCatalog.ACE_TITLE, settled.playerProfile.title)
    }

    @Test
    fun defeatPreservesCompletionButStillRecordsCareerProgress() {
        val settled = ProgressionSystem.settleBattle(
            current = SaveData(achievements = AchievementCatalog.defaultStates()),
            summary = BattleSummary(
                scenarioId = "lost",
                campaignId = "kursk-1943",
                victory = false,
                score = 500,
                destroyedEnemies = 3,
                elapsedSeconds = 45,
                earnedCredits = 20,
            ),
            nowEpochMs = 10L,
        )

        assertTrue(settled.completedScenarios.isEmpty())
        assertTrue(settled.completedCampaigns.isEmpty())
        assertEquals(3, settled.totalKills)
        assertEquals(45L, settled.totalPlayTimeSec)
        assertTrue(settled.achievements.first { it.id == "ace_bronze_3" }.unlocked)
    }

    @Test
    fun careerAchievementsAndHighestTitleUseTotalKills() {
        val settled = ProgressionSystem.settleBattle(
            current = SaveData(
                totalKills = 95,
                achievements = AchievementCatalog.defaultStates(),
            ),
            summary = BattleSummary(
                scenarioId = "test",
                campaignId = "test",
                victory = false,
                score = 1,
                destroyedEnemies = 5,
                elapsedSeconds = 1,
                earnedCredits = 0,
            ),
            nowEpochMs = 20L,
        )

        assertTrue(settled.achievements.first { it.id == "career_hero" }.unlocked)
        assertTrue(settled.achievements.first { it.id == "british_victoria_cross" }.unlocked)
        assertTrue(settled.achievements.first { it.id == "american_medal_of_honor" }.unlocked)
        assertEquals(AchievementCatalog.HERO_TITLE, settled.playerProfile.title)
        assertTrue(AchievementCatalog.all.any { it.tier == MedalTier.PLATINUM })
    }

    @Test
    fun vehiclePurchaseChargesOnceAndAddsOwnership() {
        val vehicle = VehicleCatalog.get(VehicleId.TIGER_I)
        val initial = SaveData(
            credits = vehicle.price + 100,
            achievements = AchievementCatalog.defaultStates(),
        )

        val first = VehiclePurchaseSystem.purchase(initial, vehicle)
        assertTrue(first is PurchaseResult.Success)
        val purchased = (first as PurchaseResult.Success).save
        assertEquals(100, purchased.credits)
        assertTrue(vehicle.id in purchased.ownedVehicles)

        val second = VehiclePurchaseSystem.purchase(purchased, vehicle)
        assertTrue(second is PurchaseResult.AlreadyOwned)
        assertEquals(100, purchased.credits)
    }

    @Test
    fun insufficientCreditsNeverGoNegative() {
        val vehicle = VehicleCatalog.get(VehicleId.TIGER_II)
        val result = VehiclePurchaseSystem.purchase(
            SaveData(credits = 0, achievements = AchievementCatalog.defaultStates()),
            vehicle,
        )

        assertTrue(result is PurchaseResult.InsufficientCredits)
    }
}
