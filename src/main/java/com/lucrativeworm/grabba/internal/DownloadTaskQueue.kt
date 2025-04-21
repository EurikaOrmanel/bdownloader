package com.lucrativeworm.bdownloader.internal

import com.lucrativeworm.bdownloader.daos.DownloadRequestDao
import com.lucrativeworm.bdownloader.internal.stream.FileDownloadRandomAccessFile
import com.lucrativeworm.bdownloader.models.Status
import com.lucrativeworm.bdownloader.utils.FileUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class DownloadTaskQueue(private val downloadRequestDao: DownloadRequestDao) {
    fun init() {
        dbScope.launch {
            //TODO: load all existing tasks from database insto list.
        }
    }

    private val dbScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            println(throwable.stackTrace)
        })
    private val idReqTask = mutableMapOf<Int, DownloadTask>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main +
            CoroutineExceptionHandler { _, throwable ->
                println(throwable.stackTrace)
            })

    fun status(id: Int): Status {
        return idReqTask[id]?.downloadRequest?.status ?: Status.UNKNOWN
    }

    fun getTasks(): List<DownloadTask> {
        return idReqTask.values.toList()
    }

    private suspend fun execute(task: DownloadTask) {
        withContext(Dispatchers.IO) {
            task.run()
        }
    }

    suspend fun addToQueue(task: DownloadTask, listener: DownloadTask.Listener): Int? {
        val downloadRequest = task.downloadRequest
        val toDB = dbScope.async {
            val existingDownloadTask = downloadRequestDao.byFileName(downloadRequest.url)
            if (existingDownloadTask != null) {
                listener.onError("Download already exists")
                return@async null
            }
            downloadRequestDao.insert(task.downloadRequest)
            val foundReq = downloadRequestDao.byUrl(downloadRequest.url)
            foundReq?.id
        }

        val id = toDB.await() ?: return null


        val job = scope.launch {
            task.listener = listener
            execute(task)
        }
        task.job = job
        idReqTask[id] = task
        return id
    }

    fun cancelAll() {
        scope.cancel()
        idReqTask.clear()
        dbScope.launch {
            downloadRequestDao.deleteAll()
        }
    }

    fun cancel(id: Int) {
        val found = idReqTask[id]
        val status = found?.downloadRequest?.status ?: return
        if (status != Status.CANCELLED) {
            found.job.cancel()
            dbScope.launch {
                downloadRequestDao.deleteById(id)
            }
            idReqTask.remove(id)
        }

    }


    private suspend fun sync(outputStream: FileDownloadRandomAccessFile) {
        var success: Boolean
        try {
            outputStream.flushAndSync()
            success = true
        } catch (e: IOException) {
            success = false
            e.printStackTrace()
        }
//        if (success && isResumeSupported) {
//            dbHelper
//                .updateProgress(
//                    req.downloadId,
//                    req.downloadedBytes,
//                    System.currentTimeMillis()
//                )
//        }
    }

    fun clearTempFile(task: DownloadTask) {
        val tempPath = FileUtils.getTempPath(task.downloadRequest.filePath)
        val file = File(tempPath)
        if (file.exists()) {
            file.delete()
        }
        task.reset()
    }

    private fun executeOnMainThread(block: () -> Unit) {
        scope.launch {
            block()
        }
    }

    private fun downloadFile(task: DownloadTask) {

    }
}