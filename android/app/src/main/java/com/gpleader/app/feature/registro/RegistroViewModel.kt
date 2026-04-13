package com.gpleader.app.feature.registro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpleader.app.core.data.repository.AsistenciaParaGuardar
import com.gpleader.app.core.data.repository.MiembroRepository
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
    val miembroId: String,
    val nombre:    String,
    val iniciales: String,
    val cantidad:  Int = 0,
)

data class ActividadRegistro(
    val id:               String,
    val nombre:           String,
    val nivel:            NivelActividad,
    val unidad:           String,
    val esOficial:        Boolean,
    val esExtra:          Boolean = false,
    val esObligatoria:    Boolean = false,
    val cantidad:         Int?    = null,
    val notas:            String? = null,
    val bloqueada:        Boolean = false,
    val tieneDesglose:    Boolean = false,
    val desgloseExpandido: Boolean = false,
    val desgloseMiembros: List<MiembroDesglose> = emptyList(),
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class RegistroUiState(
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
    private val session: SessionManager,
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("gpleader_prefs", android.content.Context.MODE_PRIVATE)
    }
    private fun justificadoUsos() = prefs.getInt("justificado_usos", 0)

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(showJustificadoHint = justificadoUsos() < 5) }
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
                    _uiState.update { s ->
                        s.copy(
                            miembros    = miembrosAsistencia,
                            actividades = actividadesIniciales(desgloseVacio),
                        )
                    }
                }
        }
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

    private fun actividadesIniciales(desgloseVacio: List<MiembroDesglose>) = listOf(
        // UNIÓN — bloqueadas, solo lectura (cantidad pre-llenada por sistema)
        ActividadRegistro("a1", "Recolección ofrendas", NivelActividad.UNION,  "personas", esOficial = true, bloqueada = true, cantidad = 8),
        ActividadRegistro("a2", "Repartir literatura",  NivelActividad.UNION,  "libros",   esOficial = true, bloqueada = true, cantidad = 5),
        // PASTOR — editables, obligatorias, inician en —
        ActividadRegistro("a3", "Estudios Bíblicos",     NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true,
            tieneDesglose = true, desgloseMiembros = desgloseVacio),
        ActividadRegistro("a4", "Peticiones de Oración", NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true,
            tieneDesglose = true, desgloseMiembros = desgloseVacio),
        ActividadRegistro("a5", "Interesados Nuevos",    NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true,
            tieneDesglose = true, desgloseMiembros = desgloseVacio),
        // MI GP — inician en —
        ActividadRegistro("a6", "Oración especial", NivelActividad.GP, "veces", esOficial = false, esExtra = true),
    )

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

    fun onAgregarActividadExtra(nombre: String, cantidad: Int?, unidad: String) {
        if (nombre.isBlank()) return
        val id = "extra_${System.currentTimeMillis()}"
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades + ActividadRegistro(
                    id        = id,
                    nombre    = nombre.trim(),
                    nivel     = NivelActividad.GP,
                    unidad    = unidad,
                    esOficial = false,
                    esExtra   = true,
                    cantidad  = cantidad,
                )
            )
        }
    }

    fun onSiguienteClick() {
        val hayVacia = _uiState.value.actividades
            .any { it.esObligatoria && it.cantidad == null }
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

        _uiState.update { it.copy(isEnviando = true, errorEnvio = null) }
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
                            // Visita anterior: ya tiene id real en member. Nueva: null → se crea en saveReunion.
                            miembroId    = if (!v.esNueva) v.id else null,
                            nombreVisita = if (v.esNueva) v.nombre else null,
                            esVisita     = true,
                            estado       = v.estado.name,
                        )
                    )
                }
            }
            reunionRepo.saveReunion(
                grupoId       = session.grupoId,
                fecha         = state.fecha,
                noHuboReunion = state.noHuboReunion,
                asistencias   = asistencias,
            ).fold(
                onSuccess = { _uiState.update { it.copy(isEnviando = false, navigateToExitoEnviado = true) } },
                onFailure = { e ->
                    val msg = if (e.message?.contains("23505") == true || e.message?.contains("duplicate key") == true)
                        "Ya existe una reunión registrada para esta fecha."
                    else
                        e.message ?: "Error al enviar. Intenta de nuevo."
                    _uiState.update { it.copy(isEnviando = false, errorEnvio = msg) }
                },
            )
        }
    }

    fun consumeExitoEnviadoNavigation() {
        _uiState.update { it.copy(navigateToExitoEnviado = false) }
    }

    fun consumeExitoOfflineNavigation() {
        _uiState.update { it.copy(navigateToExitoOffline = false) }
    }
}
