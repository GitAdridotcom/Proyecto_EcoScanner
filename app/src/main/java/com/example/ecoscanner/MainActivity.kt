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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        SupabaseManager.setActivity(this)
        
        val hasSession = SupabaseManager.client.auth.currentSessionOrNull() != null
        NavigationState.currentPage = if (hasSession) "escaner" else "Registro"
        
        setContent {
            EcoscannerTheme {
                EcoscannerApp(
                    onRequestCameraPermission = { requestCameraPermission() }
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

                        val userLocation = LocationHelper.getLastKnownLocation()
                        val weightKgValue = product.weightKg ?: 1.0
                        val carbonResult = CarbonCalculator.calculateCarbonFootprintWithCoordinates(
                            productOrigin = product.origin,
                            userLat = userLocation?.latitude,
                            userLon = userLocation?.longitude,
                            weightKg = weightKgValue
                        )

                        val co2Saved = if (carbonResult.co2Kg.toDouble() > 0.0) carbonResult.co2Kg.toDouble() else 0.5
                        val kmReduced = carbonResult.kmDistance.toDouble()
                        
                        CarbonFootprintTracker.addScan(co2Saved, kmReduced)

                        try {
                            coroutineScope {
                                StatsRepository.saveScan(
                                    supabase = SupabaseManager.client,
                                    productCode = product.code,
                                    productName = product.name,
                                    productBrand = product.brand,
                                    origin = carbonResult.originCountry,
                                    co2Kg = co2Saved,
                                    kmDistance = kmReduced
                                )
                            }
                        } catch (e: Exception) {
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
                                    onRequestCameraPermission = { requestCameraPermission() }
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
fun EcoscannerApp(onRequestCameraPermission: () -> Unit) {
    val context = LocalContext.current
    val supabase = remember {
        createSupabaseClient(
            supabaseUrl = "https://buodriyoosvuxwclzcyh.supabase.co",
            supabaseKey = "sb_publishable_26_DWNG8dxnkBbr8bf1aFg_zjgz1Mav"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    LaunchedEffect(supabase) {
        SupabaseManager.setClient(supabase)
    }

    LaunchedEffect(Unit) {
        try {
            LocationHelper.getUserLocation(context)
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        try {
            StatsRepository.loadUserScans(supabase)
        } catch (e: Exception) {
            // Si falla, limpiar datos en lugar de cargar datos antiguos
            StatsRepository.resetLocal()
            CarbonFootprintTracker.reset()
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
                        StatsRepository.resetLocal()
                        CarbonFootprintTracker.reset()
                        Toast.makeText(context, "Historial y estadísticas limpiados", Toast.LENGTH_SHORT).show()
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