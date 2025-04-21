package com.lucrativeworm.bdownloader.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadRequest(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    val filePath: String,
    val url: String,
    var totalBytes: Long = 0,
    var downloadedBytes: Long = 0,
    var status: Status = Status.UNKNOWN,
    val updatedAt: Long,
) {
    fun reset() {
        totalBytes = 0;
        downloadedBytes = 0;
        status = Status.UNKNOWN
    }
}
