package com.example.ecoscanner

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
data class ProductData(
    val code: String = "",
    val name: String = "",
    val brand: String = "",
    val imageUrl: String? = null,
    val origin: String? = null,
    val categories: String? = null,
    val nutriscoreGrade: String? = null,
    val ecoscoreGrade: String? = null,
    val carbonFootprint: Double? = null,
    val nutriments: NutrientsData = NutrientsData(),
    val ingredients: String = "",
    val isScanned: Boolean = false
)

@Stable
data class NutrientsData(
    val calories: Double? = null,
    val fat: Double? = null,
    val saturatedFat: Double? = null,
    val carbohydrates: Double? = null,
    val sugars: Double? = null,
    val proteins: Double? = null,
    val salt: Double? = null
)

object ProductRepository {
    private val _lastScannedProduct = MutableStateFlow<ProductData?>(null)
    val lastScannedProduct: StateFlow<ProductData?> = _lastScannedProduct.asStateFlow()

    fun updateProduct(product: ProductData) {
        _lastScannedProduct.value = product
    }

    fun clearProduct() {
        _lastScannedProduct.value = null
    }
}