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
import com.example.ecoscanner.ui.theme.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

suspend fun loginUser(supabase: SupabaseClient, email: String, password: String): Result<Unit> {
    return try {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun registerUser(supabase: SupabaseClient, email: String, password: String): Result<Unit> {
    return try {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
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
fun AuthScreen(
    supabaseClient: SupabaseClient,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Tradewind, Color.White)
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
                text = "Haz visible lo invisible del consumo.",
                modifier = Modifier.width(200.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.width(300.dp)
            )

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Como)
            } else {
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            message = ""
                            scope.launch {
                                val result = if (isLoginMode) {
                                    loginUser(supabaseClient, email.trim(), password.trim())
                                } else {
                                    registerUser(supabaseClient, email.trim(), password.trim())
                                }

                                result.onSuccess {
                                    isLoading = false
                                    onAuthSuccess()
                                }.onFailure { error ->
                                    isLoading = false
                                    message = when {
                                        isLoginMode && error.message?.contains("Invalid login credentials") == true ->
                                            "Correo o contraseña incorrectos"
                                        !isLoginMode && error.message?.contains("short") == true ->
                                            "Contraseña demasiado corta (mín. 6)"
                                        !isLoginMode && error.message?.contains("already") == true ->
                                            "Este correo ya está registrado"
                                        else -> "Error: ${error.localizedMessage ?: "Fallo al conectar"}"
                                    }
                                }
                            }
                        } else {
                            message = "Rellena todos los campos"
                        }
                    },
                    modifier = Modifier.width(280.dp)
                ) {
                    Text(if (isLoginMode) "Iniciar Sesión" else "Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.width(280.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(if (isLoginMode) "¿No tienes cuenta? " else "Ya tengo una cuenta. ")
                Text(
                    text = if (isLoginMode) "Regístrate aquí" else "Iniciar Sesión",
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Tradewind
                    ),
                    modifier = Modifier.clickable { isLoginMode = !isLoginMode }
                )
            }
        }
    }
}