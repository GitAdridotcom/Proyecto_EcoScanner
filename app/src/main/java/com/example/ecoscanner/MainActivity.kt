package com.example.ecoscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ecoscanner.ui.theme.EcoscannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App() }
    }
}

@Composable
fun App() {
    var pantallaActual by remember { mutableStateOf("registro") }

    when (pantallaActual) {
        "registro" -> {
            Registro(
                onClickInici = { pantallaActual = "inicioSesion" }
            )
        }

        "inicioSesion" -> {
            InicioSesion(
                onClickRegistrarme = { pantallaActual = "registro" }
            )
        }
    }
}
