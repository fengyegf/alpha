package com.appecho.alpha.ui.theme.category

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.appecho.alpha.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

data class MediaItem(
    val uri: Uri,
    val displayName: String,
    val dateAddedSeconds: Long,
    val sizeBytes: Long = 0L,
    val coverUri: Uri? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen() {
    val categories = remember { CategoryMappings.specs }

    // 2. 初始化 Pager 状态
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // 3. 顶部 Primary Tab Row
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            categories.forEachIndexed { index, item ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = {
                        // 点击 Tab 时让 Pager 滚动到对应页面
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(item.title) },
                    //添加小图标
                    icon = {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }

        // 4. 左右滑动的 Pager 内容区
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            // 根据页面显示不同的内容
            CategoryPageContent(categories[page].title)
        }
    }
}

@Composable
fun CategoryPageContent(title: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var items by remember(title) { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember(title) { mutableStateOf(true) }
    var isRefreshing by remember(title) { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        items = loadMediaItemsForCategory(context, title)
    }

    LaunchedEffect(title) {
        isLoading = true
        reload()
        isLoading = false
    }

    DisposableEffect(title, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isRefreshing) {
                scope.launch {
                    reload()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = refreshState,
        onRefresh = {
            if (!isRefreshing) {
                isRefreshing = true
                scope.launch {
                    reload()
                    isRefreshing = false
                }
            }
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = refreshState,
                isRefreshing = isRefreshing
            )
        }
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "正在加载...", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "这里是 $title 分类内容", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                if (CategoryMappings.find(title)?.isAudio == true) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items, key = { it.uri }) { item ->
                            AudioGridItemCard(
                                item = item,
                                onOpen = { openMedia(context, title, item.uri) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items, key = { it.uri }) { item ->
                            MediaItemCard(
                                iconRes = CategoryMappings.find(title)?.iconRes ?: R.drawable.dataset_24px,
                                item = item,
                                onOpen = { openMedia(context, title, item.uri) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaItemCard(iconRes: Int, item: MediaItem, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    text = "已保存到本地",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.play_arrow_24px),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AudioGridItemCard(item: MediaItem, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Top part: Image
            SubcomposeAsyncImage(
                model = item.coverUri ?: item.uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.audio_file_24px),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }
                }
            )
            
            // Bottom part: Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                val title = item.displayName.substringBeforeLast(".")
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sizeText = if (item.sizeBytes >= 1024 * 1024) {
                    "${item.sizeBytes / (1024 * 1024)} MB"
                } else {
                    "${item.sizeBytes / 1024} KB"
                }
                Text(
                    text = sizeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private suspend fun loadMediaItemsForCategory(context: Context, title: String): List<MediaItem> {
    val category = CategoryMappings.find(title) ?: return emptyList()
    val collection = category.collectionUri
    val relativePathPrefix = category.relativePathPrefix
    return withContext(Dispatchers.IO) {
        val audioCoverMap = if (category.isAudio) loadAudioCoverMap(context) else emptyMap()
        val projection = buildList {
            add(MediaStore.MediaColumns._ID)
            add(MediaStore.MediaColumns.DISPLAY_NAME)
            add(MediaStore.MediaColumns.DATE_ADDED)
            add(MediaStore.MediaColumns.RELATIVE_PATH)
            add(MediaStore.MediaColumns.SIZE)
            if (category.isAudio) {
                add(MediaStore.Audio.AudioColumns.TITLE)
                add(MediaStore.Audio.AudioColumns.ALBUM_ID)
            }
        }.toTypedArray()
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("$relativePathPrefix%")
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        val results = mutableListOf<MediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val titleIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)
            val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val displayName = cursor.getString(nameIndex).orEmpty()
                val audioTitle = if (titleIndex >= 0) cursor.getString(titleIndex).orEmpty() else ""
                val dateAdded = cursor.getLong(dateIndex)
                val sizeBytes = cursor.getLong(sizeIndex)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val resolvedName = when {
                    audioTitle.isNotBlank() -> audioTitle
                    displayName.isNotBlank() -> displayName.substringBeforeLast(".", displayName)
                    else -> "未命名音频"
                }
                val albumCoverUri = if (albumIdIndex >= 0 && !cursor.isNull(albumIdIndex)) {
                    val albumId = cursor.getLong(albumIdIndex)
                    if (albumId > 0L) ContentUris.withAppendedId(albumArtBaseUri, albumId) else null
                } else {
                    null
                }
                val coverUri = albumCoverUri ?: audioCoverMap[displayName.substringBeforeLast(".", displayName)] ?: audioCoverMap[resolvedName]
                results.add(MediaItem(contentUri, resolvedName, dateAdded, sizeBytes, coverUri))
            }
        }
        results
    }
}

private fun loadAudioCoverMap(context: Context): Map<String, Uri> {
    val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
    val selectionArgs = arrayOf("Pictures/Alpha/AudioCover/%")
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.TITLE
    )

    val map = mutableMapOf<String, Uri>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val titleIndex = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIndex)
            val displayName = cursor.getString(nameIndex).orEmpty()
            val title = if (titleIndex >= 0) cursor.getString(titleIndex).orEmpty() else ""
            val key = if (title.isNotBlank()) title else displayName.substringBeforeLast(".", displayName)
            if (key.isBlank()) continue
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            map[key] = uri
        }
    }
    return map
}

private fun openMedia(context: Context, title: String, uri: Uri) {
    val mimeType = CategoryMappings.find(title)?.mimeType ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(intent, "选择播放器"))
    } else {
        Toast.makeText(context, "未找到可用应用", Toast.LENGTH_SHORT).show()
    }
}