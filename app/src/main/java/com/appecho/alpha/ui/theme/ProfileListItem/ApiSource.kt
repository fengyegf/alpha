package com.appecho.alpha.ui.theme.ProfileListItem

data class ApiSource(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val iconUrl: String,
    val apiUrl: String,
    val timeout: String,
    val type: String,
    val jsonMapping: String,
    // 建议增加一个参数列表字段，方便以后扩展
    val params: List<Pair<String, String>> = emptyList()
)