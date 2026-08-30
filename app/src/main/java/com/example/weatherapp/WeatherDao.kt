package com.example.weatherapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeatherDao {

    @Insert
    suspend fun insert(record: WeatherRecord)

    @Update
    suspend fun update(record: WeatherRecord)

    @Delete
    suspend fun delete(record: WeatherRecord)

    @Query("SELECT * FROM weather_records ORDER BY savedAt DESC")
    suspend fun getAllRecords(): List<WeatherRecord>

    @Query("SELECT * FROM weather_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Int): WeatherRecord?
}