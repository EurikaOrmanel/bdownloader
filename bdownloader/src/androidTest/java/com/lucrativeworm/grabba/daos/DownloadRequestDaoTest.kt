package com.lucrativeworm.bdownloader.daos

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lucrativeworm.bdownloader.databases.DownloadRequestDB
import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset


@RunWith(AndroidJUnit4::class)
class DownloadRequestDaoTest {
    lateinit var db: DownloadRequestDB
    lateinit var dao: DownloadRequestDao

    @Before
    fun setupDb() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(appContext, DownloadRequestDB::class.java, "test")
            .allowMainThreadQueries().build()
        dao = db.downloadReqDao()
    }


    @Test
    fun insertRequest() {
        val downloadReq = DownloadRequest(
            url = "https://download.url",
            filePath = "kofi",
            downloadedBytes = 0L,
            id = 1,
            status = Status.UNKNOWN,
            updatedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )
        dao.insert(downloadReq)
        val fetched = dao.byId(1)
        assertEquals(downloadReq.url, fetched.url)
    }
}