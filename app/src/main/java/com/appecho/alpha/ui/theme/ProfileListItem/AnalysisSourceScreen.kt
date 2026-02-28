package com.appecho.alpha.ui.theme.ProfileListItem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appecho.alpha.R

@Composable
fun AnalysisSourceScreen(
    apiList: MutableList<ApiSource>,
    onBack: () -> Unit,
    onNavigateToManualApi: () -> Unit,
    onEditApi: (ApiSource) -> Unit,
    onDeleteApi: (ApiSource) -> Unit,
    onImportFromUrl: (String) -> Unit
) {
    var subscriptionUrl by remember { mutableStateOf("") }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column {
        ProfileTopBar(
            title = "解析源",
            onBack = onBack
        )
        Text(
            text = "订阅链接",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = subscriptionUrl,
            onValueChange = { subscriptionUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("粘贴订阅地址") },
            placeholder = { Text("https://...") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = {
                if (subscriptionUrl.isEmpty()) {
                    // 无内容时显示粘贴
                    IconButton(onClick = {
                        clipboardManager.getText()
                            ?.let { subscriptionUrl = it.text }
                    }) {
                        Icon(
                            painterResource(id = R.drawable.content_paste_24px),
                            "粘贴"
                        )
                    }
                } else {
                    // 有内容时显示清除
                    IconButton(onClick = { subscriptionUrl = "" }) {
                        Icon(painterResource(id = R.drawable.delete_24px), "清除")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        Text(
            text = "支持 JSON 或 YAML 格式的订阅源",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Button(
            onClick = {
                onImportFromUrl(subscriptionUrl)
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = subscriptionUrl.isNotBlank()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.check_24px),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("保存链接并测试")
            }
        }
        Text(
            text = "更多",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        ProfileListItem(
            icon = painterResource(id = R.drawable.add_circle_24px),
            label = "手动配置自定义 API 接口",
            onClick = onNavigateToManualApi
        )
        Text(
            text = "解析列表",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 6.dp)
        )
        apiList.forEach { api ->
            var showMenu by remember { mutableStateOf(false) } // 控制每个项的菜单显示

            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text(api.name) },
                supportingContent = {
                    Text(
                        api.type,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = {
                    AsyncImage(
                        model = api.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.link_24px) // 如果没有图像那就用默认图标
                    )
                },
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                painterResource(id = R.drawable.more_vert_24px),
                                contentDescription = "更多"
                            )
                        }

                        // 三点展开的菜单
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("编辑") },
                                onClick = {
                                    showMenu = false
                                    onEditApi(api)
                                },
                                leadingIcon = { Icon(painterResource(id = R.drawable.edit_24px), null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "删除",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteApi(api)
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(id = R.drawable.delete_24px),
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
