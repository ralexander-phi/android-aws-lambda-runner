package com.alexsci.android.lambdarunner.util.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_ACCESS_KEY_ID
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_FILE_SELECTED
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_FUNCTION_NAME
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_REGION
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.ui.list_keys.ListKeysActivity

class PreferencesUtil(private val context: Context) {
    private val preferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE_SELECTED, MODE_PRIVATE)
    private val listKeysIntent = Intent(context, ListKeysActivity::class.java)
    private val listFunctionsIntent = Intent(context, ListFunctionsActivity::class.java)

    private val prefToActivityMap = mapOf(
        SHARED_PREFERENCE_ACCESS_KEY_ID to listKeysIntent,
        SHARED_PREFERENCE_FUNCTION_NAME to listFunctionsIntent,
        SHARED_PREFERENCE_REGION to listFunctionsIntent
    )

    /**
     * Load a preferences from shared preferences
     *
     * If default is null - Launch the associated activity to select the preference
     * If default is non-null - Set the default, and return it
     */
    fun get(name: String, default: String? = null): String {
        return when {
            preferences.contains(name) -> preferences.getString(name, null)!!

            default != null -> {
                // Also save the default
                set(name, default)
                default
            }

            else -> {
                context.startActivity(prefToActivityMap[name])
                ""
            }
        }
    }

    fun set(name: String, value: String) {
        preferences.edit().putString(name, value).apply()
    }
}