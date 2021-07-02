package com.example.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit


private const val PREFERENCE_SEARCH_QUERY = "search_query"
private const val PREFERENCE_LAST_RESULT_ID = "last_result_id"
private const val PREFERENCE_IS_POLLING = "is_polling"

// TODO: replace with DataStore
object QueryPreferences {
    fun getStoredQuery(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(PREFERENCE_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREFERENCE_SEARCH_QUERY, query)
            }
    }

    fun getLastResultId(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(PREFERENCE_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREFERENCE_LAST_RESULT_ID, lastResultId)
            }
    }

    fun isPolling(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(PREFERENCE_IS_POLLING, false)
    }

    fun setPolling(context: Context, isPolling: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(PREFERENCE_IS_POLLING, isPolling)
            }
    }
}