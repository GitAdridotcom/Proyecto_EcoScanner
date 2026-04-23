package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Estadisticas(onVolverEscaner: () -> Unit) {

    // 1. Variables de estado
    var nombreProducto by remember { mutableStateOf("Cargando...") }
    var paisOrigen by remember { mutableStateOf("") }
    var co2 by remember { mutableStateOf(0.0) }
    var error by remember { mutableStateOf("") }

    // 2. Llamada a las APIs, va aquí arriba, fuera de cualquier Box o Column
    LaunchedEffect(Unit) {
        try {
            val productoResponse = withContext(Dispatchers.IO) {
                RetrofitClient.openFoodFacts.getProducto("8410180103507")
            }
            nombreProducto = productoResponse.product.product_name
            paisOrigen = productoResponse.product.countries_tags.firstOrNull() ?: "Desconocido"

            val co2Response = withContext(Dispatchers.IO) {
                RetrofitClient.climatiq.calcularCO2(
                    token = "Bearer P1CVT47HV575KEXZM18MP8T4P0",
                    body = ClimatiqRequest(
                        emission_factor = EmissionFactor(),
                        parameters = Parameters(distance = 500.0)
                    )
                )
            }
            co2 = co2Response.co2e

        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            error = "Error: $errorBody"
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        }
    }

    // 3. UI
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "EcoScanner Menu",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Text("Secciones", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("Escáner") },
                        selected = false,
                        onClick = { onVolverEscaner() }
                    )
                    NavigationDrawerItem(
                        label = { Text("Estadísticas") },
                        selected = true,
                        onClick = { }
                    )
                }
            }
        }
    ) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Estadísticas", style = MaterialTheme.typography.headlineMedium)
                    Text("Aquí verás el historial de tus escaneos", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. Mostrar datos obtenidos de las APIs
                    if (error.isNotEmpty()) {
                        Text(error, color = Color.Red)
                    } else {
                        Text("Producto: $nombreProducto")
                        Text("Origen: $paisOrigen")
                        Text("CO₂ estimado: $co2 kg")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onVolverEscaner() },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("Tornar a Escanear")
                    }
                }
            }
        }
    }
}