package com.lucrativeworm.bdownloader.internal.stream

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class FileDownloadRandomAccessFile(file: File) {
    private val out: BufferedOutputStream
    private val fd: FileDescriptor
    private val randomAccess: RandomAccessFile = RandomAccessFile(file, "rw")

    init {
        fd = randomAccess.fd
        out = BufferedOutputStream(FileOutputStream(randomAccess.fd))
    }

    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        out.write(b, off, len)
    }

    @Throws(IOException::class)
    fun flushAndSync() {
        out.flush()
        fd.sync()
    }

    @Throws(IOException::class)
    fun close() {
        out.close()
        randomAccess.close()
    }

    @Throws(IOException::class)
    fun seek(offset: Long) {
        randomAccess.seek(offset)
    }

    @Throws(IOException::class)
    fun setLength(newLength: Long) {
        randomAccess.setLength(newLength)
    }
}