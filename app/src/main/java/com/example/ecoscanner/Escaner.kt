package com.example.ecoscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ecoscanner.R
import com.example.ecoscanner.ui.theme.*

@Composable
fun Escaner(
    onClickEstadisticas: () -> Unit,
    onClickDatos: () -> Unit,
    onOpenCamera: () -> Unit,
    onClickHistorial: () -> Unit = {},
    onClickCerrarSesion: () -> Unit = {},
    onClickLimpiarHistorial: () -> Unit = {},
    onClickPoliticaPrivacidad: () -> Unit = {}
) {
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
                        label = { Text("Escaner") },
                        selected = true,
                        onClick = { },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MossGreen.copy(alpha = 0.3f)
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Datos del Producto") },
                        selected = false,
                        onClick = { onClickDatos() }
                    )

                    NavigationDrawerItem(
                        label = { Text("Estadisticas") },
                        selected = false,
                        onClick = { onClickEstadisticas() }
                    )

                    NavigationDrawerItem(
                        label = { Text("Historial") },
                        selected = false,
                        onClick = { onClickHistorial() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GrayNurse)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onClickLimpiarHistorial() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(
                            Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar Historial")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("Política de Privacidad") },
                        selected = false,
                        onClick = { onClickPoliticaPrivacidad() },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MossGreen.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onClickCerrarSesion() },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Como)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    ) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SpringWood, GrayNurse)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(GrayNurse, Color.White)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo EcoScanner",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Escanea productos para conocer su\nimpacto ambiental y valor nutricional",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Como.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { onOpenCamera() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Tradewind
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Escanear Producto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Cómo funciona",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Tradewind
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoItem("1", "Escanea el código de barras")
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoItem("2", "Ve información del producto")
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoItem("3", "Consulta el impacto ambiental")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MossGreen.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Como,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Desliza a la derecha",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Como
                                )
                                Text(
                                    "Accede al menú para ver tus estadísticas y datos del producto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Como.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoItem(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Tradewind),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.7f)
        )
    }
}