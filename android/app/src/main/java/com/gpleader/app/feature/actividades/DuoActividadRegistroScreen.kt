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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.data.repository.DuoActividadRecord
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DIAS_SEMANA_DUO = listOf("D", "L", "M", "M", "J", "V", "S")
private val FMT_DUO = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

@Composable
fun DuoActividadRegistroScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuoActividadRegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
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
                text      = uiState.nombre,
                style     = MaterialTheme.typography.titleLarge,
                color     = Ink,
                textAlign = TextAlign.Center,
                maxLines  = 1,
                modifier  = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // Nota compartida
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Accent.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                "Un aporte del dúo cuenta para ambos miembros",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            }
            return@Column
        }

        if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = Blush)
            }
            return@Column
        }

        if (uiState.registros.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin registros aún", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
            }
            return@Column
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.registros, key = { it.id }) { registro ->
                RegistroDuoCard(registro = registro)
            }
        }
    }
}

@Composable
private fun RegistroDuoCard(registro: DuoActividadRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Bloque fecha
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .neuElevatedSm(cornerRadius = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            val idx = if (registro.recordDate.dayOfWeek == DayOfWeek.SUNDAY) 0 else registro.recordDate.dayOfWeek.value
            Text(DIAS_SEMANA_DUO[idx], style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(
                registro.recordDate.dayOfMonth.toString(),
                style      = MaterialTheme.typography.headlineMedium,
                color      = Accent,
                fontWeight = FontWeight.Bold,
            )
            Text(registro.recordDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall, color = Muted)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                registro.recordDate.format(FMT_DUO),
                style      = MaterialTheme.typography.bodyLarge,
                color      = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            if (registro.isDone) {
                Text("✓ Marcado", style = MaterialTheme.typography.bodyMedium, color = Sage)
            }
        }

        // Valor
        when {
            registro.count != null && registro.count > 0 -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text("${registro.count}", style = MaterialTheme.typography.bodyLarge, color = Accent, fontWeight = FontWeight.Bold)
                }
            }
            registro.isDone -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Sage.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text("✓", style = MaterialTheme.typography.bodyLarge, color = Sage, fontWeight = FontWeight.Bold)
                }
            }
            else -> Text("—", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}
