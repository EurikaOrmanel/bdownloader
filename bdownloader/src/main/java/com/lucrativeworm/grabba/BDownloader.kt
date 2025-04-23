package com.lucrativeworm.bdownloader

import android.content.Context
import androidx.room.Room
import com.lucrativeworm.bdownloader.databases.DownloadRequestDB
import com.lucrativeworm.bdownloader.internal.DownloadTask
import com.lucrativeworm.bdownloader.internal.DownloadTaskQueue
import com.lucrativeworm.bdownloader.models.DownloadRequest

class AllDownloaderManager(context: Context) {

    fun tasksById(ids: List<Long>): List<DownloadRequest> {
        return downloadTaskQueue.fetchByIds(ids)
    }

    private val db = Room.databaseBuilder(context, DownloadRequestDB::class.java, "downloads")
        .allowMainThreadQueries().build()
    private val downloadTaskQueue = DownloadTaskQueue(db.downloadReqDao())

    suspend fun enqueue(req: DownloadRequest, listener: DownloadTask.Listener) =
        downloadTaskQueue.addToQueue(req, listener)

    fun pause(id: Long) {
        downloadTaskQueue.pause(id)
    }

    fun resume(id: Long, listener: DownloadTask.Listener) {
        downloadTaskQueue.resume(id, listener)


    }

    fun byId(id: Long) {
        downloadTaskQueue.requestById(id)
    }

    fun cancel(id: Long) {
        downloadTaskQueue.cancel(id)
    }

    fun cancelAll() {
        downloadTaskQueue.cancelAll()
    }
}