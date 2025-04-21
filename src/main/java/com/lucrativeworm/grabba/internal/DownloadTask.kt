package com.lucrativeworm.bdownloader.internal

import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import com.lucrativeworm.bdownloader.utils.FileUtils
import kotlinx.coroutines.*
import okhttp3.*
import okio.appendingSink
import okio.buffer
import java.io.File
import java.io.IOException

class DownloadTask(
    val downloadRequest: DownloadRequest
) {

    companion object {
        private const val TIME_GAP_FOR_SYNC: Long = 2000
        private const val MIN_BYTES_FOR_SYNC: Long = 65536
        private const val BUFFER_SIZE = 1024 * 4
    }

    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    internal lateinit var job: Job
    internal lateinit var listener: Listener
    private val client = OkHttpClient()
    private val tempPath = FileUtils.getTempPath(downloadRequest.filePath)
    private var tempFile = FileUtils.getTempFile(downloadRequest.filePath)
    private val file = File(downloadRequest.filePath)

    suspend fun run() = withContext(Dispatchers.IO) {
        if (file.exists()) {
            listener.onError("File already exists")
            return@withContext
        }

        if (file.exists() && tempFile.length() <= file.length()) {
            if (!deleteTempFile(tempFile)) {
                tempFile = File(tempPath.split(".")[0] + "2." + tempFile.extension)
                tempFile.createNewFile()
            }
        }

        val totalContentLength = getContentLength(downloadRequest.url)
        if (totalContentLength > 0L) {
            chunkDownload(totalContentLength)
        } else {
            downloadFileChunk(downloadRequest.url, 0L, null, tempFile)
        }

        listener.onCompleted()
        tempFile.renameTo(file)
    }

    private suspend fun chunkDownload(fileSize: Long) = coroutineScope {
        val chunkTempFiles = mutableListOf<File>()
        listener.onStart()
        downloadRequest.status = Status.RUNNING

        val downloadJobs = mutableListOf<Deferred<Unit>>()
        val connectionsCount = genConnectionCount(fileSize)
        val bytesPerConnection = fileSize / connectionsCount

        for (i in 0 until connectionsCount) {
            val chunkStart = i * bytesPerConnection
            val chunkEnd = if (i + 1 == connectionsCount) null else (i + 1) * bytesPerConnection - 1
            val chunkFile = File(tempFile.absolutePath + "_${i + 1}")
            if (!chunkFile.exists()) chunkFile.createNewFile()
            chunkTempFiles.add(chunkFile)

            val job = async {
                downloadFileChunk(downloadRequest.url, chunkStart, chunkEnd, chunkFile)
            }
            downloadJobs.add(job)
        }

        downloadJobs.awaitAll()
        mergeChunksIntoFinalFile(chunkTempFiles, file)
    }

    private fun mergeChunksIntoFinalFile(tempChunkFiles: List<File>, outputFile: File) {
        if (!outputFile.exists()) outputFile.createNewFile()

        try {
            outputFile.outputStream().use { output ->
                tempChunkFiles.sortedBy { it.name }.forEach { chunk ->
                    chunk.inputStream().use { it.copyTo(output) }

                    deleteTempFile(chunk)
                }
            }

            tempChunkFiles.forEach { }
        } catch (e: Exception) {
            println(e)
            listener.onError("Failed to compile downloaded files into one.")
        }
    }

    private fun genConnectionCount(fileSize: Long): Int = when {
        fileSize < 5_000_000L -> 1
        fileSize < 50_000_000L -> 2
        else -> 4
    }

    private fun getContentLength(url: String): Long {
        val request = Request.Builder().url(url).head().build()
        return try {
            val response = client.newCall(request).execute()
            response.headers["content-length"]?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private suspend fun downloadFileChunk(url: String, start: Long, end: Long?, chunkFile: File) =
        suspendCancellableCoroutine<Unit> { cont ->
            val existingSize = chunkFile.length()
            val actualStart = start + existingSize
            val rangeHeader = if (end != null) "bytes=$actualStart-$end" else "bytes=$actualStart-"

            val request = Request.Builder().url(url).addHeader("Range", rangeHeader).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWith(Result.failure(e))
                    listener.onError("Failed to download chunk: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful || response.body == null) {
                        cont.resumeWith(Result.failure(IOException("Unsuccessful response")))
                        return
                    }

                    response.body!!.source().use { source ->
                        chunkFile.appendingSink().buffer().use { sink ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            while (true) {
                                val bytesRead = source.inputStream().read(buffer)
                                if (bytesRead == -1) break

                                if (downloadRequest.status == Status.CANCELLED) {
                                    listener.onError("Download cancelled")
                                    call.cancel()
                                    cont.resumeWith(Result.failure(CancellationException("Download cancelled")))
                                    return
                                } else if (downloadRequest.status == Status.PAUSED) {
                                    listener.onPause()
                                    sink.flush()
                                    return
                                }

                                sink.outputStream().write(buffer, 0, bytesRead)
                            }
                            sink.flush()
                        }
                    }

                    cont.resumeWith(Result.success(Unit))
                }
            })
        }

    private fun deleteTempFile(tempFile: File): Boolean =
        tempFile.takeIf { it.exists() }?.delete() ?: false

    fun reset() {
        downloadRequest.reset()
    }

    interface Listener {
        fun onStart()
        fun onProgress(value: Int)
        fun onPause()
        fun onCompleted()
        fun onError(error: String)
    }
}