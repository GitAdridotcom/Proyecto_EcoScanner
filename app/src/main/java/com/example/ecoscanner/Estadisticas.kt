package com.example.ecoscanner

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ecoscanner.ui.theme.*

@SuppressLint("DefaultLocale")
@Composable
fun Estadisticas(onVolverEscaner: () -> Unit) {
    val co2Saved by CarbonFootprintTracker.totalCo2Saved.collectAsState()
    val kmReduced by CarbonFootprintTracker.totalKmReduced.collectAsState()
    val scanCount by CarbonFootprintTracker.scanCount.collectAsState()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "EcoScanner Menu",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = Como
                    )
                    HorizontalDivider(color = GrayNurse)
                    Text("Secciones", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = Como)
                    NavigationDrawerItem(
                        label = { Text("Escáner") },
                        selected = false,
                        onClick = { onVolverEscaner() }
                    )
                    NavigationDrawerItem(
                        label = { Text("Estadísticas") },
                        selected = true,
                        onClick = { },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MossGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SpringWood)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Como
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Resumen de tu impacto ambiental",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Como.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        title = "CO₂ Estimado",
                        value = String.format("%.2f", co2Saved),
                        unit = "kg",
                        color = Como
                    )

                    StatCard(
                        title = "Km Estimados",
                        value = String.format("%.1f", kmReduced),
                        unit = "km",
                        color = Como
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrayNurse)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total escaneos",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "$scanCount productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Como
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Resumen de impacto estimado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Como
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Impacto estimado de transporte: ${
                                String.format("%.2f", co2Saved)
                            } kg de CO₂ para los productos escaneados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Como.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onVolverEscaner() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver al Escáner")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}