package com.lucrativeworm.bdownloader.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class DownloadTaskQueueTest {

    private lateinit var downloadRequest: DownloadRequest

    @Before
    fun setupDownloadRequest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        downloadRequest = DownloadRequest(
            1,
            appContext.filesDir.absolutePath,
            "https://www.rd.usda.gov/sites/default/files/pdf-sample_0.pdf",
            0,
            0,
            Status.UNKNOWN,
            updatedAt = 0
        )
    }

    @Test
    fun testDownloadTaskRun() = runBlocking {
        val downloadTask = DownloadTask(downloadRequest)
        var progress = 0
        var errorMsg = "";
        val completed = CompletableDeferred<Unit>()

        downloadTask.run(object : DownloadTask.Listener {
            override fun onCompleted() {
                completed.complete(Unit)
            }

            override fun onProgress(value: Int) {
                progress = value
            }

            override fun onError(error: String) {
                errorMsg = error
            }

            override fun onPause() {}
            override fun onStart() {}
        })

        // Suspend until download completes
        completed.await()

        assertEquals(100, progress)
        assertEquals("", errorMsg)
    }
}
