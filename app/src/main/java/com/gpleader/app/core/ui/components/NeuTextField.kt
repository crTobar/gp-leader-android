package com.gpleader.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpleader.app.core.ui.theme.Background
import com.gpleader.app.core.ui.theme.Blush
import com.gpleader.app.core.ui.theme.GpLeaderTheme
import com.gpleader.app.core.ui.theme.Ink
import com.gpleader.app.core.ui.theme.Mid
import com.gpleader.app.core.ui.theme.Muted
import com.gpleader.app.core.ui.theme.Sage
import com.gpleader.app.core.ui.theme.neuInset

private val TextFieldShape = RoundedCornerShape(14.dp)

/**
 * Campo de texto neumórfico.
 *
 * - Sin borde — apariencia "hundida" via [neuInset].
 * - [label] opcional: texto pequeño encima del campo.
 * - [isError]: borde [Blush] + label rojo.
 * - [isSuccess]: borde [Sage] + label verde.
 * - [readOnly]: texto en [Mid], no editable.
 * - [isPassword]: toggle ojo interno para mostrar/ocultar.
 * - [leadingContent]: composable fijo a la izquierda (ej. prefijo "+506").
 */
@Composable
fun NeuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    isPassword: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    // Estado del toggle ojo — ephemeral UI state
    var passwordVisible by remember { mutableStateOf(false) }

    val effectiveTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        isPassword                     -> VisualTransformation.None
        else                           -> visualTransformation
    }

    val borderColor: Color? = when {
        isError   -> Blush
        isSuccess -> Sage
        else      -> null
    }

    val labelColor = when {
        isError   -> Blush
        isSuccess -> Sage
        else      -> Mid
    }

    val borderModifier = if (borderColor != null) {
        Modifier.drawWithContent {
            drawContent()
            val strokePx = 1.5.dp.toPx()
            drawRoundRect(
                color        = borderColor,
                topLeft      = Offset(strokePx / 2, strokePx / 2),
                size         = Size(size.width - strokePx, size.height - strokePx),
                cornerRadius = CornerRadius(14.dp.toPx()),
                style        = Stroke(width = strokePx),
            )
        }
    } else Modifier

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelSmall,
                color    = labelColor,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
            )
        }

        BasicTextField(
            value                = value,
            onValueChange        = onValueChange,
            singleLine           = singleLine,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            visualTransformation = effectiveTransformation,
            readOnly             = readOnly,
            textStyle            = MaterialTheme.typography.bodyLarge.copy(
                color = if (readOnly) Mid else Ink,
            ),
            cursorBrush = SolidColor(Ink),
            modifier    = Modifier
                .fillMaxWidth()
                .neuInset(cornerRadius = 14.dp)
                .clip(TextFieldShape)
                .background(color = Background, shape = TextFieldShape)
                .then(borderModifier),
            decorationBox = { innerTextField ->
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingContent != null) {
                        leadingContent()
                        Spacer(Modifier.width(4.dp))
                    }
                    Box(
                        modifier         = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && !readOnly) {
                            Text(
                                text  = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Muted,
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint     = Muted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { passwordVisible = !passwordVisible },
                        )
                    }
                }
            },
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1)
@Composable
private fun NeuTextFieldEmptyPreview() {
    GpLeaderTheme {
        Column(modifier = Modifier.padding(32.dp)) {
            NeuTextField(
                value         = "",
                onValueChange = {},
                placeholder   = "correo@ejemplo.com",
                label         = "CORREO ELECTRÓNICO",
                modifier      = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "NeuTextField filled")
@Composable
private fun NeuTextFieldFilledPreview() {
    GpLeaderTheme {
        Column(modifier = Modifier.padding(32.dp)) {
            NeuTextField(
                value         = "juan.perez@iglesia.org",
                onValueChange = {},
                placeholder   = "correo@ejemplo.com",
                label         = "CORREO ELECTRÓNICO",
                modifier      = Modifier.padding(8.dp),
            )
            NeuTextField(
                value      = "MiContra123",
                onValueChange = {},
                label      = "CONTRASEÑA",
                isPassword = true,
                modifier   = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECEEF1, name = "NeuTextField states")
@Composable
private fun NeuTextFieldStatesPreview() {
    GpLeaderTheme {
        Column(modifier = Modifier.padding(32.dp)) {
            NeuTextField(
                value         = "",
                onValueChange = {},
                placeholder   = "Primer nombre",
                label         = "PRIMER NOMBRE *",
                isError       = true,
                modifier      = Modifier.padding(8.dp),
            )
            NeuTextField(
                value         = "Abc12345",
                onValueChange = {},
                label         = "NUEVA CONTRASEÑA *",
                isPassword    = true,
                isSuccess     = true,
                modifier      = Modifier.padding(8.dp),
            )
            NeuTextField(
                value         = "maria.garcia@gmail.com",
                onValueChange = {},
                label         = "CORREO ELECTRÓNICO",
                readOnly      = true,
                modifier      = Modifier.padding(8.dp),
            )
        }
    }
}
