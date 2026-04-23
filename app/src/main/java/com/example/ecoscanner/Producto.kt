package com.example.ecoscanner



data class Producto(
    val id: Int,
    val nombre: String,
    val co2: Double,
    val categoria: String
)

data class ProductoResponse(
    val product: ProductoDetalle
)

data class ProductoDetalle(
    val product_name: String,
    val countries_tags: List<String>
)

data class ClimatiqRequest(
    val emission_factor: EmissionFactor,
    val parameters: Parameters
)

data class EmissionFactor(
    val activity_id: String = "passenger_vehicle-vehicle_type_car-fuel_source_petrol-engine_size_na-vehicle_age_na-passengers_na",
    val data_version: String = "^6"
)

data class Parameters(
    val distance: Double,
    val distance_unit: String = "km"
)

data class ClimatiqResponse(
    val co2e: Double
)