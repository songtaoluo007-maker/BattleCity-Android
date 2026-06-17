package com.songtaoluo.battlecity.ui.art

import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OriginalArtNamesTest {
    @Test
    fun everyVehicleHasUniqueFullResolutionResourceName() {
        val names = VehicleId.entries.map(OriginalArtNames::vehicle)

        assertEquals(24, names.size)
        assertEquals(24, names.toSet().size)
        assertTrue(names.all { it.startsWith("tank_") })
    }

    @Test
    fun allCampaignsMapToDistinctHistoricalCovers() {
        val ids = listOf(
            "moscow-1941",
            "stalingrad-1942",
            "el-alamein-1942",
            "kursk-1943",
            "normandy-1944",
            "berlin-1945",
        )

        val names = ids.map(OriginalArtNames::campaign)
        assertEquals(6, names.toSet().size)
        assertTrue(names.all { it.startsWith("campaign_") })
    }

    @Test
    fun medalAndResultResourceNamesMatchPrivateAssetManifest() {
        assertEquals("medal_first_blood", OriginalArtNames.medal("first_blood", true))
        assertEquals("medal_first_blood_locked", OriginalArtNames.medal("first_blood", false))
        assertEquals("result_victory_archive", OriginalArtNames.result(true))
        assertEquals("result_defeat_archive", OriginalArtNames.result(false))
    }
}
