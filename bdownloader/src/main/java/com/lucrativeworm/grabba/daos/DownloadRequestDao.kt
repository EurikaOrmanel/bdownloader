package com.lucrativeworm.bdownloader.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lucrativeworm.bdownloader.models.DownloadRequest

@Dao
interface DownloadRequestDao {
    @Query("SELECT * FROM  downloadrequest")
    fun getAll(): List<DownloadRequest>

    @Query("SELECT * FROM downloadrequest WHERE id= :id ")
    fun byId(id: Long): DownloadRequest?

    @Delete
    fun delete(downloadRequest: DownloadRequest)

    @Query("SELECT * FROM downloadrequest WHERE id IN (:ids)")
    fun getByIds(ids: List<Long>): List<DownloadRequest>

    @Query("DELETE FROM downloadrequest")
    fun deleteAll()

    @Query("DELETE FROM downloadrequest WHERE id= :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM downloadrequest WHERE url= :url")
    fun byLink(url: String): DownloadRequest?

    @Query("SELECT * FROM downloadrequest WHERE filePath= :name")
    fun byFileName(name: String): DownloadRequest?

    @Query("SELECT * FROM downloadrequest WHERE filePath= :url")
    fun byUrl(url: String): DownloadRequest?

    @Insert
    fun insert(downloadRequest: DownloadRequest): Long

    @Update
    fun update(downloadRequest: DownloadRequest)
}