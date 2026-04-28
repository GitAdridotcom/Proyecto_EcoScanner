package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecoscanner.ui.theme.*

@Composable
fun Datos(
    onVolverEscaner: () -> Unit,
    onClickEstadisticas: () -> Unit = {},
    onClickHistorial: () -> Unit = {}
) {
    val product by ProductRepository.lastScannedProduct.collectAsState()
    val currentProduct = product

    val originNormalizado = currentProduct?.origin?.let {
        CarbonCalculator.calculateCarbonFootprint(it, null).originCountry
    } ?: "Por determinar"

    val co2Estimado = currentProduct?.origin?.let {
        CarbonCalculator.calculateCarbonFootprint(it, null).co2Kg
    } ?: 0.0

    val ultimaDistanciaKm by CarbonFootprintTracker.lastKmReduced.collectAsState()
    val ultimaDistanciaKmValue = ultimaDistanciaKm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpringWood)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            "Datos del Producto",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            if (currentProduct?.isScanned == true) currentProduct?.name ?: "Producto escaneado"
            else "Escanea un producto para ver sus datos",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (currentProduct?.isScanned == true) {
            // Product Image Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!currentProduct.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = currentProduct.imageUrl,
                            contentDescription = "Imagen del producto",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(GrayNurse, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Como.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        currentProduct.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Como,
                        textAlign = TextAlign.Center
                    )
                    if (!currentProduct.brand.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            currentProduct.brand,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Como.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Environmental Impact Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Tradewind,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Impacto Ambiental",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Como
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CO2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFF3E0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Factory,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "CO₂ Estimado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Como.copy(alpha = 0.7f)
                                )
                                Text(
                                    "Transporte",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            String.format("%.2f kg", co2Estimado),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GrayNurse)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Route,
                                    contentDescription = null,
                                    tint = MossGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Distancia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Como.copy(alpha = 0.7f)
                                )
                                Text(
                                    "Origen a usuario",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${String.format("%.0f", ultimaDistanciaKmValue)} km",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Tradewind
                            )
                            val statusLabel = when {
                                ultimaDistanciaKmValue < 1000 -> "Cerca"
                                ultimaDistanciaKmValue < 5000 -> "Moderado"
                                else -> "Lejos"
                            }
                            Text(
                                statusLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    ultimaDistanciaKmValue < 1000 -> MossGreen
                                    ultimaDistanciaKmValue < 5000 -> Color(0xFFFFA000)
                                    else -> Color(0xFFD32F2F)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GrayNurse)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Origin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE3F2FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "País de Origen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Como.copy(alpha = 0.7f)
                                )
                                Text(
                                    "Fabricación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            originNormalizado,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scores Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Eco-Score
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = null,
                            tint = MossGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Eco-Score",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Como
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val ecoGrade = currentProduct.ecoscoreGrade
                        Card(
                            modifier = Modifier.size(48.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(OpenFoodFactsApi.getEcoScoreColor(ecoGrade))
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    ecoGrade ?: "-",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            OpenFoodFactsApi.getEcoScoreLabel(ecoGrade),
                            style = MaterialTheme.typography.bodySmall,
                            color = Como.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Nutri-Score
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nutri-Score",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Como
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val nutriGrade = currentProduct.nutriscoreGrade
                        Card(
                            modifier = Modifier.size(48.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(OpenFoodFactsApi.getNutriScoreColor(nutriGrade))
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    nutriGrade?.uppercase() ?: "-",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            OpenFoodFactsApi.getNutriScoreLabel(nutriGrade),
                            style = MaterialTheme.typography.bodySmall,
                            color = Como.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nutritional Info Card
            if (currentProduct.nutriments.calories != null ||
                currentProduct.nutriments.fat != null ||
                currentProduct.nutriments.proteins != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Tradewind,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Información Nutricional",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Como
                            )
                            Text(
                                " (por 100g)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Como.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val nutrients = listOf(
                            Triple("Calorías", currentProduct.nutriments.calories, "kcal"),
                            Triple("Grasas", currentProduct.nutriments.fat, "g"),
                            Triple("Grasas saturadas", currentProduct.nutriments.saturatedFat, "g"),
                            Triple("Hidratos de carbono", currentProduct.nutriments.carbohydrates, "g"),
                            Triple("Azúcares", currentProduct.nutriments.sugars, "g"),
                            Triple("Proteínas", currentProduct.nutriments.proteins, "g"),
                            Triple("Sal", currentProduct.nutriments.salt, "g")
                        )

                        nutrients.filter { it.second != null }.forEach { (label, value, unit) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Como.copy(alpha = 0.7f)
                                )
                                Text(
                                    "${String.format("%.1f", value)} $unit",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Como
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Categories Card
            if (!currentProduct.categories.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = Tradewind,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Categorías",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Como
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            currentProduct.categories,
                            style = MaterialTheme.typography.bodySmall,
                            color = Como.copy(alpha = 0.7f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Barcode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Como,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Código de barras",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Como.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        currentProduct.code,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Como
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        } else {
            // No product scanned
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Como.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin producto escaneado",
                        style = MaterialTheme.typography.titleMedium,
                        color = Como
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Escanea un producto con el código de barras para ver sus datos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Como.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onVolverEscaner() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Tradewind),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear Producto", modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}