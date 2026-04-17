package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun EstaPreview() { Esta(onTornar = {}) }
@Composable
fun Esta (onTornar: () -> Unit){
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ){
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text("Estadisticas")
            Button( onClick = {onTornar()},
                modifier = Modifier.width(200.dp))
            {Text("Tornar a Escanear")}
        }

    }
}