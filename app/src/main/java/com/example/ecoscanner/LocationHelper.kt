package com.example.ecoscanner

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.Locale

object LocationHelper {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastKnownLocation: UserLocation? = null

    data class UserLocation(
        val latitude: Double,
        val longitude: Double,
        val country: String,
        val city: String
    )

    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(context: Context): UserLocation? = withContext(Dispatchers.IO) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        try {
            val cancellationToken = CancellationTokenSource()
            val location = suspendCancellableCoroutine { continuation ->
                fusedLocationClient?.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationToken.token
                )?.addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }?.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            }

            if (location != null) {
                val address = getAddressFromLocation(context, location.latitude, location.longitude)
                lastKnownLocation = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    country = address?.countryName ?: "Desconocido",
                    city = address?.locality ?: address?.subAdminArea ?: "Desconocido"
                )
                lastKnownLocation
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getAddressFromLocation(context: Context, lat: Double, lng: Double): android.location.Address? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        continuation.resume(addresses.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getLastKnownLocation(): UserLocation? = lastKnownLocation
}

object CarbonCalculator {

    data class CarbonResult(
        val co2Kg: Double,
        val kmDistance: Double,
        val originCountry: String,
        val userCountry: String,
        val transportType: String,
        val message: String
    )

    private val countryDistances = mapOf(
        "España" to mapOf(
            "España" to 0,
            "Portugal" to 180,
            "Francia" to 850,
            "Italia" to 1400,
            "Alemania" to 1900,
            "Reino Unido" to 1700,
            "Países Bajos" to 1800,
            "Bélgica" to 1600,
            "Polonia" to 2400,
            "Suiza" to 1300,
            "Austria" to 1700,
            "Suecia" to 3000,
            "Noruega" to 2800,
            "Dinamarca" to 2500,
            "Finlandia" to 3200,
            "Irlanda" to 1900,
            "Grecia" to 2800,
            "Hungría" to 2200,
            "República Checa" to 2000,
            "Rumanía" to 2600,
            "China" to 9500,
            "EE.UU." to 9000,
            "Canadá" to 8500,
            "Brasil" to 8500,
            "México" to 9500,
            "Argentina" to 11000,
            "Chile" to 12000,
            "Colombia" to 9000,
            "Perú" to 10000,
            "Japón" to 11000,
            "Corea del Sur" to 10500,
            "Australia" to 17500,
            "India" to 8000,
            "Rusia" to 4000,
            "Turquía" to 3500,
            "Marruecos" to 1100,
            "Egipto" to 4000,
            "Sudáfrica" to 8500,
            "Emiratos Árabes" to 6000,
            "Arabia Saudita" to 5500,
            "Israel" to 4000,
            "Tailandia" to 10000,
            "Vietnam" to 10500,
            "Indonesia" to 12000,
            "Malaysia" to 11000,
            "Singapur" to 11000,
            "Filipinas" to 12000,
            "Nueva Zelanda" to 19500,
            "Sudán" to 5500
        ),
        "默认" to mapOf(
            "España" to 0,
            "默认" to 1500,
            "Francia" to 800,
            "Italia" to 1400,
            "Alemania" to 1900,
            "Reino Unido" to 1700,
            "China" to 9500,
            "EE.UU." to 9000,
            "Brasil" to 8500,
            "Japón" to 11000,
            "Australia" to 17500,
            "India" to 8000,
            "Rusia" to 4000
        )
    )

    private val transportEmissionsPerKm = mapOf(
        "avión" to 0.250,
        "barco" to 0.015,
        "camión" to 0.060,
        "tren" to 0.035,
        "local" to 0.0
    )

    fun calculateCarbonFootprint(
        productOrigin: String?,
        userCountry: String?
    ): CarbonResult {
        val originCountry = normalizeCountry(productOrigin)
        val userCountryNorm = normalizeCountry(userCountry ?: "España")

        val distanceKm = getDistance(originCountry, userCountryNorm).toDouble()
        val transportType = getTransportType(distanceKm)
        val emissionPerKm = transportEmissionsPerKm[transportType]?.toDouble() ?: 0.060
        val co2Kg = distanceKm * emissionPerKm

        val message = when {
            distanceKm == 0.0 -> "Producto local - 0 emisiones de transporte"
            distanceKm < 300 -> "Transporte por carretera - Bajas emisiones"
            distanceKm < 1000 -> "Transporte nacional - Emisiones moderadas"
            distanceKm < 2500 -> "Transporte internacional - Emisiones significativas"
            distanceKm < 5000 -> "Transporte de larga distancia - Altas emisiones"
            else -> "Transporte intercontinental - Muy altas emisiones"
        }

        return CarbonResult(
            co2Kg = co2Kg,
            kmDistance = distanceKm,
            originCountry = originCountry,
            userCountry = userCountryNorm,
            transportType = transportType,
            message = message
        )
    }

    private fun normalizeCountry(country: String?): String {
        if (country.isNullOrBlank()) return "Otro"
        
        // Eliminar acentos y normalizar
        val normalized = country
            .lowercase()
            .trim()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
        
        return when {
            // España
            normalized.contains("espana") || normalized.contains("spain") || normalized.contains("espagne") || normalized.contains("spanien") -> "España"
            normalized.contains("portugal") -> "Portugal"
            normalized.contains("francia") || normalized.contains("france") -> "Francia"
            normalized.contains("italia") || normalized.contains("italy") -> "Italia"
            normalized.contains("alemania") || normalized.contains("germany") || normalized.contains("deutschland") -> "Alemania"
            normalized.contains("reino Unido") || normalized.contains("uk") || normalized.contains("united kingdom") -> "Reino Unido"
            normalized.contains("países bajos") || normalized.contains("netherlands") || normalized.contains("holanda") -> "Países Bajos"
            normalized.contains("bélgica") || normalized.contains("belgium") -> "Bélgica"
            normalized.contains("polonia") || normalized.contains("poland") -> "Polonia"
            normalized.contains("suiza") || normalized.contains("switzerland") -> "Suiza"
            normalized.contains("austria") -> "Austria"
            normalized.contains("suecia") || normalized.contains("sweden") -> "Suecia"
            normalized.contains("noruega") || normalized.contains("norway") -> "Noruega"
            normalized.contains("dinamarca") || normalized.contains("denmark") -> "Dinamarca"
            normalized.contains("finlandia") || normalized.contains("finland") -> "Finlandia"
            normalized.contains("irlanda") || normalized.contains("ireland") -> "Irlanda"
            normalized.contains("grecia") || normalized.contains("greece") -> "Grecia"
            normalized.contains("hungria") || normalized.contains("hungary") -> "Hungría"
            normalized.contains("república checa") || normalized.contains("czech") -> "República Checa"
            normalized.contains("rumania") || normalized.contains("romania") -> "Rumanía"
            normalized.contains("ee.uu") || normalized.contains("usa") || normalized.contains("estados unidos") || normalized.contains("united states") -> "EE.UU."
            normalized.contains("canada") || normalized.contains("canada") -> "Canadá"
            normalized.contains("china") || normalized.contains("prc") -> "China"
            normalized.contains("brasil") || normalized.contains("brazil") -> "Brasil"
            normalized.contains("méxico") || normalized.contains("mexico") -> "México"
            normalized.contains("argentina") -> "Argentina"
            normalized.contains("chile") -> "Chile"
            normalized.contains("colombia") -> "Colombia"
            normalized.contains("perú") || normalized.contains("peru") -> "Perú"
            normalized.contains("japón") || normalized.contains("japan") -> "Japón"
            normalized.contains("corea") || normalized.contains("korea") -> "Corea del Sur"
            normalized.contains("australia") || normalized.contains("australia") -> "Australia"
            normalized.contains("india") || normalized.contains("india") -> "India"
            normalized.contains("rusia") || normalized.contains("russia") || normalized.contains("ucrania") || normalized.contains("ukraine") -> "Rusia"
            normalized.contains("turquía") || normalized.contains("turkey") -> "Turquía"
            normalized.contains("marruecos") || normalized.contains("morocco") -> "Marruecos"
            normalized.contains("egipto") || normalized.contains("egypt") -> "Egipto"
            normalized.contains("sudáfrica") || normalized.contains("south africa") -> "Sudáfrica"
            normalized.contains("emiratos") || normalized.contains("uae") || normalized.contains("emirates") -> "Emiratos Árabes"
            normalized.contains("arabia") || normalized.contains("saudi") -> "Arabia Saudita"
            normalized.contains("israel") -> "Israel"
            normalized.contains("tailandia") || normalized.contains("thailand") -> "Tailandia"
            normalized.contains("vietnam") || normalized.contains("vietnam") -> "Vietnam"
            normalized.contains("indonesia") -> "Indonesia"
            normalized.contains("malaysia") -> "Malaysia"
            normalized.contains("singapur") || normalized.contains("singapore") -> "Singapur"
            normalized.contains("filipinas") || normalized.contains("philippines") -> "Filipinas"
            normalized.contains("nueva zelanda") || normalized.contains("new zealand") -> "Nueva Zelanda"
            else -> "Otro"
        }
    }

private fun getDistance(origin: String, destination: String): Double {
        val originDistances = countryDistances[origin] ?: countryDistances["Otro"] ?: return 500.0
        return (originDistances[destination] ?: 500.0).toDouble()
    }

    private fun getTransportType(distanceKm: Double): String = when {
        distanceKm == 0.0 -> "local"
        distanceKm < 400 -> "camión"
        distanceKm < 1500 -> "tren"
        distanceKm < 3500 -> "barco"
        else -> "avión"
    }

    fun estimateDefault(): CarbonResult {
        return CarbonResult(
            co2Kg = 0.5,
            kmDistance = 500.0,
            originCountry = "Por determinar",
            userCountry = "España",
            transportType = "estimado",
            message = "Sin datos de origen - cálculo estimado"
        )
    }
}