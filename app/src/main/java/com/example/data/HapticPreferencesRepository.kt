package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.HapticFeel
import com.example.model.HapticPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hapticsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "formgram_haptics_prefs"
)

class HapticPreferencesRepository(private val context: Context) {

    private object Keys {
        val HAPTIC_FEEL = stringPreferencesKey("haptic_feel")
        val SNAP_TO_POST = booleanPreferencesKey("snap_to_post")
        val MICRO_SCROLL_TICKS = booleanPreferencesKey("micro_scroll_ticks")
    }

    val preferencesFlow: Flow<HapticPreferences> = context.hapticsDataStore.data.map { prefs ->
        val feelName = prefs[Keys.HAPTIC_FEEL]
        val feel = HapticFeel.entries.firstOrNull { it.name == feelName } ?: HapticFeel.CRISP_DETENT
        HapticPreferences(
            feel = feel,
            snapToPost = prefs[Keys.SNAP_TO_POST] ?: false,
            microScrollTicks = prefs[Keys.MICRO_SCROLL_TICKS] ?: true
        )
    }

    suspend fun setHapticFeel(feel: HapticFeel) {
        context.hapticsDataStore.edit { it[Keys.HAPTIC_FEEL] = feel.name }
    }

    suspend fun setSnapToPost(enabled: Boolean) {
        context.hapticsDataStore.edit { it[Keys.SNAP_TO_POST] = enabled }
    }

    suspend fun setMicroScrollTicks(enabled: Boolean) {
        context.hapticsDataStore.edit { it[Keys.MICRO_SCROLL_TICKS] = enabled }
    }
}
