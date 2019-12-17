package com.alexsci.android.lambdarunner.util.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_FILE_SELECTED

class PreferencesUtil(private val context: Context) {
    private val preferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE_SELECTED, MODE_PRIVATE)

    /**
     * Load a preferences from shared preferences
     *
     * If there is no value, set the provided default, and return it
     */
    fun get(name: String, default: String): String {
        return when {
            preferences.contains(name) -> get(name)!!

            else -> {
                set(name, default)
                default
            }
        }
    }

    fun get(name: String): String? {
        return preferences.getString(name, null)
    }

    /**
     * Load boolean preferences from shared preferences
     *
     * If default is null - Launch the associated activity to select the preference
     * If default is non-null - Set the default, and return it
     */
    fun getBoolean(name: String, default: Boolean): Boolean {
        return when {
            preferences.contains(name) -> preferences.getBoolean(name, false)

            else -> {
                setBoolean(name, default)
                default
            }
        }
    }

    fun set(name: String, value: String) {
        preferences.edit().putString(name, value).apply()
    }

    fun setBoolean(name: String, value: Boolean) {
        preferences.edit().putBoolean(name, value).apply()
    }
}