package com.appecho.alpha.ui.theme.home

import com.appecho.alpha.ui.theme.ParsedResult

data class DownloadRequest(
    val url: String,
    val title: String?,
    val author: String?,
    val description: String?,
    val coverUrl: String?,
    val timestamp: Long,
    val type: String = "视频"
)

fun buildDownloadRequest(
    result: ParsedResult,
    url: String,
    titleOverride: String? = null
): DownloadRequest {
    return DownloadRequest(
        url = url,
        title = titleOverride ?: result.title,
        author = result.author,
        description = result.description,
        coverUrl = result.coverUrl,
        timestamp = result.timestamp,
        type = result.type
    )
}

