package com.example.ecoscanner

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface OpenFoodFactsService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProducto(@Path("barcode") barcode: String): ProductoResponse
}

interface ClimatiqService {
    @POST("data/v1/estimate")
    suspend fun calcularCO2(
        @Header("Authorization") token: String,
        @Body body: ClimatiqRequest
    ): ClimatiqResponse
}