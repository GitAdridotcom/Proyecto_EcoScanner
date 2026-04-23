package com.example.ecoscanner

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf

class DetallViewModel {
    val fotoCapturada = mutableStateOf<Bitmap?>(null)
    val latitud = mutableStateOf("")
    val longitud = mutableStateOf("")
    val etiqueta = mutableStateOf("")
    val origenProducto = mutableStateOf("")
    val distanciaKm = mutableStateOf("")
    val co2Estimado = mutableStateOf("")

    fun netejarDades() {
        fotoCapturada.value = null
        latitud.value = ""
        longitud.value = ""
        etiqueta.value = ""
        origenProducto.value = ""
        distanciaKm.value = ""
        co2Estimado.value = ""
    }
}