package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {

    private const val PREFS_NAME = "weather_app_prefs"
    private const val KEY_TEMP_UNIT = "temp_unit"

    const val UNIT_CELSIUS = "C"
    const val UNIT_FAHRENHEIT = "F"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTemperatureUnit(context: Context): String {
        return prefs(context).getString(KEY_TEMP_UNIT, UNIT_CELSIUS) ?: UNIT_CELSIUS
    }

    fun setTemperatureUnit(context: Context, unit: String) {
        prefs(context).edit().putString(KEY_TEMP_UNIT, unit).apply()
    }

    fun formatTemperature(context: Context, celsius: Double): String {
        val unit = getTemperatureUnit(context)
        return if (unit == UNIT_FAHRENHEIT) {
            val fahrenheit = celsius * 9.0 / 5.0 + 32.0
            "${fahrenheit.toInt()}°F"
        } else {
            "${celsius.toInt()}°C"
        }
    }
}