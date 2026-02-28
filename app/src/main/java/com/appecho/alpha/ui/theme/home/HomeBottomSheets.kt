package com.appecho.alpha.ui.theme.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.appecho.alpha.ui.theme.ProfileListItem.ProfileSheetContent
import com.appecho.alpha.ui.theme.home.details.ResultDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomSheets(
    state: HomeState,
    actions: HomeActions,
    sheetState: SheetState
) {
    if (state.showDownloadSheet) {
        ModalBottomSheet(onDismissRequest = actions.onDismissDownloadSheet) {
            DownloadSheetContent(
                onDismiss = actions.onDismissDownloadSheet,
                onAnalyze = actions.onAnalyze,
                selectedType = state.selectedAnalyzeType,
                onTypeChange = actions.onTypeChange
            )
        }
    }

    val selectedResult = state.selectedResult
    if (selectedResult != null) {
        ModalBottomSheet(onDismissRequest = actions.onClearSelectedResult) {
            ResultDetailSheet(
                result = selectedResult,
                onDownload = actions.onDownload,
                onCopyLink = actions.onCopyLink,
                onDelete = {
                    actions.onDeleteResult(selectedResult)
                    actions.onClearSelectedResult()
                }
            )
        }
    }

    if (state.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = actions.onDismissBottomSheet,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets.ime }
        ) {
            ProfileSheetContent()
        }
    }
}
