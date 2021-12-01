package github.leavesc.reactivehttpsamples.core.http

import github.leavesc.reactivehttpsamples.core.mode.DistrictMode
import github.leavesc.reactivehttpsamples.core.mode.DistrictsWrapMode
import github.leavesc.reactivehttpsamples.core.mode.ForecastsMode
import github.leavesc.reactivehttpsamples.core.mode.ForecastsWrapMode
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @Author: leavesC
 * @Date: 2020/2/25 16:34
 * @Desc:
 * @GitHub：https://github.com/leavesC
 */
interface ApiService {

    @GET("config/district")
    suspend fun getProvince(): DistrictsWrapMode<List<DistrictMode>>

    @GET("config/district")
    suspend fun getCity(@Query("keywords") keywords: String): DistrictsWrapMode<List<DistrictMode>>

    @GET("config/district")
    suspend fun getCounty(@Query("keywords") keywords: String): DistrictsWrapMode<List<DistrictMode>>

    @GET("weather/weatherInfo?extensions=all")
    suspend fun getWeather(@Query("city") city: String): ForecastsWrapMode<List<ForecastsMode>>

    @GET("config/district")
    suspend fun mustFailed(): DistrictsWrapMode<List<String>>

}