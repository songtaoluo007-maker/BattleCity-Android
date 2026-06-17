package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioCatalogTest {
    @Test
    fun kurskMapsAreValidSeventeenBySeventeenBoards() {
        ScenarioCatalog.all.forEach { scenario ->
            val tiles = TileMapParser.parse(scenario.map)
            assertEquals(17, tiles.size)
            assertTrue(tiles.all { row -> row.size == 17 })
        }
    }

    @Test
    fun symbolsMapToExpectedTileTypes() {
        assertEquals(TileType.EMPTY, TileMapParser.fromSymbol('.'))
        assertEquals(TileType.BRICK, TileMapParser.fromSymbol('B'))
        assertEquals(TileType.STEEL, TileMapParser.fromSymbol('S'))
        assertEquals(TileType.WATER, TileMapParser.fromSymbol('W'))
        assertEquals(TileType.FOREST, TileMapParser.fromSymbol('F'))
        assertEquals(TileType.BASE, TileMapParser.fromSymbol('E'))
        assertEquals(TileType.OBJECTIVE, TileMapParser.fromSymbol('O'))
        assertEquals(TileType.VILLAGE, TileMapParser.fromSymbol('H'))
        assertEquals(TileType.MINE, TileMapParser.fromSymbol('M'))
    }

    @Test
    fun factionLookupReturnsCorrectKurskSide() {
        assertEquals(Faction.GERMAN, ScenarioCatalog.forFaction(Faction.GERMAN).faction)
        assertEquals(Faction.SOVIET, ScenarioCatalog.forFaction(Faction.SOVIET).faction)
    }

    @Test
    fun campaignsKeepHistoricalProgressionOrder() {
        assertEquals(6, CampaignCatalog.all.size)
        assertEquals("moscow-1941", CampaignCatalog.all.first().id)
        assertEquals("berlin-1945", CampaignCatalog.all.last().id)
        assertEquals("stalingrad-1942", CampaignCatalog.nextAfter("moscow-1941")?.id)
    }
}
