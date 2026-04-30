package com.example.ecoscanner

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UserScan(
    val id: Int = 0,
    val user_id: String = "",
    val product_code: String = "",
    val product_name: String? = null,
    val product_brand: String? = null,
    val origin: String? = null,
    val co2_kg: Double = 0.0,
    val km_distance: Double = 0.0,
    val created_at: String = ""
)

object StatsRepository {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userScans = MutableStateFlow<List<UserScan>>(emptyList())
    val userScans: StateFlow<List<UserScan>> = _userScans.asStateFlow()

    private var _totalCo2 = MutableStateFlow(0.0)
    val totalCo2: StateFlow<Double> = _totalCo2.asStateFlow()

    private var _totalKm = MutableStateFlow(0.0)
    val totalKm: StateFlow<Double> = _totalKm.asStateFlow()

    private var _scanCount = MutableStateFlow(0)
    val scanCount: StateFlow<Int> = _scanCount.asStateFlow()

    suspend fun saveScan(
        supabase: SupabaseClient,
        productCode: String,
        productName: String?,
        productBrand: String?,
        origin: String?,
        co2Kg: Double,
        kmDistance: Double
    ): Result<UserScan> {
        return try {
            _isLoading.value = true

            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User not logged in")

            val scanData = buildJsonObject {
                put("user_id", userId)
                put("product_code", productCode)
                put("product_name", productName ?: "")
                put("product_brand", productBrand ?: "")
                put("origin", origin ?: "")
                put("co2_kg", co2Kg)
                put("km_distance", kmDistance)
            }

            val response = supabase.postgrest["user_scans"].insert(scanData)
            val inserted = response.decodeSingle<UserScan>()

            _totalCo2.value += co2Kg
            _totalKm.value += kmDistance
            _scanCount.value += 1
            _userScans.value = _userScans.value + inserted

            Result.success(inserted)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadUserScans(supabase: SupabaseClient): Result<List<UserScan>> {
        return try {
            _isLoading.value = true

            val session = supabase.auth.currentSessionOrNull()
            val currentUserId = session?.user?.id
                ?: throw IllegalStateException("User not logged in")

            val allScans = supabase.postgrest["user_scans"]
                .select()
                .decodeList<UserScan>()

            // Filter by exact match with trimmed strings
            val userScans = allScans.filter { scan ->
                scan.user_id.trim() == currentUserId.trim()
            }

            _userScans.value = userScans
            _scanCount.value = userScans.size
            
            _totalCo2.value = userScans.sumOf { it.co2_kg }
            _totalKm.value = userScans.sumOf { it.km_distance }

            Result.success(userScans)
        } catch (e: Exception) {
            resetLocalForced()
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun resetLocalForced() {
        _userScans.value = emptyList()
        _totalCo2.value = 0.0
        _totalKm.value = 0.0
        _scanCount.value = 0
    }

    fun resetLocal() {
        _userScans.value = emptyList()
        _totalCo2.value = 0.0
        _totalKm.value = 0.0
        _scanCount.value = 0
    }

    fun loadFromMemory() {
        _totalCo2.value = CarbonFootprintTracker.totalCo2Saved.value
        _totalKm.value = CarbonFootprintTracker.totalKmReduced.value
        _scanCount.value = CarbonFootprintTracker.scanCount.value
    }

    suspend fun deleteAllUserScans(supabase: SupabaseClient): Result<Unit> {
        return try {
            supabase.postgrest.rpc("delete_user_scans")
            resetLocal()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}