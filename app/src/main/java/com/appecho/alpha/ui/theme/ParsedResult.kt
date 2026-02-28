package com.appecho.alpha.ui.theme

data class ParsedResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val videoUrl: String,
    val coverUrl: String? = null,
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val imageUrls: List<String> = emptyList(),
    val type: String = "视频",
    val timestamp: Long = System.currentTimeMillis()
)