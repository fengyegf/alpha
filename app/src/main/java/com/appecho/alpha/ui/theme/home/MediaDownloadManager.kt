package com.appecho.alpha.ui.theme.home

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object MediaDownloadManager {

    suspend fun downloadMedia(
        context: Context,
        url: String,
        fileName: String,
        coverUrl: String? = null,
        onProgress: ((Int) -> Unit)? = null
    ): Uri? = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val contentType = connection.contentType ?: "application/octet-stream"
            val contentLength = connection.contentLength
            val isAudio = contentType.startsWith("audio/")

            val tableUri = when {
                contentType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                contentType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                contentType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }

            val relativePath = when {
                contentType.startsWith("video/") -> "${Environment.DIRECTORY_MOVIES}/Alpha/Video"
                contentType.startsWith("audio/") -> "${Environment.DIRECTORY_MUSIC}/Alpha/Audio"
                contentType.startsWith("image/") -> "${Environment.DIRECTORY_PICTURES}/Alpha/Image"
                else -> "${Environment.DIRECTORY_DOWNLOADS}/Alpha/Other"
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.TITLE, fileName.substringBeforeLast("."))
                put(MediaStore.MediaColumns.MIME_TYPE, contentType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val parsedUri =
                context.contentResolver.insert(tableUri, values) ?: return@withContext null

            inputStream = connection.inputStream
            outputStream =
                context.contentResolver.openOutputStream(parsedUri) ?: return@withContext null

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (contentLength > 0) {
                    val progress = (totalBytesRead * 100 / contentLength).toInt()
                    onProgress?.invoke(progress)
                }
            }
            outputStream.flush()


            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            parsedUri.let { uri ->
                context.contentResolver.update(uri, values, null, null)
            }

            if (isAudio && !coverUrl.isNullOrBlank()) {
                val audioName = fileName.substringBeforeLast(".", fileName)
                saveAudioCoverImage(context, coverUrl, audioName)
            }


            return@withContext parsedUri

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun saveAudioCoverImage(context: Context, coverUrl: String, audioName: String): Uri? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var connection: HttpURLConnection? = null
        var imageUri: Uri? = null

        return try {
            connection = URL(coverUrl).openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null

            val mimeType = connection.contentType?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("gif") -> "gif"
                else -> "jpg"
            }
            val imageDisplayName = "$audioName.$extension"
            deleteExistingCover(context, imageDisplayName)

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageDisplayName)
                put(MediaStore.MediaColumns.TITLE, audioName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Alpha/AudioCover")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null

            inputStream = connection.inputStream
            outputStream = context.contentResolver.openOutputStream(imageUri ?: return null) ?: return null

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()

            val doneValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            context.contentResolver.update(imageUri ?: return null, doneValues, null, null)
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            imageUri?.let { context.contentResolver.delete(it, null, null) }
            null
        } finally {
            inputStream?.close()
            outputStream?.close()
            connection?.disconnect()
        }
    }

    private fun deleteExistingCover(context: Context, displayName: String) {
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("${Environment.DIRECTORY_PICTURES}/Alpha/AudioCover/%", displayName)
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                context.contentResolver.delete(uri, null, null)
            }
        }
    }
}
