package com.songtaoluo.battlecity.ui

import com.songtaoluo.battlecity.game.CampaignCatalog
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OriginalVisualAssetCatalogTest {
    @Test
    fun everyVehicleHasUniqueOriginalTankStem() {
        val stems = VehicleId.entries.map(OriginalVisualAssetCatalog::tankStem)

        assertEquals(24, stems.size)
        assertEquals(stems.size, stems.toSet().size)
        assertTrue(stems.all { it.startsWith("tank_") })
    }

    @Test
    fun everyCampaignArchiveEntryHasBackgroundStem() {
        val stems = CampaignCatalog.all.map { campaign ->
            OriginalVisualAssetCatalog.campaignStem(campaign.id)
        }

        assertEquals(6, stems.size)
        assertTrue(stems.all { it != null })
        assertEquals(6, stems.filterNotNull().toSet().size)
    }

    @Test
    fun everyFactionHasFlagAndBothResultsHaveArtwork() {
        Faction.entries.forEach { faction ->
            assertTrue(OriginalVisualAssetCatalog.factionFlagStem(faction).startsWith("flag_"))
        }
        assertEquals("result_victory_archive", OriginalVisualAssetCatalog.resultStem(true))
        assertEquals("result_defeat_archive", OriginalVisualAssetCatalog.resultStem(false))
        assertNotNull(OriginalVisualAssetCatalog.medalStems.singleOrNull { it == "medal_kursk" })
    }
}
