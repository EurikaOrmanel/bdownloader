package com.lucrativeworm.bdownloader.utils

import com.lucrativeworm.bdownloader.models.DownloadRequest
import java.io.File
import java.io.IOException

object FileUtils {

    fun getTempPath(filePath: String): String {
        return "$filePath.temp"
    }

    fun getTempFile(filePath: String): File = File(getTempPath(filePath))
    fun renameFileName(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        try {
            val newFile = File(newPath)
            if (newFile.exists()) {
                if (!newFile.delete()) {
                    throw IOException("Deletion Failed")
                }
            }
            if (!oldFile.renameTo(newFile)) {
                throw IOException("Rename Failed")
            }
        } finally {
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }
    }


}
