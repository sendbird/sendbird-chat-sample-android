package com.sendbird.chat.module.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream


object FileUtils {

    data class FileResult(val file: File, val size: Int, val mime: String?, val name: String)

    fun getFileInfo(uri: Uri, context: Context): FileResult? {
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                val mime = context.contentResolver.getType(uri)
                if (cursor != null) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        var name = cursor.getString(nameIndex)
                        val size = cursor.getLong(sizeIndex).toInt()
                        if (name.isNullOrEmpty()) {
                            name = "Temp_" + uri.hashCode() + "." + extractExtension(context, uri)
                        }
                        val file = File(context.cacheDir, name)
                        val inputPFD =
                            context.contentResolver.openFileDescriptor(uri, "r")
                        var fd: FileDescriptor? = null
                        if (inputPFD != null) {
                            fd = inputPFD.fileDescriptor
                        }
                        inputStream = FileInputStream(fd)
                        outputStream = FileOutputStream(file)
                        var read: Int
                        val bytes = ByteArray(1024)
                        while (inputStream!!.read(bytes).also { read = it } != -1) {
                            outputStream!!.write(bytes, 0, read)
                        }
                        return FileResult(file, size, mime, name)
                    }
                    return null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(e.localizedMessage, "File not found.")
            return null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        return null
    }

    private fun extractExtension(context: Context, uri: Uri): String {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val type = context.contentResolver.getType(uri)
            if (type != null) {
                extractExtension(type) ?: fileExtension(uri)
            } else {
                return fileExtension(uri)
            }
        } else {
            fileExtension(uri)
        }
    }

    private fun fileExtension(uri: Uri): String {
        val path = uri.path
        return if (path != null) {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(path)).toString()) ?: ""
        } else {
            ""
        }
    }

    private fun extractExtension(mimeType: String): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(mimeType)
    }

}