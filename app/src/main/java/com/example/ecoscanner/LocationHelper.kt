package com.example.ecoscanner

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationHelper {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastKnownLocation: UserLocation? = null

    data class UserLocation(
        val latitude: Double,
        val longitude: Double,
        val country: String,
        val city: String
    )

    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    @Suppress("MissingPermission")
    suspend fun getUserLocation(context: Context): UserLocation? = withContext(Dispatchers.IO) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
        // Check for location permissions before attempting to access location
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            // Permissions not granted; caller should request them
            return@withContext null
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
                // Resolve country and city via reverse geocoding if possible
                val countryCity = resolveCountryCity(context, location.latitude, location.longitude)
                val country = countryCity?.first ?: "Desconocido"
                val city = countryCity?.second ?: "Desconocido"
                lastKnownLocation = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    country = country,
                    city = city
                )
                lastKnownLocation
            } else {
                null
            }
        } catch (e: Exception) {
            // log and return null to signal location could not be retrieved
            Log.e("LocationHelper", "Error fetching user location", e)
            null
        }
    }

    fun getLastKnownLocation(): UserLocation? = lastKnownLocation

    fun getCountryCoordinates(country: String): Coordinates? {
        return countryCoordinates[normalizeCountryName(country)]
    }

    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun normalizeCountryName(country: String): String {
        if (country.isBlank()) return "Otro"
        val normalized = country.lowercase().trim()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
        return when {
            normalized.contains("espana") || normalized.contains("spain") -> "España"
            normalized.contains("portugal") -> "Portugal"
            normalized.contains("francia") || normalized.contains("france") -> "Francia"
            normalized.contains("italia") || normalized.contains("italy") -> "Italia"
            normalized.contains("alemania") || normalized.contains("germany") -> "Alemania"
            normalized.contains("reino unido") || normalized.contains("uk") || normalized.contains("united kingdom") -> "Reino Unido"
            else -> "Otro"
        }
    }

    private val countryCoordinates = mapOf(
        "España" to Coordinates(40.4168, -3.7038),
        "Portugal" to Coordinates(38.7223, -9.1393),
        "Francia" to Coordinates(48.8566, 2.3522),
        "Italia" to Coordinates(41.9028, 12.4964),
        "Alemania" to Coordinates(52.5200, 13.4050),
        "Reino Unido" to Coordinates(51.5074, -0.1278),
        "Otro" to Coordinates(0.0, 0.0)
    )

    private fun resolveCountryCity(context: Context, lat: Double, lon: Double): Pair<String, String>? {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val country = a.countryName ?: a.adminArea ?: "Desconocido"
                val city = a.locality ?: a.subAdminArea ?: a.subLocality ?: "Desconocido"
                Pair(country, city)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
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

    private val transportEmissionsPerKm = mapOf(
        "avión" to 0.250,
        "barco" to 0.015,
        "camión" to 0.060,
        "tren" to 0.035,
        "local" to 0.0
    )

    fun calculateCarbonFootprint(productOrigin: String?, userCountry: String?): CarbonResult {
        val originCountry = normalizeCountry(productOrigin)
        val userCountryNorm = normalizeCountry(userCountry ?: "España")
        val distanceKm = getDistanceFromCountries(originCountry, userCountryNorm)
        return buildResult(originCountry, userCountryNorm, distanceKm)
    }

    fun calculateCarbonFootprintWithCoordinates(productOrigin: String?, userLat: Double?, userLon: Double?, weightKg: Double? = null): CarbonResult {
        val originCountry = normalizeCountry(productOrigin)
        val distanceKm = if (userLat != null && userLon != null) {
            val originCoords = LocationHelper.getCountryCoordinates(originCountry)
            if (originCoords != null) {
                LocationHelper.calculateHaversineDistance(userLat, userLon, originCoords.latitude, originCoords.longitude)
            } else {
                getDistanceFromCountries(originCountry, "España")
            }
        } else {
            getDistanceFromCountries(originCountry, "España")
        }
        val userCountry = if (userLat != null && userLon != null) "Tu ubicación" else "España"
        return buildResult(originCountry, userCountry, distanceKm, weightKg)
    }

    private fun buildResult(originCountry: String, userCountry: String, distanceKm: Double, weightKg: Double? = null): CarbonResult {
        val transportType = getTransportType(distanceKm)
        val emissionPerKm = transportEmissionsPerKm[transportType]?.toDouble() ?: 0.060
        val weightFactor = weightKg ?: 1.0
        
        // Verificar si hay datos靠谱os de origen
        val hasValidOrigin = originCountry.isNotBlank() && originCountry != "Otro"
        val hasValidDistance = distanceKm > 0 && distanceKm < 20000 // Distancia máxima razonable: 20000 km (circunferencia terrestre)
        
        // Si no hay datos靠谱os, usar valores por defecto seguros
        val co2Kg = when {
            // Caso 1: Sin datos de origen, usar默认值 segura
            !hasValidOrigin -> {
                when {
                    userCountry.contains("España") -> 0.3  // Producto local
                    userCountry.contains("Europa") -> 1.0   // Producto europeo
                    else -> 2.0                           // Producto internacional
                }
            }
            // Caso 2: Datos existentes pero distancia fuera de rango
            !hasValidDistance -> {
                when {
                    distanceKm == 0.0 -> 0.0                 // Producto local confirmado
                    else -> 1.0                             //默认值 por distancia irreal
                }
            }
            // Caso 3: Cálculo normal con límite de seguridad
            else -> {
                val calculated = distanceKm * emissionPerKm * weightFactor
                minOf(calculated, 10.0) // Máximo 10 kg por producto
            }
        }
        
        val message = when {
            distanceKm == 0.0 -> "Producto local - 0 emisiones de transporte"
            !hasValidOrigin -> "Origen no verificado - CO₂ estimado"
            distanceKm < 300 -> "Transporte por carretera - Bajas emisiones"
            distanceKm < 1000 -> "Transporte nacional - Emisiones moderadas"
            distanceKm < 2500 -> "Transporte internacional - Emisiones significativas"
            distanceKm < 5000 -> "Transporte de larga distancia - Altas emisiones"
            else -> "Transporte intercontinental - Muy altas emisiones"
        }
        
        return CarbonResult(co2Kg, minOf(distanceKm, 20000.0), originCountry, userCountry, transportType, message)
    }

    private fun normalizeCountry(country: String?): String {
        if (country.isNullOrBlank()) return "Otro"
        val normalized = country.lowercase().trim()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
        return when {
            normalized.contains("espana") || normalized.contains("spain") -> "España"
            normalized.contains("portugal") -> "Portugal"
            normalized.contains("francia") || normalized.contains("france") -> "Francia"
            normalized.contains("italia") || normalized.contains("italy") -> "Italia"
            normalized.contains("alemania") || normalized.contains("germany") -> "Alemania"
            normalized.contains("reino unido") || normalized.contains("uk") || normalized.contains("united kingdom") -> "Reino Unido"
            else -> "Otro"
        }
    }

    private fun getDistanceFromCountries(origin: String, destination: String): Double {
        val originCoords = LocationHelper.getCountryCoordinates(origin)
        val destCoords = LocationHelper.getCountryCoordinates(destination)
        if (originCoords != null && destCoords != null) {
            return LocationHelper.calculateHaversineDistance(
                originCoords.latitude,
                originCoords.longitude,
                destCoords.latitude,
                destCoords.longitude
            )
        }
        return 500.0
    }

    private fun getTransportType(distanceKm: Double): String = when {
        distanceKm == 0.0 -> "local"
        distanceKm < 400 -> "camión"
        distanceKm < 1500 -> "tren"
        distanceKm < 3500 -> "barco"
        else -> "avión"
    }

    fun estimateDefault(): CarbonResult {
        return CarbonResult(0.5, 500.0, "Por determinar", "España", "estimado", "Sin datos de origen - cálculo estimado")
    }
}
