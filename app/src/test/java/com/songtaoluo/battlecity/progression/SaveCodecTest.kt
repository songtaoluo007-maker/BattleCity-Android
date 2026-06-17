package com.songtaoluo.battlecity.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCodecTest {
    @Test
    fun roundTripPreservesSettingsAndScenarioRecords() {
        val original = SaveData(
            progress = PlayerProgress(
                totalCredits = 420,
                totalVictories = 2,
                totalBattles = 3,
                scenarioRecords = mapOf(
                    "kursk-1943|german" to ScenarioRecord(
                        bestScore = 1900,
                        bestCredits = 280,
                        victories = 2,
                        attempts = 3,
                    ),
                ),
            ),
            settings = GameSettings(
                musicEnabled = false,
                effectsEnabled = true,
                musicVolume = 0.35f,
                effectsVolume = 0.91f,
                vibrationEnabled = false,
            ),
        )

        val decoded = SaveCodec.decode(SaveCodec.encode(original))

        assertEquals(original.progress, decoded.progress)
        assertEquals(original.settings, decoded.settings)
    }

    @Test
    fun corruptedValuesFallBackSafely() {
        val decoded = SaveCodec.decode(
            """
            version=1
            credits=-4
            battles=oops
            musicEnabled=maybe
            musicVolume=9.0
            effectsVolume=-3.0
            record=broken
            """.trimIndent(),
        )

        assertEquals(0, decoded.progress.totalCredits)
        assertEquals(0, decoded.progress.totalBattles)
        assertTrue(decoded.settings.musicEnabled)
        assertEquals(1f, decoded.settings.musicVolume, 0.001f)
        assertEquals(0f, decoded.settings.effectsVolume, 0.001f)
    }

    @Test
    fun futureSaveVersionsDoNotCrashOlderBuilds() {
        val decoded = SaveCodec.decode("version=999\ncredits=500")

        assertEquals(SaveData(), decoded)
        assertFalse(decoded.progress.totalCredits > 0)
    }
}
