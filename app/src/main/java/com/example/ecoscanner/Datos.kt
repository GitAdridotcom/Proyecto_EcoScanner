package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Datos(
    onVolverEscaner: () -> Unit,
    onClickEstadisticas: () -> Unit = {},
    onClickHistorial: () -> Unit = {}
) {
    val product by ProductRepository.lastScannedProduct.collectAsState()
    val currentProduct = product

    val originNormalizado = if (currentProduct?.origin != null) {
        CarbonCalculator.calculateCarbonFootprint(currentProduct.origin, null).originCountry
    } else {
        "España"
    }

    val co2Estimado = if (currentProduct?.origin != null) {
        CarbonCalculator.calculateCarbonFootprint(currentProduct.origin, null).co2Kg
    } else {
        0.0
    }

    val ultimaDistanciaKm by CarbonFootprintTracker.lastKmReduced.collectAsState()
    val ultimaDistanciaKmValue = ultimaDistanciaKm

    // State for eco alternatives
    var showAlternativesSheet by remember { mutableStateOf(false) }
    var alternatives by remember { mutableStateOf<List<ProductData>>(emptyList()) }
    var isLoadingAlternatives by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                                    .background(AmberAlert.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Factory,
                                    contentDescription = null,
                                    tint = AmberAlert,
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
                            color = AmberAlert
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
                                    .background(MossGreen. copy(0.2f), CircleShape),
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
                                    ultimaDistanciaKmValue < 1_000 -> MossGreen
                                    ultimaDistanciaKmValue < 5_000 -> AmberAlert
                                    else -> CoralAlert
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
                                    .background(TealInfo.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    tint = TealInfo,
                                    modifier = Modifier.size(20. dp)
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
                            color = TealInfo
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
                            tint = Tradewind,
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

            // Carbon Footprint Card from API
            val hasCarbonData = currentProduct?.carbonFootprint != null || !currentProduct?.carbonFootprintEquivalent.isNullOrEmpty()
            if (currentProduct?.isScanned == true && hasCarbonData) {
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
                                "Huella de carbono",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Como
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        currentProduct.carbonFootprint?.let { cf ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Impacto climático",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Como.copy(alpha = 0.7f)
                                )
                                Text(
                                    "${String.format("%.1f", cf)} kg CO₂/kg",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Tradewind
                                )
                            }
                        }

                        currentProduct.carbonFootprintEquivalent?.let { eq ->
                            if (eq.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Como.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Equivalencia",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Tradewind
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    eq,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Tradewind
                                )
                            }
                        }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Eco Alternatives Button
            val currentEcoScore = currentProduct?.ecoscoreGrade
            if (currentProduct?.isScanned == true && !currentEcoScore.isNullOrEmpty()) {
                Button(
                    onClick = {
                        val category = currentProduct.categories?.split(",")?.firstOrNull() ?: ""
                        if (category.isNotEmpty()) {
                            isLoadingAlternatives = true
                            showAlternativesSheet = true
                            scope.launch {
                                alternatives = OpenFoodFactsApi.searchAlternatives(
                                    category = category,
                                    currentEcoScore = currentEcoScore,
                                    limit = 15
                                )
                                isLoadingAlternatives = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MossGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver alternativas ecológicas", modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
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

    // Modal Bottom Sheet for Eco Alternatives
    if (showAlternativesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlternativesSheet = false },
            containerColor = SpringWood
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Alternativas Ecológicas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Como
                    )
                    IconButton(onClick = { showAlternativesSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Productos con mejor o igual Eco-Score en la misma categoría",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Como.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingAlternatives) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Tradewind)
                    }
                } else if (alternatives.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Como.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No se encontraron alternativas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Como
                            )
                            Text(
                                "Intenta con otro producto",
                                style = MaterialTheme.typography.bodySmall,
                                color = Como.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alternatives) { alt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Product Image
                                    if (!alt.imageUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = alt.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(GrayNurse, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = null,
                                                tint = Como.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            alt.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Como,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!alt.brand.isNullOrEmpty()) {
                                            Text(
                                                alt.brand,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Como.copy(alpha = 0.6f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Eco Score Badge
                                            Card(
                                                modifier = Modifier.size(28.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(OpenFoodFactsApi.getEcoScoreColor(alt.ecoscoreGrade))
                                                )
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        alt.ecoscoreGrade ?: "?",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Origen: ${alt.origin?.take(20) ?: "?"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Como.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showAlternativesSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Tradewind)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}