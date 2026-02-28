package com.appecho.alpha.ui.theme.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appecho.alpha.R
import com.appecho.alpha.ui.theme.ProfileListItem.ProfileSheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onNavigateToManage: () -> Unit
) {
    val stateHolder = rememberHomeState(onNavigateToManage)
    val state = stateHolder.state
    val actions = stateHolder.actions
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = actions.onShowDownloadSheet,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(painterResource(id = R.drawable.download_24px), contentDescription = "解析")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp,
                    bottom = (innerPadding.calculateBottomPadding() - 16.dp).coerceAtLeast(0.dp)
                )
        ) {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onAvatarClick = actions.onShowBottomSheet,
                avatarUri = state.userAvatar,
                avatarLetter = state.userName.firstOrNull()?.toString() ?: "F"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HomeMainContent(
                    parsedResults = state.parsedResults,
                    onItemClick = actions.onSelectResult
                )
            }
        }
        
        HomeBottomSheets(
            state = state,
            actions = actions,
            sheetState = sheetState
        )
    }
}
