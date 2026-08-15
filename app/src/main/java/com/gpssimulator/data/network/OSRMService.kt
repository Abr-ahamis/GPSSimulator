package com.gpssimulator.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class OSRMResponse(
    val code: String,
    val routes: List<OSRMRoute>
)

data class OSRMRoute(
    val geometry: String, // Polyline encoded string
    val distance: Double,
    val duration: Double
)

interface OSRMService {
    @GET("route/v1/foot/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates", encoded = true) coordinates: String, // lon,lat;lon,lat
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline",
        @Header("User-Agent") userAgent: String = USER_AGENT
    ): OSRMResponse

    companion object {
        // OpenStreetMap Germany hosts this public, open-source OSRM walking profile.
        // HTTPS avoids Android 9+ clear-text traffic failures and the service needs no API key.
        private const val BASE_URL = "https://routing.openstreetmap.de/routed-foot/"
        private const val USER_AGENT = "GPSimulator/1.0 (Android)"

        fun create(): OSRMService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OSRMService::class.java)
        }
    }
}
