package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_records")
data class WeatherRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val locationName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val savedAt: Long = System.currentTimeMillis()
)