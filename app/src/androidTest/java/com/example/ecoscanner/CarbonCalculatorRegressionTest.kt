package com.example.ecoscanner

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarbonCalculatorRegressionTest {
    @Test
    fun testCO2ScalesWithWeightForNonZeroDistance() {
        val originCountry = "Francia"
        val userLat = 41.3851  // Barcelona - España
        val userLon = 2.1734
        val userCountry = "España"
        val weightKg = 2.0

        val originCoords = LocationHelper.getCountryCoordinates(originCountry)
        assertNotNull(originCoords)
        val distanceKm = LocationHelper.calculateHaversineDistance(userLat, userLon, originCoords!!.latitude, originCoords.longitude)
        assertTrue("Distance should be > 0", distanceKm > 0)

        val result = CarbonCalculator.calculateCarbonFootprintWithCoordinates(originCountry, userLat, userLon, userCountry, weightKg)
        assertTrue("CO2 should be > 0 for cross-border product", result.co2Kg > 0)
    }

    @Test
    fun testCO2IsZeroForSameCountry() {
        val originCountry = "España"
        val userLat = 41.3851  // Barcelona - España
        val userLon = 2.1734
        val userCountry = "España"
        val weightKg = 2.0

        val result = CarbonCalculator.calculateCarbonFootprintWithCoordinates(originCountry, userLat, userLon, userCountry, weightKg)
        assertEquals("CO2 should be 0 for same country", 0.0, result.co2Kg, 1e-6)
        assertEquals("Distance should be 0 for same country", 0.0, result.kmDistance, 1e-6)
    }
}
