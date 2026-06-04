# Design System — GP Leader

## Relación wireframe → diseño visual

Los **wireframes aprobados** (Balsamiq lo-fi) definen:
- Qué pantallas existen y qué elementos contiene cada una
- El flujo de navegación entre pantallas
- La distribución y jerarquía del contenido

El **style board SDA** define:
- Colores, tipografía y sombras neumórficas
- Cómo se ven los elementos (no dónde van)

Regla: **estructura del wireframe + apariencia del style board**. Nunca inventar layouts nuevos ni ignorar el diseño visual.

---

## Filosofía visual

**Neumorfismo suave** — sin bordes duros, sombras dobles (luz arriba-izquierda, sombra abajo-derecha).
Fondo gris perla uniforme. Cards que "flotan" o "se hunden" según estado.

---

## Colores (Material3 → Custom)

```kotlin
// core/ui/theme/Color.kt
val Background     = Color(0xFFECEEF1)  // Fondo general
val BackgroundDeep = Color(0xFFE0E3E9)  // Fondo secciones
val Shadow         = Color(0xFFC2C8D4)  // Sombra oscura (neu)
val Light          = Color(0xFFFFFFFF)  // Sombra clara (neu)
val Accent         = Color(0xFF4A7FD4)  // Azul principal — botones, selección activa
val AccentLight    = Color(0xFF6497E0)  // Azul claro — hover, secundario
val Gold           = Color(0xFFC9A84C)  // Dorado — badges oficiales, Unión
val Sage           = Color(0xFF6AAB8E)  // Verde sage — éxito, presentes
val Blush          = Color(0xFFD4836A)  // Naranja — alertas, ausentes
val Ink            = Color(0xFF1E2733)  // Texto principal
val Mid            = Color(0xFF5A6577)  // Texto secundario
val Muted          = Color(0xFF9AA4B2)  // Texto deshabilitado / hints
```

### Semántica de colores por contexto

```kotlin
private fun levelColor(level: String): Color = when (level) {
    "union"  -> Gold
    "pastor" -> Ink
    else     -> Accent  // my_group
}
```

---

## Sombras neumórficas

```kotlin
// core/ui/theme/Elevation.kt
fun Modifier.neuElevated() = this.shadow(
    // Sombra oscura abajo-derecha + sombra clara arriba-izquierda
    // Usar BoxWithConstraints + drawBehind para implementar
)

// Niveles:
// neu-up    → card flotante (estado normal)
// neu-up-sm → elemento pequeño flotante
// neu-in    → campo de entrada / botón presionado
// neu-in-sm → campo pequeño presionado
```

`bgColor: Color = NeuBg` en `drawNeuShadows/neuElevated/neuElevatedSm` para soportar fondos distintos al `Background` global.

---

## Tipografía

```kotlin
// core/ui/theme/Type.kt
// Display/Títulos → Cormorant Garamond (serif, elegante)
// Body/UI         → DM Sans (sans-serif, limpio)
// Labels/Mono     → DM Mono (monoespaciado, datos técnicos)

val Typography = Typography(
    displayLarge  = TextStyle(fontFamily = CormorantGaramond, fontSize = 40.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium= TextStyle(fontFamily = CormorantGaramond, fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge    = TextStyle(fontFamily = DMSans, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge     = TextStyle(fontFamily = DMSans, fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium    = TextStyle(fontFamily = DMSans, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelSmall    = TextStyle(fontFamily = DMMono, fontSize = 11.sp, letterSpacing = 2.sp),
)
```

`CormorantGaramond` registra Normal + SemiBold + **Bold (700)**.

---

## Radios de esquinas

```kotlin
val ShapeTokens = Shapes(
    extraLarge = RoundedCornerShape(28.dp),  // Cards principales
    large      = RoundedCornerShape(20.dp),  // Cards internas
    medium     = RoundedCornerShape(14.dp),  // Botones, avatares
    small      = RoundedCornerShape(8.dp),   // Chips, badges
    extraSmall = RoundedCornerShape(4.dp),   // Tags pequeños
)
```

---

## Patrones visuales de código

### Borde de estado (error/active) en campos custom

```kotlin
Modifier.drawWithContent {
    drawContent()
    val strokePx = 1.5.dp.toPx()
    drawRoundRect(
        color        = Accent, // o Blush para error
        topLeft      = Offset(strokePx / 2, strokePx / 2),
        size         = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(14.dp.toPx()),
        style        = Stroke(width = strokePx),
    )
}
```

### Patrón dashedBorder

Se repite en múltiples archivos como función `private fun Modifier.dashedBorder(...)`.
Usa `drawBehind` + `drawIntoCanvas` + `android.graphics.DashPathEffect`.
**No está centralizado en components/ — cada archivo tiene su copia local.**

---

## Componentes reutilizables (core/ui/components/)

| Composable | Descripción |
|-----------|-------------|
| `AppLogo.kt` | Logo con `size/cornerRadius/iconSize` configurables |
| `NeuButton.kt` | `NeuButtonPrimary` (Accent+neuGlow) / `NeuButtonSecondary` (Background+neuElevated) |
| `NeuCard.kt` | Card neumórfica base |
| `NeuTextField.kt` | Campo con `isError/isSuccess/readOnly/isPassword/leadingContent` |
| `AppBottomNavBar.kt` | 3 tabs: Inicio (Home), Actividades (Assignment), Perfil (Person) |
| `NeuAvatar.kt` | Avatar circular hundido con iniciales — ver sección NeuAvatar abajo |

---

## YaRegistrasteBadge — tarjeta hundida (agregado desde diseño HTML, 2026-06-03)

Extraído del prototipo HTML. Usa `NeuCard variant="in"` — **sin sombra exterior, hundida**.

| Elemento | Estilo |
|----------|--------|
| Contenedor | `clip(24.dp)` + `background` + `neuInsetInner(24.dp)` · padding `24dp/26dp` |
| Círculo check | 44dp · `Color(0xFF2EA86A)` (verde vivo, ≠ Sage) · **sin sombra** |
| Ícono check | 24dp · blanco |
| Gap círculo→texto | 16dp |
| Título | DM Sans · 16sp · **Bold** · `Ink` |
| Subtítulo | DM Sans · 13sp · `Mid` · Spacer 2dp desde título |

> `Color(0xFF2EA86A)` es el verde presente del HTML (`P.present`). Es más vivo que `Sage` (0xFF6AAB8E).
>
> **Orden de modificadores crítico:** `clip` → `background` → `neuInsetInner` → `padding`.
> `neuInsetInner` usa `drawWithContent` y pinta gradientes **sobre** el contenido ya clippeado — sin sombra exterior.
> Nunca usar `neuInset` para este efecto: `neuInset` usa `drawBehind` y produce sombras externas visibles.

---

## RegistrarCard — tarjeta de asistencia (agregado desde diseño HTML, 2026-06-03)

Extraído del prototipo HTML `Presencia Mobile App.html`. NeuCard elevada con tres elementos:

| Elemento | Estilo | Valor |
|----------|--------|-------|
| Eyebrow "HOY" | DM Mono labelSmall | color `Accent` |
| Texto italic | Cormorant Garamond italic | 24sp · Medium · `Ink` · lineHeight 28sp |
| Botón | DM Sans · 17sp · SemiBold · blanco | fondo `Accent` + `neuGlow(14.dp)` · radius 14dp · padding vertical 16dp |

Card padding: `horizontal = 26.dp, vertical = 30.dp`
Espacios: eyebrow→texto 6dp · texto→botón 22dp

```kotlin
// Botón primario con ícono — patrón del HTML
Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .fillMaxWidth()
        .neuGlow(cornerRadius = 14.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Accent)
        .clickable(onClick = onClick)
        .padding(vertical = 16.dp),
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.AssignmentTurnedIn, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Tomar Asistencia", style = bodyLarge.copy(fontSize = 17.sp), color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
```

> El HTML usa `P.neuUp` (sombras gris/blanco) sobre el fondo azul. En Kotlin se usa `neuGlow` (sombra tintada en Accent) — resultado visual equivalente.

---

## HomeHeader — tipografía (agregado desde diseño HTML, 2026-06-03)

Extraído del prototipo HTML `Presencia Mobile App.html`. El header del Home tiene tres niveles:

| Nivel | Fuente | Tamaño | Peso | Color | Notas |
|-------|--------|--------|------|-------|-------|
| Eyebrow (fecha) | DM Mono | 11sp | 500 | `Muted` | uppercase · letterSpacing 0.18em |
| Nombre del grupo | Cormorant Garamond | **38sp** | **Bold (700)** | `Ink` | lineHeight 1.05 · **dos líneas**: "Grupo" / NombreGrupo |
| Subtítulo | DM Sans | 15sp | Normal | `Mid` | "X miembros · reunión semanal" |

### Implementación Kotlin (HomeScreen.kt — `HomeHeader`)

```kotlin
// Eyebrow → MaterialTheme.typography.labelSmall, color = Muted  (sin cambios)

// Nombre del grupo — dos Text() seguidos con el mismo estilo:
val groupNameStyle = MaterialTheme.typography.displayLarge.copy(
    fontSize   = 38.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 40.sp,   // 38 * 1.05 ≈ 40
)
Text(text = "Grupo",      style = groupNameStyle, color = Ink)
Text(text = grupoNombre,  style = groupNameStyle, color = Ink)

// Subtítulo
Text(
    text  = "$totalMiembros miembros · reunión semanal",
    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
    color = Mid,   // ← Mid, no Muted
)
```

> `displayLarge` ya usa CormorantGaramond — solo se sobreescribe `fontSize`, `fontWeight` y `lineHeight`.

## Tipografía — fuentes locales (agregado 2026-06-04)

Las fuentes se bundlean en `res/font/` (no Google Fonts SDK — los certificados estaban vacíos y caía al sistema serif).

| Archivo | Fuente | Tipo |
|---------|--------|------|
| `cormorant_garamond.ttf` | Cormorant Garamond | Variable wght (300–700) · upright |
| `cormorant_garamond_italic.ttf` | Cormorant Garamond | Variable wght · italic |
| `dm_sans.ttf` | DM Sans | Variable opsz+wght |
| `dm_mono_regular.ttf` | DM Mono | Estático 400 |
| `dm_mono_medium.ttf` | DM Mono | Estático 500 |

Con `FontVariation.Settings(FontVariation.weight(N))` Compose selecciona el peso exacto del eje variable.
Requiere `@OptIn(ExperimentalTextApi::class)` en las declaraciones de `FontFamily`.

---

## NeuAvatar — componente compartido (agregado 2026-06-04)

Avatar circular neumórfico con efecto hundido. Diseñado del HTML: `P.bg + P.neuInSm + P.serif Bold`.

```kotlin
NeuAvatar(
    iniciales: String,
    modifier:  Modifier = Modifier,
    size:      Dp = 44.dp,
)
```

| Propiedad | Valor |
|-----------|-------|
| Forma | `CircleShape` |
| Fondo | `Background` (gris perla) |
| Efecto | `neuInsetInner(shadowSize = size * 0.28f)` — **sin sombra exterior** |
| Fuente | Cormorant Garamond Bold |
| Tamaño texto | `size * 0.36f` sp (ratio del HTML) |
| Color texto | `Mid` |

**Orden de modificadores crítico:**
```kotlin
.clip(CircleShape)
.background(Background)
.neuInsetInner(shadowSize = size * 0.28f)
```

**Usado en (todos los avatares de iniciales de la app):**
- `RegistroPaso1Screen` — `InitialsAvatar()`
- `MiembrosListaScreen`, `DetalleReunionScreen`, `QuienEresScreen`, `ConfirmarIdentidadScreen`
- `RegistroPaso2Screen` — desglose de miembros (32dp)
- `PerfilPrincipalScreen` — avatar de perfil (80dp)
- `DuosListScreen`, `DuoDetalleScreen`, `DuoMisioneroScreen`

**NO usar para avatares hero con fondo Accent** (MiembroDetalleScreen 64dp Accent — diseño propio).
