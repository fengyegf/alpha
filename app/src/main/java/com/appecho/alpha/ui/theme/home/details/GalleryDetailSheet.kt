package com.appecho.alpha.ui.theme.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.home.DownloadRequest
import com.appecho.alpha.ui.theme.home.buildDownloadRequest

@Composable
fun GalleryDetailSheet(
    result: ParsedResult,
    onDownload: (DownloadRequest) -> Unit,
    onCopyLink: (String) -> Unit,
    onDelete: () -> Unit
) {
    val images = when {
        result.imageUrls.isNotEmpty() -> result.imageUrls
        !result.coverUrl.isNullOrBlank() -> listOf(result.coverUrl)
        result.videoUrl.isNotBlank() -> listOf(result.videoUrl)
        else -> emptyList()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
        Text("图集解析结果", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        if (images.isEmpty()) {
            Text("暂无图片", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(images) { index, url ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = {
                                val titleOverride = "${result.title ?: "image"}-${index + 1}"
                                onDownload(buildDownloadRequest(result, url, titleOverride))
                            }) {
                                Icon(painterResource(R.drawable.download_24px), null, modifier = Modifier.size(18.dp))
                                Text("保存该图", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    images.forEachIndexed { index, url ->
                        val titleOverride = "${result.title ?: "image"}-${index + 1}"
                        onDownload(buildDownloadRequest(result, url, titleOverride))
                    }
                },
                enabled = images.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(painterResource(R.drawable.download_24px), null)
                Text("全部保存")
            }
            OutlinedButton(
                onClick = { images.firstOrNull()?.let { onCopyLink(it) } },
                enabled = images.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(painterResource(R.drawable.content_copy_24px), null)
                Text("复制首图")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDelete) {
                Text("删除该解析")
            }
        }
    }
}

