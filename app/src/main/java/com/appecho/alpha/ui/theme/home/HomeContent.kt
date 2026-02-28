package com.appecho.alpha.ui.theme.home

import com.appecho.alpha.ui.theme.ParsedResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appecho.alpha.ui.theme.home.components.FullScreenCarousel
import com.appecho.alpha.ui.theme.home.components.PeekCarousel

@Composable
fun HomeMainContent(
    parsedResults: List<ParsedResult>,
    onItemClick: (ParsedResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (parsedResults.isEmpty()) {
            // 默认状态：展示一张漂亮的占位大图（后续可远程修改）
            val defaultImages = listOf("https://image-assets.soutushenqi.com/UserUploadWallpaper_upload/1769835945393.jpg")
            FullScreenCarousel(defaultImages)
        } else {
            // 解析后：展示 Peek 样式的相册
            Text(
                text = "最近解析",
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            PeekCarousel(
                results = parsedResults,
                onItemClick = onItemClick
            )
        }

    }
}