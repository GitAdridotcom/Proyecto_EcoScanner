package com.example.ecoscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable

fun InicioSesion(onClickRegistrarme: () -> Unit){
    var correo by remember { mutableStateOf("") }

    var contraseña by remember { mutableStateOf("") }

    Box(modifier = Modifier .fillMaxSize() .background(Color.White)){

        Column(
            modifier = Modifier .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {

            Image(painter = painterResource(R.drawable.logo), contentDescription = "", Modifier.scale(2f))
            Spacer(modifier = Modifier .height(40.dp))
            Text("Asistent de Petjada de Transport i Km 0", modifier = Modifier .width(150.dp), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier .height(20.dp))
            OutlinedTextField(value = correo, onValueChange = {correo = it}, placeholder = {Text("Pon tu correo aqui")})
            Spacer(modifier = Modifier .height(20.dp))
            OutlinedTextField(value = contraseña, onValueChange = {contraseña = it}, placeholder = {Text("Pon tu contraseña aqui")})
            Spacer(modifier = Modifier .height(20.dp))
            Button(onClick = {}, modifier = Modifier .width(280.dp)) {
                Text("Iniciar Sesió")
            }
            Spacer(modifier = Modifier .height(10.dp))
            Row( modifier = Modifier .width(180.dp),horizontalArrangement = Arrangement.SpaceBetween) {
                Text("No tinc compte")
                Text("Registrarme", style = TextStyle(textDecoration = TextDecoration.Underline), modifier = Modifier .clickable(onClick = { onClickRegistrarme() })) }
        }
    }
}