package com.songtaoluo.battlecity.progression

data class ScenarioRecord(
    val bestScore: Int = 0,
    val bestCredits: Int = 0,
    val victories: Int = 0,
    val attempts: Int = 0,
)

data class PlayerProgress(
    val totalCredits: Int = 0,
    val totalVictories: Int = 0,
    val totalBattles: Int = 0,
    val scenarioRecords: Map<String, ScenarioRecord> = emptyMap(),
) {
    fun recordFor(scenarioId: String): ScenarioRecord =
        scenarioRecords[scenarioId] ?: ScenarioRecord()
}

data class BattleSummary(
    val scenarioId: String,
    val victory: Boolean,
    val score: Int,
    val creditsEarned: Int,
)

object ProgressionSystem {
    fun applyBattle(
        progress: PlayerProgress,
        summary: BattleSummary,
    ): PlayerProgress {
        val previous = progress.recordFor(summary.scenarioId)
        val updatedRecord = previous.copy(
            bestScore = maxOf(previous.bestScore, summary.score),
            bestCredits = maxOf(previous.bestCredits, summary.creditsEarned),
            victories = previous.victories + if (summary.victory) 1 else 0,
            attempts = previous.attempts + 1,
        )
        return progress.copy(
            totalCredits = progress.totalCredits + summary.creditsEarned.coerceAtLeast(0),
            totalVictories = progress.totalVictories + if (summary.victory) 1 else 0,
            totalBattles = progress.totalBattles + 1,
            scenarioRecords = progress.scenarioRecords + (summary.scenarioId to updatedRecord),
        )
    }
}
