package com.example.ecoscanner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarbonFootprintTracker {
    private val _totalCo2Saved = MutableStateFlow(0.0)
    val totalCo2Saved: StateFlow<Double> = _totalCo2Saved.asStateFlow()

    private val _totalKmReduced = MutableStateFlow(0.0)
    val totalKmReduced: StateFlow<Double> = _totalKmReduced.asStateFlow()

    private val _scanCount = MutableStateFlow(0)
    val scanCount: StateFlow<Int> = _scanCount.asStateFlow()

    // Distance of last scanned item origin-usuario (km). Used for UI display in Datos screen.
    private val _lastKmReduced = MutableStateFlow(0.0)
    val lastKmReduced: StateFlow<Double> = _lastKmReduced.asStateFlow()

    fun addScan(co2Saved: Double, kmReduced: Double) {
        _totalCo2Saved.value += co2Saved
        _totalKmReduced.value += kmReduced
        _scanCount.value += 1
        // Persist the last calculated distance for UI display (Datos screen)
        _lastKmReduced.value = kmReduced
    }

    fun reset() {
        _totalCo2Saved.value = 0.0
        _totalKmReduced.value = 0.0
        _scanCount.value = 0
    }

    }
