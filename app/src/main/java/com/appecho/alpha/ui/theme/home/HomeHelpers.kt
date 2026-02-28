package com.appecho.alpha.ui.theme.home

import androidx.core.net.toUri
import java.util.Locale

fun buildSafeFileName(title: String?, fallbackId: String, extension: String): String {
    val base = (title ?: fallbackId)
        .trim()
        .ifEmpty { fallbackId }
    val sanitized = base.replace(Regex("[/:*?\"<>|]"), "_")
    val normalizedExtension = extension.trim().ifEmpty { "mp4" }
    return String.format(Locale.US, "%s.%s", sanitized, normalizedExtension)
}

fun inferFileExtension(url: String, type: String = "视频"): String {
    val ext = url.toUri().lastPathSegment
        ?.substringAfterLast('.', "")
        ?.lowercase(Locale.US)
        ?.trim()
        .orEmpty()
    
    if (ext.isNotEmpty()) return ext
    
    return when (type) {
        "音频" -> "mp3"
        "图像" -> "jpg"
        else -> "mp4"
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val minutes = diffMs / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "$minutes 分钟前"
        hours < 24 -> "$hours 小时前"
        else -> "$days 天前"
    }
}

