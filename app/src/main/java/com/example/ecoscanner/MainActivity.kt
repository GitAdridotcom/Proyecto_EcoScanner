@file:Suppress("UNUSED_PARAMETER")
package com.example.ecoscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ecoscanner.ui.theme.EcoscannerTheme
import com.google.zxing.integration.android.IntentIntegrator
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NavigationState {
    var currentPage by mutableStateOf("Registro")
}

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startBarcodeScanner()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocation || coarseLocation) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    LocationHelper.getUserLocation(this@MainActivity)
                } catch (e: Exception) {
                    // Silent fail - will use default calculations
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        SupabaseManager.setActivity(this)
        
        val hasSession = SupabaseManager.client.auth.currentSessionOrNull() != null
        NavigationState.currentPage = if (hasSession) "escaner" else "Registro"
        
        setContent {
            EcoscannerTheme {
                EcoscannerApp(
                    onRequestCameraPermission = { requestCameraPermission() },
                    onRequestLocationPermission = { requestLocationPermission() }
                )
            }
        }
    }

    fun requestCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startBarcodeScanner()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun requestLocationPermission() {
        val fineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!fineLocation && !coarseLocation) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!fineLocation && coarseLocation) {
            // Only coarse granted, request fine
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Escanea el código de barras del producto")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    @SuppressLint("DefaultLocale")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val barcode = result.contents
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val product = withContext(Dispatchers.IO) {
                        OpenFoodFactsApi.getProductByBarcode(barcode)
                    }
                    if (product != null && product.isScanned) {
                        ProductRepository.updateProduct(product)

                        val userLocation = LocationHelper.getUserLocation(this@MainActivity)
                        val weightKgValue = product.weightKg ?: 1.0
                        val carbonResult = CarbonCalculator.calculateCarbonFootprintWithCoordinates(
                            productOrigin = product.origin,
                            userLat = userLocation?.latitude,
                            userLon = userLocation?.longitude,
                            weightKg = weightKgValue
                        )

                        val co2Saved = carbonResult.co2Kg.toDouble()
                        val kmReduced = carbonResult.kmDistance.toDouble()
                        
                        CarbonFootprintTracker.addScan(co2Saved, kmReduced)

                        try {
                            coroutineScope {
                                val result = StatsRepository.saveScan(
                                    supabase = SupabaseManager.client,
                                    productCode = product.code,
                                    productName = product.name,
                                    productBrand = product.brand,
                                    origin = carbonResult.originCountry,
                                    co2Kg = co2Saved,
                                    kmDistance = kmReduced
                                )
                                if (result.isFailure) {
                                    Toast.makeText(this@MainActivity, "Error guardando en Supabase", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                        val toastMessage = if (carbonResult.kmDistance > 0) {
                            "Producto: ${product.name}\nOrigen: ${carbonResult.originCountry}\nCO₂ estimado: ${String.format("%.2f", co2Saved)} kg"
                        } else {
                            "Producto: ${product.name}"
                        }
                        Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_LONG).show()

                        NavigationState.currentPage = "escaner"
                        setContent {
                            EcoscannerTheme {
                                EcoscannerApp(
                                    onRequestCameraPermission = { requestCameraPermission() },
                                    onRequestLocationPermission = { requestLocationPermission() }
                                )
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Producto no encontrado en la base de datos", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

@Composable
fun EcoscannerApp(
    onRequestCameraPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    val context = LocalContext.current
    val supabase = remember {
        createSupabaseClient(
            supabaseUrl = "https://xhwuqwfqbyplcohsbomq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhod3Vxd2ZxYnlwbGNvaHNib21xIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc0NjgyODgsImV4cCI6MjA5MzA0NDI4OH0.kqHHy1RyqHZHIqg7el9t61E-lvQdnlsI81HSmIPfpXk"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    LaunchedEffect(supabase) {
        SupabaseManager.setClient(supabase)
    }

    

    LaunchedEffect(Unit) {
        // Wait for auth state to be ready
        kotlinx.coroutines.delay(1000)
        try {
            val session = supabase.auth.currentSessionOrNull()
            if (session != null && session.user != null) {
                val result = StatsRepository.loadUserScans(supabase)
                if (result.isFailure) {
                    android.util.Log.e("EcoScanner", "Error loading scans: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EcoScanner", "Error: ${e.message}")
        }
    }

    var paginaSeleccionada by remember { mutableStateOf(NavigationState.currentPage) }

    LaunchedEffect(paginaSeleccionada) {
        NavigationState.currentPage = paginaSeleccionada
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (paginaSeleccionada) {
            "Registro" -> {
                Registro(
                    supabaseClient = supabase,
                    onClickInici = { paginaSeleccionada = "InicioSesion" },
                    onClickRegistrarse = { paginaSeleccionada = "escaner" }
                )
            }
            "InicioSesion" -> {
                InicioSesion(
                    supabaseClient = supabase,
                    onClickRegistrarme = { paginaSeleccionada = "Registro" },
                    onClickIniciar = { paginaSeleccionada = "escaner" }
                )
            }
            "escaner" -> {
                Escaner(
                    onClickEstadisticas = { paginaSeleccionada = "Estadisticas" },
                    onClickDatos = { paginaSeleccionada = "Datos" },
                    onClickHistorial = { paginaSeleccionada = "Historial" },
                    onClickCerrarSesion = { 
                        SupabaseManager.logout()
                    },
                    onClickLimpiarHistorial = {
                        GlobalScope.launch(Dispatchers.Main) {
                            StatsRepository.deleteAllUserScans(SupabaseManager.client)
                            CarbonFootprintTracker.reset()
                            Toast.makeText(context, "Historial y estadísticas eliminados", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenCamera = { onRequestCameraPermission() }
                )
            }
            "Estadisticas" -> {
                Estadisticas(
                    onVolverEscaner = { paginaSeleccionada = "escaner" },
                    onClickDatos = { paginaSeleccionada = "Datos" },
                    onClickHistorial = { paginaSeleccionada = "Historial" }
                )
            }
            "Datos" -> {
                Datos(
                    onVolverEscaner = { paginaSeleccionada = "escaner" },
                    onClickEstadisticas = { paginaSeleccionada = "Estadisticas" },
                    onClickHistorial = { paginaSeleccionada = "Historial" }
                )
            }
            "Historial" -> {
                HistorialScreen(
                    onVolverEscaner = { paginaSeleccionada = "escaner" },
                    onClickDatos = { paginaSeleccionada = "Datos" },
                    onClickEstadisticas = { paginaSeleccionada = "Estadisticas" }
                )
            }
        }
    }
}