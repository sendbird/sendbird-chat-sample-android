package com.sendbird.chat.sample.groupchannel.basic.util

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream


object FileUtils {

    fun selectFile(
        type: String,
        startForResult: ActivityResultLauncher<Intent>,
        context: Context
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestStoragePermission(context)
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, type)
            startForResult.launch(intent)
        }
    }

    fun selectFile(startForResult: ActivityResultLauncher<Intent>, context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestStoragePermission(context)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startForResult.launch(intent)
        }
    }

    private fun requestStoragePermission(context: Context) {
        context.showAlertDialog(
            "Permission",
            "Storage access permissions are required to upload/download files.",
            "Accept",
            "Cancel",
            {
                requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    10001
                )
            },
            {}
        )
    }

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