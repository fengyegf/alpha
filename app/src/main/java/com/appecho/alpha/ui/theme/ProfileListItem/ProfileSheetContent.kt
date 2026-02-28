package com.appecho.alpha.ui.theme.ProfileListItem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ApiDataManager
import com.appecho.alpha.logic.SubscriptionHelper
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ProfileMenuLevel(val order: Int) {
    MAIN(0),      // 主菜单层级
    INTERFACE(1), // 界面设置层级
    THEME(1),      // 主题与色彩层级
    ANALYSIS(1),    //解析数据源
    MANUAL_API(2)   //自定义数据源
}

@Composable
fun ProfileSheetContent() {
    val context = LocalContext.current
    val dataManager = remember { ApiDataManager(context) }
    val scope = rememberCoroutineScope()
    var currentMenu by remember { mutableStateOf(ProfileMenuLevel.MAIN) }
    // 状态提升
    val apiList = remember { mutableStateListOf<ApiSource>() }
    var editingApi by remember { mutableStateOf<ApiSource?>(null) }

    val userName by dataManager.userNameFlow.collectAsState(initial = "枫叶")
    val userAvatar by dataManager.userAvatarFlow.collectAsState(initial = null)

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                        val file = File(context.filesDir, fileName)

                        // Delete old avatar if exists
                        userAvatar?.let { oldUriString ->
                            try {
                                val oldFile = File(oldUriString.toUri().path ?: "")
                                if (oldFile.exists() && oldFile.absolutePath.contains(context.filesDir.absolutePath)) {
                                    oldFile.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        FileOutputStream(file).use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                        dataManager.saveUserAvatar(file.toUri().toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("修改名称") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank()) {
                         scope.launch { dataManager.saveUserName(tempName) }
                    }
                    showNameDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("取消") }
            }
        )
    }

    LaunchedEffect(Unit) {
        dataManager.apiListFlow.collect { savedList ->
            if (apiList.isEmpty()) { // 防止重复添加
                apiList.addAll(savedList)
            }
        }
    }

    fun triggerSave() {
        scope.launch {
            dataManager.saveApiList(apiList.toList())
        }
    }

    AnimatedContent(
        targetState = currentMenu,
        transitionSpec = {
            val isForward = targetState.order > initialState.order


            if (isForward) {
                // 前进：从右滑入
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            } else {
                // 后退：从左滑入
                (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
            }.using(SizeTransform(clip = false))
        },
        label = "MenuTransition"
    ) { targetLevel ->
        // 使用 Column 作为主容器
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(renderEffect = null)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 核心：根据当前状态切换显示内容
            when (targetLevel) {
                ProfileMenuLevel.MAIN -> {
                    // 1. 头部卡片
                    ProfileHeaderCard(
                        name = userName,
                        avatarUri = userAvatar,
                        avatarLetter = userName.firstOrNull()?.toString() ?: "F",
                        onNameClick = {
                            tempName = userName
                            showNameDialog = true
                        },
                        onAvatarClick = {
                            imagePicker.launch("image/*")
                        }
                    )

                    // 2. 分类标题
                    Text(
                        text = "应用与界面",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )

                    // 3. 功能列表
                    ProfileListItem(
                        icon = painterResource(id = R.drawable.dataset_24px),
                        label = "界面",
                        onClick = { currentMenu = ProfileMenuLevel.INTERFACE }
                    )
                    ProfileListItem(
                        icon = painterResource(id = R.drawable.palette_24px),
                        label = "主题与色彩",
                        onClick = { currentMenu = ProfileMenuLevel.THEME }
                    )
                    //二级列表
                    Text(
                        text = "数据源和质量",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    ProfileListItem(
                        icon = painterResource(id = R.drawable.deployed_code_update_24px),
                        label = "解析源",
                        onClick = { currentMenu = ProfileMenuLevel.ANALYSIS }
                    )
                    Text(
                        text = "数据管理",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )

                }

                ProfileMenuLevel.INTERFACE -> {
                    // 进入子菜单：顶置返回栏 + 子项
                    ProfileTopBar(
                        title = "界面设置",
                        onBack = { currentMenu = ProfileMenuLevel.MAIN }
                    )
                    // 这里可以复用 ProfileListItem 编写具体的界面开关
                    Text("此处放置界面相关的设置项...", modifier = Modifier.padding(16.dp))
                }

                ProfileMenuLevel.THEME -> {
                    ProfileTopBar(
                        title = "主题与色彩",
                        onBack = { currentMenu = ProfileMenuLevel.MAIN }
                    )
                    Text("此处放置主题相关的设置项...", modifier = Modifier.padding(16.dp))
                }
                //解析源页面
                ProfileMenuLevel.ANALYSIS -> {
                    AnalysisSourceScreen(
                        apiList = apiList,
                        onBack = { currentMenu = ProfileMenuLevel.MAIN },
                        onNavigateToManualApi = {
                            editingApi = null // 确保是新增模式
                            currentMenu = ProfileMenuLevel.MANUAL_API
                        },
                        onEditApi = { api ->
                            editingApi = api
                            currentMenu = ProfileMenuLevel.MANUAL_API
                        },
                        onDeleteApi = { api ->
                            apiList.remove(api)
                            triggerSave()
                        },
                        onImportFromUrl = { url ->
                            scope.launch {
                                val newApis = SubscriptionHelper.fetchAndParse(url)
                                if (newApis.isNotEmpty()) {
                                    for (api in newApis) {
                                        //注意id
                                        val exists = apiList.any { it.apiUrl == api.apiUrl && it.name == api.name }
                                        if (!exists) {
                                            apiList.add(api)
                                        }
                                    }
                                    triggerSave()
                                }
                            }
                        }
                    )
                }
                // 新增分支
                //自定义api添加页
                ProfileMenuLevel.MANUAL_API -> {
                    ManualApiScreen(
                        editingApi = editingApi,
                        onBack = {
                            editingApi = null // 返回清空
                            currentMenu = ProfileMenuLevel.ANALYSIS
                        },
                        onSave = { newApi ->
                            if (editingApi != null) {
                                // 编辑模式：找到旧对象并替换
                                val index = apiList.indexOf(editingApi)
                                if (index != -1) {
                                    apiList[index] = newApi
                                }
                            } else {
                                // 新增模式
                                apiList.add(newApi)
                            }
                            triggerSave()
                            editingApi = null // 保存后清空
                            currentMenu = ProfileMenuLevel.ANALYSIS
                        }
                    )
                }
            }
        }
    }
}
