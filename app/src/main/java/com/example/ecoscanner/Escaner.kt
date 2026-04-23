package com.example.ecoscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp


@Composable
fun Escaner(onClickEstadisticas: () -> Unit, onClickCamaraGps: () -> Unit) {
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
                        label = { Text("Escaner") },
                        selected = true,
                        onClick = { /* ... */ }
                    )

                    NavigationDrawerItem(
                        label = { Text("Estadisticas") },
                        selected = false,
                        onClick = { onClickEstadisticas() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Premium")
                                Text(
                                    "VER PLANES",
                                    style = TextStyle(textDecoration = TextDecoration.Underline),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Configuración") },
                        onClick = { /* ... */ }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {/**/},
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    ) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Desliza aquí hacia la derecha para ver el menú", modifier = Modifier.padding(16.dp))

                // Box para el logo con un tamaño definido
                Box(
                    modifier = Modifier
                        .width(350.dp)
                        .height(600.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cameraicon),
                        contentDescription = "Logo de EcoScanner",
                        modifier = Modifier.size(150.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier.width(100.dp).height(50.dp).background(Color(0xFF388E3C)).padding(10.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text("Ubicació: ", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { onClickCamaraGps() }) {
                    Text("Abrir cámara y GPS")
                }

                Spacer(modifier = Modifier.height(26.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.width(200.dp).height(100.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.cameraa),
                            contentDescription = "",
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}