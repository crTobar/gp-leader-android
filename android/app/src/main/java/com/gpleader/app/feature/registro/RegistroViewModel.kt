package com.gpleader.app.feature.registro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.R
import com.gpleader.app.core.data.repository.ActividadRepository
import com.gpleader.app.core.data.repository.ActividadTipoData
import com.gpleader.app.core.data.repository.AsistenciaParaGuardar
import com.gpleader.app.core.data.repository.MiembroRepository
import com.gpleader.app.core.data.repository.RegistroActividadData
import com.gpleader.app.core.data.repository.ReunionRepository
import com.gpleader.app.core.data.repository.iniciales
import com.gpleader.app.core.data.repository.nombreCompleto
import com.gpleader.app.core.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
enum class TipoMarcador     { CHECKBOX, CONTADOR, MONETARIO }

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
    val cantidad:      Int    = 0,
    val montoDesglose: Double = 0.0,  // para actividades MONETARIO
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
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class RegistroUiState(
    val nombreGrupo:       String                  = "",
    val mensajePaso3:    String?                 = null,
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
    @ApplicationContext private val context: Context,
    private val miembroRepo: MiembroRepository,
    private val reunionRepo: ReunionRepository,
    private val actividadRepo: ActividadRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("gpleader_prefs", android.content.Context.MODE_PRIVATE)
    }
    private fun justificadoUsos() = prefs.getInt("justificado_usos", 0)

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                showJustificadoHint = justificadoUsos() < 5,
                nombreGrupo         = session.grupoNombre,
            )
        }
        cargarMiembros()
        cargarVisitasAnteriores()
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
            actividadRepo.getActividadesTipo(session.iglesiaId).fold(
                onSuccess = { tipos ->
                    _uiState.update { s ->
                        s.copy(actividades = tipos.map { it.toActividadRegistro(desgloseVacio) })
                    }
                },
                onFailure = { /* mantener lista vacía */ },
            )
        }
    }

    private fun ActividadTipoData.toActividadRegistro(desgloseVacio: List<MiembroDesglose>): ActividadRegistro {
        val nivel = when (level) {
            "union"  -> NivelActividad.UNION
            "pastor" -> NivelActividad.PASTOR
            else     -> NivelActividad.GP
        }
        val tipo = when (markerType) {
            "monetary" -> TipoMarcador.MONETARIO
            "checkbox" -> TipoMarcador.CHECKBOX
            else       -> TipoMarcador.CONTADOR
        }
        val bloqueada     = nivel == NivelActividad.UNION
        val esObligatoria = nivel == NivelActividad.PASTOR && tipo == TipoMarcador.CONTADOR
        val tieneDesglose = nivel == NivelActividad.PASTOR && tipo == TipoMarcador.CONTADOR
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
            desgloseMiembros = if (tieneDesglose) desgloseVacio else emptyList(),
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

    fun onContinuarClick() {
        val alguno = _uiState.value.miembros.any { it.estado != null }
        if (!alguno) {
            _uiState.update { it.copy(errorSinAsistencia = true, errorSinAsistenciaTrigger = it.errorSinAsistenciaTrigger + 1) }
            return
        }
        _uiState.update { it.copy(navigateToPaso2 = true) }
    }

    // ── Paso 2 ────────────────────────────────────────────────────────────────

    fun onCantidadChange(actividadId: String, cantidad: Int?) {
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades.map { a ->
                    if (a.id != actividadId) return@map a
                    // Si el desglose existente supera el nuevo total, resetear contadores
                    val newDesglose = if (a.tieneDesglose && cantidad != null) {
                        val sum = a.desgloseMiembros.sumOf { it.cantidad }
                        if (sum > cantidad) a.desgloseMiembros.map { it.copy(cantidad = 0) }
                        else a.desgloseMiembros
                    } else a.desgloseMiembros
                    a.copy(cantidad = cantidad, desgloseMiembros = newDesglose)
                },
                errorActividadesObligatorias = false,
            )
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
                val totalGeneral = a.cantidad ?: Int.MAX_VALUE
                val sumOtros = a.desgloseMiembros
                    .filter { it.miembroId != miembroId }
                    .sumOf { it.cantidad }
                val maxEste = (totalGeneral - sumOtros).coerceAtLeast(0)
                a.copy(desgloseMiembros = a.desgloseMiembros.map { m ->
                    if (m.miembroId == miembroId) m.copy(cantidad = nuevaCantidad.coerceIn(0, maxEste)) else m
                })
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
                val montoTotal = a.monto ?: Double.MAX_VALUE
                val sumOtros = a.desgloseMiembros
                    .filter { it.miembroId != miembroId }
                    .sumOf { it.montoDesglose }
                val maxEste = (montoTotal - sumOtros).coerceAtLeast(0.0)
                a.copy(desgloseMiembros = a.desgloseMiembros.map { m ->
                    if (m.miembroId == miembroId) m.copy(montoDesglose = nuevoMonto.coerceIn(0.0, maxEste)) else m
                })
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
        nombre:        String,
        tipoMarcador:  TipoMarcador,
        cantidad:      Int?,
        unidad:        String,
        monto:         Double?,
        tieneDesglose: Boolean,
    ) {
        if (nombre.isBlank()) return
        val id = "extra_${System.currentTimeMillis()}"
        val desgloseVacio = if (tieneDesglose)
            _uiState.value.miembros.map { m ->
                MiembroDesglose(m.id, m.nombre, m.iniciales)
            }
        else emptyList()
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades + ActividadRegistro(
                    id             = id,
                    nombre         = nombre.trim(),
                    nivel          = NivelActividad.GP,
                    unidad         = unidad,
                    esOficial      = false,
                    esExtra        = true,
                    tipoMarcador   = tipoMarcador,
                    cantidad       = cantidad,
                    monto          = monto,
                    tieneDesglose  = tieneDesglose,
                    desgloseMiembros = desgloseVacio,
                )
            )
        }
    }

    fun onSiguienteClick() {
        val hayVacia = _uiState.value.actividades.any { a ->
            if (!a.esObligatoria) return@any false
            when (a.tipoMarcador) {
                TipoMarcador.CHECKBOX  -> a.realizado == null
                TipoMarcador.CONTADOR  -> a.cantidad == null
                TipoMarcador.MONETARIO -> a.monto == null
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
            val asistencias = buildList {
                state.miembros.forEach { m ->
                    if (m.estado != null) add(
                        AsistenciaParaGuardar(
                            miembroId    = m.id,
                            nombreVisita = null,
                            esVisita     = false,
                            estado       = m.estado.name,
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
            )

            if (reunionResult.isFailure) {
                val e = reunionResult.exceptionOrNull()
                val msg = if (e?.message?.contains("23505") == true || e?.message?.contains("duplicate key") == true)
                    "Ya existe una reunión registrada para esta fecha."
                else
                    e?.message ?: "Error al enviar. Intenta de nuevo."
                _uiState.update { it.copy(isEnviando = false, errorEnvio = msg) }
                return@launch
            }

            val meetingId = reunionResult.getOrThrow()

            // Guardar registros de actividades (best-effort; no bloquea al usuario si falla)
            if (!state.noHuboReunion) {
                val registros = state.actividades
                    .filter { !it.esExtra }
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
            TipoMarcador.CONTADOR  -> a.cantidad  // null = no llenada, se guarda como null
            TipoMarcador.MONETARIO -> null         // monto va en campo separado
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
