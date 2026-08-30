package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.app.AlertDialog

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var recordId: Int = -1

    private lateinit var etLocation: EditText
    private lateinit var etTemperature: EditText
    private lateinit var etCondition: EditText
    private lateinit var etHumidity: EditText
    private lateinit var etWindSpeed: EditText

    companion object {
        const val EXTRA_RECORD_ID = "extra_record_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        database = AppDatabase.getDatabase(this)
        recordId = intent.getIntExtra(EXTRA_RECORD_ID, -1)

        etLocation = findViewById(R.id.etDetailLocation)
        etTemperature = findViewById(R.id.etDetailTemperature)
        etCondition = findViewById(R.id.etDetailCondition)
        etHumidity = findViewById(R.id.etDetailHumidity)
        etWindSpeed = findViewById(R.id.etDetailWindSpeed)

        if (recordId == -1) {
            Toast.makeText(this, "Invalid record.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadRecord()

        findViewById<Button>(R.id.btnUpdateRecord).setOnClickListener {
            updateRecord()
        }

        findViewById<Button>(R.id.btnDeleteRecord).setOnClickListener {
            deleteRecord()
        }

        findViewById<Button>(R.id.btnShareRecord).setOnClickListener {
            shareRecord()
        }
    }

    private fun loadRecord() {
        lifecycleScope.launch {
            val record = database.weatherDao().getRecordById(recordId)
            if (record != null) {
                etLocation.setText(record.locationName)
                etTemperature.setText(record.temperature.toString())
                etCondition.setText(record.condition)
                etHumidity.setText(record.humidity.toString())
                etWindSpeed.setText(record.windSpeed.toString())
            } else {
                Toast.makeText(this@RecordDetailActivity, "Record not found.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateRecord() {
        val location = etLocation.text.toString().trim()
        val tempText = etTemperature.text.toString().trim()
        val condition = etCondition.text.toString().trim()
        val humidityText = etHumidity.text.toString().trim()
        val windText = etWindSpeed.text.toString().trim()

        if (location.isEmpty() || tempText.isEmpty() || condition.isEmpty() || humidityText.isEmpty() || windText.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val temperature = tempText.toDoubleOrNull()
        val humidity = humidityText.toIntOrNull()
        val windSpeed = windText.toDoubleOrNull()

        if (temperature == null || humidity == null || windSpeed == null) {
            Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedRecord = WeatherRecord(
            id = recordId,
            locationName = location,
            temperature = temperature,
            condition = condition,
            humidity = humidity,
            windSpeed = windSpeed
        )

        lifecycleScope.launch {
            database.weatherDao().update(updatedRecord)
            Toast.makeText(this@RecordDetailActivity, "Record updated.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteRecord() {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this weather record? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val record = database.weatherDao().getRecordById(recordId)
                    if (record != null) {
                        database.weatherDao().delete(record)
                        Toast.makeText(this@RecordDetailActivity, "Record deleted.", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareRecord() {
        val text = "Weather in ${etLocation.text}: ${etTemperature.text}°C, " +
                "${etCondition.text}, Humidity: ${etHumidity.text}%, Wind: ${etWindSpeed.text} km/h"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "Share weather via"))
    }
}