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
        val directLookup = countryCoordinates[country]
        if (directLookup != null) return directLookup
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
        "Otro" to Coordinates(0.0, 0.0),
        "Estados Unidos" to Coordinates(37.0902, -95.7129),
        "México" to Coordinates(23.6345, -102.5528),
        "Brasil" to Coordinates(-14.2350, -51.9253),
        "Argentina" to Coordinates(-38.4161, -63.6167),
        "Suiza" to Coordinates(46.8182, 8.2275),
        "Canadá" to Coordinates(56.1304, -106.3468),
        "Polonia" to Coordinates(51.9194, 19.1451),
        "China" to Coordinates(35.8617, 104.1954),
        "India" to Coordinates(20.5937, 78.9629),
        "Países Bajos" to Coordinates(52.1326, 5.2913),
        "Australia" to Coordinates(-25.2744, 133.7751),
        "Austria" to Coordinates(47.5162, 14.5501),
        "Turquía" to Coordinates(38.9637, 35.2433),
        "Bélgica" to Coordinates(50.5039, 4.4699),
        "Bolivia" to Coordinates(-16.2902, -63.5887),
        "Tailandia" to Coordinates(15.8700, 100.9925),
        "Noruega" to Coordinates(60.4720, 8.4689),
        "Grecia" to Coordinates(39.0742, 21.8243),
        "Perú" to Coordinates(-9.1900, -75.0152),
        "Suecia" to Coordinates(60.1282, 18.6435),
        "Japón" to Coordinates(36.2048, 138.2529),
        "Túnez" to Coordinates(33.8869, 9.5375),
        "Irlanda" to Coordinates(53.1424, -7.6921),
        "Marruecos" to Coordinates(31.7917, -7.0926),
        "Vietnam" to Coordinates(14.0583, 108.2772),
        "Chile" to Coordinates(-35.6751, -71.5430),
        "Finlandia" to Coordinates(61.9241, 25.7482),
        "Rumanía" to Coordinates(45.9432, 24.9668),
        "Nueva Zelanda" to Coordinates(-40.9006, 174.8860),
        "Sudáfrica" to Coordinates(-30.5595, 22.9375),
        "Filipinas" to Coordinates(12.8797, 121.7740),
        "Dinamarca" to Coordinates(56.2639, 9.5018),
        "Hungría" to Coordinates(47.1625, 19.5033),
        "Indonesia" to Coordinates(-0.7893, 113.9213),
        "Rusia" to Coordinates(61.5240, 105.3188),
        "Lituania" to Coordinates(55.1694, 23.8813),
        "Paraguay" to Coordinates(-23.4425, -58.4438),
        "Croacia" to Coordinates(45.1000, 15.2000),
        "Kenia" to Coordinates(-0.0236, 37.9062),
        "Irán" to Coordinates(32.4279, 53.6880),
        "Camboya" to Coordinates(12.5657, 104.9910),
        "Eslovaquia" to Coordinates(48.6690, 19.6990),
        "Israel" to Coordinates(31.0461, 34.8516),
        "Líbano" to Coordinates(33.8547, 35.8623),
        "Ecuador" to Coordinates(-1.8312, -78.1834),
        "Malta" to Coordinates(35.9375, 14.3754),
        "Serbia" to Coordinates(44.0165, 21.0059),
        "Ucranía" to Coordinates(48.3794, 31.1656),
        "Catar" to Coordinates(25.3548, 51.1839),
        "Estonia" to Coordinates(58.5953, 25.0136),
        "Malasia" to Coordinates(4.2105, 101.9758),
        "Hong Kong" to Coordinates(22.3193, 114.1694),
        "República Checa" to Coordinates(49.8175, 15.4730),
        "Singapur" to Coordinates(1.3521, 103.8198),
        "Arabia Saudita" to Coordinates(23.8859, 45.0792),
        "Argelia" to Coordinates(28.0339, 1.6596),
        "Emiratos Árabes Unidos" to Coordinates(23.4241, 53.8478),
        "Chipre" to Coordinates(35.1264, 33.4299),
        "Georgia" to Coordinates(42.3154, 43.3569),
        "Costa Rica" to Coordinates(9.7489, -83.7534),
        "Taiwán" to Coordinates(23.6978, 120.9605),
        "Colombia" to Coordinates(4.5709, -74.2973),
        "Uruguay" to Coordinates(-32.5228, -55.7658),
        "Egipto" to Coordinates(26.8206, 30.8025),
        "Corea del Sur" to Coordinates(35.9078, 127.7669),
        "Pakistán" to Coordinates(30.3753, 69.3451),
        "Bangladesh" to Coordinates(23.6850, 90.3563)
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
        val normalizedOrigin = normalizeCountry(productOrigin)
        val displayOrigin = if (normalizedOrigin == "Otro" && !productOrigin.isNullOrBlank()) 
            translateCountryToSpanish(productOrigin) else normalizedOrigin
        
        if (displayOrigin == "Otro" || productOrigin.isNullOrBlank()) {
            return CarbonResult(
                co2Kg = 0.0,
                kmDistance = 0.0,
                originCountry = "España",
                userCountry = userCountry ?: "España",
                transportType = "local",
                message = "Origen no especificado - asumido local (España)"
            )
        }
        
        val userCountryNorm = normalizeCountry(userCountry ?: "España")
        val countryForDistance = if (normalizedOrigin == "Otro" && LocationHelper.getCountryCoordinates(displayOrigin) != null) displayOrigin else normalizedOrigin
        val distanceKm = getDistanceFromCountries(countryForDistance, userCountryNorm)
        return buildResult(displayOrigin, userCountryNorm, distanceKm)
    }

    fun calculateCarbonFootprintWithCoordinates(productOrigin: String?, userLat: Double?, userLon: Double?, weightKg: Double? = null): CarbonResult {
        val normalizedOrigin = normalizeCountry(productOrigin)
        val displayOrigin = if (normalizedOrigin == "Otro" && !productOrigin.isNullOrBlank()) 
            translateCountryToSpanish(productOrigin) else normalizedOrigin
        
        if (displayOrigin == "Otro" || productOrigin.isNullOrBlank()) {
            return CarbonResult(
                co2Kg = 0.0,
                kmDistance = 0.0,
                originCountry = "España",
                userCountry = if (userLat != null && userLon != null) "Tu ubicación" else "España",
                transportType = "local",
                message = "Origen no especificado - asumido local (España)"
            )
        }
        
        val countryForDistance = if (normalizedOrigin == "Otro" && LocationHelper.getCountryCoordinates(displayOrigin) != null) displayOrigin else normalizedOrigin
        val distanceKm = if (userLat != null && userLon != null) {
            val originCoords = LocationHelper.getCountryCoordinates(countryForDistance)
            if (originCoords != null) {
                LocationHelper.calculateHaversineDistance(userLat, userLon, originCoords.latitude, originCoords.longitude)
            } else {
                getDistanceFromCountries(countryForDistance, "España")
            }
        } else {
            getDistanceFromCountries(countryForDistance, "España")
        }
        val userCountry = if (userLat != null && userLon != null) "Tu ubicación" else "España"
        return buildResult(displayOrigin, userCountry, distanceKm, weightKg)
    }

    private fun buildResult(originCountry: String, userCountry: String, distanceKm: Double, weightKg: Double? = null): CarbonResult {
        val hasValidOrigin = originCountry.isNotBlank() && originCountry != "Otro"
        val hasValidDistance = distanceKm > 0 && distanceKm < 20000
        
        val effectiveDistance = if (!hasValidOrigin) 0.0 else distanceKm
        val transportType = getTransportType(effectiveDistance)
        val emissionPerKm = transportEmissionsPerKm[transportType]?.toDouble() ?: 0.060
        val weightFactor = weightKg ?: 1.0
        
        val co2Kg = when {
            !hasValidOrigin -> {
                0.0
            }
            !hasValidDistance -> {
                when {
                    distanceKm == 0.0 -> 0.0
                    else -> 1.0
                }
            }
            else -> {
                val calculated = distanceKm * emissionPerKm * weightFactor
                minOf(calculated, 10.0)
            }
        }
        
        val message = when {
            !hasValidOrigin -> "Origen no especificado - asumido local (España)"
            distanceKm == 0.0 -> "Producto local - 0 emisiones de transporte"
            distanceKm < 300 -> "Transporte por carretera - Bajas emisiones"
            distanceKm < 1000 -> "Transporte nacional - Emisiones moderadas"
            distanceKm < 2500 -> "Transporte internacional - Emisiones significativas"
            distanceKm < 5000 -> "Transporte de larga distancia - Altas emisiones"
            else -> "Transporte intercontinental - Muy altas emisiones"
        }
        
        val finalDistance = if (!hasValidOrigin) 0.0 else minOf(distanceKm, 20000.0)
        val finalOriginCountry = if (!hasValidOrigin) "España" else originCountry
        
        return CarbonResult(co2Kg, finalDistance, finalOriginCountry, userCountry, transportType, message)
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

    private fun translateCountryToSpanish(country: String?): String {
        if (country.isNullOrBlank()) return country ?: ""
        val normalized = country.lowercase().trim()
        return when {
            normalized.contains("spain") || normalized.contains("espana") -> "España"
            normalized.contains("portugal") -> "Portugal"
            normalized.contains("france") || normalized.contains("francia") -> "Francia"
            normalized.contains("italy") || normalized.contains("italia") -> "Italia"
            normalized.contains("germany") || normalized.contains("alemania") -> "Alemania"
            normalized.contains("united kingdom") || normalized.contains("uk") || normalized.contains("great britain") -> "Reino Unido"
            normalized.contains("united states") || normalized.contains("usa") || normalized.contains("etats-unis") -> "Estados Unidos"
            normalized.contains("mexico") || normalized.contains("mexiko") -> "México"
            normalized.contains("brazil") || normalized.contains("brasil") || normalized.contains("bresil") -> "Brasil"
            normalized.contains("argentina") -> "Argentina"
            normalized.contains("switzerland") || normalized.contains("suisse") || normalized.contains("schweiz") -> "Suiza"
            normalized.contains("canada") -> "Canadá"
            normalized.contains("poland") || normalized.contains("polonia") -> "Polonia"
            normalized.contains("china") || normalized.contains("chine") -> "China"
            normalized.contains("india") -> "India"
            normalized.contains("netherlands") || normalized.contains("holanda") || normalized.contains("nederland") -> "Países Bajos"
            normalized.contains("australia") || normalized.contains("australie") -> "Australia"
            normalized.contains("austria") || normalized.contains("osterreich") -> "Austria"
            normalized.contains("turkey") || normalized.contains("turquia") -> "Turquía"
            normalized.contains("belgium") || normalized.contains("belgique") || normalized.contains("belgica") -> "Bélgica"
            normalized.contains("bolivia") -> "Bolivia"
            normalized.contains("thailand") || normalized.contains("tailandia") -> "Tailandia"
            normalized.contains("norway") || normalized.contains("norge") || normalized.contains("norvegia") -> "Noruega"
            normalized.contains("greece") || normalized.contains("grecia") || normalized.contains("grece") -> "Grecia"
            normalized.contains("peru") || normalized.contains("perou") -> "Perú"
            normalized.contains("bulgaria") -> "Bulgaria"
            normalized.contains("sweden") || normalized.contains("sverige") || normalized.contains("suede") -> "Suecia"
            normalized.contains("japan") || normalized.contains("japon") -> "Japón"
            normalized.contains("tunisia") || normalized.contains("tunisie") -> "Túnez"
            normalized.contains("ireland") || normalized.contains("irlanda") || normalized.contains("irlande") -> "Irlanda"
            normalized.contains("morocco") || normalized.contains("maroc") || normalized.contains("marruecos") -> "Marruecos"
            normalized.contains("vietnam") || normalized.contains("viet nam") -> "Vietnam"
            normalized.contains("chile") -> "Chile"
            normalized.contains("finland") || normalized.contains("finlandia") || normalized.contains("finlande") -> "Finlandia"
            normalized.contains("romania") || normalized.contains("rumania") -> "Rumanía"
            normalized.contains("new zealand") || normalized.contains("nueva zelanda") -> "Nueva Zelanda"
            normalized.contains("south africa") || normalized.contains("sudafrica") || normalized.contains("afrique du sud") -> "Sudáfrica"
            normalized.contains("philippines") || normalized.contains("filipinas") || normalized.contains("philippines") -> "Filipinas"
            normalized.contains("denmark") || normalized.contains("dinamarca") || normalized.contains("danemark") -> "Dinamarca"
            normalized.contains("hungary") || normalized.contains("hungria") || normalized.contains("hongrie") -> "Hungría"
            normalized.contains("indonesia") -> "Indonesia"
            normalized.contains("russia") || normalized.contains("rusia") || normalized.contains(" federation") -> "Rusia"
            normalized.contains("lithuania") || normalized.contains("lituania") || normalized.contains("lituanie") -> "Lituania"
            normalized.contains("paraguay") -> "Paraguay"
            normalized.contains("croatia") || normalized.contains("croacia") || normalized.contains("croatie") -> "Croacia"
            normalized.contains("kenya") || normalized.contains("kenia") -> "Kenia"
            normalized.contains("iran") || normalized.contains("persia") -> "Irán"
            normalized.contains("cambodia") || normalized.contains("camboya") || normalized.contains("cambodge") -> "Camboya"
            normalized.contains("slovakia") || normalized.contains("eslovaquia") || normalized.contains("slovaquie") -> "Eslovaquia"
            normalized.contains("ethiopia") || normalized.contains("etiopia") || normalized.contains("ethiopie") -> "Etiopía"
            normalized.contains("israel") -> "Israel"
            normalized.contains("lebanon") || normalized.contains("libano") || normalized.contains("liban") -> "Líbano"
            normalized.contains("ecuador") -> "Ecuador"
            normalized.contains("malta") || normalized.contains("malta") -> "Malta"
            normalized.contains("serbia") || normalized.contains("serbie") -> "Serbia"
            normalized.contains("ukraine") || normalized.contains("ucrania") || normalized.contains("ukraine") -> "Ucrania"
            normalized.contains("qatar") -> "Catar"
            normalized.contains("estonia") || normalized.contains("estonie") -> "Estonia"
            normalized.contains("malaysia") || normalized.contains("malasia") -> "Malasia"
            normalized.contains("hong kong") -> "Hong Kong"
            normalized.contains("czech") || normalized.contains("republica checa") || normalized.contains("tchequie") -> "República Checa"
            normalized.contains("singapore") || normalized.contains("singapur") -> "Singapur"
            normalized.contains("saudi") || normalized.contains("arabia") -> "Arabia Saudita"
            normalized.contains("algeria") || normalized.contains("argelia") || normalized.contains("algerie") -> "Argelia"
            normalized.contains("united arab emirates") || normalized.contains("emiratos arabes") -> "Emiratos Árabes Unidos"
            normalized.contains("cyprus") || normalized.contains("chipre") || normalized.contains("chyper") -> "Chipre"
            normalized.contains("georgia") || normalized.contains("georgie") -> "Georgia"
            normalized.contains("costa rica") -> "Costa Rica"
            normalized.contains("taiwan") || normalized.contains("formosa") -> "Taiwán"
            normalized.contains("cuba") -> "Cuba"
            normalized.contains("colombia") -> "Colombia"
            normalized.contains("uruguay") -> "Uruguay"
            normalized.contains("venezuela") -> "Venezuela"
            normalized.contains("ecuador") -> "Ecuador"
            normalized.contains("indonesia") -> "Indonesia"
            normalized.contains("egypt") || normalized.contains("egipto") || normalized.contains("egypte") -> "Egipto"
            normalized.contains("south korea") || normalized.contains("korea") || normalized.contains("corea") -> "Corea del Sur"
            normalized.contains("north korea") || normalized.contains("corea del norte") -> "Corea del Norte"
            normalized.contains("pakistan") || normalized.contains("pakistan") -> "Pakistán"
            normalized.contains("bangladesh") -> "Bangladesh"
            normalized.contains("peru") || normalized.contains("perou") -> "Perú"
            normalized.contains("nicaragua") -> "Nicaragua"
            normalized.contains("honduras") -> "Honduras"
            normalized.contains("guatemala") -> "Guatemala"
            normalized.contains("panama") || normalized.contains("panama") -> "Panamá"
            normalized.contains("dominican republic") || normalized.contains("republica dominicana") -> "República Dominicana"
            normalized.contains("jordan") || normalized.contains("jordania") -> "Jordania"
            normalized.contains("iraq") || normalized.contains("irak") -> "Irak"
            normalized.contains("syria") || normalized.contains("siria") || normalized.contains("syrie") -> "Siria"
            normalized.contains("libya") || normalized.contains("libia") || normalized.contains("libye") -> "Libia"
            normalized.contains("angola") -> "Angola"
            normalized.contains("cameroon") || normalized.contains("camerun") || normalized.contains("cameroun") -> "Camerún"
            normalized.contains("ivory coast") || normalized.contains("costa de marfil") || normalized.contains("cote d'ivoire") -> "Costa de Marfil"
            normalized.contains("madagascar") -> "Madagascar"
            normalized.contains("mozambique") || normalized.contains("mocambique") -> "Mozambique"
            normalized.contains("zimbabwe") -> "Zimbabue"
            normalized.contains("zambia") || normalized.contains("zambie") -> "Zambia"
            normalized.contains("tanzania") -> "Tanzania"
            normalized.contains("kenya") || normalized.contains("kenia") -> "Kenia"
            normalized.contains("uganda") -> "Uganda"
            normalized.contains("senegal") || normalized.contains("senegal") -> "Senegal"
            normalized.contains("ghana") -> "Ghana"
            normalized.contains("niger") || normalized.contains("niger") -> "Níger"
            normalized.contains("mali") -> "Mali"
            normalized.contains("burkina faso") -> "Burkina Faso"
            normalized.contains("european union") || normalized.contains("union europea") -> "Unión Europea"
            normalized.contains("non european union") || normalized.contains("non-ue") -> "No UE"
            normalized.contains("europe") || normalized.contains("europa") -> "Europa"
            normalized.contains("world") || normalized.contains("mundo") || normalized.contains("monde") -> "Mundial"
            normalized.contains("atlantic") || normalized.contains("atlantico") -> "Océano Atlántico"
            normalized.contains("pacific") || normalized.contains("pacifico") -> "Océano Pacífico"
            else -> country
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
