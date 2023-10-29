package com.example.weatherapp

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.weatherapp.utils.Utils.LOCATION_PERMISSION_REQUEST_CODE
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.utils.PermissionHelper
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import androidx.activity.viewModels
import com.example.weatherapp.utils.ConnectivityHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var GET:SharedPreferences
    private lateinit var SET:SharedPreferences.Editor
    @Inject
    lateinit var connectivityHelper: ConnectivityHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )

        binding.apply {
            lifecycleOwner = this@MainActivity
            viewModel = mainViewModel
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()
        checkPermission()
        getLiveData()
        initViews()
    }

    private fun checkPermission() {
        when {
            PermissionHelper.isFineLocationPermissionGranted(this) -> {
                when {
                    PermissionHelper.isLocationEnabled(this) -> {
                        if(connectivityHelper.isNetworkAvailable())
                            setUpLocationListener()
                    }
                }
            }
            else -> {
                PermissionHelper.requestFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, request location updates
                    when {
                        PermissionHelper.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                    }
                } else {
                    val cityName = GET.getString(Utils.CITY_NAME, Utils.DEFAULT_CITY)?.lowercase()
                    if(cityName.isNullOrEmpty()) {
                        mainViewModel.refreshData(Utils.DEFAULT_CITY)
                    } else {
                        mainViewModel.refreshData(cityName)
                    }
                }
            }
        }
    }

    private fun setUpLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val cityName = GET.getString(Utils.CITY_NAME, "")?.lowercase()
        if(!cityName.isNullOrEmpty()) {
            mainViewModel.refreshData(cityName)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    val cityName = addresses?.get(0)?.locality
                    if (cityName != null) {
                        mainViewModel.refreshData(cityName)
                        writePrefLocation(cityName)
                    } else {
                        mainViewModel.refreshData(readPrefLocation())
                    }
                }
            }
    }

    private fun initViews(){
        val cityName = readPrefLocation()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.weatherData.visibility = View.GONE
            binding.errorText.visibility = View.GONE
            binding.progressLoading.visibility = View.GONE

            binding.edtCityName.setText(cityName)
            mainViewModel.refreshData(cityName)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.imgSearchCity.setOnClickListener{
            val newCityName = binding.edtCityName.text.toString()
            writePrefLocation(newCityName)
            mainViewModel.refreshData(newCityName)
        }
    }

    private fun getLiveData() {
        mainViewModel.viewLiveData.observe(this, Observer { data ->
           data?.let{
               Glide.with(this)
                   .load( Utils.BASE_URL_IMG + data.weather[0].icon + Utils.BASE_URL_IMG_END)
                   .into(binding.weatherImg)
           }
        })
    }

    private fun readPrefLocation():String {
        val cityName = GET.getString(Utils.CITY_NAME, Utils.DEFAULT_CITY)?.lowercase()
        if(cityName.isNullOrEmpty()) return Utils.DEFAULT_CITY
        return cityName
    }

    private fun writePrefLocation(name:String) {
        SET.putString(Utils.CITY_NAME, name)
        SET.apply()
    }
}