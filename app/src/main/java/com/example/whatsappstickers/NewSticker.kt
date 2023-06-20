package com.example.whatsappstickers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.IOException


@SuppressLint("Range")
@Throws(IOException::class)
fun copyStickerToInternalStorage(uriFrom: Uri, context: Context) {

    fun lastPartOfPath (uri : Uri) :String? {
        var result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            return result!!.substring(cut + 1)
        }
        else return result
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) result = lastPartOfPath (uri)

        return result
    }

    val StickerPackFolderName = lastPartOfPath(uriFrom)

    val df = DocumentFile.fromTreeUri(context, uriFrom)


    for (document in df!!.listFiles()) {
        var documentUri =  document.uri
        File(context.filesDir.path+"/"+StickerPackFolderName).mkdir()
        context.contentResolver.openInputStream(documentUri)!!.copyTo(File(context.filesDir.path+"/"+StickerPackFolderName, getFileName(documentUri)).outputStream())
    }
}