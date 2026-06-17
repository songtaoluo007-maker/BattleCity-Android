package com.songtaoluo.battlecity.progression

import android.content.Context

class SharedPreferencesSaveRepository(
    context: Context,
) : SaveRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE,
    )

    override fun load(): SaveData = SaveCodec.decode(
        preferences.getString(KEY_SAVE_DATA, null),
    )

    override fun save(data: SaveData) {
        preferences.edit()
            .putString(KEY_SAVE_DATA, SaveCodec.encode(data))
            .apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY_SAVE_DATA).apply()
    }

    private companion object {
        const val FILE_NAME = "battle_city_save"
        const val KEY_SAVE_DATA = "save_data"
    }
}
