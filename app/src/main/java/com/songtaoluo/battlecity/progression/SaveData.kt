package com.songtaoluo.battlecity.progression

import java.nio.charset.StandardCharsets
import java.util.Base64

data class SaveData(
    val progress: PlayerProgress = PlayerProgress(),
    val settings: GameSettings = GameSettings(),
)

interface SaveRepository {
    fun load(): SaveData
    fun save(data: SaveData)
    fun clear()
}

object SaveCodec {
    private const val VERSION = 1

    fun encode(data: SaveData): String = buildString {
        val settings = data.settings.normalized()
        appendLine("version=$VERSION")
        appendLine("credits=${data.progress.totalCredits}")
        appendLine("victories=${data.progress.totalVictories}")
        appendLine("battles=${data.progress.totalBattles}")
        appendLine("musicEnabled=${settings.musicEnabled}")
        appendLine("effectsEnabled=${settings.effectsEnabled}")
        appendLine("musicVolume=${settings.musicVolume}")
        appendLine("effectsVolume=${settings.effectsVolume}")
        appendLine("vibrationEnabled=${settings.vibrationEnabled}")
        data.progress.scenarioRecords.toSortedMap().forEach { (scenarioId, record) ->
            append("record=")
            append(encodeId(scenarioId))
            append('|').append(record.bestScore)
            append('|').append(record.bestCredits)
            append('|').append(record.victories)
            append('|').append(record.attempts)
            appendLine()
        }
    }

    fun decode(raw: String?): SaveData {
        if (raw.isNullOrBlank()) return SaveData()
        val values = mutableMapOf<String, String>()
        val records = mutableMapOf<String, ScenarioRecord>()

        raw.lineSequence().forEach { line ->
            when {
                line.startsWith("record=") -> parseRecord(line.removePrefix("record="))
                    ?.let { (id, record) -> records[id] = record }
                '=' in line -> {
                    val index = line.indexOf('=')
                    values[line.substring(0, index)] = line.substring(index + 1)
                }
            }
        }

        val version = values["version"]?.toIntOrNull() ?: 0
        if (version !in 0..VERSION) return SaveData()

        return SaveData(
            progress = PlayerProgress(
                totalCredits = values["credits"].nonNegativeInt(),
                totalVictories = values["victories"].nonNegativeInt(),
                totalBattles = values["battles"].nonNegativeInt(),
                scenarioRecords = records,
            ),
            settings = GameSettings(
                musicEnabled = values["musicEnabled"].toBooleanOrDefault(true),
                effectsEnabled = values["effectsEnabled"].toBooleanOrDefault(true),
                musicVolume = values["musicVolume"].toFloatOrNull() ?: 0.72f,
                effectsVolume = values["effectsVolume"].toFloatOrNull() ?: 0.78f,
                vibrationEnabled = values["vibrationEnabled"].toBooleanOrDefault(true),
            ).normalized(),
        )
    }

    private fun parseRecord(raw: String): Pair<String, ScenarioRecord>? {
        val parts = raw.split('|')
        if (parts.size != 5) return null
        val id = runCatching { decodeId(parts[0]) }.getOrNull() ?: return null
        return id to ScenarioRecord(
            bestScore = parts[1].toIntOrNull()?.coerceAtLeast(0) ?: 0,
            bestCredits = parts[2].toIntOrNull()?.coerceAtLeast(0) ?: 0,
            victories = parts[3].toIntOrNull()?.coerceAtLeast(0) ?: 0,
            attempts = parts[4].toIntOrNull()?.coerceAtLeast(0) ?: 0,
        )
    }

    private fun encodeId(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeId(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8,
    )

    private fun String?.nonNegativeInt(): Int =
        this?.toIntOrNull()?.coerceAtLeast(0) ?: 0

    private fun String?.toBooleanOrDefault(default: Boolean): Boolean = when (this) {
        "true" -> true
        "false" -> false
        else -> default
    }
}
