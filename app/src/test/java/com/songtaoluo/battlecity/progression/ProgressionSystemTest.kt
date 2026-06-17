package com.songtaoluo.battlecity.progression

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionSystemTest {
    @Test
    fun battleSettlementAccumulatesTotalsAndBestValues() {
        var progress = PlayerProgress()
        progress = ProgressionSystem.applyBattle(
            progress,
            BattleSummary("kursk", victory = true, score = 1200, creditsEarned = 180),
        )
        progress = ProgressionSystem.applyBattle(
            progress,
            BattleSummary("kursk", victory = false, score = 900, creditsEarned = 60),
        )
        progress = ProgressionSystem.applyBattle(
            progress,
            BattleSummary("kursk", victory = true, score = 1500, creditsEarned = 140),
        )

        val record = progress.recordFor("kursk")
        assertEquals(3, progress.totalBattles)
        assertEquals(2, progress.totalVictories)
        assertEquals(380, progress.totalCredits)
        assertEquals(3, record.attempts)
        assertEquals(2, record.victories)
        assertEquals(1500, record.bestScore)
        assertEquals(180, record.bestCredits)
    }

    @Test
    fun negativeCreditsNeverReduceLifetimeWallet() {
        val progress = ProgressionSystem.applyBattle(
            PlayerProgress(totalCredits = 100),
            BattleSummary("test", victory = false, score = 0, creditsEarned = -50),
        )

        assertEquals(100, progress.totalCredits)
        assertEquals(1, progress.totalBattles)
    }
}
