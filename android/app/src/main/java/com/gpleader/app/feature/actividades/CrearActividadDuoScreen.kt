package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuTextField
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuGlow
import com.gpleader.app.core.ui.theme.neuInsetSm

@Composable
fun CrearActividadDuoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearActividadDuoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedOk) {
        if (uiState.savedOk) onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevatedSm(cornerRadius = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .clickable(onClick = onNavigateBack)
                    .padding(10.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Text(
                text      = "Nueva Actividad del Dúo",
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier            = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NeuTextField(
                value         = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label         = "Nombre de la actividad",
            )

            // Tipo de marcador
            Text("Tipo", style = MaterialTheme.typography.bodyMedium, color = Mid, fontWeight = FontWeight.SemiBold)
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("counter" to "Contador", "checkbox" to "Verificación").forEach { (value, label) ->
                    val selected = uiState.markerType == value
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .then(
                                if (selected) Modifier.neuGlow(cornerRadius = 20.dp)
                                else Modifier.neuElevatedSm(cornerRadius = 20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Accent else Background)
                            .clickable { viewModel.onMarkerTypeChange(value) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (selected) androidx.compose.ui.graphics.Color.White else Ink, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (uiState.markerType == "counter") {
                NeuTextField(
                    value         = uiState.unitLabel,
                    onValueChange = viewModel::onUnitLabelChange,
                    label         = "Unidad (ej: visitas, oraciones)",
                )
            }

            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Blush)
            }
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            NeuButtonPrimary(
                text     = if (uiState.isGuardando) "Guardando…" else "Crear actividad",
                onClick  = viewModel::onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled  = uiState.nombre.isNotBlank() && !uiState.isGuardando,
            )
        }
    }
}
