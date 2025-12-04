package com.example.weatherapp

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.*

class LocationViewModel : ViewModel() {
    // Location properties
    var currentCity by mutableStateOf("")
        private set

    var latitude by mutableDoubleStateOf(0.0)
        private set

    var longitude by mutableDoubleStateOf(0.0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    // Weather properties
    var weatherData by mutableStateOf<WeatherResponse?>(null)
        private set

    var isLoadingWeather by mutableStateOf(false)
        private set

    var weatherError by mutableStateOf("")
        private set

    private val API_KEY = ""
    private val weatherApi = WeatherApiService.create()

    suspend fun getCurrentLocation(context: Context) {
        try {
            isLoading = true
            errorMessage = ""

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(
                    latitude, longitude, 1
                )

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    currentCity = address.locality
                        ?: address.subAdminArea
                                ?: address.adminArea
                                ?: "Unknown Location"
                } else {
                    currentCity = "Unknown Location"
                    errorMessage = "Could not determine city name"
                }

                getWeatherData(context)

            } else {
                errorMessage = "Could not get location"
            }
        } catch (e: SecurityException) {
            errorMessage = "Location permission denied"
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    private suspend fun getWeatherData(context: Context) {
        try {
            isLoadingWeather = true
            weatherError = ""

            if (API_KEY == "YOUR_API_KEY_HERE") {
                weatherError = "Please add your OpenWeatherMap API key"
                return
            }
            val langCode = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("lang", "en") ?: "en"
            val weather = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = API_KEY,
                lang = langCode
            )

            weatherData = weather

        } catch (e: Exception) {
            weatherError = "Failed to get weather: ${e.message}"
        } finally {
            isLoadingWeather = false
        }
    }

    suspend fun refreshWeather(context: Context) {
        if (latitude != 0.0 && longitude != 0.0) {
            getWeatherData(context)
        }
    }
}
