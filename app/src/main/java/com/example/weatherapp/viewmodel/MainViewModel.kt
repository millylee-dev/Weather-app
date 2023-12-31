package com.example.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.WeatherFormat
import com.example.weatherapp.service.WeatherRepository
import com.example.weatherapp.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val connectivityHelper: ConnectivityHelper
) : ViewModel() {

    val viewLiveData = MutableLiveData<WeatherFormat>()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isLoadFailed = MutableLiveData<Boolean>(false)
    val isLoadFailed: LiveData<Boolean>
        get() = _isLoadFailed

    private val _cityName = MutableLiveData<String>()
    val cityName: LiveData<String>
        get() = _cityName

    private val _cityCode = MutableLiveData<String>()
    val cityCode: LiveData<String>
        get() = _cityCode

    private val _degree = MutableLiveData<String>()
    val degree: LiveData<String>
        get() = _degree

    private val _degreeFeels = MutableLiveData<String>()
    val degreeFeels: LiveData<String>
        get() = _degreeFeels

    private val _humidity = MutableLiveData<String>()
    val humidity: LiveData<String>
        get() = _humidity

    private val _windSpeed = MutableLiveData<String>()
    val windSpeed: LiveData<String>
        get() = _windSpeed

    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    private val _type = MutableLiveData<String>()
    val type: LiveData<String>
        get() = _type

    fun refreshData(cityName: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        if (connectivityHelper.isNetworkAvailable()) {
            val response = weatherRepository.getDataService(cityName).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    viewLiveData.postValue(it)
                    _cityName.postValue(it.name)
                    _cityCode.postValue(it.sys.country)
                    _degree.postValue(it.main.temp.toString() + "°C")
                    _degreeFeels.postValue( it.main.feels_like.toString() + "°C")
                    _humidity.postValue(it.main.humidity.toString())
                    _windSpeed.postValue(it.wind.speed.toString())
                    _description.postValue(it.weather.getOrNull(0)?.description)
                    _type.postValue(it.weather.getOrNull(0)?.main)
                }

                _isLoadFailed.postValue(false)
                _isLoading.postValue(false)
            } else {
                _isLoadFailed.postValue(true)
                _isLoading.postValue(false)
            }
        } else {
            _isLoadFailed.postValue(true)
            _isLoading.postValue(false)
        }
    }
}