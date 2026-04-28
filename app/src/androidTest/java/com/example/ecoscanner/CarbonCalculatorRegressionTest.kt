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
        val originCountry = "España"
        val userLat = 41.3851  // Barcelona approx
        val userLon = 2.1734
        val weightKg = 2.0

        val originCoords = LocationHelper.getCountryCoordinates(originCountry)
        assertNotNull(originCoords)
        val distanceKm = LocationHelper.calculateHaversineDistance(userLat, userLon, originCoords!!.latitude, originCoords.longitude)
        assertTrue("Distance should be > 0", distanceKm > 0)

        val result = CarbonCalculator.calculateCarbonFootprintWithCoordinates(originCountry, userLat, userLon, weightKg)
        val emissionPerKm = 0.035 // based on tren for this distance range
        val expected = distanceKm * emissionPerKm * weightKg
        val delta = 1e-6
        assertEquals(expected, result.co2Kg, delta)
    }
}
