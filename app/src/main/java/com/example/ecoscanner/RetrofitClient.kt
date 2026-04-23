package com.example.ecoscanner

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val openFoodFacts: OpenFoodFactsService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "EcoScanner/1.0 (Android)")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    val climatiq: ClimatiqService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.climatiq.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimatiqService::class.java)
    }
}