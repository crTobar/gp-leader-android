package com.gpleader.app.feature.registro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadTipoData
import com.gpleader.app.core.data.repository.ActividadTotalData
import com.gpleader.app.core.data.repository.MemberContribution
import com.gpleader.app.core.data.repository.AsistenciaParaGuardar
import com.gpleader.app.core.data.repository.ChurchHit
import com.gpleader.app.core.data.repository.GroupLogRepository
import com.gpleader.app.core.data.repository.IglesiaRepository
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.RegistroActividadData
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class EstadoAsistencia { PRESENTE, AUSENTE, JUSTIFICADO }
enum class NivelActividad   { UNION, PASTOR, GP }
enum class TipoMarcador     { CHECKBOX, CONTADOR, MONETARIO, PARTICIPANTES }
enum class RegistryKind     { GP_MEETING, SATURDAY_WORSHIP }

// ── Domain models ──────────────────────────────────────────────────────────────

data class MiembroAsistencia(
    val id:        String,
    val nombre:    String,
    val iniciales: String,
    val estado:    EstadoAsistencia? = null,
)

data class VisitaAnterior(
    val id:                String,
    val nombre:            String,
    val fechaUltimaVisita: LocalDate,
    val visitasCount:      Int = 0,
)

data class VisitaHoy(
    val id:      String,
    val nombre:  String,
    val esNueva: Boolean,
    val estado:  EstadoAsistencia? = EstadoAsistencia.PRESENTE,
)

data class MiembroDesglose(
    val miembroId:     String,
    val nombre:        String,
    val iniciales:     String,
    val cantidad:      Int     = 0,
    val montoDesglose: Double  = 0.0,  // para actividades MONETARIO
    val participo:     Boolean = false, // para actividades PARTICIPANTES
)

data class ActividadRegistro(
    val id:               String,
    val nombre:           String,
    val nivel:            NivelActividad,
    val unidad:           String,
    val esOficial:        Boolean,
    val esExtra:          Boolean    = false,
    val esObligatoria:    Boolean    = false,
    val tipoMarcador:     TipoMarcador = TipoMarcador.CONTADOR,
    val cantidad:         Int?       = null,
    val realizado:        Boolean?   = null,   // solo si tipoMarcador == CHECKBOX
    val monto:            Double?    = null,   // solo si tipoMarcador == MONETARIO
    val notas:            String?    = null,
    val bloqueada:        Boolean    = false,
    val tieneDesglose:    Boolean    = false,
    val desgloseExpandido: Boolean   = false,
    val desgloseMiembros: List<MiembroDesglose> = emptyList(),
    val totalAcumulado:   Int?       = null,   // acumulado del periodo para CONTADOR y PARTICIPANTES
    val montoAcumulado:   Double?    = null,   // acumulado del periodo para MONETARIO
    val startDate:        java.time.LocalDate? = null,
    val endDate:          java.time.LocalDate? = null,
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class RegistroUiState(
    val nombreGrupo:       String                  = "",
    val mensajePaso3:    String?                 = null,
    // ── Tipo de registro ─────────────────────────────────────────────────────
    val registryKind:      RegistryKind            = RegistryKind.GP_MEETING,
    // ── Paso 1 ───────────────────────────────────────────────────────────────
    val fecha:             LocalDate               = LocalDate.now(),
    val noHuboReunion:     Boolean                 = false,
    val miembros:          List<MiembroAsistencia>  = emptyList(),
    val visitasAnteriores: List<VisitaAnterior>    = emptyList(),
    val visitasDeHoy:      List<VisitaHoy>         = emptyList(),
    val visitasColapsadas: Boolean                 = true,
    val esSuplente:        Boolean                 = false,
    val showConfirmTodosAusentes: Boolean          = false,
    val showConfirmNoHuboReunion: Boolean          = false,
    val errorSinAsistencia: Boolean                = false,
    val errorSinAsistenciaTrigger: Int             = 0,
    // ── Culto de sábado — iglesia por miembro ────────────────────────────────
    val groupChurch:           ChurchHit?                   = null,
    val memberChurches:        Map<String, ChurchHit>       = emptyMap(),
    val memberChurchQueries:   Map<String, String>          = emptyMap(),
    val memberChurchResults:   Map<String, List<ChurchHit>> = emptyMap(),
    val memberChurchSearching: Set<String>                  = emptySet(),
    // ── Paso 2 ───────────────────────────────────────────────────────────────
    val actividades:                  List<ActividadRegistro> = emptyList(),
    val errorActividadesObligatorias: Boolean                 = false,
    val errorActividadesObligatoriasTrigger: Int              = 0,
    val showJustificadoHint:          Boolean                 = true,
    // ── Navegación ───────────────────────────────────────────────────────────
    val navigateToPaso2:          Boolean = false,
    val navigateToPaso3:          Boolean = false,
    val navigateToExitoEnviado:   Boolean = false,
    val navigateToExitoOffline:   Boolean = false,
    val isEnviando:               Boolean = false,
    val errorEnvio:               String? = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class RegistroViewModel @Inject constructor(
    savedStateHandle:              SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val miembroRepo:       MiembroRepository,
    private val reunionRepo:       ReunionRepository,
    private val actividadRepo:     ActividadRepository,
    private val groupLogRepo:      GroupLogRepository,
    private val iglesiaRepo:       IglesiaRepository,
    private val session:           SessionManager,
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("gpleader_prefs", android.content.Context.MODE_PRIVATE)
    }
    private fun justificadoUsos() = prefs.getInt("justificado_usos", 0)

    private val registryKind: RegistryKind = when (savedStateHandle.get<String>("kind")) {
        "saturday_worship" -> RegistryKind.SATURDAY_WORSHIP
        else               -> RegistryKind.GP_MEETING
    }

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val churchSearchJobs = mutableMapOf<String, Job>()

    init {
        _uiState.update {
            it.copy(
                showJustificadoHint = justificadoUsos() < 5,
                nombreGrupo         = session.grupoNombre,
                registryKind        = registryKind,
            )
        }
        cargarMiembros()
        cargarVisitasAnteriores()
        if (registryKind == RegistryKind.SATURDAY_WORSHIP) {
            cargarGroupChurch()
        }
    }

    private fun cargarGroupChurch() {
        val iglesiaId = session.iglesiaId
        if (iglesiaId.isBlank()) return
        viewModelScope.launch {
            val hit = runCatching { iglesiaRepo.getChurchById(iglesiaId) }.getOrNull()
            if (hit != null) {
                _uiState.update { it.copy(groupChurch = hit) }
            }
        }
    }

    private fun cargarMiembros() {
        viewModelScope.launch {
            miembroRepo.getMiembrosActivos(session.grupoId)
                .catch { /* ignorar error, lista queda vacía */ }
                .collect { miembrosData ->
                    val miembrosAsistencia = miembrosData.map { m ->
                        MiembroAsistencia(m.id, m.nombreCompleto, m.iniciales)
                    }
                    val desgloseVacio = miembrosData.map { m ->
                        MiembroDesglose(m.id, m.nombreCompleto, m.iniciales)
                    }
                    _uiState.update { s -> s.copy(miembros = miembrosAsistencia) }
                    cargarActividades(desgloseVacio)
                }
        }
    }

    private fun cargarActividades(desgloseVacio: List<MiembroDesglose>) {
        viewModelScope.launch {
            actividadRepo.getActividadesTipo(session.iglesiaId, session.districtId, session.campoId, session.grupoId).fold(
                onSuccess = { tipos ->
                    val filtrados = filteredActivityTypes(tipos)
                    val totales = actividadRepo.getActividadesConTotales(session.grupoId)
                        .getOrElse { emptyMap() }

                    // Cargar contribuciones de miembros desde la última reunión
                    val lastMeeting = actividadRepo.getLastMeetingDate(session.grupoId)
                        .getOrNull() ?: java.time.LocalDate.of(2000, 1, 1)
                    val memberContribs = actividadRepo.getMemberContributionsSinceDate(
                        grupoId = session.grupoId,
                        desde   = lastMeeting,
                        hasta   = java.time.LocalDate.now(),
                    ).getOrElse { emptyMap() }

                    _uiState.update { s ->
                        s.copy(actividades = filtrados.map { tipo ->
                            tipo.toActividadRegistro(
                                desgloseVacio = desgloseVacio,
                                total         = totales[tipo.id],
                                contribs      = memberContribs[tipo.id] ?: emptyList(),
                            )
                        })
                    }
                },
                onFailure = { /* mantener lista vacía */ },
            )
        }
    }

    private fun filteredActivityTypes(source: List<ActividadTipoData>): List<ActividadTipoData> {
        if (_uiState.value.registryKind != RegistryKind.SATURDAY_WORSHIP) return source
        val keywords = listOf("sabado", "sabbath", "culto", "escuela sabatica", "escuela sab")
        fun normalize(s: String) = s.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
        val filtered = source.filter { tipo ->
            val haystack = normalize(tipo.nombre)
            keywords.any { haystack.contains(it) }
        }
        return if (filtered.isEmpty()) source else filtered
    }

    fun recargarActividades() {
        val desgloseVacio = _uiState.value.miembros.map { m ->
            MiembroDesglose(m.id, m.nombre, m.iniciales)
        }
        cargarActividades(desgloseVacio)
    }

    private fun ActividadTipoData.toActividadRegistro(
        desgloseVacio: List<MiembroDesglose>,
        total: ActividadTotalData? = null,
        contribs: List<MemberContribution> = emptyList(),
    ): ActividadRegistro {
        val nivel = when (level) {
            "union"  -> NivelActividad.UNION
            "pastor" -> NivelActividad.PASTOR
            else     -> NivelActividad.GP
        }
        val tipo = when (markerType) {
            "monetary"     -> TipoMarcador.MONETARIO
            "checkbox"     -> TipoMarcador.CHECKBOX
            "realizado"    -> TipoMarcador.CHECKBOX
            else           -> TipoMarcador.CONTADOR
        }
        val bloqueada     = false
        val esObligatoria = nivel == NivelActividad.PASTOR && tipo == TipoMarcador.CONTADOR
        val tieneDesglose = !bloqueada && tipo != TipoMarcador.CHECKBOX

        // Pre-poblar desglose con contribuciones de miembros desde última reunión
        val desgloseConContribs = if (tieneDesglose && contribs.isNotEmpty()) {
            val contribPorMiembro = contribs.groupBy { it.miembroId }
            desgloseVacio.map { m ->
                val suma = contribPorMiembro[m.miembroId]?.sumOf { it.count ?: 0 } ?: 0
                m.copy(cantidad = suma)
            }
        } else if (tieneDesglose) desgloseVacio else emptyList()

        val cantidadPreCargada = if (tipo == TipoMarcador.CONTADOR && contribs.isNotEmpty()) {
            val suma = contribs.sumOf { it.count ?: 0 }
            if (suma > 0) suma else null
        } else null

        return ActividadRegistro(
            id               = id,
            nombre           = nombre,
            nivel            = nivel,
            unidad           = unitLabel,
            esOficial        = nivel != NivelActividad.GP,
            esExtra          = false,
            esObligatoria    = esObligatoria,
            tipoMarcador     = tipo,
            bloqueada        = bloqueada,
            tieneDesglose    = tieneDesglose,
            desgloseExpandido = true,
            desgloseMiembros = desgloseConContribs,
            cantidad         = cantidadPreCargada,
            totalAcumulado   = total?.totalCantidad,
            montoAcumulado   = total?.montoTotal,
        )
    }

    private fun cargarVisitasAnteriores() {
        viewModelScope.launch {
            miembroRepo.getVisitasAnteriores(session.grupoId)
                .catch { /* ignorar error */ }
                .collect { visitasData ->
                    val visitas = visitasData.map { m ->
                        VisitaAnterior(
                            id                = m.id,
                            nombre            = m.nombreCompleto,
                            fechaUltimaVisita = m.createdAt
                                ?.take(10)
                                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                                ?: LocalDate.now(),
                            visitasCount      = 1,
                        )
                    }
                    _uiState.update { it.copy(visitasAnteriores = visitas) }
                }
        }
    }

    // ── Paso 1 ────────────────────────────────────────────────────────────────

    fun onFechaChange(fecha: LocalDate) {
        _uiState.update { it.copy(fecha = fecha) }
    }

    fun onAsistenciaChange(miembroId: String, estado: EstadoAsistencia) {
        val prevEstado = _uiState.value.miembros.find { it.id == miembroId }?.estado
        _uiState.update { s ->
            s.copy(
                miembros = s.miembros.map { m ->
                    if (m.id == miembroId)
                        m.copy(estado = if (m.estado == estado) null else estado)
                    else m
                },
                errorSinAsistencia = false,
            )
        }
        // Contar usos de Justificado para apagar el hint tras 5 activaciones
        if (estado == EstadoAsistencia.JUSTIFICADO && prevEstado != EstadoAsistencia.JUSTIFICADO) {
            val nuevosUsos = justificadoUsos() + 1
            prefs.edit().putInt("justificado_usos", nuevosUsos).apply()
            if (nuevosUsos >= 5) {
                _uiState.update { it.copy(showJustificadoHint = false) }
            }
        }
    }

    fun onSelTodos(estado: EstadoAsistencia) {
        val nuevos = _uiState.value.miembros.map { it.copy(estado = estado) }
        if (estado == EstadoAsistencia.AUSENTE) {
            _uiState.update { it.copy(miembros = nuevos, showConfirmTodosAusentes = true) }
        } else {
            _uiState.update { it.copy(miembros = nuevos, errorSinAsistencia = false) }
        }
    }

    fun onDismissConfirmTodosAusentes() {
        _uiState.update { it.copy(showConfirmTodosAusentes = false) }
    }

    fun onNoHuboReunionClick() {
        _uiState.update { it.copy(showConfirmNoHuboReunion = true) }
    }

    fun onConfirmNoHuboReunion() {
        _uiState.update {
            it.copy(
                noHuboReunion            = true,
                showConfirmNoHuboReunion = false,
                navigateToPaso3          = true,
            )
        }
    }

    fun onDismissConfirmNoHuboReunion() {
        _uiState.update { it.copy(showConfirmNoHuboReunion = false) }
    }

    fun onAgregarVisitaAnterior(visitaId: String) {
        val visita = _uiState.value.visitasAnteriores.find { it.id == visitaId } ?: return
        if (_uiState.value.visitasDeHoy.any { it.id == visitaId }) return
        _uiState.update { s ->
            s.copy(
                visitasDeHoy = s.visitasDeHoy + VisitaHoy(
                    id      = visita.id,
                    nombre  = visita.nombre,
                    esNueva = false,
                    estado  = EstadoAsistencia.PRESENTE,
                )
            )
        }
    }

    fun onAgregarNuevaVisita(primerNombre: String, primerApellido: String) {
        val nombre = "${primerNombre.trim()} ${primerApellido.trim()}".trim()
        if (nombre.isBlank()) return
        val id = "new_${System.currentTimeMillis()}"
        _uiState.update { s ->
            s.copy(
                visitasDeHoy = s.visitasDeHoy + VisitaHoy(
                    id      = id,
                    nombre  = nombre,
                    esNueva = true,
                    estado  = EstadoAsistencia.PRESENTE,
                )
            )
        }
    }

    fun onVisitaAsistenciaChange(visitaId: String, estado: EstadoAsistencia) {
        _uiState.update { s ->
            s.copy(
                visitasDeHoy = s.visitasDeHoy.map { v ->
                    if (v.id == visitaId)
                        v.copy(estado = if (v.estado == estado) null else estado)
                    else v
                }
            )
        }
    }

    fun onEliminarVisitaAnterior(visitaId: String) {
        _uiState.update { s ->
            s.copy(visitasDeHoy = s.visitasDeHoy.filter { it.id != visitaId })
        }
    }

    fun onVisitaAnteriorAsistenciaChange(visitaId: String, estado: EstadoAsistencia) {
        val yaAgregada = _uiState.value.visitasDeHoy.any { it.id == visitaId }
        if (!yaAgregada) {
            val visita = _uiState.value.visitasAnteriores.find { it.id == visitaId } ?: return
            _uiState.update { s ->
                s.copy(
                    visitasDeHoy = s.visitasDeHoy + VisitaHoy(
                        id      = visita.id,
                        nombre  = visita.nombre,
                        esNueva = false,
                        estado  = estado,
                    )
                )
            }
        } else {
            val current = _uiState.value.visitasDeHoy.find { it.id == visitaId }?.estado
            if (current == estado) {
                _uiState.update { s ->
                    s.copy(visitasDeHoy = s.visitasDeHoy.filter { it.id != visitaId })
                }
            } else {
                _uiState.update { s ->
                    s.copy(
                        visitasDeHoy = s.visitasDeHoy.map { v ->
                            if (v.id == visitaId) v.copy(estado = estado) else v
                        }
                    )
                }
            }
        }
    }

    fun onToggleVisitasColapsadas() {
        _uiState.update { it.copy(visitasColapsadas = !it.visitasColapsadas) }
    }

    // ── Culto de sábado — búsqueda de iglesia por miembro ────────────────────

    fun updateMemberChurchQuery(memberId: String, query: String) {
        _uiState.update { s ->
            s.copy(memberChurchQueries = s.memberChurchQueries + (memberId to query))
        }
        churchSearchJobs[memberId]?.cancel()
        churchSearchJobs[memberId] = viewModelScope.launch {
            _uiState.update { s -> s.copy(memberChurchSearching = s.memberChurchSearching + memberId) }
            if (query.isNotBlank()) delay(380)
            val results = runCatching {
                iglesiaRepo.searchChurches(query, maxResults = if (query.isBlank()) 20 else 5)
            }.getOrElse { emptyList() }
            _uiState.update { s ->
                s.copy(
                    memberChurchResults   = s.memberChurchResults + (memberId to results),
                    memberChurchSearching = s.memberChurchSearching - memberId,
                )
            }
        }
    }

    fun selectMemberChurch(memberId: String, hit: ChurchHit) {
        churchSearchJobs[memberId]?.cancel()
        _uiState.update { s ->
            s.copy(
                memberChurches      = s.memberChurches + (memberId to hit),
                memberChurchQueries = s.memberChurchQueries + (memberId to hit.churchName),
                memberChurchResults = s.memberChurchResults - memberId,
                memberChurchSearching = s.memberChurchSearching - memberId,
            )
        }
    }

    fun clearMemberChurch(memberId: String) {
        churchSearchJobs[memberId]?.cancel()
        _uiState.update { s ->
            s.copy(
                memberChurches      = s.memberChurches - memberId,
                memberChurchQueries = s.memberChurchQueries + (memberId to ""),
                memberChurchResults = s.memberChurchResults - memberId,
                memberChurchSearching = s.memberChurchSearching - memberId,
            )
        }
    }

    fun onContinuarClick() {
        val alguno = _uiState.value.miembros.any { it.estado != null }
        if (!alguno) {
            _uiState.update { it.copy(errorSinAsistencia = true, errorSinAsistenciaTrigger = it.errorSinAsistenciaTrigger + 1) }
            return
        }
        if (_uiState.value.registryKind == RegistryKind.SATURDAY_WORSHIP) {
            _uiState.update { it.copy(navigateToPaso3 = true) }
        } else {
            _uiState.update { it.copy(navigateToPaso2 = true) }
        }
    }

    fun onRegistryKindChange(kind: RegistryKind) {
        _uiState.update { it.copy(registryKind = kind) }
        if (kind == RegistryKind.SATURDAY_WORSHIP && _uiState.value.groupChurch == null) {
            cargarGroupChurch()
        }
        recargarActividades()
    }

    // ── Paso 2 ────────────────────────────────────────────────────────────────

    fun onCantidadChange(actividadId: String, cantidad: Int?) {
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades.map { a ->
                    if (a.id != actividadId) return@map a
                    val newDesglose = when {
                        a.tipoMarcador == TipoMarcador.CONTADOR && a.tieneDesglose && cantidad != null -> {
                            val sum = a.desgloseMiembros.sumOf { it.cantidad }
                            if (sum > cantidad) a.desgloseMiembros.map { it.copy(cantidad = 0) }
                            else a.desgloseMiembros
                        }
                        a.tipoMarcador == TipoMarcador.PARTICIPANTES && cantidad != null -> {
                            val checked = a.desgloseMiembros.count { it.participo }
                            if (checked > cantidad) {
                                // Desmarcar el exceso desde el final de la lista
                                var restante = checked - cantidad
                                a.desgloseMiembros.map { m ->
                                    if (m.participo && restante > 0) { restante--; m.copy(participo = false) }
                                    else m
                                }
                            } else a.desgloseMiembros
                        }
                        else -> a.desgloseMiembros
                    }
                    a.copy(cantidad = cantidad, desgloseMiembros = newDesglose)
                },
                errorActividadesObligatorias = false,
            )
        }
    }

    fun onDesgloseParticipacionChange(actividadId: String, miembroId: String, checked: Boolean) {
        _uiState.update { s ->
            s.copy(actividades = s.actividades.map { a ->
                if (a.id != actividadId) return@map a
                val newDesglose = a.desgloseMiembros.map { m ->
                    if (m.miembroId == miembroId) m.copy(participo = checked) else m
                }
                val count = newDesglose.count { it.participo }
                a.copy(
                    desgloseMiembros = newDesglose,
                    cantidad = count.takeIf { count > 0 },
                )
            })
        }
    }

    fun onToggleDesglose(actividadId: String) {
        _uiState.update { s ->
            s.copy(actividades = s.actividades.map { a ->
                if (a.id == actividadId) a.copy(desgloseExpandido = !a.desgloseExpandido) else a
            })
        }
    }

    fun onDesgloseChange(actividadId: String, miembroId: String, nuevaCantidad: Int) {
        _uiState.update { s ->
            s.copy(actividades = s.actividades.map { a ->
                if (a.id != actividadId) return@map a
                val newDesglose = a.desgloseMiembros.map { m ->
                    if (m.miembroId == miembroId) m.copy(cantidad = nuevaCantidad.coerceAtLeast(0)) else m
                }
                val nuevoTotal = newDesglose.sumOf { it.cantidad }
                a.copy(
                    desgloseMiembros = newDesglose,
                    cantidad = nuevoTotal,
                )
            })
        }
    }

    fun onNotasChange(actividadId: String, notas: String) {
        _uiState.update { s ->
            s.copy(actividades = s.actividades.map { a ->
                if (a.id == actividadId) a.copy(notas = notas.ifBlank { null }) else a
            })
        }
    }

    fun onCheckboxToggle(actividadId: String) {
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades.map { a ->
                    if (a.id == actividadId) a.copy(realizado = !(a.realizado ?: false)) else a
                },
                errorActividadesObligatorias = false,
            )
        }
    }

    fun onDesgloseMontoChange(actividadId: String, miembroId: String, nuevoMonto: Double) {
        _uiState.update { s ->
            s.copy(actividades = s.actividades.map { a ->
                if (a.id != actividadId) return@map a
                val newDesglose = a.desgloseMiembros.map { m ->
                    if (m.miembroId == miembroId) m.copy(montoDesglose = nuevoMonto.coerceAtLeast(0.0)) else m
                }
                val nuevoMontoTotal = newDesglose.sumOf { it.montoDesglose }
                a.copy(
                    desgloseMiembros = newDesglose,
                    monto = nuevoMontoTotal.takeIf { nuevoMontoTotal > 0.0 },
                )
            })
        }
    }

    fun onMontoChange(actividadId: String, monto: Double?) {
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades.map { a ->
                    if (a.id == actividadId) a.copy(monto = monto) else a
                },
                errorActividadesObligatorias = false,
            )
        }
    }

    fun onAgregarActividadExtra(
        nombre:             String,
        markerType:         String,
        isMemberAccessible: Boolean,
        frecuencia:         String = "semanal",
        startDate:          java.time.LocalDate? = null,
        endDate:            java.time.LocalDate? = null,
    ) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            actividadRepo.saveActividadTipo(
                nombre             = nombre.trim(),
                level              = "my_group",
                markerType         = markerType,
                frecuencia         = frecuencia,
                unitLabel          = "",
                isMemberAccessible = isMemberAccessible,
                iglesiaId          = session.iglesiaId,
                grupoId            = session.grupoId,
                scope              = "group",
                startDate          = startDate,
                endDate            = endDate,
            ).onSuccess {
                recargarActividades()
            }
        }
    }

    fun onSiguienteClick() {
        val hayVacia = _uiState.value.actividades.any { a ->
            if (!a.esObligatoria) return@any false
            when (a.tipoMarcador) {
                TipoMarcador.CHECKBOX      -> a.realizado == null
                TipoMarcador.CONTADOR      -> a.cantidad == null
                TipoMarcador.MONETARIO     -> a.monto == null
                TipoMarcador.PARTICIPANTES -> a.cantidad == null
            }
        }
        if (hayVacia) {
            _uiState.update { it.copy(errorActividadesObligatorias = true, errorActividadesObligatoriasTrigger = it.errorActividadesObligatoriasTrigger + 1) }
            return
        }
        _uiState.update { it.copy(navigateToPaso3 = true) }
    }

    // ── Consumidores de navegación ────────────────────────────────────────────

    fun consumePaso2Navigation() {
        _uiState.update { it.copy(navigateToPaso2 = false) }
    }

    fun consumePaso3Navigation() {
        _uiState.update { it.copy(navigateToPaso3 = false) }
    }

    // ── Paso 3 ────────────────────────────────────────────────────────────────

    fun onGuardarBorradorClick() {
        _uiState.update {
            it.copy(mensajePaso3 = context.getString(R.string.paso3_borrador_proximo))
        }
    }

    fun onEnviarClick() {
        if (_uiState.value.isEnviando) return
        val cm     = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps   = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!online) {
            // Sin conexión: mostrar pantalla offline (guardar local cuando haya PowerSync)
            _uiState.update { it.copy(navigateToExitoOffline = true) }
            return
        }

        _uiState.update { it.copy(isEnviando = true, errorEnvio = null, mensajePaso3 = null) }
        viewModelScope.launch {
            val state = _uiState.value
            val esSabado = state.registryKind == RegistryKind.SATURDAY_WORSHIP
            val asistencias = buildList {
                state.miembros.forEach { m ->
                    add(
                        AsistenciaParaGuardar(
                            miembroId       = m.id,
                            nombreVisita    = null,
                            esVisita        = false,
                            estado          = (m.estado ?: EstadoAsistencia.AUSENTE).name,
                            iglesiaVisitadaId = if (esSabado)
                                state.memberChurches[m.id]?.id ?: state.groupChurch?.id
                            else null,
                        )
                    )
                }
                state.visitasDeHoy.forEach { v ->
                    if (v.estado != null) add(
                        AsistenciaParaGuardar(
                            miembroId    = if (!v.esNueva) v.id else null,
                            nombreVisita = if (v.esNueva) v.nombre else null,
                            esVisita     = true,
                            estado       = v.estado.name,
                        )
                    )
                }
            }
            val reunionResult = reunionRepo.saveReunion(
                grupoId       = session.grupoId,
                fecha         = state.fecha,
                noHuboReunion = state.noHuboReunion,
                asistencias   = asistencias,
                tipoReunion   = if (esSabado) "saturday_worship" else "gp_meeting",
            )

            if (reunionResult.isFailure) {
                val e = reunionResult.exceptionOrNull()
                val msg = if (e?.message?.contains("23505") == true || e?.message?.contains("duplicate key") == true || e?.message?.contains("meeting_unique") == true)
                    "Ya existe una reunión registrada para esta fecha."
                else
                    e?.message ?: "Error al enviar. Intenta de nuevo."
                _uiState.update { it.copy(isEnviando = false, errorEnvio = msg) }
                return@launch
            }

            val meetingId = reunionResult.getOrThrow()

            // Log de reunión enviada (best-effort)
            val fmtFecha = java.time.format.DateTimeFormatter.ofPattern("EEE d 'De' MMMM", java.util.Locale("es"))
            val fechaLabel = state.fecha.format(fmtFecha).split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            val actionType = if (esSabado) "saturday_worship_submitted" else "meeting_submitted"
            val actionMsg  = if (esSabado) "Culto de sábado del $fechaLabel enviado por ${session.miembroNombre}"
                             else "Reunión de GP del $fechaLabel enviada por ${session.miembroNombre}"
            groupLogRepo.logAccion(session.grupoId, actionType, actionMsg)

            // Guardar registros de actividades (best-effort; no bloquea al usuario si falla)
            if (!state.noHuboReunion) {
                val registros = state.actividades
                    .filter { !it.esExtra && !it.bloqueada }
                    .mapNotNull { a -> mapActividadToRegistro(a) }
                actividadRepo.saveRegistros(meetingId, registros)
            }

            _uiState.update { it.copy(isEnviando = false, navigateToExitoEnviado = true) }
        }
    }

    private fun mapActividadToRegistro(a: ActividadRegistro): RegistroActividadData? {
        val cantidad = when (a.tipoMarcador) {
            TipoMarcador.CHECKBOX  -> when (a.realizado) {
                true  -> 1
                false -> 0
                null  -> return null  // no interactuada, no guardar
            }
            TipoMarcador.CONTADOR      -> a.cantidad  // null = no llenada, se guarda como null
            TipoMarcador.MONETARIO     -> null         // monto va en campo separado
            TipoMarcador.PARTICIPANTES -> a.cantidad  // se guarda el total global declarado
        }
        return RegistroActividadData(
            actividadTipoId = a.id,
            cantidad        = cantidad,
            monto           = a.monto,
            notas           = a.notas,
        )
    }

    fun consumeExitoEnviadoNavigation() {
        _uiState.update { it.copy(navigateToExitoEnviado = false) }
    }

    fun consumeExitoOfflineNavigation() {
        _uiState.update { it.copy(navigateToExitoOffline = false) }
    }
}
