# GP Leader — CLAUDE.md

Contexto completo del proyecto para Claude Code. Leer antes de tocar cualquier archivo.


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
    ├── miembros/            # Lista, detalle, editar, agregar (rol LIDER)
    ├── miembro/             # Home y actividades del rol MIEMBRO
    ├── actividades/         # Lista y historial de actividades del grupo
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
   - Cada actividad es una **NeuCard individual** con chip de valor (`—` vacío o total calculado)
   - CHECKBOX: toggle inline dentro de su NeuCard
   - Tap en actividad editable → navega a `DetalleActividadScreen` (pantalla completa)
   - El total del chip se auto-calcula desde el desglose por miembro
   - Si se intenta avanzar con algún `—` en actividad obligatoria → error en rojo
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

ACTIVIDADES
├── ActividadesListScreen          → lista de actividades con filtros nivel/estado + tarjetas NeuCard
└── ActividadHistorialScreen       → resumen acumulado + historial semanal editable
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

**Nombres reales en Supabase (en inglés):**

| Tabla Supabase | Descripción |
|---------------|-------------|
| `campo`        | Unión/campo eclesiástico |
| `district`     | Distrito (hijo de campo) |
| `church`       | Iglesia (hija de distrito) |
| `small_group`  | Grupo pequeño (hijo de iglesia) |
| `member`       | Miembro del GP |
| `meeting`      | Reunión registrada |
| `attendance`   | Asistencia por reunión |
| `activity_type`| Tipo de actividad (level, marker_type, scope, start_date, end_date) |
| `activity_record` | Registro de actividad por reunión (count nullable, monto) |
| `member_activity_record` | Registro individual de actividad por miembro (count, monto, is_done, status) |
| `deputy_code`  | Código de suplente |

### Columnas importantes

**`small_group`:**
- `gp_username` — username para auth (ej: `ucn-cat-icb-gp-los-olivos`)
- `gp_password_set` — boolean: si ya cambió la contraseña temporal
- `gp_password` — contraseña temporal (primera vez)
- `gp_code` — código suplente activo
- `church_id` — FK a `church`

**`member`:**
- `is_visitor` — boolean: `true` = visita puntual, `false` = miembro permanente
- `is_active` — boolean: `true` = activo, `false` = archivado
- Los miembros con `is_visitor=true` NO aparecen en la lista de miembros del grupo

### Auth — mecanismo real

El login usa Supabase Auth con email sintético:
```
email:    "{gp_username}@login.presencia.app"
password: contraseña del grupo
```

- **Primera vez** (`gp_password_set = false`): navegar a `CAMBIAR_CONTRASENA`
- **Login exitoso normal**: navegar a `QUIEN_ERES`
- La carga de datos del login (campos, distritos, iglesias, GPs) requiere una sesión anónima previa (`loginAnonymously()` en Application startup)

### Jerarquía eclesiástica (para login)

```
campo → district → church → small_group
```

El LoginViewModel carga las 4 tablas en paralelo al init y aplica filtrado cliente:
- Seleccionar campo → filtra distritos; resetea iglesia y GP
- Seleccionar distrito → filtra iglesias; resetea GP
- Seleccionar iglesia → filtra GPs
- Seleccionar GP directamente → auto-rellena los 3 campos superiores

Todos los dropdowns están **siempre habilitados** — no dependen del superior para activarse.

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

## Implementación actual (2026-06-01)

### Archivos creados — estado COMPLETO

```
feature/auth/
  LoginScreen.kt              ✅ dropdowns campo/distrito/iglesia/GP con búsqueda y tarjetas
                                  todos habilitados siempre, filtrado inteligente en cascada
                                  GrupoDropdown: muestra "nombre · Iglesia · Distrito · Campo"
                                  IglesiaDropdown: muestra "nombre · Distrito · Campo"
                                  tap en tarjeta de GP → navega directo a QuienEres (sin botón intermedio)
  LoginViewModel.kt           ✅ carga 4 tablas en paralelo (getCampos/getDistritos/getIglesias/getGrupos)
                                  enriquece iglesias y GPs cliente-side con nombres completos
                                  auth real: supabase.auth.signInWith(Email) → gp_username@login.presencia.app
                                  onGrupoTap() guarda sesión y dispara navigateToQuienEres (sin selectedGrupo en state)
  QuienEresScreen.kt          ✅ pantalla para seleccionar rol (LIDER / SUPLENTE) post-login
  QuienEresViewModel.kt       ✅
  ConfirmarIdentidadScreen.kt ✅ pantalla de confirmación de identidad post-código suplente
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
                                  usa OnResumeEffect { viewModel.cargarReuniones() } para refrescar al volver
  HistorialViewModel.kt       ✅ carga reuniones reales desde Supabase via ReunionRepository
                                  filtrado real por trimestre: aplicarFiltro() filtra todasLasReuniones
                                  trimestresDelAnio usa año actual dinámicamente
                                  cargarReuniones() es public — llamado desde OnResumeEffect
  DetalleReunionScreen.kt     ✅ stats P/A/J/%, asistencia list, actividades por nivel
  DetalleReunionViewModel.kt  ✅ carga detalle real desde Supabase via getDetalleReunion
                                  muestra error real en pantalla (no mensaje genérico)
                                  ⚠️ tiene Log.e temporal — remover después de debug

feature/registro/
  RegistroPaso1Screen.kt      ✅ acepta esSuplente: Boolean
                                  botón "No hubo reunión" con NeuCard neumórfica (neuElevatedSm)
  RegistroPaso2Screen.kt      ✅ REDISEÑADO (2026-06-01):
                                  - SeccionActividades: etiqueta de nivel como pill + NeuCard por actividad
                                  - ActividadCard: ícono de tipo + nombre + chip valor (— o total) + chevron/🔒
                                  - CHECKBOX: toggle inline en su NeuCard
                                  - Sin controles inline (ContadorInline/MontoInline eliminados de Paso 2)
                                  - Toda edición numérica ocurre en DetalleActividadScreen
  RegistroPaso3Screen.kt      ✅
  AgregarActividadScreen.kt   ✅ chips tipo marcador en horizontalScroll, nombre neuInsetSm,
                                  monto MontoGrande ₡, sección PERÍODO con DatePickerDialog
                                  startDate default = hoy, endDate default = null
  DetalleActividadScreen.kt   ✅ REDISEÑADO (2026-06-01):
                                  - Total: TotalReadOnly (solo lectura, calculado del desglose)
                                  - Desglose: siempre expandido, soporta CONTADOR/MONETARIO/PARTICIPANTES
                                  - sinLimite=true → cada miembro ingresa su valor libremente
                                  - onGuardar solo persiste notas (total vive en el VM)
                                  - ContadorGrande mantenido como internal (usado por otras pantallas)
  ExitoEnviadoScreen.kt       ✅
  ExitoOfflineScreen.kt       ✅
  RegistroViewModel.kt        ✅ onEnviarClick guarda reunión real en Supabase via ReunionRepository
                                  isEnviando flag previene doble submit
                                  carga actividades reales desde activity_type via ActividadRepository
                                  onAgregarActividadExtra acepta startDate/endDate (LocalDate?)
                                  ActividadRegistro tiene campos startDate y endDate
                                  error 23505 (duplicado de fecha) → mensaje amigable en español
                                  onSiguienteClick solo bloquea actividades esObligatoria=true
                                  tieneDesglose = !bloqueada && tipo != CHECKBOX (todos los editables)
                                  desgloseExpandido = true por defecto
                                  onDesgloseChange/MontoChange/ParticipacionChange: auto-calcula total
                                    (dirección invertida: desglose → total, no total → desglose)

feature/actividades/
  ActividadesListScreen.kt    ✅ filtros FiltroNivel + FiltroEstado en horizontalScroll
                                  NivelBadge (Gold=Unión, Ink=Pastor, Accent=Mi GP)
                                  EstadoBadge (Sage=Activa, Blush=Vencida)
                                  ACUMULADO en titleLarge con color de nivel
                                  ⚠️ badge de pendientes (drafts) pendiente de implementar
  ActividadesListViewModel.kt ✅ FiltroNivel + FiltroEstado + ActividadConResumen
                                  getTodasActividadesTipo sin filtro período + getActividadesConTotales
                                  ⚠️ getPendingCountPerTipo pendiente de conectar a la UI
  ActividadHistorialScreen.kt ✅ ResumenCard serif + RegistroRow con bloque fecha
                                  edición inline: BasicTextField + cancelar/guardar
                                  ⚠️ sección "Enviados por miembros" pendiente de implementar
  ActividadHistorialViewModel.kt ✅ getRegistrosSemanal + updateRegistro en Supabase
  CrearActividadTipoScreen.kt ✅ stub
  CrearActividadTipoViewModel.kt ✅ stub

feature/miembro/               ← ROL MIEMBRO (perfil futuro)
  MiembroHomeScreen.kt        ✅ stub
  MiembroHomeViewModel.kt     ✅ stub
  MiembroActividadesScreen.kt ✅ stub
  MiembroActividadesViewModel.kt ✅ stub

feature/miembros/
  MiembrosListaScreen.kt      ✅ buscador, sección ACTIVOS/ARCHIVADOS, badge, bottom nav
  MiembroDetalleScreen.kt     ✅ HeroCard, InfoCard, HistorialCard
  MiembroEditarScreen.kt      ✅ expandables, EstadoToggle neuInset/neuElevated, historial
  MiembroAgregarScreen.kt     ✅ avatar animado (dashed→iniciales), NotaActivo, NO toggle
  MiembrosViewModel.kt        ⚠️ aún usa sample data (10 miembros hardcoded)

feature/perfil/
  PerfilPrincipalScreen.kt    ✅ incluye ítem "Actividades" en sección MI GRUPO
                                  navega a ACTIVIDADES_LISTA via onNavigateToActividadesLista
  PerfilDatosPersonalesScreen.kt ✅
  PerfilCambiarContrasenaScreen.kt ✅
  PerfilDatosGrupoScreen.kt   ✅ 6 campos identificación, ubicación readonly, dropdowns horario
  PerfilViewModel.kt          ✅ navigateToActividadesLista flag + consumeActividadesListaNavigation()

core/data/repository/
  GrupoRepository.kt          ✅ interfaz jerarquía campo→district→church→small_group
  GrupoRepositoryImpl.kt      ✅
  MiembroRepository.kt        ✅ interfaz getMiembros/getMiembrosActivos/getVisitasAnteriores
  MiembroRepositoryImpl.kt    ✅
  ReunionRepository.kt        ✅ interfaz getReuniones + saveReunion + getSabbathMeeting + submitSabbathMeeting
  ReunionRepositoryImpl.kt    ✅ usa takeIf { it !is JsonNull } en todos los joins embebidos de getDetalleReunion
                                  ⚠️ tiene Log.d temporales en parseReuniones y getDetalleReunion — remover después de debug
  ActividadRepository.kt      ✅ interfaz:
                                  getActividadesTipo — con filtro período (para Paso 2)
                                  getTodasActividadesTipo — sin filtro período (para lista)
                                  saveRegistros, getActividadesConTotales, getRegistrosSemanal, updateRegistro
                                  getActividadesMiembro, getRegistrosMiembro, toggleMiembroActividad
                                  getContadorSemanalMiembro, upsertContadorSemanalMiembro
                                  getPendingMemberActivities(grupoId, actividadTipoId) → List<MemberActivitySubmission>
                                  getPendingCountPerTipo(grupoId) → Map<tipoId, count>
                                  approveMemberActivity(recordId, correctedCount, correctedMonto, isMonetary)
                                  rejectMemberActivity(recordId)
  ActividadRepositoryImpl.kt  ✅ toggleMiembroActividad y upsertContadorSemanalMiembro incluyen status="draft"
                                  getActividadesConTotales filtra member_activity_record por status IN (approved, pending_board)
                                  approveMemberActivity → status "approved" o "pending_board" según isMonetary
  GroupLogRepository.kt       ✅ stub (referenciado en RepositoryModule)
  GroupLogRepositoryImpl.kt   ✅ stub
  IglesiaRepository.kt        ✅ stub
  IglesiaRepositoryImpl.kt    ✅ stub
  SolicitudRepository.kt      ✅ stub
  SolicitudRepositoryImpl.kt  ✅ stub

core/ui/navigation/NavGraph.kt  ✅ incluye todas las rutas, ver lista abajo
core/ui/theme/                  ✅ Elevation.kt (neuElevated/neuElevatedSm/neuInset/neuInsetSm/neuGlow)
                                    bgColor: Color = NeuBg en drawNeuShadows/neuElevated/neuElevatedSm
                                    para soportar fondos distintos al Background global
                                    Color.kt, Type.kt (sin FontStyle.Italic), Theme.kt
core/ui/components/
  AppLogo.kt                  ✅ composable reutilizable con size/cornerRadius/iconSize configurables
  NeuButton.kt                ✅ NeuButtonPrimary (Accent+neuGlow) / NeuButtonSecondary (Background+neuElevated)
  NeuCard.kt                  ✅
  NeuTextField.kt             ✅ isError/isSuccess/readOnly/isPassword/leadingContent
```

### NavGraph — rutas actuales

```kotlin
object NavRoutes {
    LOGIN                = "login"
    QUIEN_ERES           = "quien_eres"               // post-login: selección de rol
    SUPLENTE_GRAPH       = "suplente_graph"           // nested graph
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
    ACTIVIDADES_LISTA    = "actividades_lista"
    ACTIVIDAD_HISTORIAL  = "actividad_historial/{actividadTipoId}"
    // helper: fun actividadHistorial(id: String) = "actividad_historial/$id"
}
```

Flujo de navegación a actividades:
- `PERFIL` → "Actividades" ítem → `ACTIVIDADES_LISTA`
- `ACTIVIDADES_LISTA` → tap tarjeta → `ACTIVIDAD_HISTORIAL`

**Flujo AUTH:**
- `LOGIN` → login exitoso con `passwordSet=true` → `QUIEN_ERES`
- `LOGIN` → login con `passwordSet=false` → `PERFIL_CAMBIAR_CONTRASENA`
- `LOGIN` → "Ingresar como suplente" → navega a `SUPLENTE_GRAPH` (no a SUPLENTE_CODIGO directamente)

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
- Miembros en MiembrosViewModel: 10 miembros hardcoded (8 ACTIVO, 2 ARCHIVADO) ⚠️ pendiente reemplazar
- HomeViewModel: nombre del grupo viene de session.grupoNombre, reuniones recientes son reales; stats del grupo (iglesia, horario) ⚠️ pendiente
- Actividades: ✅ CONECTADAS A SUPABASE — carga desde `activity_type` via ActividadRepository

### Patrón LoginViewModel — cascading dropdown filter

```kotlin
// LoginUiState tiene:
val allCampos: List<CampoItem>       // todos, cargado una vez al init
val allDistritos: List<DistritoItem> // todos, cargado una vez al init
val allIglesias: List<IglesiaItem>   // todos + enriquecidos (districtNombre, campoNombre)
val allGrupos: List<GrupoItem>       // todos + enriquecidos (iglesiaNombre, districtNombre, campoNombre)

val filteredDistritos: List<DistritoItem>  // subconjunto mostrado en el dropdown
val filteredIglesias: List<IglesiaItem>    // subconjunto mostrado en el dropdown
val filteredGrupos: List<GrupoItem>        // subconjunto mostrado en el dropdown

// onCampoSelected: filtra filteredDistritos; resetea distrito/iglesia/grupo seleccionados
// onDistritoSelected: filtra filteredIglesias; resetea iglesia/grupo seleccionados
// onIglesiaSelected: filtra filteredGrupos; resetea grupo seleccionado
// onGrupoSelected: auto-rellena iglesia→distrito→campo desde los all* maps, ajusta filtered*
```

### Patrón ReunionRepository — saveReunion

```kotlin
// Guarda reunión + asistencias en Supabase
// Flujo:
// 1. INSERT en meeting con status="submitted"
// 2. Visitas nuevas (miembroId=null) → INSERT en member con is_visitor=true → obtener IDs
// 3. INSERT en attendance para miembros + visitas anteriores + visitas nuevas
//    status mapeado: PRESENTE→"present", AUSENTE→"absent", JUSTIFICADO→"justified"

data class AsistenciaParaGuardar(
    val miembroId: String?,    // null solo si es visita NUEVA (se crea en saveReunion)
    val nombreVisita: String?, // nombre completo si es visita nueva
    val esVisita: Boolean,
    val estado: String,        // "PRESENTE" | "AUSENTE" | "JUSTIFICADO"
)
```

### Schema Supabase confirmado — enums

```
meeting_status    = {draft, submitted}          -- usar "submitted" al guardar
attendance_status = {present, absent, justified} -- mapear desde dominio español
```

### Tabla `attendance` — NO tiene is_visitor ni visitor_name

```
attendance: id, meeting_id, member_id (NOT NULL), status (attendance_status), note
```
Las visitas se guardan en `member` con `is_visitor=true`. Su `id` va en `attendance.member_id`.
Unique constraint en `meeting`: `UNIQUE(small_group_id, meeting_date, registry_kind)` — permite una `gp_meeting` Y una `saturday_worship` en el mismo día para el mismo grupo.

### Patrón MiembroRepository — filtrado is_visitor

```kotlin
// member.is_visitor = true  → visita puntual, aparece en lista de visitas del Paso 1
// member.is_visitor = false → miembro real, aparece en MiembrosListaScreen
// member.is_active  = false → archivado, aparece en sección ARCHIVADOS de MiembrosListaScreen

// getMiembros()       → filtra is_visitor=false (activos + archivados reales)
// getMiembrosActivos()→ filtra is_visitor=false AND is_active=true
```

### SuplementeViewModel — responsabilidades

Un solo ViewModel cubre dos flujos:
1. **Suplente entrante** (SuplementeCodigoScreen → SuplementeBienvenidaScreen): valida código, recibe grupoInfo, almacena nombreSuplente
2. **Líder generando código** (SheetGenerarCodigoSuplente): genera/revoca código, muestra vigencia

El ViewModel vive en el scope de SUPLENTE_GRAPH (para flujo suplente) y como instancia independiente en HomeScreen (para generación de código por el líder).

### Tabla `member_activity_record` — columnas confirmadas

```
id, member_id, activity_type_id, record_date (date), count (int, nullable),
monto (numeric, nullable), is_done (boolean), status (text)
```

- `status`: `"draft"` | `"approved"` | `"pending_board"` | `"rejected"`
  - `draft` → recién enviado por miembro, pendiente de aprobación del líder
  - `approved` → aprobado por líder (actividades no monetarias)
  - `pending_board` → aprobado por líder, pendiente de Junta de Iglesia (monetarias)
  - `rejected` → rechazado por líder
- Unique constraint en `member_id, activity_type_id, record_date`
- Los totales en `getActividadesConTotales` solo suman `status IN ('approved', 'pending_board')`
- DDL aplicado: `ALTER TABLE member_activity_record ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','pending_board','rejected'));`

### Tabla `activity_type` — columnas confirmadas

```
id, name, level, marker_type, unit_label, sort_order, scope, church_id,
start_date (date, nullable), end_date (date, nullable), is_active (boolean)
```

- `level`: `"union"` | `"pastor"` | `"my_group"`
- `marker_type`: `"counter"` | `"monetary"` | `"checkbox"` | `"participants"`
- `scope`: `"global"` | `"church"` (church-scoped filtra por church_id == iglesiaId)
- `start_date` / `end_date`: definen período de vigencia. NULL = sin restricción.
- DDL aplicado: `ALTER TABLE activity_type ADD COLUMN IF NOT EXISTS start_date date, ADD COLUMN IF NOT EXISTS end_date date;`

**Dos métodos en ActividadRepository:**
- `getActividadesTipo(iglesiaId)` — aplica filtro scope + filtro período (solo activas hoy) → para Paso 2 del registro
- `getTodasActividadesTipo(iglesiaId)` — aplica filtro scope, sin filtro período → para ActividadesListScreen (muestra vencidas con badge "Vencida")

### Patrón ActividadConResumen + filtros

```kotlin
data class ActividadConResumen(
    val tipo:          ActividadTipoData,
    val totalCantidad: Int,      // suma de activity_record.count para este grupo
    val montoTotal:    Double,   // suma de activity_record.monto
    val esActiva:      Boolean,  // calculado client-side con start/endDate
)

enum class FiltroNivel(val valor: String?, val label: String) {
    TODOS(null, "Todo"), UNION("union", "Unión"),
    PASTOR("pastor", "Pastor"), MI_GP("my_group", "Mi GP"),
}
enum class FiltroEstado(val label: String) {
    TODAS("Todas"), ACTIVAS("Activas"), INACTIVAS("Inactivas"),
}
```

Fórmula `esActiva`:
```kotlin
(tipo.startDate == null || !today.isBefore(tipo.startDate)) &&
(tipo.endDate   == null || !today.isAfter(tipo.endDate))
```

### Patrón formatTotalValor en ActividadesListScreen

```kotlin
private fun formatTotalValor(item: ActividadConResumen): String = when (item.tipo.markerType) {
    "monetary"     -> "₡${item.montoTotal.toLong()}"
    "checkbox"     -> "${item.totalCantidad} semanas"
    "participants" -> "${item.totalCantidad} ${item.tipo.unitLabel}"
    else           -> "${item.totalCantidad} ${item.tipo.unitLabel}"
}
```

### Patrón levelColor

```kotlin
private fun levelColor(level: String): Color = when (level) {
    "union"  -> Gold
    "pastor" -> Ink
    else     -> Accent  // my_group
}
```

### AgregarActividadScreen — campos de fecha

- `startDate: LocalDate?` default = `LocalDate.now()`
- `endDate: LocalDate?` default = `null`
- `FechaRow` composable usa Material3 `DatePickerDialog` + `rememberDatePickerState`
- Conversión LocalDate ↔ millis: `date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()`
- La fecha de inicio NO tiene botón de limpiar (`puedeEliminar = false`); la de vencimiento sí
- `@OptIn(ExperimentalMaterial3Api::class)` requerido en `FechaRow`

### ActividadRegistro — campos de fecha (en RegistroViewModel)

```kotlin
data class ActividadRegistro(
    ...
    val startDate: java.time.LocalDate? = null,
    val endDate:   java.time.LocalDate? = null,
)
```
`onAgregarActividadExtra(nombre, tipoMarcador, cantidad, unidad, monto, tieneDesglose, startDate, endDate)`
Las fechas se guardan en ActividadRegistro pero aún NO se persisten en Supabase (pendiente crear activity_type para extras).

### Patrón JsonNull en respuestas PostgREST

Cuando una FK es NULL (ej: `visited_church_id = NULL`), PostgREST devuelve `"church": null` en el JSON.
`kotlinx.serialization` representa esto como `JsonNull` — un `JsonElement` no-nulo de Kotlin.
`?.jsonObject` solo guarda contra Kotlin `null`, NO contra `JsonNull` → lanza `"Element class JsonNull is not a JsonObject"`.

**Siempre usar `takeIf { it !is JsonNull }` para joins embebidos que pueden ser null:**

```kotlin
// ❌ Falla si el campo es SQL NULL:
val church = a["church"]?.jsonObject

// ✅ Correcto:
val church = a["church"]?.takeIf { it !is JsonNull }?.jsonObject
```

Aplica a cualquier join embebido PostgREST: `member`, `church`, `activity_type`, etc.
Requiere `import kotlinx.serialization.json.JsonNull`.

### Patrón MainActivity — startDestination

```kotlin
val startDestination = when {
    session.isMiembroGuardado -> NavRoutes.MIEMBRO_HOME  // miembro regular con sesión guardada
    session.isLoggedIn        -> NavRoutes.HOME           // líder con grupoId + miembroId en SharedPrefs
    else                      -> NavRoutes.LOGIN
}
// NO llamar session.clear() en ningún caso al iniciar — borraba la sesión del líder en cada reinicio
// isMiembroGuardado solo lo setea guardarPerfilMiembro(), nunca para líderes
// isLoggedIn = grupoId.isNotEmpty() && miembroId.isNotEmpty()
```

### PaddingValues — error frecuente

`PaddingValues(horizontal = 16.dp, bottom = 24.dp)` NO compila.
Siempre usar todas las direcciones individuales:
```kotlin
PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
```

### Patrón desglose granular por miembro — dirección INVERTIDA (2026-06-01)

El desglose de actividades funciona en dirección **miembro → total** (no al revés):

```kotlin
// En RegistroViewModel:
fun onDesgloseChange(actividadId, miembroId, nuevaCantidad) {
    // Actualiza el valor del miembro
    val newDesglose = a.desgloseMiembros.map { m ->
        if (m.miembroId == miembroId) m.copy(cantidad = nuevaCant.coerceAtLeast(0)) else m
    }
    // Total = suma del desglose (no al revés)
    val nuevoTotal = newDesglose.sumOf { it.cantidad }
    a.copy(desgloseMiembros = newDesglose, cantidad = nuevoTotal.takeIf { it > 0 })
}
// Mismo patrón para onDesgloseMontoChange (monto) y onDesgloseParticipacionChange (count{it.participo})
```

- `tieneDesglose = !bloqueada && tipo != TipoMarcador.CHECKBOX` — todos los editables tienen desglose
- `desgloseExpandido = true` por defecto en ActividadRegistro
- En `DetalleActividadScreen`: `sinLimite = true` → cada miembro puede ingresar cualquier valor sin tope
- En `RegistroPaso2Screen`: chip muestra `—` o el total calculado; no hay edición inline

### Patrón MemberActivitySubmission — flujo de aprobación

```kotlin
// ActividadRepository.kt
data class MemberActivitySubmission(
    val recordId:      String,
    val miembroId:     String,
    val miembroNombre: String,
    val recordDate:    LocalDate,
    val count:         Int?,
    val monto:         Double?,
    val isDone:        Boolean,
    val status:        String,  // "draft" | "approved" | "pending_board" | "rejected"
)

// Flujo:
// Miembro registra → status = "draft"
// Líder aprueba:
//   isMonetary = false → status = "approved"   (suma en totales del GP)
//   isMonetary = true  → status = "pending_board" (espera Junta de Iglesia)
// Líder rechaza → status = "rejected"
```

### Patrón sinLimite en filas de desglose

`MiembroDesgloseRow` y `MiembroDesgloseMonetarioRow` aceptan `sinLimite: Boolean = false`:
- `sinLimite = false` (default): limita el valor al `totalGeneral - sumOtros` (modo Paso 2 anterior)
- `sinLimite = true`: sin límite superior, `allowDirectInput = true` (modo DetalleActividadScreen)

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
- ❌ No poner ContadorInline/MontoInline en RegistroPaso2Screen — toda edición numérica va en DetalleActividadScreen
- ❌ No limitar el desglose de miembros por el total global — el total se calcula DEL desglose, no al revés
- ❌ No sumar member_activity_record con status="draft" en totales — solo approved y pending_board

---

## Pendientes próximas sesiones (2026-06-01)

- ⚠️ **Sección "Enviados por miembros"** en DetalleActividadScreen / ActividadHistorialScreen: borradores con [✓ Aprobar][✎ Editar][✗ Rechazar]
- ⚠️ **Badge de pendientes** en ActividadesListScreen: pill en Blush con count de drafts por tipo
- ⚠️ **Perfil MIEMBRO** (feature/miembro): pantalla para que el miembro registre sus actividades propias
- ⚠️ **Perfil Junta de Iglesia**: revisar actividades `pending_board` — diseño no definido aún
- ⚠️ **HomeViewModel**: datos reales de estadísticas del grupo (iglesia, horario)
- ⚠️ **MiembrosViewModel**: reemplazar 10 miembros hardcoded con datos reales de Supabase
- ⚠️ **Logs temporales**: remover Log.d en ReunionRepositoryImpl y Log.e en DetalleReunionViewModel
