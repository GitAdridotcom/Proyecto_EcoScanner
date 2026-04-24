package com.example.ecoscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

suspend fun iniciosesionn(supabase: SupabaseClient, emailUser: String, passUser: String): Result<Unit> {
    return try {
        supabase.auth.signInWith(Email) {
            email = emailUser
            password = passUser
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Composable
fun InicioSesion(
    supabaseClient: SupabaseClient,
    onClickRegistrarme: () -> Unit,
    onClickIniciar: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4CAF50), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.scale(2f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "Asistent de Petjada de Transport i Km 0",
                modifier = Modifier.width(200.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = contraseña,
                onValueChange = { contraseña = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.width(300.dp)
            )

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (cargando) {
                CircularProgressIndicator(color = Color(0xFF1B5E20))
            } else {
                Button(
                    onClick = {
                        if (correo.isNotBlank() && contraseña.isNotBlank()) {
                            scope.launch {
                                cargando = true
                                mensaje = ""
                                val resultado = iniciosesionn(supabaseClient, correo.trim(), contraseña.trim())

                                resultado.onSuccess {
                                    cargando = false
                                    onClickIniciar()
                                }.onFailure { error ->
                                    cargando = false
                                    mensaje = when {
                                        error.message?.contains("Invalid login credentials") == true -> "Correo o contraseña incorrectos"
                                        else -> "Error: ${error.localizedMessage ?: "Credenciales inválidas"}"
                                    }
                                }
                            }
                        } else {
                            mensaje = "Introduce tus credenciales"
                        }
                    },
                    modifier = Modifier.width(280.dp)
                ) {
                    Text("Iniciar Sesión")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Text("¿No tienes cuenta? ")
                Text(
                    text = "Regístrate aquí",
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(0xFF1B5E20)
                    ),
                    modifier = Modifier.clickable { onClickRegistrarme() }
                )
            }
        }
    }
}