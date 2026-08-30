package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase

    private lateinit var tvLocationName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var mainProgressBar: ProgressBar
    private lateinit var btnRefresh: Button
    private lateinit var btnSave: Button
    private lateinit var btnViewSaved: Button
    private lateinit var btnUnitToggle: Button

    private val locationPermissionRequestCode = 100

    private var currentWeather: WeatherResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = AppDatabase.getDatabase(this)

        tvLocationName = findViewById(R.id.tvLocationName)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCondition = findViewById(R.id.tvCondition)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        mainProgressBar = findViewById(R.id.mainProgressBar)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnSave = findViewById(R.id.btnSave)
        btnViewSaved = findViewById(R.id.btnViewSaved)
        btnUnitToggle = findViewById(R.id.btnUnitToggle)

        updateUnitToggleLabel()

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRefresh.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        btnSave.setOnClickListener {
            saveCurrentWeather()
        }

        btnViewSaved.setOnClickListener {
            startActivity(Intent(this, SavedRecordsActivity::class.java))
        }

        btnUnitToggle.setOnClickListener {
            val current = PreferencesManager.getTemperatureUnit(this)
            val newUnit = if (current == PreferencesManager.UNIT_CELSIUS)
                PreferencesManager.UNIT_FAHRENHEIT else PreferencesManager.UNIT_CELSIUS
            PreferencesManager.setTemperatureUnit(this, newUnit)
            updateUnitToggleLabel()
            currentWeather?.let { displayWeather(it) }
        }

        checkLocationPermissionAndFetch()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateUnitToggleLabel() {
        val current = PreferencesManager.getTemperatureUnit(this)
        btnUnitToggle.text = if (current == PreferencesManager.UNIT_CELSIUS)
            "Switch to °F" else "Switch to °C"
    }

    private fun checkLocationPermissionAndFetch() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                showError("Location permission denied. Please enable it to see local weather.")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Always request a fresh fix instead of relying on Play Services' cached
        // lastLocation, which can return a stale value (e.g. after changing the
        // emulator's mock location) and never get refreshed.
        showLoading()
        requestFreshLocation()
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation() {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                fetchWeather(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Using default test location (GPS unavailable)", Toast.LENGTH_SHORT).show()
                fetchWeather(26.85, 89.39)
            }
        }.addOnFailureListener {
            showError("Failed to get location: ${it.localizedMessage}")
        }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        showLoading()

        val apiKey = BuildConfig.OPENWEATHER_API_KEY

        if (apiKey.isBlank()) {
            showError("API key missing. Check local.properties setup.")
            return
        }

        RetrofitClient.weatherApi.getCurrentWeather(latitude, longitude, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    hideLoading()
                    if (response.isSuccessful && response.body() != null) {
                        displayWeather(response.body()!!)
                    } else {
                        showError("Failed to load weather (code ${response.code()}). Try again.")
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    hideLoading()
                    showError("No internet connection or server unreachable.")
                }
            })
    }

    private fun displayWeather(weather: WeatherResponse) {
        currentWeather = weather

        tvLocationName.text = weather.name
        tvTemperature.text = PreferencesManager.formatTemperature(this, weather.main.temp)
        tvCondition.text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        tvHumidity.text = "${weather.main.humidity}%"
        tvWindSpeed.text = "${weather.wind.speed} km/h"

        tvErrorMessage.visibility = View.GONE
    }

    private fun saveCurrentWeather() {
        val weather = currentWeather
        if (weather == null) {
            Toast.makeText(this, "No weather data to save yet.", Toast.LENGTH_SHORT).show()
            return
        }

        val record = WeatherRecord(
            locationName = weather.name,
            temperature = weather.main.temp,
            condition = weather.weather.firstOrNull()?.description ?: "Unknown",
            humidity = weather.main.humidity,
            windSpeed = weather.wind.speed
        )

        lifecycleScope.launch {
            database.weatherDao().insert(record)
            Toast.makeText(this@MainActivity, "Weather record saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading() {
        mainProgressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        btnRefresh.isEnabled = false
    }

    private fun hideLoading() {
        mainProgressBar.visibility = View.GONE
        btnRefresh.isEnabled = true
    }

    private fun showError(message: String) {
        mainProgressBar.visibility = View.GONE
        tvErrorMessage.text = message
        tvErrorMessage.visibility = View.VISIBLE
        btnRefresh.isEnabled = true
    }
}