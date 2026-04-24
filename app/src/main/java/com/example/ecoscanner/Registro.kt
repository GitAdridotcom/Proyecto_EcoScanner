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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

suspend fun registrouser(supabase: SupabaseClient, emailUser: String, passUser: String): Result<Unit> {
    return try {
        supabase.auth.signUpWith(Email) {
            email = emailUser
            password = passUser
            data = buildJsonObject {
                put("display_name", "Usuario Eco")
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Composable
fun Registro(
    supabaseClient: SupabaseClient,
    onClickInici: () -> Unit,
    onClickRegistrarse: () -> Unit
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
                contentDescription = "Logo EcoScanner",
                modifier = Modifier.scale(2f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Tu asistente de huella de transporte y Km 0",
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
                    modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            if (cargando) {
                CircularProgressIndicator(color = Color(0xFF1B5E20))
            } else {
                Button(
                    onClick = {
                        if (correo.isNotBlank() && contraseña.isNotBlank()) {
                            scope.launch {
                                cargando = true
                                mensaje = ""

                                val resultado = registrouser(supabaseClient, correo.trim(), contraseña.trim())

                                resultado.onSuccess {
                                    cargando = false
                                    onClickRegistrarse()
                                }.onFailure { error ->
                                    cargando = false
                                    mensaje = when {
                                        error.message?.contains("short") == true -> "Contraseña demasiado corta (mín. 6)"
                                        error.message?.contains("already") == true -> "Este correo ya está registrado"
                                        else -> "Error: ${error.localizedMessage ?: "Fallo al conectar con el servidor"}"
                                    }
                                }
                            }
                        } else {
                            mensaje = "Rellena todos los campos"
                        }
                    },
                    modifier = Modifier.width(280.dp)
                ) {
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.width(280.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Ya tengo una cuenta. ")
                Text(
                    text = "Iniciar Sesión",
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(0xFF1B5E20)
                    ),
                    modifier = Modifier.clickable { onClickInici() }
                )
            }
        }
    }
}