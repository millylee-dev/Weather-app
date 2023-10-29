package com.example.weatherapp

import com.example.weatherapp.service.WeatherServiceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class ServiceTestModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }
    @Provides
    fun provideWeatherService(): WeatherServiceApi {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
            .create(WeatherServiceApi::class.java)
    }
}