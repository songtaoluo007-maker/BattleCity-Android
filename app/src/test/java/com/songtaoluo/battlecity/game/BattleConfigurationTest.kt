package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BattleConfigurationTest {
    @Test
    fun selectedSovietScenarioAndVehicleConfigureEntireBattle() {
        val engine = GameEngine(
            random = Random(21),
            scenario = ScenarioCatalog.kurskSovietDefense,
            selectedVehicleId = VehicleId.T70,
        )

        assertEquals(ScenarioCatalog.kurskSovietDefense.id, engine.scenario.id)
        assertEquals(VehicleId.T70, engine.player.vehicleId)
        assertEquals(Faction.SOVIET, engine.player.faction)
        assertTrue(engine.enemies.all { it.faction == Faction.GERMAN })
    }

    @Test
    fun vehicleOutsideScenarioFallsBackToScenarioDefault() {
        val engine = GameEngine(
            random = Random(22),
            scenario = ScenarioCatalog.kurskSovietDefense,
            selectedVehicleId = VehicleId.TIGER_I,
        )

        assertEquals(ScenarioCatalog.kurskSovietDefense.allyVehicles.first(), engine.player.vehicleId)
    }

    @Test
    fun migratedContentCatalogExposesOnlyRealScenarioContent() {
        assertTrue(MigratedContentCatalog.isCampaignPlayable("kursk-1943"))
        assertEquals(2, MigratedContentCatalog.scenariosForCampaign("kursk-1943").size)
        assertFalse(MigratedContentCatalog.isCampaignPlayable("moscow-1941"))
        assertTrue(MigratedContentCatalog.scenariosForCampaign("moscow-1941").isEmpty())
    }
}
