package com.songtaoluo.battlecity.audio

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.ScenarioData

enum class MusicTheme(val resourceStem: String) {
    PROUD_MENU("one_minute_of_proud"),
    NEUTRAL_MENU("bgm"),
    GERMAN_MENU("german_theme"),
    GERMAN_BATTLE("german_battle"),
    SOVIET_MENU("soviet_theme"),
    SOVIET_BATTLE("soviet_battle"),
    SOVIET_BATTLE_MOSCOW("soviet_battle_moscow"),
    BRITISH_MENU("british_menu"),
    BRITISH_BATTLE("british_battle"),
    AMERICAN_MENU("american_menu"),
    AMERICAN_BATTLE("american_battle"),
    GERMAN_VICTORY("victory_german"),
    GERMAN_DEFEAT("defeat_german"),
    ALLIED_VICTORY("victory_allied"),
    VICTORY("victory"),
    DEFEAT("defeat"),
}

object MusicThemeResolver {
    fun menuFor(faction: Faction?): MusicTheme = when (faction) {
        Faction.GERMAN -> MusicTheme.GERMAN_MENU
        Faction.SOVIET -> MusicTheme.SOVIET_MENU
        Faction.BRITISH -> MusicTheme.BRITISH_MENU
        Faction.AMERICAN -> MusicTheme.AMERICAN_MENU
        null -> MusicTheme.PROUD_MENU
    }

    fun battleFor(scenario: ScenarioData): MusicTheme = when (scenario.faction) {
        Faction.GERMAN -> MusicTheme.GERMAN_BATTLE
        Faction.SOVIET -> if (scenario.id.contains("moscow", ignoreCase = true)) {
            MusicTheme.SOVIET_BATTLE_MOSCOW
        } else {
            MusicTheme.SOVIET_BATTLE
        }
        Faction.BRITISH -> MusicTheme.BRITISH_BATTLE
        Faction.AMERICAN -> MusicTheme.AMERICAN_BATTLE
    }

    fun resultFor(faction: Faction, victory: Boolean): MusicTheme = when {
        faction == Faction.GERMAN && victory -> MusicTheme.GERMAN_VICTORY
        faction == Faction.GERMAN && !victory -> MusicTheme.GERMAN_DEFEAT
        faction in setOf(Faction.BRITISH, Faction.AMERICAN) && victory -> MusicTheme.ALLIED_VICTORY
        victory -> MusicTheme.VICTORY
        else -> MusicTheme.DEFEAT
    }
}
