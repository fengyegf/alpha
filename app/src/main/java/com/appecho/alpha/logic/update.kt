package com.appecho.alpha.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val version: String,
    val description: String,
    val img: String?,
    val video: String?,
    val url: String // 下载直链
)

object UpdateManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun checkUpdate(context: Context, updateUrl: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(updateUrl).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string() ?: return@withContext null

            val jsonObject = JSONObject(jsonStr)
            val remoteVersion = jsonObject.optString("version")

            if (isNewVersion(context, remoteVersion)) {
                UpdateInfo(
                    version = remoteVersion,
                    description = jsonObject.optString("description").replace("\\n", "\n"),
                    img = jsonObject.optString("img").takeIf { it.isNotEmpty() && it != "null" },
                    video = jsonObject.optString("video").takeIf { it.isNotEmpty() && it != "null" },
                    url = jsonObject.optString("url")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isNewVersion(context: Context, remoteVersion: String): Boolean {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "1.0"
            return compareVersions(remoteVersion, currentVersion) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(parts1.size, parts2.size)

        for (i in 0 until length) {
            val part1 = parts1.getOrElse(i) { 0 }
            val part2 = parts2.getOrElse(i) { 0 }
            if (part1 != part2) {
                return part1.compareTo(part2)
            }
        }
        return 0
    }

    fun downloadApk(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

