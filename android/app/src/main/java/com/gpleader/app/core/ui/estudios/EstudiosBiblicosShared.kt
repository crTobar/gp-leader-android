package com.gpleader.app.core.ui.estudios

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.data.repository.DuoBibleStudy
import com.gpleader.app.core.data.repository.EstudioBiblico
import com.gpleader.app.core.ui.components.NeuButtonPrimary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.ui.theme.neuInset
import com.gpleader.app.core.ui.theme.neuInsetSm

data class EstudioBiblicoItem(
    val id:               String,
    val studentName:      String,
    val completedLessons: List<Int>,
) {
    val totalCompleted: Int get() = completedLessons.size
    val currentLesson:  Int get() = ((completedLessons.maxOrNull() ?: 0) + 1).coerceAtMost(20)
}

fun EstudioBiblico.asItem() = EstudioBiblicoItem(id, studentName, completedLessons)
fun DuoBibleStudy.asItem()  = EstudioBiblicoItem(id, studentName, completedLessons)

val TITULOS_LECCIONES_ESTUDIO = listOf(
    "La Santa Biblia", "Dios", "La Oración", "Origen del Pecado", "Salvación",
    "Perdón de Pecados", "Segunda Venida", "Señales de la 2ª Venida", "Santa Ley de Dios",
    "El Sábado", "Cómo Guardar el Sábado", "Sostén de la Iglesia", "El Bautismo",
    "La Muerte", "El Juicio", "La Iglesia", "Don de Profecía", "Normas Cristianas",
    "Vida Cristiana", "Dios Nos Llama",
)

@Composable
fun EstudiosBiblicosLista(
    estudios:          List<EstudioBiblicoItem>,
    onEstudioClick:    (String) -> Unit,
    modifier:          Modifier = Modifier,
    contentPadding:    PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
    emptyTitle:        String = "Aún no hay estudios bíblicos",
    emptySubtitle:     String = "Agrega a las personas a quienes\nestán dando estudio bíblico.",
    onAgregar:         (() -> Unit)? = null,
) {
    if (estudios.isEmpty()) {
        EstudiosBiblicosEmpty(
            title    = emptyTitle,
            subtitle = emptySubtitle,
            onAgregar = onAgregar,
            modifier = modifier.fillMaxSize(),
        )
        return
    }
    LazyColumn(
        modifier            = modifier,
        contentPadding      = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(estudios, key = { it.id }) { estudio ->
            EstudioBiblicoCard(estudio = estudio, onClick = { onEstudioClick(estudio.id) })
        }
    }
}

@Composable
fun EstudiosBiblicosEmpty(
    title:     String,
    subtitle:  String,
    onAgregar: (() -> Unit)?,
    modifier:  Modifier = Modifier,
) {
    Column(
        modifier            = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📖", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        if (onAgregar != null) {
            Spacer(Modifier.height(24.dp))
            NeuButtonPrimary(text = "Agregar alumno", onClick = onAgregar)
        }
    }
}

@Composable
fun EstudioBiblicoCard(estudio: EstudioBiblicoItem, onClick: () -> Unit) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = estudio.studentName,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text  = "Lección ${estudio.currentLesson} · ${estudio.totalCompleted} de 20 completadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            val progreso = (estudio.totalCompleted / 20f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(BackgroundDeep),
            ) {
                if (progreso > 0f) {
                    Box(
                        modifier = Modifier.fillMaxWidth(progreso).height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(Accent),
                    )
                }
            }
        }
    }
}

@Composable
fun AgregarAlumnoEstudioDialog(
    nombre:       String,
    isCreating:   Boolean,
    onNombreChange: (String) -> Unit,
    onDismiss:    () -> Unit,
    onConfirm:    () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Background,
        title = { Text("Agregar alumno", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            Column {
                Text("NOMBRE DEL ALUMNO", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().neuInsetSm(cornerRadius = 10.dp)
                        .background(BackgroundDeep, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    if (nombre.isEmpty()) {
                        Text("Ej: Juan Pérez", style = MaterialTheme.typography.bodyLarge, color = Muted)
                    }
                    BasicTextField(
                        value = nombre,
                        onValueChange = onNombreChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = nombre.isNotBlank() && !isCreating) {
                if (isCreating) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Accent, strokeWidth = 2.dp)
                } else {
                    Text("Crear", color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Mid) } },
    )
}

@Composable
fun EstudioBiblicoDetalleContent(
    estudio:        EstudioBiblicoItem,
    togglingLesson: Int?,
    onToggleLesson: ((Int) -> Unit)?,
    modifier:       Modifier = Modifier,
) {
    val progreso = (estudio.totalCompleted / 20f).coerceIn(0f, 1f)
    LazyColumn(
        modifier            = modifier,
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PROGRESO", style = MaterialTheme.typography.labelSmall, color = Muted)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${estudio.totalCompleted} de 20 lecciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Ink,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            "${(progreso * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                            .clip(RoundedCornerShape(3.dp)).background(BackgroundDeep),
                    ) {
                        if (progreso > 0f) {
                            Box(
                                modifier = Modifier.fillMaxWidth(progreso).height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)).background(Accent),
                            )
                        }
                    }
                }
            }
        }
        items(20) { idx ->
            val leccionNum = idx + 1
            val completada = leccionNum in estudio.completedLessons
            val esActual   = leccionNum == estudio.currentLesson
            LeccionEstudioRow(
                numero     = leccionNum,
                titulo     = TITULOS_LECCIONES_ESTUDIO.getOrElse(idx) { "Lección $leccionNum" },
                completada = completada,
                esActual   = esActual && !completada,
                toggling   = togglingLesson == leccionNum,
                onClick    = onToggleLesson?.let { fn -> { fn(leccionNum) } },
            )
        }
    }
}

@Composable
private fun LeccionEstudioRow(
    numero:     Int,
    titulo:     String,
    completada: Boolean,
    esActual:   Boolean,
    toggling:   Boolean,
    onClick:    (() -> Unit)?,
) {
    NeuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (completada) Sage.copy(alpha = 0.15f) else BackgroundDeep),
            ) {
                Text(
                    numero.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (completada) Sage else Muted,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (completada) Mid else Ink,
                    fontWeight = if (completada) FontWeight.Normal else FontWeight.Medium,
                )
                if (esActual) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier.background(Accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("PRÓXIMO", style = MaterialTheme.typography.labelSmall, color = Accent)
                    }
                }
            }
            LeccionEstudioCheckbox(checked = completada, isLoading = toggling, onClick = onClick)
        }
    }
}

@Composable
private fun LeccionEstudioCheckbox(checked: Boolean, isLoading: Boolean, onClick: (() -> Unit)?) {
    Box(
        modifier = Modifier.size(40.dp)
            .then(if (checked) Modifier.neuInset(cornerRadius = 10.dp) else Modifier.neuElevatedSm(cornerRadius = 10.dp))
            .background(if (checked) Sage.copy(alpha = 0.15f) else Background, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = !isLoading, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator(Modifier.size(18.dp), color = Accent, strokeWidth = 2.dp)
            checked   -> Icon(Icons.Default.Check, "Completada", tint = Sage, modifier = Modifier.size(22.dp))
        }
    }
}
