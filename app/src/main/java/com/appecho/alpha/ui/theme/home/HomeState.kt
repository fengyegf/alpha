package com.appecho.alpha.ui.theme.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.appecho.alpha.logic.ApiAnalyzer
import com.appecho.alpha.ui.theme.ApiDataManager
import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.ProfileListItem.ApiSource
import com.appecho.alpha.ui.theme.ResultType
import com.appecho.alpha.ui.theme.manage.DownloadTask
import kotlinx.coroutines.launch

data class HomeState(
    val userName: String,
    val userAvatar: String?,
    val parsedResults: List<ParsedResult>,
    val selectedResult: ParsedResult?,
    val selectedAnalyzeType: String,
    val showDownloadSheet: Boolean,
    val showBottomSheet: Boolean
)

data class HomeActions(
    val onShowDownloadSheet: () -> Unit,
    val onDismissDownloadSheet: () -> Unit,
    val onShowBottomSheet: () -> Unit,
    val onDismissBottomSheet: () -> Unit,
    val onSelectResult: (ParsedResult) -> Unit,
    val onClearSelectedResult: () -> Unit,
    val onAnalyze: (String) -> Unit,
    val onTypeChange: (String) -> Unit,
    val onDownload: (DownloadRequest) -> Unit,
    val onCopyLink: (String) -> Unit,
    val onDeleteResult: (ParsedResult) -> Unit
)

data class HomeStateHolder(
    val state: HomeState,
    val actions: HomeActions
)

@Composable
fun rememberHomeState(onNavigateToManage: () -> Unit): HomeStateHolder {
    val context = LocalContext.current
    val dataManager = remember { ApiDataManager(context) }
    val analyzer = remember { ApiAnalyzer() }
    val scope = rememberCoroutineScope()
    val latestNavigateToManage by rememberUpdatedState(onNavigateToManage)

    val userName by dataManager.userNameFlow.collectAsState(initial = "枫叶")
    val userAvatar by dataManager.userAvatarFlow.collectAsState(initial = null)

    val apiList = remember { mutableStateListOf<ApiSource>() }
    val parsedResults = remember { mutableStateListOf<ParsedResult>() }

    var selectedResult by remember { mutableStateOf<ParsedResult?>(null) }
    var selectedAnalyzeType by remember { mutableStateOf(ResultType.VIDEO) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dataManager.apiListFlow.collect { savedList ->
            apiList.clear()
            apiList.addAll(savedList)
        }
    }

    LaunchedEffect(Unit) {
        dataManager.parsedResultsFlow.collect { saved ->
            parsedResults.clear()
            parsedResults.addAll(saved)
        }
    }

    fun normalizedType(type: String): String = ResultType.normalize(type)

    val actions = HomeActions(
        onShowDownloadSheet = { showDownloadSheet = true },
        onDismissDownloadSheet = { showDownloadSheet = false },
        onShowBottomSheet = { showBottomSheet = true },
        onDismissBottomSheet = { showBottomSheet = false },
        onSelectResult = { selectedResult = it },
        onClearSelectedResult = { selectedResult = null },
        onTypeChange = { selectedAnalyzeType = it },
        onAnalyze = { inputUrl ->
            scope.launch {
                val normalizedSelected = normalizedType(selectedAnalyzeType)
                val api = apiList.firstOrNull { normalizedType(it.type) == normalizedSelected }
                if (api == null) {
                    Toast.makeText(
                        context,
                        "请先在个人中心添加${normalizedSelected}解析源",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val result = analyzer.analyzeToResult(inputUrl, api)
                    if (result != null) {
                        parsedResults.add(0, result)
                        dataManager.saveParsedResults(parsedResults.toList())
                        showDownloadSheet = false
                    } else {
                        Toast.makeText(context, "解析失败，请检查链接或源", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onDownload = { request ->
            scope.launch {
                Toast.makeText(context, "开始下载...", Toast.LENGTH_SHORT).show()
                val extension = inferFileExtension(request.url, request.type)
                val fileName = buildSafeFileName(request.title, java.util.UUID.randomUUID().toString(), extension)

                val uri = MediaDownloadManager.downloadMedia(
                    context = context,
                    url = request.url,
                    fileName = fileName,
                    coverUrl = request.coverUrl
                )

                if (uri != null) {
                    val task = DownloadTask(
                        id = java.util.UUID.randomUUID().toString(),
                        title = request.title.orEmpty(),
                        author = request.author.orEmpty(),
                        description = request.description.orEmpty(),
                        daysAgo = formatTimeAgo(request.timestamp),
                        coverUrl = request.coverUrl.orEmpty(),
                        progress = 1.0f,
                        isCompleted = true,
                        uri = uri.toString()
                    )
                    dataManager.addDownloadTask(task)
                    latestNavigateToManage()
                    Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onCopyLink = { url ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Direct Link", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "直链已复制", Toast.LENGTH_SHORT).show()
        },
        onDeleteResult = { toDelete ->
            parsedResults.remove(toDelete)
            scope.launch { dataManager.saveParsedResults(parsedResults.toList()) }
        }
    )

    val state = HomeState(
        userName = userName,
        userAvatar = userAvatar,
        parsedResults = parsedResults,
        selectedResult = selectedResult,
        selectedAnalyzeType = selectedAnalyzeType,
        showDownloadSheet = showDownloadSheet,
        showBottomSheet = showBottomSheet
    )

    return HomeStateHolder(state = state, actions = actions)
}
