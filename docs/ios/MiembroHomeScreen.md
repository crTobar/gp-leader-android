# Guía de implementación iOS — Pantalla Home del Miembro

> Referencia para portar `MiembroHomeScreen` (Android/Jetpack Compose) a iOS (SwiftUI).
> Incluye: spec visual, tokens de diseño, componentes reutilizados, código Compose, contrato de datos (UiState) y lógica (ViewModel).

---

## 1. Resumen de la pantalla

Pantalla de inicio del rol **MIEMBRO**. Es la primera de un portal de 3 tabs (Inicio · Actividades · Perfil).

**Estructura visual (de arriba a abajo):**
1. **Header**
   - Fecha (eyebrow): `MARTES, 3 DE JUNIO` (mayúsculas)
   - Fila: avatar (iniciales) + nombre del miembro + etiqueta pequeña `Miembro`
   - Título grande serif: `Grupo` / `[nombre del GP]` (dos líneas, 38sp Bold)
2. **`ESTE TRIMESTRE`** (eyebrow) + tarjeta de métricas: `asistidos · cultos · promedio`
3. **Tarjetas de acceso** (icono hundido + título + subtítulo + chevron):
   - Ver Dúo Misionero — *solo si `tieneDuo == true`*
   - Ver Mis Estudios Bíblicos
   - Ver Historial
4. **Bottom nav** de 3 tabs (Inicio activo)

**Comportamiento:**
- Al cargar: datos básicos desde cache de sesión → valida perfil en servidor → carga métricas + dúo + suplente en paralelo.
- Si el perfil ya no existe en servidor → cierra sesión y navega a login.
- Pull-to-refresh recarga métricas / dúo / suplente.

---

## 2. Tokens de diseño (style board SDA — neumorfismo)

```
// Colores
Background     #ECEEF1   // Fondo general
BackgroundDeep #E0E3E9   // Fondo de secciones hundidas
Shadow         #C2C8D4   // Sombra oscura (neu, abajo-derecha)
Light          #FFFFFF   // Sombra clara (neu, arriba-izquierda)
Accent         #4A7FD4   // Azul principal
Gold           #C9A84C   // Dorado (historial)
Sage           #6AAB8E   // Verde (dúo, éxito)
Blush          #D4836A   // Naranja (alertas)
Ink            #1E2733   // Texto principal
Mid            #5A6577   // Texto secundario
Muted          #9AA4B2   // Texto deshabilitado / labels
```

```
// Tipografía (fuentes bundleadas, NO de sistema)
Cormorant Garamond (serif)  → displayLarge (títulos grandes, ej. "Grupo")
DM Sans (sans-serif)        → titleLarge (20sp), bodyLarge (16sp), bodyMedium (14sp)
DM Mono (monoespaciado)     → labelSmall (11sp, letterSpacing 2sp, usado en mayúsculas)
```

```
// Radios de esquina
extraLarge 28dp · large 20dp · medium 14dp · small 8dp
```

### Sombras neumórficas (clave del look)
Doble sombra: **luz arriba-izquierda + sombra oscura abajo-derecha**.
- **Elevado** (`NeuCard`): la tarjeta "flota" sobre el fondo. Sombra exterior.
- **Hundido** (`neuInsetInner`): la tarjeta/box se ve "embebida". Sin sombra exterior; gradientes pintados **sobre** el contenido ya clippeado.
  - Orden de capas crítico: `clip` → `background` → sombra interior → `padding`.

En SwiftUI: replicar con dos `.shadow(color:radius:x:y:)` (una blanca arriba-izq, una gris abajo-der) para elevado; para hundido, usar un `overlay`/`innerShadow` (máscara con gradiente inverso).

---

## 3. Componentes reutilizados

| Componente | Descripción | Notas para iOS |
|-----------|-------------|----------------|
| `NeuAvatar(iniciales, size)` | Círculo hundido con iniciales en Cormorant Bold, texto `Mid`, fondo `Background`, efecto `neuInsetInner(size*0.28)` | View circular con inner shadow |
| `NeuCard` | Tarjeta base elevada (neumórfica), radio 20–28dp | Card con doble shadow |
| `AppBottomNavBar(selectedTab, on…Click)` | Barra inferior de 3 tabs: Inicio (Home) · Actividades (Assignment) · Perfil (Person). Pill hundido en el tab activo. | TabBar custom |
| Iconos **Lucide** | `Users`, `BookOpen`, `History`, `ChevronRight` | SF Symbols equivalentes: `person.2`, `book`, `clock.arrow.circlepath`, `chevron.right` — o Lucide-iOS |

---

## 4. Contrato de datos — `MiembroHomeUiState`

```kotlin
data class MiembroHomeUiState(
    val miembroNombre:    String = "",
    val miembroIniciales: String = "",
    val grupoNombre:      String = "",
    val iglesiaNombre:    String = "",
    val districtNombre:   String = "",
    val campoNombre:      String = "",
    val isValidandoPerfil: Boolean = true,
    val isRefreshing:     Boolean = false,

    // Estadísticas del trimestre
    val cultosAsistidos:      Int     = 0,
    val totalCultosGP:        Int     = 0,
    val porcentajeAsistencia: Int     = 0,
    val isLoadingStats:       Boolean = false,

    // Dúo misionero
    val tieneDuo: Boolean = false,

    // Solicitud suplente
    val tieneSolicitudPendiente: Boolean = false,
    val solicitudPendienteId:    String  = "",

    // Navegación
    val navigateToLogin: Boolean = false,
)
```

> SwiftUI: `struct MiembroHomeState` o `@Published` props en un `ObservableObject`.

---

## 5. Lógica — `MiembroHomeViewModel`

```kotlin
@HiltViewModel
class MiembroHomeViewModel @Inject constructor(
    private val miembroRepo:   MiembroRepository,
    private val duoRepo:       DuoRepository,
    private val session:       SessionManager,   // cache local de sesión (UserDefaults en iOS)
    private val supabase:      SupabaseClient,
    private val solicitudRepo: SolicitudRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiembroHomeUiState())
    val uiState: StateFlow<MiembroHomeUiState> = _uiState.asStateFlow()

    init {
        val nombre = session.miembroNombre
        _uiState.update {
            it.copy(
                miembroNombre    = nombre,
                miembroIniciales = calcularIniciales(nombre),
                grupoNombre      = session.grupoNombre,
                iglesiaNombre    = session.iglesiaNombre,
                districtNombre   = session.districtNombre,
                campoNombre      = session.campoNombre,
            )
        }
        validarPerfilEnServidor()
        cargarEstadisticasTrimestre()
        verificarDuo()
        verificarSolicitudSuplente()
    }

    // ── Valida que el perfil siga existiendo; si no, cierra sesión ──────────────
    private fun validarPerfilEnServidor() {
        viewModelScope.launch {
            _uiState.update { it.copy(isValidandoPerfil = true) }
            val resultado = runCatching { miembroRepo.getMiembroById(session.miembroId) }
            when {
                resultado.isFailure           -> _uiState.update { it.copy(isValidandoPerfil = false) }
                resultado.getOrNull() == null -> {
                    session.cerrarSesionMiembro()
                    _uiState.update { it.copy(isValidandoPerfil = false, navigateToLogin = true) }
                }
                else                          -> _uiState.update { it.copy(isValidandoPerfil = false) }
            }
        }
    }

    // ── Métricas del TRIMESTRE ACTUAL (no el pasado) ───────────────────────────
    private fun cargarEstadisticasTrimestre() {
        val grupoId   = session.grupoId
        val miembroId = session.miembroId
        if (grupoId.isEmpty() || miembroId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }
            runCatching {
                val (start, end) = currentQuarterRange()

                // 1) Reuniones GP del trimestre
                //    SELECT id FROM meeting
                //    WHERE small_group_id = :grupoId
                //      AND meeting_date BETWEEN :start AND :end
                //      AND registry_kind = 'gp_meeting'
                val meetingIds: List<String> = /* … */ emptyList()
                val totalCultos = meetingIds.size

                // 2) Asistencias 'present' del miembro
                //    SELECT meeting_id FROM attendance
                //    WHERE member_id = :miembroId AND status = 'present'
                val attendedIds: Set<String> = /* … */ emptySet()

                val asistidos  = meetingIds.count { it in attendedIds }
                val porcentaje = if (totalCultos > 0) (asistidos * 100) / totalCultos else 0

                _uiState.update {
                    it.copy(
                        cultosAsistidos      = asistidos,
                        totalCultosGP        = totalCultos,
                        porcentajeAsistencia = porcentaje,
                        isLoadingStats       = false,
                    )
                }
            }.onFailure { _uiState.update { it.copy(isLoadingStats = false) } }
        }
    }

    // ── ¿Pertenece a un dúo misionero activo? ──────────────────────────────────
    private fun verificarDuo() {
        viewModelScope.launch {
            val duo = duoRepo.getDuoPorMiembro(session.miembroId).getOrNull()
            _uiState.update { it.copy(tieneDuo = duo != null) }
        }
    }

    // ── ¿Hay código de suplente vigente (no usado, no vencido)? ────────────────
    private fun verificarSolicitudSuplente() {
        val grupoId = session.grupoId; val miembroId = session.miembroId
        if (grupoId.isBlank() || miembroId.isBlank()) return
        viewModelScope.launch {
            // SELECT id, used_at FROM deputy_code
            // WHERE small_group_id = :grupoId
            //   AND assigned_member_id = :miembroId
            //   AND expires_at > now()
            // tieneAcceso = existe alguno con used_at NULL
            // → _uiState.update { it.copy(tieneSolicitudPendiente = tieneAcceso) }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            verificarDuo(); cargarEstadisticasTrimestre(); verificarSolicitudSuplente()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onCerrarSesion() {
        session.cerrarSesionMiembro()
        _uiState.update { it.copy(navigateToLogin = true) }
    }

    fun consumeLoginNavigation() { _uiState.update { it.copy(navigateToLogin = false) } }

    private fun calcularIniciales(nombre: String): String {
        val partes = nombre.trim().split(" ")
        return when {
            partes.size >= 2 -> "${partes[0].firstOrNull() ?: ""}${partes[1].firstOrNull() ?: ""}".uppercase()
            partes.size == 1 -> partes[0].take(2).uppercase()
            else             -> "??"
        }
    }

    // Trimestre VIGENTE: ene-mar / abr-jun / jul-sep / oct-dic
    private fun currentQuarterRange(): Pair<LocalDate, LocalDate> {
        val today      = LocalDate.now()
        val startMonth = ((today.monthValue - 1) / 3) * 3 + 1   // 1, 4, 7 o 10
        val start      = LocalDate.of(today.year, startMonth, 1)
        val end        = start.plusMonths(3).minusDays(1)
        return start to end
    }
}
```

### Tablas Supabase usadas
- `meeting` — `id, small_group_id, meeting_date, registry_kind` (`gp_meeting`)
- `attendance` — `member_id, meeting_id, status` (`present` / `absent` / `justified`)
- `missionary_duo` — vía `duoRepo.getDuoPorMiembro`
- `deputy_code` — `id, small_group_id, assigned_member_id, expires_at, used_at`

---

## 6. UI completa — `MiembroHomeScreen` (Jetpack Compose)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiembroHomeScreen(
    onCerrarSesion:               () -> Unit,
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToPerfil:           () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onNavigateToHistorial:        () -> Unit = {},
    viewModel: MiembroHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onCerrarSesion()
            viewModel.consumeLoginNavigation()
        }
    }

    OnResumeEffect { viewModel.onRefresh() }   // recarga al volver al frente

    MiembroHomeContent(
        uiState                      = uiState,
        onNavigateToActividades      = onNavigateToActividades,
        onNavigateToPerfil           = onNavigateToPerfil,
        onNavigateToDuoMisionero     = onNavigateToDuoMisionero,
        onNavigateToEstudiosBiblicos = onNavigateToEstudiosBiblicos,
        onNavigateToHistorial        = onNavigateToHistorial,
        onRefresh                    = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiembroHomeContent(
    uiState:                      MiembroHomeUiState,
    onNavigateToActividades:      () -> Unit = {},
    onNavigateToPerfil:           () -> Unit = {},
    onNavigateToDuoMisionero:     () -> Unit = {},
    onNavigateToEstudiosBiblicos: () -> Unit = {},
    onNavigateToHistorial:        () -> Unit = {},
    onRefresh:                    () -> Unit = {},
) {
    if (uiState.isValidandoPerfil) {
        Box(
            modifier         = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            AppBottomNavBar(
                selectedTab        = NAV_TAB_INICIO,
                onInicioClick      = {},
                onActividadesClick = onNavigateToActividades,
                onPerfilClick      = onNavigateToPerfil,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            MiembroHeader(
                iniciales   = uiState.miembroIniciales,
                nombre      = uiState.miembroNombre,
                grupoNombre = uiState.grupoNombre,
            )

            // ── Contenido scrollable ──────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier.weight(1f).fillMaxWidth(),
                indicator    = {},
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
            ) {
                // ── Métricas del trimestre ────────────────────────────────────
                Text("ESTE TRIMESTRE", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(8.dp))
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.isLoadingStats) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            StatItem(value = uiState.cultosAsistidos.toString(), label = "asistidos", color = Accent)
                            VDivider()
                            StatItem(value = uiState.totalCultosGP.toString(), label = "cultos", color = Ink)
                            VDivider()
                            StatItem(
                                value = "${uiState.porcentajeAsistencia}%",
                                label = "promedio",
                                color = when {
                                    uiState.porcentajeAsistencia >= 75 -> Sage
                                    uiState.porcentajeAsistencia >= 50 -> Accent
                                    else                               -> Blush
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Tarjetas de acceso ────────────────────────────────────────
                if (uiState.tieneDuo) {
                    MiembroOpcionCard(
                        icon = Lucide.Users, iconTint = Sage,
                        titulo = "Ver Dúo Misionero",
                        subtitulo = "Tu pareja misionera y registros compartidos",
                        onClick = onNavigateToDuoMisionero,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                MiembroOpcionCard(
                    icon = Lucide.BookOpen, iconTint = Accent,
                    titulo = "Ver Mis Estudios Bíblicos",
                    subtitulo = "Estudios bíblicos que estás dando",
                    onClick = onNavigateToEstudiosBiblicos,
                )
                Spacer(Modifier.height(12.dp))
                MiembroOpcionCard(
                    icon = Lucide.History, iconTint = Gold,
                    titulo = "Ver Historial",
                    subtitulo = "Tu asistencia a reuniones y cultos de sábado",
                    onClick = onNavigateToHistorial,
                )
            }
            }
        }
    }
}

// ── Header: fecha + avatar/nombre/"Miembro" + "Grupo"/nombre serif ─────────────
@Composable
private fun MiembroHeader(iniciales: String, nombre: String, grupoNombre: String) {
    val today          = LocalDate.now()
    val diaSemanaLabel = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("es")).uppercase()
    val fechaLabel     = "$diaSemanaLabel, ${today.dayOfMonth} DE " +
                         today.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es")).uppercase()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 4.dp),
    ) {
        Text(fechaLabel, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NeuAvatar(iniciales = iniciales, size = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Ink)
                Text("Miembro", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
        Spacer(Modifier.height(16.dp))
        val nameStyle = MaterialTheme.typography.displayLarge.copy(
            fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp,
        )
        Text("Grupo",     style = nameStyle, color = Ink)
        Text(grupoNombre, style = nameStyle, color = Ink)
    }
}

// ── Tarjeta de acceso (icono hundido + título + subtítulo + chevron) ──────────
@Composable
private fun MiembroOpcionCard(
    icon: ImageVector, iconTint: Color, titulo: String, subtitulo: String, onClick: () -> Unit,
) {
    NeuCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Background).neuInsetInner(shadowSize = 10.dp),
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Mid)
            }
            Icon(Lucide.ChevronRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Métrica individual (valor grande + label) ─────────────────────────────────
@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

// Divisor vertical entre métricas: Box 1dp × 40dp, color Muted @25% alpha
```

---

## 7. Notas de port a SwiftUI

- **`StateFlow` + `collectAsState`** → `@StateObject var vm` con `@Published var state`.
- **`Scaffold` + `bottomBar`** → `VStack { content; AppBottomNavBar() }` o `TabView` custom (la barra es propia, no la nativa, por el look neumórfico).
- **`PullToRefreshBox`** → `.refreshable { await vm.onRefresh() }`.
- **`OnResumeEffect`** → `.onAppear` / `scenePhase == .active`.
- **`LaunchedEffect(navigateToLogin)`** → `.onChange(of: state.navigateToLogin)`.
- **Fechas**: `LocalDate` → `Date`/`Calendar`; locale `es`. El label va en MAYÚSCULAS.
- **Trimestre vigente**: `startMonth = ((month-1)/3)*3 + 1`; rango `[inicio, fin]` del trimestre actual.
- **Iniciales**: 2 primeras iniciales de las 2 primeras palabras del nombre, en mayúsculas (`"??"` si vacío).
- La pantalla **muta `navigateToLogin` y lo "consume"** tras navegar (patrón de eventos one-shot). En iOS, resetear el flag tras manejar el evento.
```
