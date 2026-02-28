package com.appecho.alpha.ui.theme.home

import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.appecho.alpha.logic.UpdateInfo
import com.appecho.alpha.logic.UpdateManager

@Composable
fun UpdateChecker(updateUrl: String) {
    val context = LocalContext.current
    var showUpdateSheet by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = UpdateManager.checkUpdate(context, updateUrl)
        if (info != null) {
            showUpdateSheet = info
        }
    }

    showUpdateSheet?.let { info ->
        UpdateBottomSheet(
            updateInfo = info,
            onDismiss = { showUpdateSheet = null },
            onDownload = { url ->
                UpdateManager.downloadApk(context, url)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // 长度滚动
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "发现新版本: ${updateInfo.version}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multimedia display logic
            when {
                !updateInfo.video.isNullOrBlank() -> {
                    VideoPlayer(videoUrl = updateInfo.video)
                }
                !updateInfo.img.isNullOrBlank() -> {
                    AsyncImage(
                        model = updateInfo.img,
                        contentDescription = "更新预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 处理自动换行
            Text(
                text = updateInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onDownload(updateInfo.url) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp) // 添加底部填充
            ) {
                Text("立即更新")
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }

    DisposableEffect(Unit) {
        onDispose {
            videoView.stopPlayback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                videoView.apply {
                    setVideoURI(videoUrl.toUri())
                    setMediaController(MediaController(ctx))
                    setOnPreparedListener { mp ->
                        mp.start()
                        mp.isLooping = true
                    }
                }
            },
            modifier = Modifier.matchParentSize()
        )
    }
}
