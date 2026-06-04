# Arquitectura — GP Leader

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

## NavGraph — rutas actuales

```kotlin
object NavRoutes {
    LOGIN                = "login"
    QUIEN_ERES           = "quien_eres"               // post-login: selección de rol
    SUPLENTE_GRAPH       = "suplente_graph"           // nested graph
    SUPLENTE_CODIGO      = "suplente_codigo"          // start destination de SUPLENTE_GRAPH
    SUPLENTE_BIENVENIDA  = "suplente_bienvenida"
    HOME                 = "home"
    HISTORIAL            = "historial"                // accesible desde Home (VerHistorialCard), NO en nav
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
    CAMPANA_DETALLE      = "campana_detalle/{actividadTipoId}/{nombreCampana}/{desde}/{hasta}"
    CREAR_ACTIVIDAD_TIPO = "crear_actividad_tipo"
    MIEMBRO_ACTIVIDAD_CAMPANA = "miembro_actividad_campana/{actividadTipoId}/{nombreCampana}/{desde}/{hasta}"
    // helpers:
    // fun actividadHistorial(id) = "actividad_historial/$id"
    // fun campanaDetalle(id, nombre, desde, hasta) = "campana_detalle/$id/${Uri.encode(nombre)}/$desde/$hasta"
}
```

### Bottom nav — tabs (2026-06-03)

```
NAV_TAB_INICIO      = 0  → HOME
NAV_TAB_ACTIVIDADES = 1  → ACTIVIDADES_LISTA
NAV_TAB_PERFIL      = 2  → PERFIL
```

Pantallas que ya NO son tab del nav pero siguen existiendo: HISTORIAL, DETALLE_REUNION.
En HistorialScreen y DetalleReunionScreen el nav muestra `NAV_TAB_INICIO` como activo.

### Flujos de navegación clave

```
AUTH:
  LOGIN → login exitoso (passwordSet=true)  → QUIEN_ERES
  LOGIN → login (passwordSet=false)         → PERFIL_CAMBIAR_CONTRASENA
  LOGIN → "Ingresar como suplente"          → SUPLENTE_GRAPH (no SUPLENTE_CODIGO directamente)

ACTIVIDADES:
  HOME             → "Ver historial" card       → HISTORIAL
  ACTIVIDADES_LISTA → tap semanal               → ACTIVIDAD_HISTORIAL
  ACTIVIDADES_LISTA → tap diaria                → CAMPANA_DETALLE
  ACTIVIDADES_LISTA → FAB "+"                   → CREAR_ACTIVIDAD_TIPO
```

---

## Patrón ViewModel compartido en nested graph

```kotlin
composable(NavRoutes.ALGUNA_RUTA) { backStackEntry ->
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(NavRoutes.ALGUNA_GRAPH)
    }
    val sharedVm: AlgunViewModel = hiltViewModel(graphEntry)
    AlgunaScreen(viewModel = sharedVm, ...)
}
```

Usado en: `REGISTRO_GRAPH` (RegistroViewModel), `MIEMBROS_GRAPH` (MiembrosViewModel), `SUPLENTE_GRAPH` (SuplementeViewModel).

---

## Patrón MainActivity — startDestination

```kotlin
val startDestination = when {
    session.isMiembroGuardado -> NavRoutes.MIEMBRO_HOME  // miembro regular con sesión guardada
    session.isLoggedIn        -> NavRoutes.HOME           // líder con grupoId + miembroId en SharedPrefs
    else                      -> NavRoutes.LOGIN
}
// NO llamar session.clear() al iniciar — borra la sesión del líder en cada reinicio
// isMiembroGuardado solo lo setea guardarPerfilMiembro(), nunca para líderes
// isLoggedIn = grupoId.isNotEmpty() && miembroId.isNotEmpty()
```

---

## PaddingValues — error frecuente

`PaddingValues(horizontal = 16.dp, bottom = 24.dp)` NO compila.
Siempre usar todas las direcciones individuales:

```kotlin
PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
```
