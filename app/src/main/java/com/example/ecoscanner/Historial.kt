package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoscanner.ui.theme.*

@Composable
fun HistorialScreen(
    onVolverEscaner: () -> Unit,
    onClickDatos: () -> Unit = {},
    onClickEstadisticas: () -> Unit = {}
) {
    val scans by StatsRepository.userScans.collectAsState()
    val isLoading by StatsRepository.isLoading.collectAsState()
    
    val scannedList = scans

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpringWood)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Historial de Escaneos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            if (scannedList.isEmpty()) "No hay escaneos guardados" else "${scannedList.size} productos escaneados",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Tradewind
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (scannedList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Como.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin escaneos en el historial",
                        style = MaterialTheme.typography.titleMedium,
                        color = Como
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Escanea productos para verlos aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Como.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            scannedList.forEach { scan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                scan.product_name ?: scan.product_code ?: "Producto desconocido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Como,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${String.format("%.1f", scan.co2_kg)} kg",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "CO₂",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                        
                        if (scan.product_brand != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                scan.product_brand,
                                style = MaterialTheme.typography.bodySmall,
                                color = Como.copy(alpha = 0.7f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Como.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Origen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Como.copy(alpha = 0.6f)
                                )
                                Text(
                                    scan.origin ?: "Sin datos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Distancia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Como.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${String.format("%.1f", scan.km_distance)} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Tradewind
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Fecha",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Como.copy(alpha = 0.6f)
                                )
                                Text(
                                    scan.created_at?.take(10) ?: "-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onVolverEscaner() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Tradewind)
        ) {
            Text("Volver al Escáner", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}