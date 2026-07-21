package com.gpleader.app.feature.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.ui.components.NeuAvatar
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm

@Composable
fun DuoReporteActividadScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuoReporteActividadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(Background),
        ) {
            // Top bar: back + título centrado
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .neuElevatedSm(cornerRadius = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Background)
                        .clickable(onClick = onNavigateBack)
                        .size(48.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink, modifier = Modifier.size(22.dp))
                }
                Text(
                    text     = uiState.nombre,
                    style    = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    color    = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 60.dp),
                )
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                }
                uiState.agregada == null -> Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No se encontró la actividad.",
                        style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center,
                    )
                }
                else -> ReporteContenido(uiState.agregada!!)
            }
        }
    }
}

@Composable
private fun ReporteContenido(agregada: DuoActividadAgregada) {
    val totalFmt = duoValorFmt(agregada.markerType, agregada.totalCantidad, agregada.montoTotal, agregada.diasMarcados, agregada.unitLabel)

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tarjeta resumen
        item(key = "resumen") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(cornerRadius = 28.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Background)
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOTAL ACUMULADO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = Muted)
                    Spacer(Modifier.height(10.dp))
                    DuoTipoChip(agregada.markerType)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        totalFmt.numero,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        color = if (totalFmt.esCero) Muted else Accent,
                    )
                    if (totalFmt.unidad.isNotBlank()) {
                        Text(totalFmt.unidad, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = Muted)
                    }
                }
            }
        }

        // Encabezado sección
        item(key = "header") {
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(modifier = Modifier.size(width = 3.dp, height = 14.dp).clip(RoundedCornerShape(2.dp)).background(Accent))
                Text("REPORTE POR DÚOS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = Ink)
            }
        }

        // Filas por dúo
        items(agregada.duos, key = { it.duo.id }) { dv ->
            DuoFilaReporte(dv, agregada.markerType, agregada.unitLabel)
        }
    }
}

@Composable
private fun DuoFilaReporte(dv: DuoValor, markerType: String, unitLabel: String) {
    val fmt = duoValorFmt(markerType, dv.totalCantidad, dv.montoTotal, dv.diasMarcados, unitLabel)
    val inic1 = inicialesReporteDuo(dv.duo.member1.nombreCompleto)
    val inic2 = inicialesReporteDuo(dv.duo.member2.nombreCompleto)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(width = 56.dp, height = 48.dp)) {
            NeuAvatar(iniciales = inic2, size = 34.dp, modifier = Modifier.align(Alignment.BottomEnd))
            Box(
                modifier = Modifier.align(Alignment.TopStart).clip(androidx.compose.foundation.shape.CircleShape).background(Background).padding(2.dp),
            ) { NeuAvatar(iniciales = inic1, size = 34.dp) }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text     = "${dv.duo.member1.primerNombre} & ${dv.duo.member2.primerNombre}",
            style    = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color    = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                fmt.numero,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = if (fmt.esCero) Muted else Accent, fontWeight = FontWeight.Bold,
            )
            if (fmt.unidad.isNotBlank()) {
                Text(fmt.unidad, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = Muted)
            }
        }
    }
}

private fun inicialesReporteDuo(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
        partes.size == 1 -> partes[0].take(2).uppercase()
        else             -> "?"
    }
}
