package com.example.ecoscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecoscanner.ui.theme.*

@Composable
fun PoliticaPrivacidad(
    onVolver: () -> Unit,
    onClickEstadisticas: () -> Unit = {},
    onClickDatos: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpringWood)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Política de Privacidad",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Última actualización: 2026",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = GrayNurse)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "1. Recopilación de Datos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "EcoScanner únicamente escanea códigos de productos y consulta información nutricional de la API pública OpenFoodFacts. No almacenamos datos personales más allá del ID de usuario de Supabase necesario para la autenticación.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "2. Uso de los Datos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Los datos de escaneo se utilizan exclusivamente para mostrar información nutricional del producto, calcular la huella de carbono y mostrar estadísticas personales de impacto ambiental.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "3. Almacenamiento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Los escaneos se almacenan en Supabase (Base de datos cloud) asociados al ID de usuario autenticado. El usuario puede eliminar sus datos en cualquier momento desde la opción 'Limpiar Historial'.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "4. Cookies y Tecnologías Similares",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "No utilizamos cookies propias. Servicios de terceros como Supabase y OpenFoodFacts pueden utilizar cookies según sus propias políticas.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "5. Enlaces a Terceros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "La app puede contener enlaces a OpenFoodFacts (información de productos). No somos responsables de las prácticas de privacidad de terceros.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "6. Seguridad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Implementamos medidas de seguridad razonables para proteger sus datos. Sin embargo, ninguna transmisión por Internet es 100% segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "7. Cambios en la Política",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Podemos actualizar esta política periódicamente. Le notificaremos de cualquier cambio significativo a través de la app.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "8. Contacto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Como
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Si tiene preguntas sobre esta política, contacte con el desarrollador.",
            style = MaterialTheme.typography.bodyMedium,
            color = Como.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(color = GrayNurse)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Como)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}