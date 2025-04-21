package com.lucrativeworm.bdownloader

import android.content.Context
import androidx.room.Room
import com.lucrativeworm.bdownloader.databases.DownloadRequestDB
import com.lucrativeworm.bdownloader.internal.DownloadTask
import com.lucrativeworm.bdownloader.internal.DownloadTaskQueue

class AllDownloaderManager(context: Context) {

    private val db = Room.databaseBuilder(context, DownloadRequestDB::class.java, "downloads")
        .allowMainThreadQueries().build()
    private val downloadTaskQueue = DownloadTaskQueue(db.downloadReqDao())

    suspend fun enqueue(req: DownloadTask, listener: DownloadTask.Listener) {
        downloadTaskQueue.addToQueue(req, listener)
    }
}