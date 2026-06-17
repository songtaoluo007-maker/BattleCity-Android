package com.songtaoluo.battlecity.ui.art

import com.songtaoluo.battlecity.game.AchievementCatalog
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OriginalArtCatalogTest {
    @Test
    fun everyVehicleHasOriginalArtwork() {
        assertEquals(VehicleId.entries.size, OriginalArtCatalog.vehicles.size)
        VehicleId.entries.forEach { assertNotNull(OriginalArtCatalog.vehicle(it)) }
    }

    @Test
    fun everyCampaignAndAchievementStateHasOriginalArtwork() {
        assertEquals(6, OriginalArtCatalog.campaigns.size)
        assertEquals(AchievementCatalog.all.size * 2, OriginalArtCatalog.medals.size)
        AchievementCatalog.all.forEach {
            assertNotNull(OriginalArtCatalog.medal(it.id, unlocked = true))
            assertNotNull(OriginalArtCatalog.medal(it.id, unlocked = false))
        }
    }

    @Test
    fun resultArtworkCoversVictoryAndDefeat() {
        assertNotNull(OriginalArtCatalog.result(true))
        assertNotNull(OriginalArtCatalog.result(false))
    }
}
