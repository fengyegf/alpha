package com.appecho.alpha.logic

import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.ProfileListItem.ApiSource
import com.appecho.alpha.ui.theme.ResultType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ApiAnalyzer {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeToResult(url: String, api: ApiSource): ParsedResult? = withContext(Dispatchers.IO) {
        try {
            val requestUrl = api.apiUrl.replace("{url}", url)
            val requestBuilder = Request.Builder().url(requestUrl)

            // 注入 Header
            api.params.forEach { (key, value) ->
                if (key.isNotBlank()) requestBuilder.addHeader(key, value)
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = response.body?.string() ?: return@withContext null
                val root = JSONTokener(json).nextValue()
                val mapping = parseMapping(api.jsonMapping)
                val normalizedType = ResultType.normalize(api.type)

                val title = firstNonBlank(
                    mappedValues(root, mapping, TITLE_PLACEHOLDERS),
                    deepFindByKeys(root, setOf("title", "name", "tag"))
                )
                val author = firstNonBlank(
                    mappedValues(root, mapping, AUTHOR_PLACEHOLDERS),
                    deepFindByKeys(root, setOf("author", "uploader", "nickname", "creator", "name"))
                )
                val coverUrl = firstUsableUrl(
                    mappedValues(root, mapping, COVER_PLACEHOLDERS),
                    deepFindByKeys(root, setOf("cover", "coverUrl", "thumb", "thumbnail", "poster"))
                )
                val description = firstNonBlank(
                    mappedValues(root, mapping, DESCRIPTION_PLACEHOLDERS),
                    deepFindByKeys(root, setOf("description", "desc", "content", "summary"))
                )
                val duration = firstNonBlank(
                    mappedValues(root, mapping, DURATION_PLACEHOLDERS),
                    deepFindByKeys(root, setOf("duration", "length", "time"))
                )

                val primaryUrls = distinctUsableUrls(
                    mappedValues(root, mapping, ADDRESS_PLACEHOLDERS)
                )

                when (normalizedType) {
                    ResultType.GALLERY -> {
                        val imageUrls = distinctUsableUrls(
                            primaryUrls + deepFindByKeys(root, setOf("images", "image", "pics", "photos", "urls", "url", "src"))
                        )
                        if (title.isNullOrBlank() || author.isNullOrBlank() || imageUrls.isEmpty()) {
                            return@withContext null
                        }
                        ParsedResult(
                            videoUrl = imageUrls.first(),
                            coverUrl = coverUrl ?: imageUrls.firstOrNull(),
                            title = title,
                            author = author,
                            description = description,
                            duration = duration,
                            imageUrls = imageUrls,
                            type = normalizedType
                        )
                    }

                    else -> {
                        val mediaUrl = firstUsableUrl(
                            primaryUrls,
                            deepFindByKeys(root, setOf("url", "video", "play", "playUrl", "audio", "src"))
                        )
                        if (title.isNullOrBlank() || author.isNullOrBlank() || mediaUrl.isNullOrBlank()) {
                            return@withContext null
                        }
                        ParsedResult(
                            videoUrl = mediaUrl,
                            coverUrl = coverUrl,
                            title = title,
                            author = author,
                            description = description,
                            duration = duration,
                            type = normalizedType
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseMapping(jsonMapping: String): Map<String, List<String>> {
        if (jsonMapping.isBlank()) return emptyMap()
        return try {
            val mappingObj = JSONObject(jsonMapping)
            val grouped = linkedMapOf<String, MutableList<String>>()
            val keys = mappingObj.keys()
            while (keys.hasNext()) {
                val path = keys.next()
                val placeholder = mappingObj.optString(path).trim()
                if (placeholder.isBlank()) continue
                grouped.getOrPut(placeholder) { mutableListOf() }.add(path)
            }
            grouped.mapValues { it.value.toList() }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun mappedValues(
        root: Any?,
        mapping: Map<String, List<String>>,
        placeholders: Set<String>
    ): List<String> {
        val paths = placeholders.flatMap { mapping[it].orEmpty() }
        return paths.flatMap { resolvePathValues(root, it) }
    }

    private fun resolvePathValues(root: Any?, rawPath: String): List<String> {
        val path = rawPath.replace("\${", "").replace("}", "").trim()
        if (path.isBlank()) return emptyList()
        var nodes = listOfNotNull(root)
        for (segment in path.split(".").filter { it.isNotBlank() }) {
            nodes = nodes.flatMap { nextNodes(it, segment) }
            if (nodes.isEmpty()) break
        }
        return nodes.flatMap { flattenToStrings(it) }
    }

    private fun nextNodes(node: Any, key: String): List<Any> {
        return when (node) {
            is JSONObject -> {
                if (!node.has(key) || node.isNull(key)) emptyList() else listOfNotNull(node.opt(key))
            }

            is JSONArray -> buildList {
                for (index in 0 until node.length()) {
                    val child = node.opt(index) ?: continue
                    addAll(nextNodes(child, key))
                }
            }

            else -> emptyList()
        }
    }

    private fun deepFindByKeys(root: Any?, targetKeys: Set<String>): List<String> {
        if (root == null) return emptyList()
        val result = mutableListOf<String>()
        collectByKeys(root, targetKeys, result)
        return result
    }

    private fun collectByKeys(node: Any, targetKeys: Set<String>, out: MutableList<String>) {
        when (node) {
            is JSONObject -> {
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = node.opt(key) ?: continue
                    if (key in targetKeys) {
                        out += flattenToStrings(value)
                    }
                    collectByKeys(value, targetKeys, out)
                }
            }

            is JSONArray -> {
                for (index in 0 until node.length()) {
                    val value = node.opt(index) ?: continue
                    collectByKeys(value, targetKeys, out)
                }
            }
        }
    }

    private fun flattenToStrings(value: Any?): List<String> {
        return when (value) {
            null, JSONObject.NULL -> emptyList()
            is JSONArray -> buildList {
                for (index in 0 until value.length()) {
                    addAll(flattenToStrings(value.opt(index)))
                }
            }

            is JSONObject -> emptyList()
            else -> listOf(value.toString())
        }
    }

    private fun firstNonBlank(vararg values: List<String>): String? {
        return values.asSequence().flatten().map { it.trim() }.firstOrNull { it.isNotBlank() }
    }

    private fun firstUsableUrl(vararg values: List<String>): String? {
        return distinctUsableUrls(values.asSequence().flatten().toList()).firstOrNull()
    }

    private fun distinctUsableUrls(values: List<String>): List<String> {
        return values
            .asSequence()
            .map { normalizeUrl(it) }
            .filter { isUsableUrl(it) }
            .distinct()
            .toList()
    }

    private fun normalizeUrl(raw: String): String {
        val value = raw.trim().replace("\\/", "/")
        return if (value.startsWith("//")) "https:$value" else value
    }

    private fun isUsableUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    companion object {
        private val TITLE_PLACEHOLDERS = setOf("\${title}", "\${tag}")
        private val ADDRESS_PLACEHOLDERS = setOf("\${videoUrl}", "\${url}", "\${address}", "\${imageUrls}")
        private val AUTHOR_PLACEHOLDERS = setOf("\${author}", "\${name}")
        private val COVER_PLACEHOLDERS = setOf("\${cover}", "\${thumbnail}")
        private val DESCRIPTION_PLACEHOLDERS = setOf("\${description}", "\${desc}")
        private val DURATION_PLACEHOLDERS = setOf("\${duration}", "\${length}")
    }
}