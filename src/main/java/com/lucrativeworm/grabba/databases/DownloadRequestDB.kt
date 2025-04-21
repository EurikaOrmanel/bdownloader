package com.lucrativeworm.bdownloader.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lucrativeworm.bdownloader.daos.DownloadRequestDao
import com.lucrativeworm.bdownloader.models.DownloadRequest

@Database(entities = [DownloadRequest::class], version = 1)
abstract class DownloadRequestDB : RoomDatabase() {
    abstract fun downloadReqDao(): DownloadRequestDao
}