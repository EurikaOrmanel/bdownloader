package com.lucrativeworm.bdownloader

import android.content.Context
import androidx.room.Room
import com.lucrativeworm.bdownloader.databases.DownloadRequestDB
import com.lucrativeworm.bdownloader.internal.DownloadTask
import com.lucrativeworm.bdownloader.internal.DownloadTaskQueue
import com.lucrativeworm.bdownloader.models.DownloadRequest

class AllDownloaderManager(context: Context) {

    fun tasksById(ids: List<Long>): List<DownloadRequest> {
        return downloadTaskQueue.fetchbyIds(ids)
    }

    private val db = Room.databaseBuilder(context, DownloadRequestDB::class.java, "downloads")
        .allowMainThreadQueries().build()
    private val downloadTaskQueue = DownloadTaskQueue(db.downloadReqDao())

    suspend fun enqueue(req: DownloadTask, listener: DownloadTask.Listener) {
        downloadTaskQueue.addToQueue(req, listener)
    }
}