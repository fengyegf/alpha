package com.appecho.alpha.ui.theme.home.details

import androidx.compose.runtime.Composable
import com.appecho.alpha.ui.theme.ParsedResult
import com.appecho.alpha.ui.theme.ResultType
import com.appecho.alpha.ui.theme.home.DownloadRequest

@Composable
fun ResultDetailSheet(
    result: ParsedResult,
    onDownload: (DownloadRequest) -> Unit,
    onCopyLink: (String) -> Unit,
    onDelete: () -> Unit
) {
    when (ResultType.normalize(result.type)) {
        ResultType.MUSIC -> MusicDetailSheet(
            result = result,
            onDownload = onDownload,
            onCopyLink = onCopyLink,
            onDelete = onDelete
        )
        ResultType.GALLERY -> GalleryDetailSheet(
            result = result,
            onDownload = onDownload,
            onCopyLink = onCopyLink,
            onDelete = onDelete
        )
        else -> VideoDetailSheet(
            result = result,
            onDownload = onDownload,
            onCopyLink = onCopyLink,
            onDelete = onDelete
        )
    }
}

