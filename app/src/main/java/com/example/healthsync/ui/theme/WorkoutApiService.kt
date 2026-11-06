package com.example.healthsync

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

// 1. The API data wrappers
data class WgerResponse(
    val results: List<ApiWorkout>
)

data class ApiWorkout(
    val name: String,
    val description: String
)

// 2. The API request service (Using suspend for Coroutines)
interface WorkoutApiService {
    @Headers(
        "Authorization: Token 7607831508d66f0b8865a6b1491b64b1e5e62ca",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"

    )
    @GET("v3/b427b878-c0b7-45ba-bf1b-7fb4ba6441b8")
    suspend fun getExercises(): WgerResponse
}

// 3. The missing ApiClient that your Activity is trying to call
object ApiClient {
    // A stable, open test endpoint that won't trigger bot firewalls
    private const val BASE_URL = "https://run.mocky.io"

    val service: WorkoutApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorkoutApiService::class.java)
    }
}

