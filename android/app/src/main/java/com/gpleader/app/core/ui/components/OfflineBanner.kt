package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fmtSync: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM HH:mm", Locale("es")).withZone(ZoneId.systemDefault())

/**
 * Banner discreto que aparece SOLO cuando no hay conexión, indicando que se ven datos guardados
 * y (si existe) la fecha de la última actualización. Colócalo arriba del contenido de una pantalla.
 *
 * @param mensaje texto principal. El default ("mostrando datos guardados") solo es cierto en
 *   pantallas con caché en Room. Para las que NO tienen caché (ej. dúos) pasa un mensaje propio,
 *   porque ahí no hay datos guardados que mostrar.
 * @param mostrarUltimaSync si false, oculta la línea "Actualizado …" (irrelevante sin caché).
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    mensaje: String = "Sin conexión · mostrando datos guardados",
    mostrarUltimaSync: Boolean = true,
    viewModel: OfflineStatusViewModel = hiltViewModel(),
) {
    val isOnline by viewModel.isOnline.collectAsState()
    if (isOnline) return
    val lastSync by viewModel.lastSync.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Blush.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Blush, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text  = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
            if (mostrarUltimaSync) {
                lastSync?.let {
                    Text(
                        text  = "Actualizado ${fmtSync.format(Instant.ofEpochMilli(it))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                } ?: Text(
                    text  = "Algunos datos podrían faltar",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mid,
                )
            }
        }
    }
}
