# Pantallas — GP Leader

## Flujo de pantallas aprobado

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
│                                     switch "Grupo pequeño" / "Dúo misionero" en la parte superior
└── ActividadHistorialScreen       → resumen acumulado + historial semanal editable

DÚOS MISIONEROS (líder)
├── ActividadesMisionerasScreen    → landing con 2 cards: Dúos + Estudios Bíblicos
├── DuosListScreen                 → lista de dúos activos + FAB crear
├── CrearDuoScreen                 → seleccionar 2 miembros (excluye ya emparejados)
├── DuoDetalleScreen               → pareja + switch Actividades / Estudios Bíblicos del dúo
├── CrearActividadDuoScreen        → nombre + tipo (counter/checkbox) + unidad
└── EstudiosBiblicosMiembrosScreen → todos los miembros + badge total estudios

DÚOS MISIONEROS (miembro)
└── DuoMisioneroScreen             → pareja + switch Actividades / Estudios Bíblicos
                                      visible en MiembroHome solo si tieneDuo=true
```

---

## Implementación actual (2026-06-04) — estado de archivos

```
feature/auth/
  LoginScreen.kt              ✅ dropdowns campo/distrito/iglesia/GP con búsqueda y tarjetas
                                  todos habilitados siempre, filtrado inteligente en cascada
                                  GrupoDropdown: muestra "nombre · Iglesia · Distrito · Campo"
                                  IglesiaDropdown: muestra "nombre · Distrito · Campo"
                                  tap en tarjeta de GP → navega directo a QuienEres (sin botón intermedio)
  LoginViewModel.kt           ✅ carga 4 tablas en paralelo (getCampos/getDistritos/getIglesias/getGrupos)
                                  auth real: supabase.auth.signInWith(Email) → gp_username@login.presencia.app
                                  onGrupoTap() guarda sesión y dispara navigateToQuienEres
  QuienEresScreen.kt          ✅ pantalla para seleccionar rol (LIDER / SUPLENTE) post-login
  QuienEresViewModel.kt       ✅
  ConfirmarIdentidadScreen.kt ✅ pantalla de confirmación de identidad post-código suplente
  SuplementeCodigoScreen.kt   ✅ 6 boxes neuInset, teclado numérico, shake error, auto-valida
  SuplementeBienvenidaScreen.kt ✅ hero Ink 35%, card grupo, nota Gold, campo nombre
  SuplementeViewModel.kt      ✅ compartido entre SuplementeCodigoScreen y SuplementeBienvenidaScreen
                                  + generación de código para SheetGenerarCodigoSuplente

feature/home/
  HomeScreen.kt               ✅ REDISEÑADO (2026-06-03):
                                  - HomeHeader: fecha (MARTES, 3 DE JUNIO en labelSmall/Muted) +
                                    "Grupo / NombreGrupo" en headlineMedium serif +
                                    "X miembros · reunión semanal" en bodyMedium/Muted
                                  - RegistrarCard: NeuCard con "HOY" + texto italic + botón "Tomar Asistencia"
                                  - YaRegistrasteBadge: aparece cuando ya se registró (neuInset + círculo Sage)
                                    GP hoy → reemplaza RegistrarCard; Sábado semana → aparece debajo
                                  - ActividadesMisionerasCard: placeholder (ícono Public/Sage) — pantalla pendiente
                                  - VerHistorialCard: ícono Leaderboard/Gold → navega a HistorialScreen
                                  - TipoRegistroSheet: muestra YaRegistrasteBadge en opciones ya registradas
                                  - Eliminados: avatar/nombre líder, GrupoCard, botón "Ver actividades"
  HomeViewModel.kt            ✅ totalMiembros desde getMiembrosActivos() (real)
                                  reunionGpHoy: ReunionResumen? — reunion de hoy si existe
                                  reunionSabadoSemana: SabbathMeetingResumen? — sábado de esta semana
  SheetGenerarCodigoSuplente.kt ✅ 6 digit Ink NeuCards, barra vigencia Sage/Gold/Blush,
                                    nota dashed, Compartir/Generar nuevo/Revocar + AlertDialog

feature/historial/
  HistorialScreen.kt          ✅ tabs trimestre, stats, lista reuniones
                                  usa OnResumeEffect { viewModel.cargarReuniones() } para refrescar al volver
  HistorialViewModel.kt       ✅ carga reuniones reales desde Supabase via ReunionRepository
                                  filtrado real por trimestre: aplicarFiltro() filtra todasLasReuniones
                                  trimestresDelAnio usa año actual dinámicamente
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
                                  - Sin controles inline (ContadorInline/MontoInline eliminados)
                                  - Toda edición numérica ocurre en DetalleActividadScreen
  RegistroPaso3Screen.kt      ✅ REDISEÑADO (2026-06-28):
                                  - ResumenCard: sección MIEMBROS con 3 tiles AsistenciaStat (número grande
                                    + label singular/plural), gris si 0; ASISTENCIA solo barra (sin headline)
                                  - Secciones de actividad: pill flotante del nivel POR FUERA (neuElevatedSm)
                                    + card HUNDIDA neuInsetInner (solo lectura) con franja de acento + tinte 5%
                                  - Stepper sticky: TopBar+StepperRow fuera del LazyColumn (fijos arriba)
                                  - Colores nivel: Unión=Gold, Pastor=Ink, Mi GP=Accent
  AgregarActividadScreen.kt   ✅ chips tipo marcador en horizontalScroll, nombre neuInsetSm,
                                  monto MontoGrande ₡, sección PERÍODO con DatePickerDialog
                                  startDate default = hoy, endDate default = null
  DetalleActividadScreen.kt   ✅ REDISEÑADO (2026-06-01):
                                  - Total: TotalReadOnly (solo lectura, calculado del desglose)
                                  - Desglose: siempre expandido, soporta CONTADOR/MONETARIO/PARTICIPANTES
                                  - sinLimite=true → cada miembro ingresa su valor libremente
                                  - onGuardar solo persiste notas (total vive en el VM)
  ExitoEnviadoScreen.kt       ✅
  ExitoOfflineScreen.kt       ✅
  RegistroViewModel.kt        ✅ onEnviarClick guarda reunión real en Supabase via ReunionRepository
                                  isEnviando flag previene doble submit
                                  error 23505 (duplicado de fecha) → mensaje amigable en español
                                  onSiguienteClick solo bloquea actividades esObligatoria=true

feature/actividades/
  ActividadesListScreen.kt    ✅ filtros FiltroNivel + FiltroEstado en horizontalScroll
                                  NivelBadge (Gold=Unión, Ink=Pastor, Accent=Mi GP)
                                  EstadoBadge (Sage=Activa, Blush=Vencida)
                                  diaria activities → siempre navegan a CAMPANA_DETALLE
                                  ⚠️ badge de pendientes (drafts) pendiente de implementar
  ActividadesListViewModel.kt ✅ FiltroNivel + FiltroEstado + ActividadConResumen
                                  ⚠️ getPendingCountPerTipo pendiente de conectar a la UI
  ActividadHistorialScreen.kt ✅ ResumenCard serif + RegistroRow con bloque fecha
                                  edición inline: BasicTextField + cancelar/guardar
                                  ⚠️ sección "Enviados por miembros" pendiente de implementar
  ActividadHistorialViewModel.kt ✅ getRegistrosSemanal + updateRegistro en Supabase
  CampanaDetalleScreen.kt     ✅ lista de días (más reciente arriba), chip X/total por día
                                  expandible por día: lista de miembros + hora marcada (HH:mm TZ local)
                                  MiembroRow clickable: líder puede marcar/desmarcar cualquier miembro
                                  togglingKey para loading optimista por miembro; revierte si falla
  CampanaDetalleViewModel.kt  ✅ onToggleMiembro con actualización optimista + revert on failure
                                  actualizarMiembro: sets marcadaEn = Instant.now() al marcar
  CrearActividadTipoScreen.kt ✅ formulario completo: nombre, nivel, tipo marcador, unidad,
                                  frecuencia diaria/semanal, visible para miembros, período
  CrearActividadTipoViewModel.kt ✅ saveActividadTipo → Supabase; savedOk flag para nav

feature/miembro/               ← ROL MIEMBRO
  MiembroHomeScreen.kt        ✅ stub
  MiembroHomeViewModel.kt     ✅ stub
  MiembroActividadesScreen.kt ✅ pantalla completa: ResumenCard semana, sección DIARIAS (tarjeta
                                  D/L/M/M/J/V/S + botón toggle hoy), sección ESTA SEMANA (contador
                                  +/− para counter, campo monetario ₡)
                                  NivelChip por actividad; onNavigateToCampana para campañas
  MiembroActividadesViewModel.kt ✅ carga actividades is_member_accessible=true via getActividadesMiembro
                                  onToggleDiaria → autoApprove=true (diarias no requieren aprobación)
                                  onIncrementarSemanal/DecrementarSemanal/MontoChange → upsert

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
  GrupoRepository.kt/Impl.kt       ✅ jerarquía campo→district→church→small_group
  MiembroRepository.kt/Impl.kt     ✅ getMiembros/getMiembrosActivos/getVisitasAnteriores
  ReunionRepository.kt/Impl.kt     ✅ getReuniones + saveReunion + getSabbathMeeting + submitSabbathMeeting
                                       usa takeIf { it !is JsonNull } en todos los joins embebidos
                                       ⚠️ tiene Log.d temporales — remover después de debug
  ActividadRepository.kt/Impl.kt   ✅ getActividadesTipo, getTodasActividadesTipo, saveRegistros,
                                       getActividadesConTotales, getRegistrosSemanal, updateRegistro,
                                       getActividadesMiembro, toggleMiembroActividad(autoApprove),
                                       getContadorSemanalMiembro, upsertContadorSemanalMiembro,
                                       getRegistrosCampana, getDiasCompletionStats,
                                       getPendingMemberActivities, getPendingCountPerTipo,
                                       approveMemberActivity, rejectMemberActivity, saveActividadTipo
  GroupLogRepository.kt/Impl.kt    ✅ stub
  IglesiaRepository.kt/Impl.kt     ✅ stub
  SolicitudRepository.kt/Impl.kt   ✅ stub

core/ui/navigation/NavGraph.kt     ✅ incluye todas las rutas
core/ui/theme/                     ✅ Color.kt, Type.kt, Theme.kt
                                       Elevation.kt: neuElevated/neuElevatedSm/neuInset/neuInsetSm/neuGlow
core/ui/components/                ✅ AppBottomNavBar, AppLogo, NeuButton, NeuCard, NeuTextField
```

---

## Pendientes próximas sesiones (2026-06-03)

### Completado en sesión 2026-06-03

- ✅ Bottom nav rediseñado: Inicio + Actividades + Perfil (eliminado Historial)
- ✅ HomeScreen redesign: header fecha+grupo, RegistrarCard, YaRegistrasteBadge, VerHistorialCard
- ✅ HomeViewModel: totalMiembros real; reunionGpHoy + reunionSabadoSemana
- ✅ TipoRegistroSheet muestra badge cuando ya se registró cada tipo
- ✅ CampanaDetalleScreen: miembros clickables, líder puede marcar/desmarcar, muestra hora (HH:mm)
- ✅ toggleMiembroActividad con autoApprove; actividades diarias auto-aprobadas
- ✅ getDiasCompletionStats: columnas corregidas + lee marked_at
- ✅ MiembroMarcado.marcadaEn: Instant? para mostrar hora de marcado
- ✅ ActividadesListScreen: diarias → "X días"; semanal checkbox → "X semanas"
- ✅ Todas las actividades diarias navegan a CAMPANA_DETALLE (con o sin startDate)
- ✅ CormorantGaramond FontFamily: agrega Bold (700)
- ✅ CrearActividadTipoScreen: implementación completa
- ✅ MiembroActividadesScreen/ViewModel: implementación completa
- ✅ ActividadesMisionerasCard en Home (placeholder — pantalla pendiente)

### Pendiente — Prioridad 1

- ⚠️ **Pantalla "Actividades misioneras"** — nueva pantalla que abre ActividadesMisionerasCard en Home
- ⚠️ **Sección "Enviados por miembros"** en ActividadHistorialScreen: borradores con [✓ Aprobar][✎ Editar][✗ Rechazar]
- ⚠️ **Badge de pendientes** en ActividadesListScreen: pill Blush con count de drafts por tipo
- ⚠️ **finish_solicitude** — llamar en RegistroViewModel/ExitoEnviadoScreen cuando hay solicitud activa

### Pendiente — Prioridad 2

- ⚠️ **MiembrosViewModel**: datos reales de Supabase (actualmente sample data hardcoded)
- ⚠️ **Flujo delegado** — delegado usa grupoId del grupo delegante en RegistroViewModel
- ⚠️ **HomeViewModel**: iglesia y horario real en GrupoCard (diaSemana viene de getGrupoDetalle)
- ⚠️ **Logs temporales**: remover Log.d en ReunionRepositoryImpl y Log.e en DetalleReunionViewModel
- ⚠️ **DetalleReunionViewModel**: eliminar sample data r1-r4

---

## Completado en sesión 2026-06-04

- ✅ **Dúos Misioneros completo** — 4 tablas Supabase + DuoRepository + 8 pantallas nuevas
- ✅ **ActividadesMisionerasScreen** — landing con tarjetas Dúos / Estudios Bíblicos
- ✅ **DuosListScreen + CrearDuoScreen** — gestión de parejas por el líder
- ✅ **DuoDetalleScreen** — switch Actividades / Estudios, registro compartido, grid de lecciones
- ✅ **CrearActividadDuoScreen** — crear actividad counter o checkbox para el dúo
- ✅ **EstudiosBiblicosMiembrosScreen** — resumen de estudios por miembro
- ✅ **DuoMisioneroScreen (miembro)** — reemplaza stub, switch Actividades / Estudios
- ✅ **MiembroHomeScreen** — botón "Mi Dúo Misionero" condicional (solo si tieneDuo=true)
- ✅ **HomeScreen** — ActividadesMisionerasCard conectada a ACTIVIDADES_MISIONERAS
- ✅ **ActividadesListScreen** — switch GP / Dúo misionero en la parte superior
- ✅ **NeuAvatar** — componente compartido circular con efecto hundido (Cormorant Garamond Bold)
- ✅ **Tipografía local** — fuentes variables bundleadas en res/font/ (no Google Fonts SDK)
- ✅ **HomeHeader** — 38sp Bold Cormorant + subtítulo 15sp Mid (del HTML)
- ✅ **RegistrarCard** — neuGlow en botón, 24sp italic Medium, padding del HTML
- ✅ **YaRegistrasteBadge** — verde vivo #2EA86A, neuInsetInner (sin sombra exterior)
- ✅ **Iconos hundidos** — ActividadesMisionerasCard y VerHistorialCard con neuInsetInner
- ✅ **NeuButtonPrimary** — agrega parámetro `enabled: Boolean`

### Nuevas rutas NavGraph (2026-06-04)

```kotlin
ACTIVIDADES_MISIONERAS     = "actividades_misioneras"
DUOS_LISTA                 = "duos_lista"
CREAR_DUO                  = "crear_duo"
DUO_DETALLE                = "duo_detalle/{duoId}"
CREAR_ACTIVIDAD_DUO        = "crear_actividad_duo/{duoId}"
ESTUDIOS_BIBLICOS_MIEMBROS = "estudios_biblicos_miembros"

// helpers:
fun duoDetalle(duoId: String) = "duo_detalle/$duoId"
fun crearActividadDuo(duoId: String) = "crear_actividad_duo/$duoId"
```

### Pendiente actualizado — Prioridad 1

- ⚠️ **Sección "Enviados por miembros"** en ActividadHistorialScreen: borradores [✓ Aprobar][✎][✗]
- ⚠️ **Badge de pendientes** en ActividadesListScreen: pill Blush con count de drafts por tipo
- ⚠️ **finish_solicitude** — llamar en RegistroViewModel/ExitoEnviadoScreen
- ⚠️ **DuosListScreen** — mostrar estado (sin actividades / X actividades del día)

### Pendiente — Prioridad 2

- ⚠️ **Guardar como borrador** (RegistroPaso3Screen): botón eliminado de la UI temporalmente. Requiere insertar la reunión con `status = "draft"` en Supabase, guardar asistencia y actividades vinculadas, y navegar a pantalla de confirmación. El enum `meeting_status` ya incluye `"draft"`.
- ⚠️ **MiembrosViewModel**: datos reales de Supabase (sample data hardcoded)
- ⚠️ **Logs temporales**: remover Log.d en ReunionRepositoryImpl y Log.e en DetalleReunionViewModel
- ⚠️ **DetalleReunionViewModel**: eliminar sample data r1-r4
