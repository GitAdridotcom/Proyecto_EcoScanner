package com.example.ecoscanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
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

            // Determine weight in kilograms if available
            val weightKgValue = run {
                val w1 = product["weight"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val w2 = product["product_weight_grams"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val grams = w1 ?: w2
                grams?.div(1000.0)
            }
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
                weightKg = weightKgValue,
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

    fun getNutriScoreLabel(grade: String?): String {
        return when (grade?.uppercase()) {
            "A" -> "Muy saludable"
            "B" -> "Saludable"
            "C" -> "Moderado"
            "D" -> "Poco saludable"
            "E" -> "Poco saludable"
            else -> "Sin datos"
        }
    }

    suspend fun searchAlternatives(
        category: String,
        currentEcoScore: String,
        limit: Int = 10
    ): List<ProductData> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "https://world.openfoodfacts.org/cgi/search.pl" +
                "?search_terms=${java.net.URLEncoder.encode(category, "UTF-8")}" +
                "&search_simple=1" +
                "&action=process" +
                "&json=1" +
                "&page_size=$limit"

            val url = URL(searchUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseSearchResults(response, currentEcoScore)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseSearchResults(json: String, currentEcoScore: String): List<ProductData> {
        return try {
            val root = kotlinx.serialization.json.Json.parseToJsonElement(json)
            val products = root.jsonObject["products"]?.jsonArray ?: return emptyList()

            val scoreOrder = mapOf("A" to 1, "B" to 2, "C" to 3, "D" to 4, "E" to 5)
            val currentScore = scoreOrder[currentEcoScore.uppercase()] ?: 6

            products.mapNotNull { element ->
                try {
                    val product = element.jsonObject
                    val ecoGrade = product["ecoscore_grade"]?.jsonPrimitive?.content?.uppercase()
                    val productScore = scoreOrder[ecoGrade] ?: 6

                    // Only return products with better or equal eco-score
                    if (productScore <= currentScore) {
                        val nutriments = product["nutriments"]?.jsonObject
                        ProductData(
                            code = product["code"]?.jsonPrimitive?.content ?: "",
                            name = product["product_name_es"]?.jsonPrimitive?.content
                                ?: product["product_name"]?.jsonPrimitive?.content
                                ?: return@mapNotNull null,
                            brand = product["brands"]?.jsonPrimitive?.content ?: "",
                            imageUrl = product["image_small_url"]?.jsonPrimitive?.content
                                ?: product["image_url"]?.jsonPrimitive?.content,
                            origin = product["origins"]?.jsonPrimitive?.content
                                ?: product["origin"]?.jsonPrimitive?.content
                                ?: product["countries"]?.jsonPrimitive?.content,
                            categories = product["categories_es"]?.jsonPrimitive?.content
                                ?: product["categories"]?.jsonPrimitive?.content,
                            nutriscoreGrade = product["nutriscore_grade"]?.jsonPrimitive?.content?.uppercase(),
                            ecoscoreGrade = ecoGrade,
                            nutriments = NutrientsData(
                                calories = nutriments?.get("energy-kcal_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                fat = nutriments?.get("fat_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                saturatedFat = nutriments?.get("saturated-fat_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                carbohydrates = nutriments?.get("carbohydrates_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                sugars = nutriments?.get("sugars_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                proteins = nutriments?.get("proteins_100g")?.jsonPrimitive?.content?.toDoubleOrNull(),
                                salt = nutriments?.get("salt_100g")?.jsonPrimitive?.content?.toDoubleOrNull()
                            ),
                            isScanned = false
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { scoreOrder[it.ecoscoreGrade] ?: 6 }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
