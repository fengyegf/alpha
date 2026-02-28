package com.appecho.alpha.ui.theme.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appecho.alpha.R

@Composable
fun DetailActionButtons(
    hasPlayable: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (hasPlayable) {
            Button(onClick = onDownload, modifier = Modifier.weight(1f)) {
                Icon(painterResource(R.drawable.download_24px), null)
                Text("保存到本地")
            }
        } else {
            Button(onClick = onDelete, modifier = Modifier.weight(1f)) {
                Icon(painterResource(R.drawable.delete_24px), null)
                Text("删除该解析")
            }
        }
        OutlinedButton(onClick = onCopyLink, modifier = Modifier.weight(1f)) {
            Icon(painterResource(R.drawable.content_copy_24px), null)
            Text("复制直链")
        }
    }
}
