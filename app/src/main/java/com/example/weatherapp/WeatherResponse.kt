package com.example.weatherapp

data class WeatherResponse(
    val name: String,
    val main: MainInfo,
    val weather: List<WeatherInfo>,
    val wind: WindInfo
)

data class MainInfo(
    val temp: Double,
    val humidity: Int
)

data class WeatherInfo(
    val main: String,
    val description: String
)

data class WindInfo(
    val speed: Double
)