package com.appecho.alpha.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appecho.alpha.ui.theme.ProfileListItem.ApiSource
import com.appecho.alpha.ui.theme.manage.DownloadTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "api_settings")

class ApiDataManager(private val context: Context) {
    private val gson = Gson()
    private val API_LIST_KEY = stringPreferencesKey("api_source_list")
    private val PARSED_RESULTS_KEY = stringPreferencesKey("parsed_results_cache")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
    private val DOWNLOAD_TASKS_KEY = stringPreferencesKey("download_tasks")
    private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    // 保存列表到本地
    suspend fun saveApiList(apiList: List<ApiSource>) {
        val jsonString = gson.toJson(apiList)
        context.dataStore.edit { preferences ->
            preferences[API_LIST_KEY] = jsonString
        }
    }
    suspend fun saveParsedResults(results: List<ParsedResult>) {
        val jsonString = gson.toJson(results)
        context.dataStore.edit { it[PARSED_RESULTS_KEY] = jsonString }
    }

    suspend fun saveDownloadTasks(tasks: List<DownloadTask>) {
        val jsonString = gson.toJson(tasks)
        context.dataStore.edit { it[DOWNLOAD_TASKS_KEY] = jsonString }
    }

    suspend fun addDownloadTask(task: DownloadTask) {
        val current = downloadTasksFlow.first()
        saveDownloadTasks(listOf(task) + current)
    }
    // 从本地读取列表
    val apiListFlow: Flow<List<ApiSource>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[API_LIST_KEY] ?: ""
        if (jsonString.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<ApiSource>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }
    val parsedResultsFlow: Flow<List<ParsedResult>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[PARSED_RESULTS_KEY] ?: ""
        if (jsonString.isEmpty()) emptyList() else {
            val type = object : TypeToken<List<ParsedResult>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }

    val downloadTasksFlow: Flow<List<DownloadTask>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[DOWNLOAD_TASKS_KEY] ?: ""
        if (jsonString.isEmpty()) emptyList() else {
            val type = object : TypeToken<List<DownloadTask>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "枫叶"
    }

    val userAvatarFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY]
    }

    val searchHistoryFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[SEARCH_HISTORY_KEY] ?: ""
        if (jsonString.isEmpty()) emptyList() else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun saveUserAvatar(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_AVATAR_KEY] = uri
        }
    }

    suspend fun saveSearchHistory(history: List<String>) {
        val jsonString = gson.toJson(history)
        context.dataStore.edit { it[SEARCH_HISTORY_KEY] = jsonString }
    }

    suspend fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val current = searchHistoryFlow.first()
        val updated = listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }
        saveSearchHistory(updated.take(20))
    }
}