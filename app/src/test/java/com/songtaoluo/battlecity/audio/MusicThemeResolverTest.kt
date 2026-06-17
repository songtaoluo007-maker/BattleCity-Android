package com.songtaoluo.battlecity.audio

import com.songtaoluo.battlecity.game.ScenarioCatalog
import com.songtaoluo.battlecity.model.Faction
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicThemeResolverTest {
    @Test
    fun menuThemesMatchEveryFaction() {
        assertEquals(MusicTheme.PROUD_MENU, MusicThemeResolver.menuFor(null))
        assertEquals(MusicTheme.GERMAN_MENU, MusicThemeResolver.menuFor(Faction.GERMAN))
        assertEquals(MusicTheme.SOVIET_MENU, MusicThemeResolver.menuFor(Faction.SOVIET))
        assertEquals(MusicTheme.BRITISH_MENU, MusicThemeResolver.menuFor(Faction.BRITISH))
        assertEquals(MusicTheme.AMERICAN_MENU, MusicThemeResolver.menuFor(Faction.AMERICAN))
    }

    @Test
    fun battleThemesMatchScenarioFactionAndMoscowOverride() {
        assertEquals(
            MusicTheme.GERMAN_BATTLE,
            MusicThemeResolver.battleFor(ScenarioCatalog.kurskGermanBreakthrough),
        )
        assertEquals(
            MusicTheme.SOVIET_BATTLE,
            MusicThemeResolver.battleFor(ScenarioCatalog.kurskSovietDefense),
        )
        assertEquals(
            MusicTheme.SOVIET_BATTLE_MOSCOW,
            MusicThemeResolver.battleFor(
                ScenarioCatalog.kurskSovietDefense.copy(id = "moscow-1941-soviet-defense"),
            ),
        )
    }

    @Test
    fun resultThemesMatchOriginalFactionRules() {
        assertEquals(MusicTheme.GERMAN_VICTORY, MusicThemeResolver.resultFor(Faction.GERMAN, true))
        assertEquals(MusicTheme.GERMAN_DEFEAT, MusicThemeResolver.resultFor(Faction.GERMAN, false))
        assertEquals(MusicTheme.VICTORY, MusicThemeResolver.resultFor(Faction.SOVIET, true))
        assertEquals(MusicTheme.DEFEAT, MusicThemeResolver.resultFor(Faction.SOVIET, false))
        assertEquals(MusicTheme.ALLIED_VICTORY, MusicThemeResolver.resultFor(Faction.BRITISH, true))
        assertEquals(MusicTheme.ALLIED_VICTORY, MusicThemeResolver.resultFor(Faction.AMERICAN, true))
    }
}
