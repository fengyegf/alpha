package com.appecho.alpha.ui.theme.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ApiDataManager
import com.appecho.alpha.ui.theme.ParsedResult
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(onBack:() -> Unit){
    val context = LocalContext.current
    val dataManager = remember { ApiDataManager(context) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val history by dataManager.searchHistoryFlow.collectAsState(initial = emptyList())
    val parsedResults by dataManager.parsedResultsFlow.collectAsState(initial = emptyList())

    val normalizedQuery = searchQuery.trim()
    val localResults = if (normalizedQuery.isEmpty()) {
        parsedResults
    } else {
        parsedResults.filter { it.matchesQuery(normalizedQuery) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "搜索",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("关键词") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_24px),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    scope.launch { dataManager.addSearchHistory(searchQuery) }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )

        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                history.take(6).forEach { item ->
                    TextButton(onClick = { searchQuery = item }) {
                        Text(item)
                    }
                }
            }
        }

        Text(
            text = "本地解析历史",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (localResults.isEmpty()) {
                item {
                    Text("暂无匹配结果", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(localResults) { result ->
                    SearchResultItem(result)
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(result: ParsedResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(result.title ?: "未命名", style = MaterialTheme.typography.titleSmall)
        result.author?.let { Text("作者: $it", style = MaterialTheme.typography.bodySmall) }
        result.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

private fun ParsedResult.matchesQuery(query: String): Boolean {
    val lower = query.lowercase()
    return listOfNotNull(title, author, description)
        .any { it.lowercase().contains(lower) }
}