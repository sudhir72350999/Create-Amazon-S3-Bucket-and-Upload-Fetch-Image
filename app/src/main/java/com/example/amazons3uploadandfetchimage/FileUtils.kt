package com.example.amazons3uploadandfetchimage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val returnCursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        val fileName: String = returnCursor?.let {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "temp_file"

        returnCursor?.close()

        val file = File(context.cacheDir, fileName)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace() // Log the error (consider using a logging library)
            null
        }
    }
}

