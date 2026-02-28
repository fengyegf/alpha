package com.appecho.alpha.ui.theme.manage

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ApiDataManager
import java.io.File


data class DownloadTask(
    val id: String,
    val title: String,      // 视频标题
    val author: String,     // 作者名称
    val description: String,// 视频简介
    val daysAgo: String,    // 天数/时间
    val coverUrl: String,   // 预览图
    val progress: Float,    // 下载进度 (0.0 - 1.0)
    val isCompleted: Boolean = false,
    val uri: String? = null // 存储文件 URI 字符串
)

@Composable
fun ManageScreenRoute() {
    val context = LocalContext.current
    val dataManager = remember { ApiDataManager(context) }
    val tasks by dataManager.downloadTasksFlow.collectAsState(initial = emptyList())
    var visibleTasks by remember { mutableStateOf<List<DownloadTask>>(emptyList()) }

    LaunchedEffect(tasks) {
        val validTasks = tasks.filter { isUriAvailable(context, it.uri) }
        visibleTasks = validTasks
        if (validTasks != tasks) {
            dataManager.saveDownloadTasks(validTasks)
        }
    }

    ManageScreen(tasks = visibleTasks, onPlay = { task -> playDownloadedVideo(context, task) })
}

@Composable
fun ManageScreen(tasks: List<DownloadTask> = emptyList(), onPlay: (DownloadTask) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "下载管理",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                DownloadTaskCard(task = task, onPlay = onPlay)
            }
        }
    }
}

@Composable
fun DownloadTaskCard(task: DownloadTask, onPlay: (DownloadTask) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            //1. 顶部进度条 (根据进度变动)
            // LinearProgressIndicator(
            //     progress = { task.progress },
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .height(6.dp),
            //     color = MaterialTheme.colorScheme.primary,
            //     trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            // )

            //2. 封面图区域
            AsyncImage(
                model = task.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
                contentScale = ContentScale.Crop
            )

            //3. 信息区域
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "作者: ${task.author}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.daysAgo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                //4. 播放
                Button(
                    onClick = { onPlay(task) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play_arrow_24px), // 请确保有此资源
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Play", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun isUriAvailable(context: Context, uriString: String?): Boolean {
    if (uriString.isNullOrBlank()) return false
    val uri = runCatching { uriString.toUri() }.getOrNull() ?: return false
    return when (uri.scheme?.lowercase()) {
        "file" -> {
            val path = uri.path ?: return false
            File(path).exists()
        }
        "content" -> {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
            } catch (_: Exception) {
                false
            }
        }
        else -> false
    }
}

private fun playDownloadedVideo(context: Context, task: DownloadTask) {
    val fileUri: android.net.Uri? = if (!task.uri.isNullOrEmpty()) {
        try {
            task.uri.toUri()
        } catch (_: Exception) {
            null
        }
    } else {
        val downloadId = task.id.toLongOrNull()
        if (downloadId != null) {
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.getUriForDownloadedFile(downloadId)
        } else {
            null
        }
    }

    if (fileUri == null) {
        Toast.makeText(context, "文件尚未准备好", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(intent, "选择播放器"))
    } else {
        Toast.makeText(context, "未找到可用播放器", Toast.LENGTH_SHORT).show()
    }
}
