package com.appecho.alpha.logic

import com.appecho.alpha.ui.theme.ProfileListItem.ApiSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SubscriptionHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchAndParse(url: String): List<ApiSource> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            parseJson(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseJson(jsonString: String): List<ApiSource> {
        val list = mutableListOf<ApiSource>()
        try {
            val root = JSONObject(jsonString)
            val keys = root.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val item = root.optJSONObject(key) ?: continue

                val name = item.optString("name", "Unknown")
                val icon = item.optString("icon", "")
                val url = item.optString("url", "")
                val type = item.optString("type", "General")
                val time = item.optString("time", "5000")

                // 解析查询
                val paramsList = mutableListOf<Pair<String, String>>()
                val queryObj = item.optJSONObject("Query")
                if (queryObj != null) {
                    val queryKeys = queryObj.keys()
                    while (queryKeys.hasNext()) {
                        val qKey = queryKeys.next()
                        val qVal = queryObj.optString(qKey)
                        paramsList.add(qKey to qVal)
                    }
                }

                // 映射
                val mappingJson = JSONObject()
                val responseObj = item.optJSONObject("response")

                fun extractPlaceholders(obj: JSONObject) {
                    val rKeys = obj.keys()
                    while (rKeys.hasNext()) {
                        val rKey = rKeys.next()
                        val rVal = obj.opt(rKey)

                        if (rVal is String && rVal.startsWith("\${") && rVal.endsWith("}")) {
                             mappingJson.put(rKey, rVal)
                        } else if (rVal is JSONObject) {
                            extractPlaceholders(rVal)
                        }
                    }
                }

                if (responseObj != null) {
                    extractPlaceholders(responseObj)
                }

                val finalJsonMapping = mappingJson.toString()

                list.add(ApiSource(
                    id = System.currentTimeMillis() + list.size, // 标识符
                    name = name,
                    iconUrl = icon,
                    apiUrl = url,
                    timeout = time,
                    type = type,
                    jsonMapping = finalJsonMapping,
                    params = paramsList
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}


