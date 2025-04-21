package com.lucrativeworm.bdownloader

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lucrativeworm.bdownloader.internal.DownloadTask
import com.lucrativeworm.bdownloader.internal.DownloadTaskQueue
import org.junit.runner.RunWith
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.lucrativeworm.bdownloader.daos.DownloadRequestDao
import com.lucrativeworm.bdownloader.databases.DownloadRequestDB
import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class DownlaodTaskTest {
    //
    private lateinit var db: DownloadRequestDB
    private lateinit var dao: DownloadRequestDao
    private lateinit var downloadTaskQueue: DownloadTaskQueue

    @Before
    fun setupDb() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(appContext, DownloadRequestDB::class.java, "test")
            .allowMainThreadQueries().build()
        dao = db.downloadReqDao()
        downloadTaskQueue = DownloadTaskQueue(dao)
    }

    @Test
    fun testAddToQueue() = runBlocking {
        val task = DownloadTask(
            DownloadRequest(
                null,
                "",
                "",
                0L,
                0,
                Status.COMPLETED,
                0L
            )
        )
        val id = downloadTaskQueue.addToQueue(task, listener)
        assertEquals(id != null, true)
        val foundByStatus = downloadTaskQueue.status(id!!)
        assertEquals(foundByStatus, task.downloadRequest.status)

    }


    @Test
    fun testCancel() = runBlocking {
        val task = DownloadTask(
            DownloadRequest(
                null,
                "",
                "",
                0L,
                0,
                Status.COMPLETED,
                0L
            )
        )
        val id = downloadTaskQueue.addToQueue(task, listener)
        assertEquals(id != null, true)
        downloadTaskQueue.cancel(id!!)
        val foundByStatus = downloadTaskQueue.status(id)
        assertEquals(foundByStatus, Status.UNKNOWN)
    }

    @After
    fun close() {
        db.clearAllTables()
        db.close()
    }

    @Test
    fun testStatusCheck() {

    }


}