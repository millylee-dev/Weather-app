package com.example.weatherapp.service

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.WeatherFormat
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherServiceApi {

    @GET("weather?&units=metric")
    fun getCityWeather(
        @Query("q") cityName: String,
        @Query("appid") appid: String = BuildConfig.API_KEY
    ): Call<WeatherFormat>
}