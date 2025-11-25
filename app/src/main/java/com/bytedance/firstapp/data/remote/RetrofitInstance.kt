package com.bytedance.firstapp.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Use 10.0.2.2 to connect to the host machine's localhost from the Android emulator
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Create a logging interceptor to see request and response logs in Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create an OkHttpClient and add the interceptor
    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Client for SSE that avoids buffering the response body
    val sseClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
        .readTimeout(0, java.util.concurrent.TimeUnit.NANOSECONDS) // SSE connections can be long-lived
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use the custom client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
