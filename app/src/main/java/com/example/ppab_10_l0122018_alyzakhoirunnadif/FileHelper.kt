package com.example.ppab_10_l0122018_alyzakhoirunnadif

import android.content.Context
import android.os.Environment
import java.io.File

internal object FileHelper {
    fun readFromFile(context: Context, filename: String): FileModel {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, filename)
        val fileModel = FileModel()
        fileModel.filename = filename
        if (file.exists()) {
            fileModel.data = file.readText()
        } else {
            fileModel.data = ""
        }
        return fileModel
    }

    fun prependToFile(context: Context, fileModel: FileModel) {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = fileModel.filename?.let { File(directory, it) }
        val existingText = if (file?.exists() == true) file.readText() else ""
        file?.writeText((fileModel.data ?: "") + existingText)
    }
}