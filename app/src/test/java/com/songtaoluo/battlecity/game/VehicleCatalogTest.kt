package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VehicleCatalogTest {
    @Test
    fun catalogContainsAllHarmonyVehicles() {
        assertEquals(24, VehicleCatalog.all.size)
        assertEquals(6, VehicleCatalog.forFaction(Faction.GERMAN).size)
        assertEquals(6, VehicleCatalog.forFaction(Faction.SOVIET).size)
        assertEquals(6, VehicleCatalog.forFaction(Faction.BRITISH).size)
        assertEquals(6, VehicleCatalog.forFaction(Faction.AMERICAN).size)
    }

    @Test
    fun everyVehicleIdHasASpecification() {
        VehicleId.entries.forEach { vehicleId ->
            assertNotNull(VehicleCatalog.get(vehicleId))
        }
    }

    @Test
    fun starterVehiclesMatchHarmonyVersion() {
        assertEquals(VehicleId.PZ_IV_H, VehicleCatalog.defaultForFaction(Faction.GERMAN))
        assertEquals(VehicleId.T34_76, VehicleCatalog.defaultForFaction(Faction.SOVIET))
        assertEquals(VehicleId.CROMWELL, VehicleCatalog.defaultForFaction(Faction.BRITISH))
        assertEquals(VehicleId.SHERMAN_M4, VehicleCatalog.defaultForFaction(Faction.AMERICAN))
    }
}
