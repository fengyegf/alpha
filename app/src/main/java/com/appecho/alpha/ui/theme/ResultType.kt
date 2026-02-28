package com.appecho.alpha.ui.theme

object ResultType {
    const val VIDEO = "视频"
    const val MUSIC = "音乐"
    const val GALLERY = "图集"

    fun normalize(type: String): String {
        return when (type.trim()) {
            "Video", "video", "视频" -> VIDEO
            "Audio", "audio", "音频", "音乐" -> MUSIC
            "Image", "image", "图像", "图集", "Gallery", "gallery" -> GALLERY
            else -> type.trim()
        }
    }
}

