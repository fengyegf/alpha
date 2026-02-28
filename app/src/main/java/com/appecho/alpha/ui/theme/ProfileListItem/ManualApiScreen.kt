package com.appecho.alpha.ui.theme.ProfileListItem

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appecho.alpha.R

@Composable
fun ManualApiScreen(
    editingApi: ApiSource?,
    onBack: () -> Unit,
    onSave: (ApiSource) -> Unit
) {
    var jsonTextFieldValue by remember { mutableStateOf(TextFieldValue()) }
    // var jsonMapping by remember { mutableStateOf("") } // Removed unused variable
    var apiName by remember { mutableStateOf("") }
    var apiIconUrl by remember { mutableStateOf("") }
    val customParams = remember { mutableStateListOf<Pair<String, String>>() }
    var apiUrl by remember { mutableStateOf("") }
    var timeout by remember { mutableStateOf("5000") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val customMappingTags = remember { mutableStateListOf<Pair<String, String>>() }
    val options = listOf("视频", "图像", "音频")
    val selectedType = options[selectedIndex]
    val fixedTags = listOf(
        "名称" to "\${title}",
        "地址" to if (selectedType == "图像") "\${imageUrls}" else "\${videoUrl}",
        "作者" to "\${author}"
    )

    // 回填数据
    LaunchedEffect(editingApi) {
        editingApi?.let {
            apiName = it.name
            apiIconUrl = it.iconUrl
            apiUrl = it.apiUrl
            timeout = it.timeout
            selectedIndex = options.indexOf(it.type).coerceAtLeast(0)
            jsonTextFieldValue = TextFieldValue(it.jsonMapping)
            customParams.clear()
            customParams.addAll(it.params)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        ProfileTopBar(
            title = if (editingApi == null) "手动添加 API" else "编辑 API",
            onBack = onBack
        )

        //顶部动态预览区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标：仅在有链接时显示
            if (apiIconUrl.isNotBlank()) {
                AsyncImage(
                    model = apiIconUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(modifier = Modifier.size(80.dp))
            }

            Spacer(modifier = Modifier.size(12.dp))

            // 名称：仅在有文字时显示
            Text(
                text = apiName, // 直接显示状态值，为空则自然不显示
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 输入区域
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // 名称输入框
            OutlinedTextField(
                value = apiName,
                onValueChange = { apiName = it },
                label = { Text("名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // 图标链接输入框
            OutlinedTextField(
                value = apiIconUrl,
                onValueChange = { apiIconUrl = it },
                label = { Text("图标链接") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
        Text(
            text = "解析接口设置",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = apiUrl,
            onValueChange = { apiUrl = it },
            label = { Text("接口地址") },
            placeholder = { Text("https://example.com/api?url={url}") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            supportingText = {
                Text("使用 {url} 作为分享链接的占位符")
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "请求参数 (Header/Query)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    customParams.add("" to "")
                }
            ) {
                Icon(
                    painterResource(id = R.drawable.add_circle_24px),
                    contentDescription = "添加参数",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        customParams.forEachIndexed { index, pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pair.first,
                    onValueChange = { customParams[index] = it to pair.second },
                    placeholder = { Text("名称") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = pair.second,
                    onValueChange = { customParams[index] = pair.first to it },
                    placeholder = { Text("内容") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                IconButton(onClick = { customParams.removeAt(index) }) {
                    Icon(
                        painterResource(id = R.drawable.delete_24px),
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Text(
            text = "全局设置",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = timeout,
            onValueChange = { if (it.all { char -> char.isDigit() }) timeout = it },
            label = { Text("超时时长（ms)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            suffix = { Text("ms") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true
        )
        Text(
            text = "解析类型",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        @OptIn(ExperimentalMaterial3Api::class)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    label = { Text(label) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "响应内容映射",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { customMappingTags.add("" to "") }) {
                Icon(
                    painterResource(id = R.drawable.add_circle_24px),
                    contentDescription = "添加自定义映射",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        fixedTags.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pair.first,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true,
                    enabled = false
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = pair.second,
                    onValueChange = {},
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true,
                    enabled = false
                )
            }
        }
        customMappingTags.forEachIndexed { index, pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pair.first,
                    onValueChange = { customMappingTags[index] = it to pair.second },
                    placeholder = { Text("名称") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = pair.second,
                    onValueChange = { customMappingTags[index] = pair.first to it },
                    placeholder = { Text("标签") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                IconButton(onClick = { customMappingTags.removeAt(index) }) {
                    Icon(
                        painterResource(id = R.drawable.delete_24px),
                        contentDescription = "删除自定义映射",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allTags =
                fixedTags + customMappingTags.filter { it.first.isNotBlank() && it.second.isNotBlank() }
            allTags.forEach { (label, tag) ->
                AssistChip(
                    onClick = {
                        val text = jsonTextFieldValue.text
                        val selection = jsonTextFieldValue.selection
                        val newText = text.replaceRange(selection.start, selection.end, tag)
                        jsonTextFieldValue = jsonTextFieldValue.copy(
                            text = newText,
                            selection = TextRange(selection.start + tag.length)
                        )
                    },
                    label = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value = jsonTextFieldValue,
            onValueChange = { jsonTextFieldValue = it },
            label = { Text("配置 JSON 映射模板") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            minLines = 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Button(
            onClick = {
                if (apiName.isNotBlank() && apiUrl.isNotBlank()) {
                    val id = editingApi?.id ?: System.currentTimeMillis()
                    val newApi = ApiSource(
                        id = id,
                        name = apiName,
                        iconUrl = apiIconUrl,
                        apiUrl = apiUrl,
                        timeout = timeout,
                        type = options[selectedIndex],
                        jsonMapping = jsonTextFieldValue.text,
                        params = customParams.toList()
                    )

                    onSave(newApi)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = apiName.isNotBlank() && apiUrl.isNotBlank()
        ) {
            Text("确认添加并保存")
        }
    }
}
