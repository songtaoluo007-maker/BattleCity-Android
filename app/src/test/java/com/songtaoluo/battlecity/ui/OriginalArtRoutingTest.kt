package com.songtaoluo.battlecity.ui

import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OriginalArtRoutingTest {
    @Test
    fun everyVehicleMapsToAnOriginalTankStem() {
        val stems = VehicleId.entries.map(OriginalArt::vehicleStem)

        assertEquals(24, stems.size)
        assertEquals(24, stems.toSet().size)
        assertTrue(stems.all { it.startsWith("tank_") })
    }

    @Test
    fun allCampaignIdsMapToHistoricalCovers() {
        val ids = listOf(
            "moscow-1941",
            "stalingrad-1942",
            "el-alamein-1942",
            "kursk-1943",
            "normandy-1944",
            "berlin-1945",
        )

        val stems = ids.map(OriginalArt::campaignStem)
        assertEquals(6, stems.toSet().size)
        assertTrue(stems.all { it.startsWith("campaign_") })
    }

    @Test
    fun medalAndResultStemsMatchOriginalNaming() {
        assertEquals("medal_first_blood", OriginalArt.achievementStem("first_blood", true))
        assertEquals("medal_first_blood_locked", OriginalArt.achievementStem("first_blood", false))
        assertEquals("result_victory_archive", OriginalArt.resultStem(true))
        assertEquals("result_defeat_archive", OriginalArt.resultStem(false))
    }
}
