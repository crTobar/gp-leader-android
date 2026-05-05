package com.gpleader.app.feature.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gpleader.app.R
import com.gpleader.app.core.ui.components.NeuButtonSecondary
import com.gpleader.app.core.ui.components.NeuCard
import com.gpleader.app.core.ui.components.SkeletonBox
import com.gpleader.app.core.ui.components.SkeletonText
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Gold
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuElevatedSm
import com.gpleader.app.core.data.repository.SabbathMeetingResumen
import com.gpleader.app.core.ui.theme.neuGlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ── Constantes para las pestañas del bottom nav ───────────────────────────────
private const val TAB_INICIO    = 0
private const val TAB_HISTORIAL = 1
private const val TAB_PERFIL    = 2

// Nombres de mes abreviados en español
private val MESES_ES = arrayOf(
    "ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC",
)

// ── Entry point (Hilt) ────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToRegistro: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToSabadoCulto: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recargar datos del sábado cada vez que el HomeScreen vuelve al frente
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.reloadSabbath()
        }
    }

    LaunchedEffect(uiState.navigateToRegistro) {
        if (uiState.navigateToRegistro) {
            onNavigateToRegistro()
            viewModel.consumeRegistroNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToHistorial) {
        if (uiState.navigateToHistorial) {
            onNavigateToHistorial()
            viewModel.consumeHistorialNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToDetalle) {
        val id = uiState.navigateToDetalle
        if (id != null) {
            onNavigateToDetalle(id)
            viewModel.consumeDetalleNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToSabadoCulto) {
        if (uiState.navigateToSabadoCulto) {
            onNavigateToSabadoCulto()
            viewModel.consumeSabadoCultoNavigation()
        }
    }

    HomeScreenContent(
        uiState              = uiState,
        onRegistrarClick     = viewModel::onRegistrarClick,
        onVerTodasClick      = viewModel::onVerTodasClick,
        onReunionClick       = viewModel::onReunionClick,
        onHistorialTabClick  = onNavigateToHistorial,
        onPerfilTabClick     = onNavigateToPerfil,
        onSabadoCultoClick   = viewModel::onSabadoCultoClick,
    )
}

// ── Content (previewable) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onRegistrarClick: () -> Unit,
    onVerTodasClick: () -> Unit,
    onReunionClick: (String) -> Unit,
    onHistorialTabClick: () -> Unit,
    onPerfilTabClick: () -> Unit = {},
    onSabadoCultoClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(TAB_INICIO) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                selectedTab     = selectedTab,
                onInicioClick   = { selectedTab = TAB_INICIO },
                onHistorialClick = {
                    selectedTab = TAB_HISTORIAL
                    onHistorialTabClick()
                },
                onPerfilClick   = { onPerfilTabClick() },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            HomeSkeletonContent(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(20.dp))

                TopBar(
                    nombreLider      = uiState.nombreLider,
                    onRegistrarClick = onRegistrarClick,
                )

                Spacer(Modifier.height(20.dp))

                uiState.grupo?.let { grupo ->
                    GrupoCard(
                        grupo                = grupo,
                        porcentajeAsistencia = uiState.porcentajeAsistencia,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                StatsRow(
                    porcentaje   = uiState.porcentajeAsistencia,
                    presentes    = uiState.totalPresentes,
                    ausentes     = uiState.totalAusentes,
                    justificados = uiState.totalJustificados,
                    totalMiembros = uiState.totalMiembros,
                )

                uiState.sabbathMeeting?.let { sabbath ->
                    Spacer(Modifier.height(16.dp))
                    SabbathMeetingCard(
                        resumen  = sabbath,
                        onClick  = onSabadoCultoClick,
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionSeparator(label = stringResource(R.string.home_section_recientes))

                Spacer(Modifier.height(12.dp))

                if (uiState.reunionesRecientes.isEmpty()) {
                    EmptyStateReuniones()
                } else {
                    uiState.reunionesRecientes.forEach { reunion ->
                        ReunionCard(
                            reunion   = reunion,
                            onClick   = { onReunionClick(reunion.id) },
                            modifier  = Modifier.padding(vertical = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                NeuButtonSecondary(
                    text     = stringResource(R.string.home_btn_ver_todas),
                    onClick  = onVerTodasClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

}

// ── Sabbath meeting card ──────────────────────────────────────────────────────

@Composable
private fun SabbathMeetingCard(
    resumen: SabbathMeetingResumen,
    onClick: () -> Unit,
) {
    val mes = resumen.fecha.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase()
    val dia = resumen.fecha.dayOfMonth
    val progreso = if (resumen.totalMiembros > 0)
        resumen.presentes.toFloat() / resumen.totalMiembros.toFloat()
    else 0f

    val badgeColor = when (resumen.status.lowercase()) {
        "submitted" -> Sage
        else        -> Gold
    }
    val badgeLabel = when (resumen.status.lowercase()) {
        "submitted" -> "Enviado"
        else        -> "Borrador"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(cornerRadius = 20.dp)
            .background(Background, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fecha
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(40.dp),
        ) {
            Text(
                text       = mes,
                style      = MaterialTheme.typography.labelSmall,
                color      = Accent,
            )
            Text(
                text       = dia.toString(),
                style      = MaterialTheme.typography.titleLarge,
                color      = Ink,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = stringResource(R.string.sabado_card_titulo),
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text  = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text  = stringResource(R.string.sabado_card_marcados, resumen.presentes, resumen.totalMiembros),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress         = { progreso },
                modifier         = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color            = Sage,
                trackColor       = BackgroundDeep,
                strokeCap        = StrokeCap.Round,
            )
        }
    }
}

// ── Skeleton loading ──────────────────────────────────────────────────────────

@Composable
private fun HomeSkeletonContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        // TopBar skeleton
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.size(40.dp), cornerRadius = 20.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonText(width = 80.dp, height = 10.dp)
                Spacer(Modifier.height(6.dp))
                SkeletonText(width = 140.dp, height = 18.dp)
            }
            SkeletonBox(modifier = Modifier.width(80.dp).height(36.dp), cornerRadius = 10.dp)
        }

        Spacer(Modifier.height(20.dp))

        // GrupoCard skeleton
        NeuCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                SkeletonText(width = 70.dp, height = 10.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonText(width = 180.dp, height = 22.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonText(width = 140.dp, height = 14.dp)
                Spacer(Modifier.height(16.dp))
                SkeletonBox(
                    modifier     = Modifier.fillMaxWidth().height(8.dp),
                    cornerRadius = 4.dp,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // StatsRow skeleton
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Dark cell
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
            // Light cell 1
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
            // Light cell 2
            SkeletonBox(
                modifier     = Modifier.weight(1f).padding(6.dp).height(88.dp),
                cornerRadius = 20.dp,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Section label skeleton
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(1.dp), cornerRadius = 1.dp)
            Spacer(Modifier.width(12.dp))
            SkeletonText(width = 80.dp, height = 10.dp)
            Spacer(Modifier.width(12.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(1.dp), cornerRadius = 1.dp)
        }

        Spacer(Modifier.height(12.dp))

        // ReunionCard skeletons x2
        repeat(2) {
            NeuCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Date block
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.width(44.dp),
                    ) {
                        SkeletonText(width = 30.dp, height = 22.dp)
                        Spacer(Modifier.height(4.dp))
                        SkeletonText(width = 24.dp, height = 10.dp)
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .height(56.dp)
                            .background(Muted.copy(alpha = 0.2f)),
                    )

                    // Content
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonText(width = 140.dp, height = 16.dp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SkeletonBox(modifier = Modifier.width(52.dp).height(20.dp), cornerRadius = 4.dp)
                            SkeletonBox(modifier = Modifier.width(44.dp).height(20.dp), cornerRadius = 4.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        SkeletonBox(
                            modifier     = Modifier.fillMaxWidth().height(5.dp),
                            cornerRadius = 3.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    nombreLider: String,
    onRegistrarClick: () -> Unit,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = stringResource(R.string.home_label_bienvenida),
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )
            Text(
                text  = nombreLider,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(8.dp))

        SmallButtonPrimary(
            text    = stringResource(R.string.home_btn_registrar),
            onClick = onRegistrarClick,
        )
    }
}

// ── Botones pequeños (variante compacta del design system) ────────────────────

@Composable
private fun SmallButtonPrimary(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .neuGlow(cornerRadius = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SmallButtonSecondary(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .neuElevatedSm(cornerRadius = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color      = Accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Card del grupo ────────────────────────────────────────────────────────────

@Composable
private fun GrupoCard(
    grupo: GrupoInfo,
    porcentajeAsistencia: Int,
) {
    NeuCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text  = stringResource(R.string.home_label_tu_grupo),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = grupo.nombre,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "${grupo.diaSemana} · ${grupo.horaInicio}",
                style = MaterialTheme.typography.bodyMedium,
                color = Mid,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = stringResource(R.string.home_label_asistencia_periodo),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Muted,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text  = "$porcentajeAsistencia%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sage,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress   = { porcentajeAsistencia / 100f },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = Sage,
                trackColor = BackgroundDeep,
                strokeCap  = StrokeCap.Round,
            )
        }
    }
}

// ── Fila de estadísticas ──────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    porcentaje:    Int,
    presentes:     Int,
    ausentes:      Int,
    justificados:  Int,
    totalMiembros: Int,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Fila superior: dark card (alto) + PRESENTES y AUSENTES apilados
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCellFeatured(
                presentes     = presentes,
                totalMiembros = totalMiembros,
                porcentaje    = porcentaje,
                modifier      = Modifier.weight(1f).padding(6.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                StatCellLight(
                    value    = presentes.toString(),
                    label    = stringResource(R.string.home_label_presentes),
                    color    = Sage,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 6.dp, top = 6.dp, bottom = 3.dp),
                )
                StatCellLight(
                    value    = ausentes.toString(),
                    label    = stringResource(R.string.home_label_ausentes),
                    color    = Blush,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, end = 6.dp, top = 3.dp, bottom = 6.dp),
                )
            }
        }
        // Fila inferior: JUSTIFICAR y PROMEDIO a ancho completo
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCellLight(
                value    = justificados.toString(),
                label    = stringResource(R.string.home_label_justificados),
                color    = Muted,
                modifier = Modifier.weight(1f).padding(start = 6.dp, end = 3.dp, top = 0.dp, bottom = 6.dp),
            )
            StatCellLight(
                value    = "$porcentaje%",
                label    = stringResource(R.string.home_label_promedio),
                color    = Accent,
                modifier = Modifier.weight(1f).padding(start = 3.dp, end = 6.dp, top = 0.dp, bottom = 6.dp),
                sublabel = "trimestre",
            )
        }
    }
}

@Composable
private fun StatCellFeatured(
    presentes:     Int,
    totalMiembros: Int,
    porcentaje:    Int,
    modifier:      Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .neuElevated(cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Ink),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(vertical = 20.dp, horizontal = 8.dp),
        ) {
            Text(
                text  = "$presentes de $totalMiembros",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "$porcentaje%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = stringResource(R.string.home_label_ult_reunion),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun StatCellLight(
    value:    String,
    label:    String,
    color:    Color,
    modifier: Modifier = Modifier,
    sublabel: String?  = null,
) {
    NeuCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = label,
                style         = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color         = Muted,
                textAlign     = androidx.compose.ui.text.style.TextAlign.Center,
            )
            if (sublabel != null) {
                Text(
                    text      = sublabel,
                    style     = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.sp,
                        fontSize      = 10.sp,
                    ),
                    color     = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ── Separador de sección ──────────────────────────────────────────────────────

@Composable
private fun SectionSeparator(label: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = Muted,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Muted.copy(alpha = 0.4f))
    }
}

// ── Card de reunión ───────────────────────────────────────────────────────────

@Composable
private fun ReunionCard(
    reunion: ReunionResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dia = reunion.fecha.dayOfMonth.toString()
    val mes = MESES_ES[reunion.fecha.monthValue - 1]
    val total = reunion.presentes + reunion.ausentes
    val progreso = if (total > 0) reunion.presentes / total.toFloat() else 0f

    val (badgeColor, badgeBgColor, badgeTexto) = when (reunion.estado) {
        EstadoReunion.ENVIADA        -> Triple(Sage, Sage.copy(alpha = 0.15f), stringResource(R.string.home_badge_enviada))
        EstadoReunion.APROBADA       -> Triple(Sage, Sage.copy(alpha = 0.15f), stringResource(R.string.home_badge_aprobada))
        EstadoReunion.PENDIENTE_SYNC -> Triple(Gold, Gold.copy(alpha = 0.15f), stringResource(R.string.home_badge_pendiente))
        EstadoReunion.BORRADOR       -> Triple(Muted, Muted.copy(alpha = 0.15f), stringResource(R.string.home_badge_borrador))
    }

    NeuCard(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp), onClick = onClick) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Fecha — izquierda
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(44.dp),
            ) {
                Text(text = dia, style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text(text = mes, style = MaterialTheme.typography.labelSmall,     color = Muted)
            }

            Spacer(Modifier.width(12.dp))

            // Contenido central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.home_reunion_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Badge estado
                    Badge(text = badgeTexto, color = badgeColor, bgColor = badgeBgColor)
                    // Badge presentes
                    Badge(
                        text    = stringResource(R.string.home_badge_presentes, reunion.presentes),
                        color   = Mid,
                        bgColor = BackgroundDeep,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress   = { progreso },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color      = Sage,
                    trackColor = BackgroundDeep,
                    strokeCap  = StrokeCap.Round,
                )
            }

            // Flecha
            Text(
                text     = "›",
                style    = MaterialTheme.typography.headlineMedium,
                color    = Muted,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateReuniones() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundDeep),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "📅", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text  = stringResource(R.string.home_empty_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(R.string.home_empty_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = Mid,
        )
    }
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onInicioClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onPerfilClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Muted.copy(alpha = 0.2f))
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTabItem(
                icon      = Icons.Default.Home,
                label     = stringResource(R.string.home_nav_inicio),
                isActive  = selectedTab == TAB_INICIO,
                onClick   = onInicioClick,
            )
            NavTabItem(
                icon      = Icons.Default.DateRange,
                label     = stringResource(R.string.home_nav_historial),
                isActive  = selectedTab == TAB_HISTORIAL,
                onClick   = onHistorialClick,
            )
            NavTabItem(
                icon      = Icons.Default.Person,
                label     = stringResource(R.string.home_nav_perfil),
                isActive  = selectedTab == TAB_PERFIL,
                onClick   = onPerfilClick,
            )
        }
    }
}

@Composable
private fun NavTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Accent else Muted,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Accent else Muted,
        )
    }
}


// ── Previews ──────────────────────────────────────────────────────────────────

private val previewUiState = HomeUiState(
    nombreLider          = "Maria Garcia",
    grupo                = GrupoInfo("GP Los Olivos", "Miércoles", "7:00 PM", "Iglesia Central"),
    porcentajeAsistencia = 67,
    totalPresentes       = 2,
    totalAusentes        = 1,
    totalJustificados    = 1,
    totalMiembros        = 4,
    reunionesRecientes   = listOf(
        ReunionResumen("r1", LocalDate.of(2026, 2, 26), EstadoReunion.ENVIADA, 12, 2),
        ReunionResumen("r2", LocalDate.of(2026, 2, 19), EstadoReunion.ENVIADA, 10, 3),
    ),
)

@Preview(showSystemUi = true, name = "Home — con reuniones")
@Composable
private fun HomeScreenPreview() {
    GpLeaderTheme {
        HomeScreenContent(
            uiState                = previewUiState,
            onRegistrarClick       = {},
            onVerTodasClick     = {},
            onReunionClick      = {},
            onHistorialTabClick = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Home — sin reuniones")
@Composable
private fun HomeScreenEmptyPreview() {
    GpLeaderTheme {
        HomeScreenContent(
            uiState                = previewUiState.copy(reunionesRecientes = emptyList()),
            onRegistrarClick       = {},
            onVerTodasClick     = {},
            onReunionClick      = {},
            onHistorialTabClick = {},
        )
    }
}
