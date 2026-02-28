package com.appecho.alpha.ui.theme.home.details

import com.appecho.alpha.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.home.DownloadRequest
import com.appecho.alpha.ui.theme.home.components.VideoPlayer
import com.appecho.alpha.ui.theme.home.buildDownloadRequest

@Composable
fun VideoDetailSheet(
    result: ParsedResult,
    onDownload: (DownloadRequest) -> Unit,
    onCopyLink: (String) -> Unit,
    onDelete: () -> Unit
) {
    var playbackError by remember(result.videoUrl) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
        if (result.videoUrl.isNotBlank() && !playbackError) {
            VideoPlayer(
                videoUrl = result.videoUrl,
                onPlaybackError = { playbackError = true }
            )
        } else {
            // 兜底：如果没视频链接或播放失败，显示封面
            AsyncImage(
                model = result.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // 2. 动态信息展示
        result.title?.let { Text(it, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
        result.author?.let { Text("作者: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
        result.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 底部按键
        val hasPlayable = result.videoUrl.isNotBlank() && !playbackError
        DetailActionButtons(
            hasPlayable = hasPlayable,
            onDownload = { onDownload(buildDownloadRequest(result, result.videoUrl)) },
            onDelete = onDelete,
            onCopyLink = { onCopyLink(result.videoUrl) }
        )
    }
}