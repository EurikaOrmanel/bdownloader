package com.lucrativeworm.bdownloader.internal

import com.lucrativeworm.bdownloader.daos.DownloadRequestDao
import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadTaskQueue(private val downloadRequestDao: DownloadRequestDao) {

    private val dbScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            println(throwable.stackTrace)
            println(throwable.message)
        })
    private val idReqTask = mutableMapOf<Long, DownloadTask>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main +
            CoroutineExceptionHandler { _, throwable ->
                println(throwable.stackTrace)
                println(throwable.message)
            })

    fun status(id: Long): Status {
        return idReqTask[id]?.downloadRequest?.status ?: Status.UNKNOWN
    }

    fun requestById(id: Long): DownloadRequest? {
        val task = idReqTask[id]
        return task?.downloadRequest ?: downloadRequestDao.byId(id)
    }

    fun fetchByIds(ids: List<Long>): List<DownloadRequest> {
        val downloadTasks = downloadRequestDao.getByIds(ids)
        val dlTasks = mutableListOf<DownloadTask>()
        for ((i, value) in downloadTasks.withIndex()) {
            val foundTask = idReqTask[value.id]
            if (foundTask != null) {
                dlTasks.add(foundTask)
            } else {
                val dlTask = DownloadTask(value, downloadRequestDao)
                dlTask.load()
                dlTasks.add(dlTask)
            }

        }
        //TOOD:change the status for a task that's not available in the req to either unknown or not ongoing
        return downloadTasks

    }

    fun getOngoingTasks(): List<DownloadTask> {
        return idReqTask.values.toList()
    }

    private suspend fun execute(task: DownloadTask) {
        withContext(Dispatchers.IO) {
            task.run()
        }
    }

    suspend fun addToQueue(req: DownloadRequest, listener: DownloadTask.Listener): Long? {
        val task = DownloadTask(req, downloadRequestDao)
        val inQueue =
            idReqTask.values.filter { current -> current.downloadRequest.filePath == req.filePath }
        if (inQueue.isNotEmpty()) {
            listener.onError("Already enqueued")
            return null
        }
        val toDB = dbScope.async {
            val existingDownloadTask = downloadRequestDao.byFileName(req.filePath)
            if (existingDownloadTask != null) {
                return@async existingDownloadTask.id
            }

            val id = downloadRequestDao.insert(req)
            println("foundReq: $id")
            return@async id
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

    fun pause(id: Long) {

        idReqTask[id]?.pause()
        idReqTask.remove(id)

    }

    fun cancelAll() {
        scope.cancel()
        idReqTask.values.forEach { eachTask ->
            eachTask.cancel()
        }
        idReqTask.clear()
        dbScope.launch {
            downloadRequestDao.deleteAll()
        }
    }

    fun cancel(id: Long) {
        val found = idReqTask[id]
        val status = found?.downloadRequest?.status ?: return
        if (status != Status.CANCELLED) {
            found.cancel()

            dbScope.launch {
                downloadRequestDao.deleteById(id)
            }
            idReqTask.remove(id)
        }

    }

    fun resume(id: Long, listener: DownloadTask.Listener) {
        val req = idReqTask[id]?.downloadRequest ?: downloadRequestDao.byId(id) ?: return
        req.status = Status.QUEUED
        CoroutineScope(Dispatchers.IO).launch {
            addToQueue(req, listener)

        }
    }


}