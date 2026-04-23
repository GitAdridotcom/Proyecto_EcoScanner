package com.example.ecoscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.ecoscanner.ui.theme.EcoscannerTheme
// IMPORTS CORRECTOS DE SUPABASE
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoscannerTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    // Es mejor usar remember para que el cliente no se recree en cada recomposición
    val supabase = remember {
        createSupabaseClient(
            supabaseUrl = "https://buodriyoosvuxwclzcyh.supabase.co",
            supabaseKey = "sb_publishable_26_DWNG8dxnkBbr8bf1aFg_zjgz1Mav"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    var paginaSeleccionada by remember { mutableStateOf("Registro") }

    // Usamos un bloque 'when' para que el código sea más limpio (estilo Kotlin)
    when (paginaSeleccionada) {
        "Registro" -> {
            Registro(
                onClickInici = { paginaSeleccionada = "InicioSesion" },
                onClickRegistrarse = { paginaSeleccionada = "escaner" }
            )
        }
        "InicioSesion" -> {
            InicioSesion(
                onClickRegistrarme = { paginaSeleccionada = "Registro" },
                onClickIniciar = { paginaSeleccionada = "escaner" }
            )
        }
        "escaner" -> {
            Escaner(
                onClickEstadisticas = { paginaSeleccionada = "Estadisticas" },
                onClickCamaraGps = { paginaSeleccionada = "PantallaCamera" }
            )
        }
        "Estadisticas" -> {
            Estadisticas(onVolverEscaner = { paginaSeleccionada = "escaner" })
        }
        "PantallaCamera" -> {
            PantallaCamera(onTornar = { paginaSeleccionada = "escaner" })
        }
    }
}