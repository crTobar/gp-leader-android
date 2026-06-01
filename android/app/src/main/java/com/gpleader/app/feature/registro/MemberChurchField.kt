package com.gpleader.app.feature.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gpleader.app.core.data.repository.ChurchHit
import com.gpleader.app.core.ui.theme.Accent
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.BackgroundDeep
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuElevated
import com.gpleader.app.core.ui.theme.neuInsetSm

@Composable
fun MemberChurchField(
    memberId:     String,
    query:        String,
    selected:     ChurchHit?,
    results:      List<ChurchHit>,
    isSearching:  Boolean,
    groupChurch:  ChurchHit?,
    onQueryChange: (String) -> Unit,
    onSelect:      (ChurchHit) -> Unit,
    onClear:       () -> Unit,
    modifier:      Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val showResults    = isFocused && results.isNotEmpty()
    val showNoResults  = isFocused && query.isNotBlank() && !isSearching && results.isEmpty() && selected == null
    val showDefault    = isFocused && query.isBlank() && selected == null && groupChurch != null && results.isEmpty()

    Column(modifier = modifier.fillMaxWidth().padding(start = 52.dp, end = 4.dp, bottom = 8.dp)) {
        Text(
            text  = "IGLESIA DEL CULTO",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // ── Campo de búsqueda ─────────────────────────────────────────────────
        if (selected != null) {
            // Chip de selección confirmada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Sage.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = null,
                    tint               = Sage,
                    modifier           = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = selected.churchName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                    )
                    if (selected.districtName.isNotBlank() || selected.campoName.isNotBlank()) {
                        Text(
                            text  = listOf(selected.campoName, selected.districtName).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = null,
                    tint               = Muted,
                    modifier           = Modifier
                        .size(16.dp)
                        .clickable(onClick = onClear),
                )
            }
        } else {
            // Campo de texto inset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuInsetSm(cornerRadius = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundDeep)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType   = KeyboardType.Text,
                        imeAction      = ImeAction.Done,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { inner ->
                        if (query.isBlank()) {
                            Text(
                                text  = "Nombre de la iglesia…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Muted,
                            )
                        }
                        inner()
                    },
                )
                if (isSearching) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        color    = Accent,
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                    )
                } else if (query.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = null,
                        tint               = Muted,
                        modifier           = Modifier
                            .size(16.dp)
                            .clickable {
                                onQueryChange("")
                                onClear()
                            },
                    )
                }
            }
        }

        // ── Resultados / defaults ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = showResults || showNoResults || showDefault,
            enter   = expandVertically(),
            exit    = shrinkVertically(),
        ) {
            when {
                showResults -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuElevated(cornerRadius = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Background),
                    ) {
                        results.forEachIndexed { idx, hit ->
                            if (idx > 0) HorizontalDivider(color = Muted.copy(alpha = 0.15f))
                            ChurchResultRow(
                                hit     = hit,
                                onClick = {
                                    focusManager.clearFocus()
                                    onSelect(hit)
                                },
                            )
                        }
                    }
                }
                showNoResults -> {
                    Text(
                        text     = "Sin resultados para «$query»",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                showDefault && groupChurch != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuElevated(cornerRadius = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Background)
                            .clickable {
                                focusManager.clearFocus()
                                onSelect(groupChurch)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text  = "IGLESIA DEL GRUPO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Accent,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = groupChurch.churchName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink,
                        )
                        if (groupChurch.districtName.isNotBlank()) {
                            Text(
                                text  = listOf(groupChurch.campoName, groupChurch.districtName).filter { it.isNotBlank() }.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChurchResultRow(hit: ChurchHit, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = hit.churchName,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
            if (hit.districtName.isNotBlank() || hit.campoName.isNotBlank()) {
                Text(
                    text  = listOf(hit.campoName, hit.districtName).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Mid,
            modifier           = Modifier.size(16.dp),
        )
    }
}

