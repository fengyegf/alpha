package com.appecho.alpha.ui.theme.home

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
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.home.components.AudioPlayer
import com.appecho.alpha.ui.theme.home.details.DetailActionButtons

@Composable
fun MusicDetailSheet(
    result: ParsedResult,
    onDownload: (DownloadRequest) -> Unit,
    onCopyLink: (String) -> Unit,
    onDelete: () -> Unit
) {
    var playbackError by remember(result.videoUrl) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
        if (!result.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = result.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (result.videoUrl.isNotBlank() && !playbackError) {
            AudioPlayer(
                audioUrl = result.videoUrl,
                onPlaybackError = { playbackError = true }
            )
        }

        result.title?.let { Text(it, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
        result.author?.let { Text("作者: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
        result.duration?.let { Text("时长: $it", style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.height(24.dp))

        val hasPlayable = result.videoUrl.isNotBlank() && !playbackError
        DetailActionButtons(
            hasPlayable = hasPlayable,
            onDownload = { onDownload(buildDownloadRequest(result, result.videoUrl)) },
            onDelete = onDelete,
            onCopyLink = { onCopyLink(result.videoUrl) }
        )
    }
}

