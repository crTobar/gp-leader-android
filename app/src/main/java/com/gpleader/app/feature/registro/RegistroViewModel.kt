package com.gpleader.app.feature.registro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

data class ActividadRegistro(
    val id:           String,
    val nombre:       String,
    val nivel:        NivelActividad,
    val unidad:       String,
    val esOficial:    Boolean,
    val esExtra:      Boolean = false,
    val esObligatoria: Boolean = false,
    val cantidad:     Int?    = null,
    val notas:        String? = null,
    val bloqueada:    Boolean = false,
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
    // ── Paso 2 ───────────────────────────────────────────────────────────────
    val actividades:                  List<ActividadRegistro> = emptyList(),
    val errorActividadesObligatorias: Boolean                 = false,
    val showJustificadoHint:          Boolean                 = true,
    // ── Navegación ───────────────────────────────────────────────────────────
    val navigateToPaso2:          Boolean = false,
    val navigateToPaso3:          Boolean = false,
    val navigateToExitoEnviado:   Boolean = false,
    val navigateToExitoOffline:   Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class RegistroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("gpleader_prefs", android.content.Context.MODE_PRIVATE)
    }
    private fun justificadoUsos() = prefs.getInt("justificado_usos", 0)

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        // TODO: reemplazar con carga real desde PowerSync
        _uiState.update {
            it.copy(
                showJustificadoHint = justificadoUsos() < 5,
                miembros = listOf(
                    MiembroAsistencia("m1", "Ana Castillo",    "AC", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m2", "Jose Rodriguez",  "JR", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m3", "Lucia Martinez",  "LM", EstadoAsistencia.AUSENTE),
                    MiembroAsistencia("m4", "Carlos Perez",    "CP", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m5", "Rosa Torres",     "RT", EstadoAsistencia.JUSTIFICADO),
                    MiembroAsistencia("m6", "Miguel Santos",   "MS", EstadoAsistencia.PRESENTE),
                    MiembroAsistencia("m7", "Carmen Vega",     "CV", EstadoAsistencia.PRESENTE),
                ),
                visitasAnteriores = listOf(
                    VisitaAnterior("v1", "Juan Lopez",    LocalDate.of(2026, 2, 26), visitasCount = 5),
                    VisitaAnterior("v2", "Maria Solano",  LocalDate.of(2026, 2, 19), visitasCount = 3),
                    VisitaAnterior("v3", "Roberto Gomez", LocalDate.of(2026, 2, 12), visitasCount = 1),
                ).sortedByDescending { v -> v.visitasCount },
                visitasDeHoy = listOf(
                    VisitaHoy("v1", "Juan Lopez", esNueva = false, EstadoAsistencia.PRESENTE),
                ),
                actividades = listOf(
                    // UNIÓN — bloqueadas, solo lectura (cantidad pre-llenada)
                    ActividadRegistro("a1", "Recolección ofrendas", NivelActividad.UNION,   "personas", esOficial = true, bloqueada = true, cantidad = 8),
                    ActividadRegistro("a2", "Repartir literatura",  NivelActividad.UNION,   "libros",   esOficial = true, bloqueada = true, cantidad = 5),
                    // PASTOR — editables, obligatorias
                    ActividadRegistro("a3", "Estudios Bíblicos",     NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true, cantidad = 3),
                    ActividadRegistro("a4", "Peticiones de Oración", NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true, cantidad = 5),
                    ActividadRegistro("a5", "Interesados Nuevos",    NivelActividad.PASTOR, "personas", esOficial = true, esObligatoria = true, cantidad = 1),
                    // MI GP — extras del grupo (total directo)
                    ActividadRegistro("a6", "Oración especial",     NivelActividad.GP,     "veces",    esOficial = false, esExtra = true, cantidad = 4),
                ),
            )
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

    fun onToggleVisitasColapsadas() {
        _uiState.update { it.copy(visitasColapsadas = !it.visitasColapsadas) }
    }

    fun onContinuarClick() {
        val alguno = _uiState.value.miembros.any { it.estado != null }
        if (!alguno) {
            _uiState.update { it.copy(errorSinAsistencia = true) }
            return
        }
        _uiState.update { it.copy(navigateToPaso2 = true) }
    }

    // ── Paso 2 ────────────────────────────────────────────────────────────────

    fun onCantidadChange(actividadId: String, cantidad: Int?) {
        _uiState.update { s ->
            s.copy(
                actividades = s.actividades.map { a ->
                    if (a.id == actividadId) a.copy(cantidad = cantidad) else a
                },
                errorActividadesObligatorias = false,
            )
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
        val hayObligatoriaVacia = _uiState.value.actividades
            .any { it.esObligatoria && it.cantidad == null }
        if (hayObligatoriaVacia) {
            _uiState.update { it.copy(errorActividadesObligatorias = true) }
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
        val cm      = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps    = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val online  = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        // TODO: escribir en PowerSync; si online también sincronizar con Supabase
        if (online) {
            _uiState.update { it.copy(navigateToExitoEnviado = true) }
        } else {
            _uiState.update { it.copy(navigateToExitoOffline = true) }
        }
    }

    fun consumeExitoEnviadoNavigation() {
        _uiState.update { it.copy(navigateToExitoEnviado = false) }
    }

    fun consumeExitoOfflineNavigation() {
        _uiState.update { it.copy(navigateToExitoOffline = false) }
    }
}
