package com.example.weatherapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.data.Clouds
import com.example.weatherapp.data.Coord
import com.example.weatherapp.data.Main
import com.example.weatherapp.data.Sys
import com.example.weatherapp.data.Weather
import com.example.weatherapp.data.WeatherFormat
import com.example.weatherapp.data.Wind
import com.example.weatherapp.service.WeatherRepository
import com.example.weatherapp.utils.ConnectivityHelper
import com.example.weatherapp.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Response

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel

    @Mock
    private lateinit var weatherRepository: WeatherRepository

    @Mock
    private lateinit var connectivityHelper: ConnectivityHelper

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(weatherRepository, connectivityHelper)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `refreshData() when network is available`() = runBlockingTest {
        `when`(connectivityHelper.isNetworkAvailable()).thenReturn(true)
        val cityName = "London"

        val weatherFormat = WeatherFormat(
           base = "station",
            clouds = Clouds(all = 67),
            cod = 200,
            coord = Coord(lat = 51.5085, lon = -0.1257),
            dt = 1698440158,
            id = 803,
            main = Main(feels_like = 280.86,
                grnd_level = 987,
                humidity = 92,
                pressure = 990,
                sea_level = 990,
                temp = 282.31,
                temp_max = 280.62,
                temp_min = 283.68
                ),
            name = "London",
            sys = Sys(country = "GB",
                id = 2006068,
                sunrise = 1698389059,
                sunset = 1698425056,
                type = 2),
            timezone = 3600,
            visibility = 10000,
            weather = listOf(
                Weather(
                    id = 803,
                    main = "Clouds",
                    description = "broken clouds",
                    icon = "04n"
                )
            ),
            wind = Wind(speed = 2.67, deg = 181, gust = 9.31)
        )

        // Assuming you have a WeatherRepository class that makes network calls
        val mockCall = mock(Call::class.java) as Call<WeatherFormat>
        `when`(weatherRepository.getDataService(cityName)).thenReturn(mockCall)

        // Assuming you want to return a successful response with your mock data
        val mockResponse = Response.success(weatherFormat)
        `when`(mockCall.execute()).thenReturn(mockResponse)

        viewModel.refreshData(cityName)

        verify(connectivityHelper).isNetworkAvailable()
        verify(weatherRepository).getDataService(cityName)
        // add more assertions based on the behavior of the ViewModel
    }

    @Test
    fun `refreshData() when network is not available`() = runBlockingTest {
        `when`(connectivityHelper.isNetworkAvailable()).thenReturn(false)
        val cityName = "London"

        viewModel.refreshData(cityName)

        verify(connectivityHelper).isNetworkAvailable()
        // add more assertions based on the behavior of the ViewModel
    }
}
