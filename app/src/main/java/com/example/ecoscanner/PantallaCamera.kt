package com.example.ecoscanner

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import com.google.android.gms.location.LocationServices

@Composable
fun PantallaCamera(onTornar: () -> Unit) {

    val context = LocalContext.current
    val viewModel = remember { DetallViewModel() }
    var permisCamara by remember { mutableStateOf(false) }

    val launcherPermis = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedit ->
        permisCamara = concedit

        if (!concedit) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val launcherPermisGps = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedit ->
        if (concedit) {
            LlegirGps(context, viewModel)
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        if (bitmap != null) {
            viewModel.fotoCapturada.value = bitmap
        } else {
            Toast.makeText(context, "No se ha capturado ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun obrirMapa(context: Context, lat: Double, lon: Double, etiqueta: String) {

        val uriString = "geo:0,0?q=$lat,$lon($etiqueta)"
        val gmmIntentUri = uriString.toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No tienes ninguna app de mapas instalada", Toast.LENGTH_SHORT).show()
        }
    }

    fun obrirCerca(context: Context, query: String) {

        val uriString = "geo:0,0?q=$query"
        val gmmIntentUri = uriString.toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No tienes ninguna app de mapas instalada", Toast.LENGTH_SHORT).show()
        }
    }

    fun parseDoubleSafe(txt: String): Double? {
        return txt.replace(',', '.').toDoubleOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Cámara y GPS", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(18.dp))

        if (viewModel.fotoCapturada.value != null) {

            Image(
                bitmap = viewModel.fotoCapturada.value!!.asImageBitmap(),
                contentDescription = "Foto hecha",
                modifier = Modifier.size(260.dp)
            )

        } else {

            Image(
                painter = painterResource(id = R.drawable.cameraicon),
                contentDescription = "Falta foto",
                modifier = Modifier.size(180.dp)
            )
        }

        AnimatedVisibility(visible = viewModel.fotoCapturada.value != null) {
            Button(
                onClick = {
                    guardarFotoAGaleria(context, viewModel.fotoCapturada.value!!)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Guardar en galería")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                if (permisCamara) {
                    launcherCamara.launch(null)
                } else {
                    launcherPermis.launch(Manifest.permission.CAMERA)
                }
            }
        ) {
            Text("Hacer foto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val tePermis = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED

                if (tePermis) {
                    LlegirGps(context, viewModel)
                } else {
                    launcherPermisGps.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        ) {
            Text("Leer mi GPS ahora")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = viewModel.latitud.value,
            onValueChange = { viewModel.latitud.value = it },
            label = { Text("Latitud") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = viewModel.longitud.value,
            onValueChange = { viewModel.longitud.value = it },
            label = { Text("Longitud") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = viewModel.etiqueta.value,
            onValueChange = { viewModel.etiqueta.value = it },
            label = { Text("Nombre del lugar") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = viewModel.origenProducto.value,
            onValueChange = { viewModel.origenProducto.value = it },
            label = { Text("Origen del producto") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val latUsuari = parseDoubleSafe(viewModel.latitud.value)
                val lonUsuari = parseDoubleSafe(viewModel.longitud.value)

                if (latUsuari == null || lonUsuari == null) {
                    Toast.makeText(context, "Primero lee el GPS", Toast.LENGTH_SHORT).show()
                } else {
                    val origen = viewModel.origenProducto.value.trim().lowercase()

                    val coordenadesOrigen = when (origen) {
                        "españa", "espanya", "madrid" -> Pair(40.4168, -3.7038)
                        "francia", "paris" -> Pair(48.8566, 2.3522)
                        "italia", "roma" -> Pair(41.9028, 12.4964)
                        "alemania", "berlin" -> Pair(52.5200, 13.4050)
                        "portugal", "lisboa" -> Pair(38.7223, -9.1393)
                        else -> null
                    }

                    if (coordenadesOrigen == null) {
                        Toast.makeText(
                            context,
                            "Origen no reconocido. Prueba con España, Francia, Italia, Alemania o Portugal",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val distancia = calcularDistanciaKm(
                            coordenadesOrigen.first,
                            coordenadesOrigen.second,
                            latUsuari,
                            lonUsuari
                        )

                        val co2 = calcularCo2(distancia)

                        viewModel.distanciaKm.value = String.format("%.2f", distancia)
                        viewModel.co2Estimado.value = String.format("%.2f", co2)
                    }
                }
            }
        ) {
            Text("Calcular distancia y CO2")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.distanciaKm.value.isNotEmpty()) {
            Text("Distancia estimada: ${viewModel.distanciaKm.value} km")
        }

        if (viewModel.co2Estimado.value.isNotEmpty()) {
            Text("CO2 estimado: ${viewModel.co2Estimado.value} kg")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {

            Button(
                onClick = {

                    val lat = parseDoubleSafe(viewModel.latitud.value)
                    val lon = parseDoubleSafe(viewModel.longitud.value)

                    if (lat == null || lon == null) {

                        Toast.makeText(context, "Coordenadas incorrectas", Toast.LENGTH_SHORT).show()

                    } else {

                        val etiquetaFinal =
                            if (viewModel.etiqueta.value.isBlank())
                                "Ubicación Entrega"
                            else
                                viewModel.etiqueta.value

                        obrirMapa(context, lat, lon, etiquetaFinal)
                    }
                }
            ) {
                Text("Ver en el mapa")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    obrirCerca(context, "gasolinera")
                }
            ) {
                Text("Estoy perdido")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onTornar) {
            Text("Volver")
        }
    }
}

fun LlegirGps(context: Context, viewModel: DetallViewModel) {

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.latitud.value = location.latitude.toString()
                    viewModel.longitud.value = location.longitude.toString()
                    Toast.makeText(context, "Ubicación capturada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se ha podido obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error leyendo el GPS", Toast.LENGTH_SHORT).show()
            }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Faltan permisos de ubicación", Toast.LENGTH_SHORT).show()
    }
}

fun guardarFotoAGaleria(context: Context, bitmap: Bitmap) {
    val valorsFitxer = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "Entrega_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EcoScanner")
    }

    val resolver = context.contentResolver
    val uriImatge = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valorsFitxer)

    if (uriImatge != null) {
        try {
            resolver.openOutputStream(uriImatge)?.use { canalSortida ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, canalSortida)
            }
            Toast.makeText(context, "Imagen guardada en la galería", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}

fun calcularDistanciaKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val radioTierra = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return radioTierra * c
}

fun calcularCo2(distanciaKm: Double): Double {
    return distanciaKm * 0.12
}