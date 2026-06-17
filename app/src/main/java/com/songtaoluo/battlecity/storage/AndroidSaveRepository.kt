package com.songtaoluo.battlecity.storage

import android.content.Context
import android.content.SharedPreferences
import com.songtaoluo.battlecity.game.AchievementCatalog
import com.songtaoluo.battlecity.game.ProgressionSystem
import com.songtaoluo.battlecity.model.AchievementState
import com.songtaoluo.battlecity.model.AudioSettings
import com.songtaoluo.battlecity.model.PlayerProfile
import com.songtaoluo.battlecity.model.SaveData
import com.songtaoluo.battlecity.model.VehicleId

class AndroidSaveRepository(context: Context) {
    private val preferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): SaveData {
        val defaults = SaveData(achievements = AchievementCatalog.defaultStates())
        val ownedVehicles = preferences.getStringSet(KEY_OWNED_VEHICLES, null)
            ?.mapNotNull { value -> VehicleId.fromWireValue(value) }
            ?.toSet()
            .orEmpty()
            .ifEmpty { defaults.ownedVehicles }
        val achievements = decodeAchievements(
            preferences.getStringSet(KEY_ACHIEVEMENTS, emptySet()).orEmpty(),
        )
        val callsign = preferences.getString(KEY_CALLSIGN, null)
            ?.trim()
            .orEmpty()
            .ifBlank { preferences.getString(KEY_PLAYER_NAME, defaults.playerName).orEmpty() }
            .ifBlank { defaults.playerProfile.callsign }
        val playerName = preferences.getString(KEY_PLAYER_NAME, defaults.playerName)
            ?.trim()
            .orEmpty()
            .ifBlank { callsign }
        val bestScore = preferences.getInt(KEY_BEST_SCORE, 0).coerceAtLeast(0)
        val totalKills = preferences.getInt(KEY_TOTAL_KILLS, 0).coerceAtLeast(0)
        val profile = PlayerProfile(
            callsign = callsign,
            avatarId = preferences.getString(KEY_AVATAR_ID, defaults.playerProfile.avatarId)
                ?.ifBlank { defaults.playerProfile.avatarId }
                ?: defaults.playerProfile.avatarId,
            title = preferences.getString(KEY_TITLE, defaults.playerProfile.title)
                ?.ifBlank { defaults.playerProfile.title }
                ?: defaults.playerProfile.title,
            totalKills = maxOf(
                totalKills,
                preferences.getInt(KEY_PROFILE_TOTAL_KILLS, 0).coerceAtLeast(0),
            ),
            bestScore = maxOf(
                bestScore,
                preferences.getInt(KEY_PROFILE_BEST_SCORE, 0).coerceAtLeast(0),
            ),
            bestOneMatchKills = preferences.getInt(KEY_BEST_MATCH_KILLS, 0).coerceAtLeast(0),
        )
        val audio = AudioSettings(
            musicVolume = preferences.getFloat(KEY_MUSIC_VOLUME, defaults.audioSettings.musicVolume),
            voiceVolume = preferences.getFloat(KEY_VOICE_VOLUME, defaults.audioSettings.voiceVolume),
            combatVolume = preferences.getFloat(KEY_COMBAT_VOLUME, defaults.audioSettings.combatVolume),
            soundEnabled = preferences.getBoolean(
                KEY_SOUND_ENABLED,
                preferences.getBoolean(KEY_LEGACY_SOUND_ENABLED, true),
            ),
        ).normalized()

        return ProgressionSystem.normalize(
            SaveData(
                playerName = playerName,
                bestScore = bestScore,
                credits = preferences.getInt(KEY_CREDITS, defaults.credits).coerceAtLeast(0),
                ownedVehicles = ownedVehicles,
                completedScenarios = preferences.getStringSet(KEY_COMPLETED_SCENARIOS, emptySet())
                    .orEmpty()
                    .filter { it.isNotBlank() }
                    .toSet(),
                completedCampaigns = preferences.getStringSet(KEY_COMPLETED_CAMPAIGNS, emptySet())
                    .orEmpty()
                    .filter { it.isNotBlank() }
                    .toSet(),
                totalKills = totalKills,
                totalPlayTimeSec = preferences.getLong(KEY_TOTAL_PLAY_TIME, 0L).coerceAtLeast(0L),
                audioSettings = audio,
                achievements = if (achievements.isEmpty()) defaults.achievements else achievements,
                playerProfile = profile,
            ),
        )
    }

    fun save(data: SaveData) {
        val normalized = ProgressionSystem.normalize(data)
        preferences.edit()
            .putInt(KEY_SAVE_VERSION, CURRENT_SAVE_VERSION)
            .putString(KEY_PLAYER_NAME, normalized.playerName)
            .putInt(KEY_BEST_SCORE, normalized.bestScore)
            .putInt(KEY_CREDITS, normalized.credits)
            .putStringSet(
                KEY_OWNED_VEHICLES,
                normalized.ownedVehicles.mapTo(mutableSetOf()) { it.wireValue },
            )
            .putStringSet(KEY_COMPLETED_SCENARIOS, normalized.completedScenarios.toMutableSet())
            .putStringSet(KEY_COMPLETED_CAMPAIGNS, normalized.completedCampaigns.toMutableSet())
            .putInt(KEY_TOTAL_KILLS, normalized.totalKills)
            .putLong(KEY_TOTAL_PLAY_TIME, normalized.totalPlayTimeSec)
            .putBoolean(KEY_SOUND_ENABLED, normalized.audioSettings.soundEnabled)
            .putBoolean(KEY_LEGACY_SOUND_ENABLED, normalized.audioSettings.soundEnabled)
            .putFloat(KEY_MUSIC_VOLUME, normalized.audioSettings.musicVolume)
            .putFloat(KEY_VOICE_VOLUME, normalized.audioSettings.voiceVolume)
            .putFloat(KEY_COMBAT_VOLUME, normalized.audioSettings.combatVolume)
            .putStringSet(KEY_ACHIEVEMENTS, encodeAchievements(normalized.achievements))
            .putString(KEY_CALLSIGN, normalized.playerProfile.callsign)
            .putString(KEY_AVATAR_ID, normalized.playerProfile.avatarId)
            .putString(KEY_TITLE, normalized.playerProfile.title)
            .putInt(KEY_PROFILE_TOTAL_KILLS, normalized.playerProfile.totalKills)
            .putInt(KEY_PROFILE_BEST_SCORE, normalized.playerProfile.bestScore)
            .putInt(KEY_BEST_MATCH_KILLS, normalized.playerProfile.bestOneMatchKills)
            .apply()
    }

    fun reset(): SaveData {
        val defaults = ProgressionSystem.normalize(
            SaveData(achievements = AchievementCatalog.defaultStates()),
        )
        preferences.edit().clear().commit()
        save(defaults)
        return defaults
    }

    private fun encodeAchievements(states: List<AchievementState>): MutableSet<String> =
        AchievementCatalog.normalize(states).mapTo(mutableSetOf()) { state ->
            listOf(
                state.id,
                if (state.unlocked) "1" else "0",
                state.unlockedAtEpochMs.coerceAtLeast(0L).toString(),
            ).joinToString(ACHIEVEMENT_SEPARATOR)
        }

    private fun decodeAchievements(raw: Set<String>): List<AchievementState> = raw.mapNotNull { encoded ->
        val parts = encoded.split(ACHIEVEMENT_SEPARATOR)
        val id = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        AchievementState(
            id = id,
            unlocked = parts.getOrNull(1) == "1",
            unlockedAtEpochMs = parts.getOrNull(2)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L,
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "battle_city_save"
        private const val CURRENT_SAVE_VERSION = 1
        private const val ACHIEVEMENT_SEPARATOR = "\u001F"

        private const val KEY_SAVE_VERSION = "saveVersion"
        private const val KEY_PLAYER_NAME = "playerName"
        private const val KEY_BEST_SCORE = "bestScore"
        private const val KEY_CREDITS = "credits"
        private const val KEY_OWNED_VEHICLES = "ownedVehicles"
        private const val KEY_COMPLETED_SCENARIOS = "completedScenarios"
        private const val KEY_COMPLETED_CAMPAIGNS = "completedCampaigns"
        private const val KEY_TOTAL_KILLS = "totalKills"
        private const val KEY_TOTAL_PLAY_TIME = "totalPlayTimeSec"
        private const val KEY_SOUND_ENABLED = "audioSoundEnabled"
        private const val KEY_LEGACY_SOUND_ENABLED = "soundEnabled"
        private const val KEY_MUSIC_VOLUME = "musicVolume"
        private const val KEY_VOICE_VOLUME = "voiceVolume"
        private const val KEY_COMBAT_VOLUME = "combatVolume"
        private const val KEY_ACHIEVEMENTS = "achievementsV1"
        private const val KEY_CALLSIGN = "profileCallsign"
        private const val KEY_AVATAR_ID = "profileAvatarId"
        private const val KEY_TITLE = "profileTitle"
        private const val KEY_PROFILE_TOTAL_KILLS = "profileTotalKills"
        private const val KEY_PROFILE_BEST_SCORE = "profileBestScore"
        private const val KEY_BEST_MATCH_KILLS = "profileBestOneMatchKills"
    }
}
