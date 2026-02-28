package com.appecho.alpha.ui.theme.category

import android.net.Uri
import android.provider.MediaStore
import com.appecho.alpha.R

data class CategorySpec(
    val title: String,
    val iconRes: Int,
    val mimeType: String,
    val collectionUri: Uri,
    val relativePathPrefix: String,
    val isAudio: Boolean = false
)

object CategoryMappings {
    val specs: List<CategorySpec> = listOf(
        CategorySpec(
            title = "视频",
            iconRes = R.drawable.videocam_24px,
            mimeType = "video/*",
            collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            relativePathPrefix = "Movies/Alpha/Video/"
        ),
        CategorySpec(
            title = "图像",
            iconRes = R.drawable.photo_24px,
            mimeType = "image/*",
            collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            relativePathPrefix = "Pictures/Alpha/Image/"
        ),
        CategorySpec(
            title = "音频",
            iconRes = R.drawable.audio_file_24px,
            mimeType = "audio/*",
            collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            relativePathPrefix = "Music/Alpha/Audio/",
            isAudio = true
        )
    )

    private val byTitle: Map<String, CategorySpec> = specs.associateBy { it.title }

    fun find(title: String): CategorySpec? = byTitle[title]
}
