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