package com.example.weatherapp.service

import com.example.weatherapp.data.WeatherFormat
import retrofit2.Call
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: WeatherServiceApi
) {
    fun getDataService(cityName:String): Call<WeatherFormat> {
        return service.getCityWeather(cityName)
    }
}