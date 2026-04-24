package com.example.ecoscanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

object OpenFoodFactsApi {
    private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/product"

    suspend fun getProductByBarcode(barcode: String): ProductData? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseProduct(response)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseProduct(json: String): ProductData {
        return try {
            val root = kotlinx.serialization.json.Json.parseToJsonElement(json)
            val status = root.jsonObject["status"]?.jsonPrimitive?.content?.toIntOrNull()

            if (status != 1) {
                return ProductData()
            }

            val product = root.jsonObject["product"]?.jsonObject ?: return ProductData()

            val nutriments = product["nutriments"]?.jsonObject
            val ecoScoreData = product["ecoscore_data"]?.jsonObject

            ProductData(
                code = product["code"]?.jsonPrimitive?.content ?: "",
                name = product["product_name_es"]?.jsonPrimitive?.content
                    ?: product["product_name"]?.jsonPrimitive?.content
                    ?: "Producto sin nombre",
                brand = product["brands"]?.jsonPrimitive?.content ?: "",
                imageUrl = product["image_small_url"]?.jsonPrimitive?.content
                    ?: product["image_url"]?.jsonPrimitive?.content,
                origin = product["origins"]?.jsonPrimitive?.content
                    ?: product["origin"]?.jsonPrimitive?.content
                    ?: product["countries"]?.jsonPrimitive?.content,
                categories = product["categories_es"]?.jsonPrimitive?.content
                    ?: product["categories"]?.jsonPrimitive?.content,
                nutriscoreGrade = product["nutriscore_grade"]?.jsonPrimitive?.content?.uppercase(),
                ecoscoreGrade = product["ecoscore_grade"]?.jsonPrimitive?.content?.uppercase(),
                carbonFootprint = ecoScoreData?.get("climate_impact")?.jsonPrimitive?.content?.toDoubleOrNull(),
                nutriments = NutrientsData(
                    calories = nutriments?.get("energy-kcal_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    fat = nutriments?.get("fat_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    saturatedFat = nutriments?.get("saturated-fat_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    carbohydrates = nutriments?.get("carbohydrates_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    sugars = nutriments?.get("sugars_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    proteins = nutriments?.get("proteins_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                    salt = nutriments?.get("salt_100g")?.jsonPrimitive?.content?.toDoubleOrNull()
                ),
                ingredients = product["ingredients_text_es"]?.jsonPrimitive?.content
                    ?: product["ingredients_text"]?.jsonPrimitive?.content
                    ?: "",
                isScanned = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ProductData()
        }
    }

    fun getNutriScoreColor(grade: String?): Long {
        return when (grade?.uppercase()) {
            "A" -> 0xFF00AA00
            "B" -> 0xFF85BB2F
            "C" -> 0xFFFFCC00
            "D" -> 0xFFEE8100
            "E" -> 0xFFE63E11
            else -> 0xFF888888
        }
    }

    fun getEcoScoreColor(grade: String?): Long {
        return when (grade?.uppercase()) {
            "A" -> 0xFF00AA00
            "B" -> 0xFF6CBF2E
            "C" -> 0xFF8DC63F
            "D" -> 0xFFF9A825
            "E" -> 0xFFE53935
            else -> 0xFF888888
        }
    }

    fun getEcoScoreLabel(grade: String?): String {
        return when (grade?.uppercase()) {
            "A" -> "Muy bajo impacto"
            "B" -> "Bajo impacto"
            "C" -> "Impacto medio"
            "D" -> "Alto impacto"
            "E" -> "Muy alto impacto"
            else -> "Sin datos"
        }
    }
}