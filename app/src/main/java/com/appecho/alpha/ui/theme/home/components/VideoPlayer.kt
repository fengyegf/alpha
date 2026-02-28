package com.appecho.alpha.ui.theme.home.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(videoUrl: String, onPlaybackError: ((PlaybackException) -> Unit)? = null) {
    val context = LocalContext.current
    val latestOnPlaybackError by rememberUpdatedState(onPlaybackError)

    // 初始化 ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // 自动播放
        }
    }

    val errorListener = remember {
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                latestOnPlaybackError?.invoke(error)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && exoPlayer.duration == 0L) {
                    // 如果准备好但时长为0，也认为是无效视频（例如过期链接返回了空内容）
                    latestOnPlaybackError?.invoke(PlaybackException(
                        "Invalid video duration",
                        null,
                        PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK
                    ))
                }
            }
        }
    }

    // 界面销毁时释放播放器
    DisposableEffect(Unit) {
        exoPlayer.addListener(errorListener)
        onDispose {
            exoPlayer.removeListener(errorListener)
            exoPlayer.release()
        }
    }

    // 将原生的 PlayerView 嵌入 Compose
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // 显示播放控制条
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}