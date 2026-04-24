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

    fun addScan(co2Saved: Double, kmReduced: Double) {
        _totalCo2Saved.value += co2Saved
        _totalKmReduced.value += kmReduced
        _scanCount.value += 1
    }

    fun reset() {
        _totalCo2Saved.value = 0.0
        _totalKmReduced.value = 0.0
        _scanCount.value = 0
    }

    fun getFormattedCo2(): String {
        return String.format("%.2f", _totalCo2Saved.value)
    }

    fun getFormattedKm(): String {
        return String.format("%.1f", _totalKmReduced.value)
    }
}