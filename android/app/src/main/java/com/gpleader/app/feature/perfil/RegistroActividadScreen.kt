package com.gpleader.app.feature.perfil

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated

// ── Modelos ───────────────────────────────────────────────────────────────────

enum class TipoEventoActividad { RESTAURADO, ARCHIVADO, REUNION_ENVIADA, REUNION_CREADA }

data class EventoActividad(
    val id:     String,
    val tipo:   TipoEventoActividad,
    val titulo: String,
    val fecha:  String,
)

// ── Datos de muestra ──────────────────────────────────────────────────────────

private val SAMPLE_EVENTOS = listOf(
    EventoActividad("1", TipoEventoActividad.RESTAURADO,      "Pedro Visitante fue restaurado",           "Lun 6 De Abril"),
    EventoActividad("2", TipoEventoActividad.ARCHIVADO,       "Pedro Visitante fue archivado",            "Lun 6 De Abril"),
    EventoActividad("3", TipoEventoActividad.RESTAURADO,      "Juan Carlos Pérez fue restaurado",         "Lun 6 De Abril"),
    EventoActividad("4", TipoEventoActividad.ARCHIVADO,       "Juan Carlos Pérez fue archivado",          "Lun 6 De Abril"),
    EventoActividad("5", TipoEventoActividad.REUNION_ENVIADA, "Reunión del Lun 6 De Abril enviada",       "Lun 6 De Abril"),
    EventoActividad("6", TipoEventoActividad.RESTAURADO,      "Pedro Visitante fue restaurado",           "Lun 6 De Abril"),
    EventoActividad("7", TipoEventoActividad.ARCHIVADO,       "Pedro Visitante fue archivado",            "Lun 6 De Abril"),
    EventoActividad("8", TipoEventoActividad.REUNION_CREADA,  "Reunión del 06/04/2026",                   "Lun 6 De Abril"),
    EventoActividad("9", TipoEventoActividad.REUNION_CREADA,  "Reunión del 06/04/2026",                   "Lun 6 De Abril"),
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun RegistroActividadScreen(
    onNavigateBack: () -> Unit,
) {
    RegistroActividadContent(
        eventos        = SAMPLE_EVENTOS,
        onNavigateBack = onNavigateBack,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistroActividadContent(
    eventos:        List<EventoActividad>,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .neuElevated(cornerRadius = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .clickable(onClick = onNavigateBack)
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = Ink,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text  = "Registro de actividad",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Lista de eventos
        LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(eventos, key = { it.id }) { evento ->
                EventoRow(evento = evento)
            }
        }
    }
}

// ── Fila de evento ────────────────────────────────────────────────────────────

@Composable
private fun EventoRow(evento: EventoActividad) {
    val (iconoColor, iconoTexto) = when (evento.tipo) {
        TipoEventoActividad.RESTAURADO      -> Sage  to "↑"
        TipoEventoActividad.ARCHIVADO       -> Gold  to "↓"
        TipoEventoActividad.REUNION_ENVIADA -> Accent to "✓"
        TipoEventoActividad.REUNION_CREADA  -> Accent to "📅"
    }

    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Ícono circular
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconoColor.copy(alpha = 0.12f)),
            ) {
                Text(
                    text  = iconoTexto,
                    color = iconoColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = evento.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text  = evento.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, showSystemUi = true)
@Composable
private fun RegistroActividadPreview() {
    GpLeaderTheme {
        RegistroActividadContent(
            eventos        = SAMPLE_EVENTOS,
            onNavigateBack = {},
        )
    }
}
