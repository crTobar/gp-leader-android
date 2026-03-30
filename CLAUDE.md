# GP Leader — CLAUDE.md

Contexto completo del proyecto para Claude Code. Leer antes de tocar cualquier archivo.

---

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

## Qué es esta app

**GP Leader** es una app móvil Android para líderes de grupos pequeños SDA (Iglesia Adventista).
Permite registrar asistencia semanal, actividades y enviar reportes al pastor.
También tiene un flujo de **suplente** — acceso temporal vía código de 6 dígitos cuando el líder no puede asistir.

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| UI | Kotlin + Jetpack Compose |
| Navegación | Navigation Compose |
| Estado | ViewModel + StateFlow |
| Base de datos local | PowerSync (offline-first) |
| Backend | Supabase (PostgreSQL + Auth) |
| Fotos/archivos | Supabase Storage |
| Inyección de dependencias | Hilt |
| Build | Gradle (Kotlin DSL) |

---

## Estructura del proyecto

```
app/src/main/java/com/gpleader/
│
├── core/
│   ├── data/
│   │   ├── remote/          # Clientes Supabase
│   │   ├── local/           # PowerSync schema y queries
│   │   ├── repository/      # Implementaciones de repositorios
│   │   └── sync/            # Lógica de sincronización offline
│   ├── domain/
│   │   ├── model/           # Entidades del dominio
│   │   └── usecase/         # Casos de uso
│   └── ui/
│       ├── theme/           # Design system (colores, tipografía, formas)
│       ├── components/      # Composables reutilizables
│       └── navigation/      # NavGraph principal y rutas
│
└── feature/
    ├── auth/                # Login, suplente código, bienvenida suplente
    ├── home/                # Home líder (con y sin reuniones)
    ├── registro/            # Paso 1 asistencia, Paso 2 actividades, Paso 3 resumen
    ├── historial/           # Lista por trimestre + ver todo + detalle reunión
    ├── miembros/            # Lista, detalle, editar, agregar
    └── perfil/              # Principal, datos personales, contraseña, datos grupo
```

---

## Roles de usuario

```kotlin
enum class UserRole {
    LIDER,      // Cuenta asignada por pastor. Acceso completo.
    SUPLENTE    // Sesión temporal vía código de 6 dígitos. Solo registra reunión.
}
```

### Navegación por rol

```
NavHost
├── AuthGraph        → sin sesión
│   ├── LoginScreen
│   └── SuplementeGraph (código → bienvenida → registro)
│
├── LiderGraph       → rol = LIDER
│   ├── HomeScreen
│   ├── RegistroGraph
│   ├── HistorialGraph
│   └── PerfilGraph
│
└── SuplementeRegistroGraph → rol = SUPLENTE
    ├── RegistroPaso1 (con banner "Modo suplente")
    ├── RegistroPaso2
    ├── RegistroPaso3
    └── ExitoSuplente ("enviado al líder para aprobar")
```

Los composables de registro son **compartidos** entre líder y suplente.
Se diferencia pasando `esSuplente: Boolean` al ViewModel.

---

## Design System

### Filosofía visual
**Neumorfismo suave** — sin bordes duros, sombras dobles (luz arriba-izquierda, sombra abajo-derecha).
Fondo gris perla uniforme. Cards que "flotan" o "se hunden" según estado.

### Colores (Material3 → Custom)

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

### Sombras neumórficas

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

### Tipografía

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

### Radios de esquinas

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

## Modelos de dominio

```kotlin
// core/domain/model/

data class Grupo(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val lugarReunion: String?,
    val cantoFavorito: String?,
    val versiculo: String?,
    val personajeBiblico: String?,
    val diaSemana: DiaSemana,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val iglesiaId: String,
    val distritoId: String,
    val campoId: String
)

data class Miembro(
    val id: String,
    val grupoId: String,
    val primerNombre: String,
    val segundoNombre: String?,
    val primerApellido: String,
    val segundoApellido: String?,
    val telefono: String?,
    val correo: String?,
    val direccion: String?,
    val estado: EstadoMiembro,  // ACTIVO, ARCHIVADO
    val fechaIngreso: LocalDate
)

data class Reunion(
    val id: String,
    val grupoId: String,
    val fecha: LocalDate,
    val noHuboReunion: Boolean,
    val estado: EstadoReunion,  // BORRADOR, ENVIADA, PENDIENTE_SYNC, APROBADA
    val creadaPorSuplente: Boolean,
    val suplementeNombre: String?,
    val aprobadaPorLider: Boolean,
    val sincronizada: Boolean
)

data class Asistencia(
    val id: String,
    val reunionId: String,
    val miembroId: String?,      // null si es visita
    val nombreVisita: String?,   // solo si es visita
    val esVisita: Boolean,
    val estado: EstadoAsistencia // PRESENTE, AUSENTE, JUSTIFICADO
)

data class Actividad(
    val id: String,
    val nombre: String,
    val nivel: NivelActividad,   // UNION, MISION, PASTOR, GP
    val tipo: TipoFormulario,    // CONTADOR, EXTENDIDO
    val unidad: String,          // "personas", "libros", etc.
    val esOficial: Boolean,
    val soloEditable: NivelActividad  // quien puede modificarla
)

data class RegistroActividad(
    val id: String,
    val reunionId: String,
    val actividadId: String,
    val cantidad: Int?,           // null = sin llenar (muestra "—")
    val notas: String?
)

enum class NivelActividad { UNION, MISION, PASTOR, GP }
enum class EstadoAsistencia { PRESENTE, AUSENTE, JUSTIFICADO }
enum class EstadoMiembro { ACTIVO, ARCHIVADO }
enum class EstadoReunion { BORRADOR, PENDIENTE_SYNC, ENVIADA, APROBADA }
```

---

## Reglas de negocio críticas

### Registro de reunión (3 pasos)
1. **Paso 1 — Asistencia**
   - Fecha auto-llena con hoy, editable con botón ✎
   - Botón "No hubo reunión" → marca la reunión y envía sin asistencia
   - "Sel. todos P/A/J" → aplica a todos los miembros según primera selección
   - Si todos quedan en A → modal de confirmación antes de continuar
   - Visitas anteriores: colapsable, misma UI que miembros, botón "+ Agregar" por cada una
   - Al escribir nueva visita → dropdown hacia ARRIBA con coincidencias de visitas anteriores
   - Nueva visita se registra como Presente automáticamente (sin toggle P/A/J)

2. **Paso 2 — Actividades**
   - Actividades agrupadas por nivel: Unión → Pastor → Mi GP
   - Unión: bloqueadas para el líder (solo lectura, 🔒)
   - Pastor y GP: editables por el líder
   - Contador empieza en `—` (sin valor), NO en 0
   - Si se intenta avanzar con algún `—` en actividad obligatoria → error en rojo
   - Actividades con formulario extendido → pantalla completa al tocar (no sheet)
   - "Agregar actividad extra" → pantalla completa, solo para nivel GP

3. **Paso 3 — Resumen**
   - Muestra resumen de asistencia + actividades clasificadas por nivel
   - Botón "Enviar al pastor" → si hay conexión envía, si no guarda offline
   - Offline: se sincroniza automáticamente al recuperar conexión

### Flujo suplente
- Código de 6 dígitos, válido 24 horas, un solo uso
- El líder lo genera desde botón "Suplente" en el nav superior del Home
- Suplente abre app → "Ingresar como suplente" → ingresa código → ve bienvenida con datos del grupo
- Suplente registra reunión igual que líder (Paso 1, 2, 3) pero con banner negro "Modo suplente"
- Al enviar → va al líder para aprobar, NO al pastor directamente
- En el historial queda registrado "Registrado por [nombre suplente] · Aprobado por [líder]"

### Actividades y jerarquía
- Solo el nivel que creó la actividad o superior puede modificar el contador
- El líder solo puede editar actividades de nivel GP y agregar extras propias
- Las de Unión/Misión/Pastor son de solo lectura para el líder

### Nombres de miembros
- Campos separados: primer nombre, segundo nombre (opcional), primer apellido, segundo apellido (opcional)
- Al agregar miembro → estado siempre ACTIVO (sin toggle en formulario de agregar)
- Toggle Activo/Archivado solo aparece en formulario de EDITAR

---

## Pantallas y flujo aprobado

```
AUTH
├── LoginScreen                    → correo + contraseña, botón "Ingresar como suplente"
├── SuplementeCodigoScreen         → teclado numérico, 6 cajas, código de 6 dígitos
└── SuplementeBienvenidaScreen     → datos del grupo, campo nombre suplente

HOME (Líder)
├── HomeScreen                     → con reuniones recientes (max 2 + "Ver todas")
├── HomeEmptyScreen                → sin reuniones, guía de inicio
└── SheetGenerarCodigoSuplente     → bottom sheet con código, vigencia 24h

REGISTRO
├── RegistroPaso1Screen            → fecha + editar + no hubo reunión + asistencia + visitas
├── RegistroPaso1VisitasScreen     → variante con visitas anteriores expandidas
├── SheetAgregarVisitaScreen       → bottom sheet con buscador y dropdown hacia arriba
├── AgregarActividadScreen         → pantalla completa (no sheet)
├── RegistroPaso2Screen            → actividades con jerarquía Unión/Pastor/GP
├── RegistroPaso3Screen            → resumen clasificado + enviar
├── ExitoEnviadoScreen             → hero negro + pastor al que se envió + resumen
└── ExitoOfflineScreen             → hero gris + nota de sincronización automática

HISTORIAL
├── HistorialScreen                → tabs por trimestre (1er/2do/3er/4to + Ver todo)
└── DetalleReunionScreen           → stats + asistencia P/A/J + actividades por nivel

MIEMBROS
├── MiembrosListaScreen            → buscador + activos + archivados
├── MiembroDetalleScreen           → nombre separado + contacto + historial asistencia
├── MiembroEditarScreen            → nombre separado + expandibles + estado + historial
└── MiembroAgregarScreen           → nombre separado + expandibles + nota activo auto

PERFIL
├── PerfilPrincipalScreen          → menú principal con avatar
├── PerfilDatosPersonalesScreen    → nombre separado + contacto + iglesia readonly
├── PerfilCambiarContrasenaScreen  → 3 campos + checklist requisitos
└── PerfilDatosGrupoScreen         → identificación + ubicación eclesiástica + horario
```

---

## Convenciones de código

### Nomenclatura
```
Screens     → NombreScreen.kt
ViewModels  → NombreViewModel.kt
Repository  → NombreRepository.kt (interfaz) + NombreRepositoryImpl.kt
UseCase     → VerbNombreUseCase.kt (ej: GetReunionesUseCase)
Model       → Nombre.kt (sin sufijo)
```

### Composables
- Un archivo por pantalla
- Composables reutilizables en `core/ui/components/`
- Preview siempre con datos de muestra (`@Preview`)
- Estado siempre en ViewModel, nunca en composable (excepto estado UI efímero)

### ViewModel pattern
```kotlin
// Estado como data class inmutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val reuniones: List<Reunion> = emptyList(),
    val error: String? = null
)

// Un solo StateFlow de estado
val uiState: StateFlow<HomeUiState>

// Eventos del usuario como funciones
fun onRegistrarClick() { ... }
fun onVerTodasClick() { ... }
```

### Offline-first
- **Toda escritura** va primero a PowerSync (local), luego se sincroniza con Supabase
- **Toda lectura** viene de PowerSync (local)
- El estado de sincronización se muestra en la UI (enviada / pendiente / offline)
- Nunca bloquear la UI esperando respuesta de red

---

## Supabase — tablas principales

```sql
grupos, miembros, reuniones, asistencias,
actividades, registro_actividades,
codigos_suplente, usuarios, iglesias, distritos, campos
```

### RLS (Row Level Security)
- Lider solo accede a datos de su grupo
- Suplente accede solo durante vigencia del código (24h)
- Pastor accede a todos los grupos de su iglesia

---

## Comandos útiles

```bash
# Build debug
./gradlew assembleDebug

# Tests
./gradlew test

# Lint
./gradlew lint

# Instalar en dispositivo
./gradlew installDebug
```

---

## Implementación actual (2026-03-16)

### Archivos creados — estado COMPLETO

```
feature/auth/
  LoginScreen.kt              ✅ correo + contraseña + btn suplente
  LoginViewModel.kt           ✅
  SuplementeCodigoScreen.kt   ✅ 6 boxes neuInset, teclado numérico, shake error, auto-valida
  SuplementeBienvenidaScreen.kt ✅ hero Ink 35%, card grupo, nota Gold, campo nombre
  SuplementeViewModel.kt      ✅ compartido entre SuplementeCodigoScreen y SuplementeBienvenidaScreen
                                  + generación de código para SheetGenerarCodigoSuplente

feature/home/
  HomeScreen.kt               ✅ reuniones recientes, stats, bottom nav
  HomeViewModel.kt            ✅
  SheetGenerarCodigoSuplente.kt ✅ 6 digit Ink NeuCards, barra vigencia Sage/Gold/Blush,
                                    nota dashed, Compartir/Generar nuevo/Revocar + AlertDialog

feature/historial/
  HistorialScreen.kt          ✅ tabs trimestre, stats, lista reuniones
  HistorialViewModel.kt       ✅
  DetalleReunionScreen.kt     ✅ stats P/A/J/%, asistencia list, actividades por nivel
  DetalleReunionViewModel.kt  ✅ SavedStateHandle["reunionId"], sample data r1-r4

feature/registro/
  RegistroPaso1Screen.kt      ✅ acepta esSuplente: Boolean
  RegistroPaso2Screen.kt      ✅
  RegistroPaso3Screen.kt      ✅
  AgregarActividadScreen.kt   ✅
  DetalleActividadScreen.kt   ✅
  ExitoEnviadoScreen.kt       ✅
  ExitoOfflineScreen.kt       ✅
  RegistroViewModel.kt        ✅

feature/miembros/
  MiembrosListaScreen.kt      ✅ buscador, sección ACTIVOS/ARCHIVADOS, badge, bottom nav
  MiembroDetalleScreen.kt     ✅ HeroCard, InfoCard, HistorialCard
  MiembroEditarScreen.kt      ✅ expandables, EstadoToggle neuInset/neuElevated, historial
  MiembroAgregarScreen.kt     ✅ avatar animado (dashed→iniciales), NotaActivo, NO toggle
  MiembrosViewModel.kt        ✅ compartido en MIEMBROS_GRAPH, onPrepararAgregar() resetea form

feature/perfil/
  PerfilPrincipalScreen.kt    ✅
  PerfilDatosPersonalesScreen.kt ✅
  PerfilCambiarContrasenaScreen.kt ✅
  PerfilDatosGrupoScreen.kt   ✅ 6 campos identificación, ubicación readonly, dropdowns horario

core/ui/navigation/NavGraph.kt  ✅ ver rutas abajo
core/ui/theme/                  ✅ Elevation.kt, Color.kt, Type.kt, Theme.kt
core/ui/components/
  NeuButton.kt                ✅ NeuButtonPrimary (Accent+neuGlow) / NeuButtonSecondary (Background+neuElevated)
  NeuCard.kt                  ✅
  NeuTextField.kt             ✅ isError/isSuccess/readOnly/isPassword/leadingContent
```

### NavGraph — rutas actuales

```kotlin
object NavRoutes {
    LOGIN                = "login"
    SUPLENTE_GRAPH       = "suplente_graph"          // nested graph
    SUPLENTE_CODIGO      = "suplente_codigo"          // start destination de SUPLENTE_GRAPH
    SUPLENTE_BIENVENIDA  = "suplente_bienvenida"
    HOME                 = "home"
    HISTORIAL            = "historial"
    PERFIL               = "perfil"
    PERFIL_DATOS_PERSONALES   = "perfil/datos_personales"
    PERFIL_CAMBIAR_CONTRASENA = "perfil/cambiar_contrasena"
    PERFIL_DATOS_GRUPO        = "perfil/datos_grupo"
    DETALLE_REUNION      = "detalle_reunion/{reunionId}"
    MIEMBROS_GRAPH       = "miembros_graph"           // nested graph
    MIEMBROS_LISTA       = "miembros"
    MIEMBROS_DETALLE     = "miembros/detalle"
    MIEMBROS_EDITAR      = "miembros/editar"
    MIEMBROS_AGREGAR     = "miembros/agregar"
    REGISTRO_GRAPH       = "registro"                 // nested graph
    REGISTRO_PASO1       = "registro/paso1"
    REGISTRO_PASO2       = "registro/paso2"
    REGISTRO_PASO3       = "registro/paso3"
    DETALLE_ACTIVIDAD    = "registro/detalle/{actividadId}"
    AGREGAR_ACTIVIDAD    = "registro/agregar_actividad"
    EXITO_ENVIADO        = "registro/exito_enviado"
    EXITO_OFFLINE        = "registro/exito_offline"
}
```

**Flujo de navegación AUTH → LOGIN navega a `SUPLENTE_GRAPH` (no a SUPLENTE_CODIGO directamente).**

### Patrón ViewModel compartido en nested graph

```kotlin
// En cada composable dentro del nested graph:
composable(NavRoutes.ALGUNA_RUTA) { backStackEntry ->
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(NavRoutes.ALGUNA_GRAPH)
    }
    val sharedVm: AlgunViewModel = hiltViewModel(graphEntry)
    AlgunaScreen(viewModel = sharedVm, ...)
}
```
Usado en: REGISTRO_GRAPH (RegistroViewModel), MIEMBROS_GRAPH (MiembrosViewModel), SUPLENTE_GRAPH (SuplementeViewModel).

### Patrón dashedBorder

Se repite en múltiples archivos como función `private fun Modifier.dashedBorder(...)`.
Usa `drawBehind` + `drawIntoCanvas` + `android.graphics.DashPathEffect`.
**No está centralizado en components/ — cada archivo tiene su copia local.**

### Patrón borde de estado (error/active) en campos custom

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

### Datos de muestra (temporal — reemplazar con PowerSync)

- Código suplente válido para pruebas: **`123456`**
- Reuniones en DetalleReunionViewModel: **r1, r2, r3, r4** (fallback para IDs desconocidos)
- Miembros en MiembrosViewModel: 10 miembros (8 ACTIVO, 2 ARCHIVADO)
- HomeViewModel: María García, GP Los Olivos, 85% asistencia, reuniones r1 y r2

### SuplementeViewModel — responsabilidades

Un solo ViewModel cubre dos flujos:
1. **Suplente entrante** (SuplementeCodigoScreen → SuplementeBienvenidaScreen): valida código, recibe grupoInfo, almacena nombreSuplente
2. **Líder generando código** (SheetGenerarCodigoSuplente): genera/revoca código, muestra vigencia

El ViewModel vive en el scope de SUPLENTE_GRAPH (para flujo suplente) y como instancia independiente en HomeScreen (para generación de código por el líder).

---

## Lo que NO hacer

- ❌ No usar `remember` para estado de negocio — va en ViewModel
- ❌ No llamar Supabase directamente desde composables — siempre vía repositorio
- ❌ No hardcodear strings — usar `strings.xml`
- ❌ No bloquear UI con llamadas de red — siempre coroutines + StateFlow
- ❌ No poner lógica en la capa UI — va en UseCase o ViewModel
- ❌ No ignorar el estado offline — toda operación debe funcionar sin red
- ❌ No usar `0` como valor inicial de contador de actividades — usar `null` que se muestra como `—`
- ❌ No mostrar toggle Activo/Archivado al AGREGAR miembro — solo al EDITAR
