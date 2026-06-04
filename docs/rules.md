# Reglas de negocio — GP Leader

## Registro de reunión (3 pasos)

### Paso 1 — Asistencia

- Fecha auto-llena con hoy, editable con botón ✎
- Botón "No hubo reunión" → marca la reunión y envía sin asistencia
- "Sel. todos P/A/J" → aplica a todos los miembros según primera selección
- Si todos quedan en A → modal de confirmación antes de continuar
- Visitas anteriores: colapsable, misma UI que miembros, botón "+ Agregar" por cada una
- Al escribir nueva visita → dropdown hacia ARRIBA con coincidencias de visitas anteriores
- Nueva visita se registra como Presente automáticamente (sin toggle P/A/J)

### Paso 2 — Actividades

- Actividades agrupadas por nivel: Unión → Pastor → Mi GP
- Unión: bloqueadas para el líder (solo lectura, 🔒)
- Pastor y GP: editables por el líder
- Cada actividad es una **NeuCard individual** con chip de valor (`—` vacío o total calculado)
- CHECKBOX: toggle inline dentro de su NeuCard
- Tap en actividad editable → navega a `DetalleActividadScreen` (pantalla completa)
- El total del chip se auto-calcula desde el desglose por miembro
- Si se intenta avanzar con algún `—` en actividad obligatoria → error en rojo
- "Agregar actividad extra" → pantalla completa, solo para nivel GP

### Paso 3 — Resumen

- Muestra resumen de asistencia + actividades clasificadas por nivel
- Botón "Enviar al pastor" → si hay conexión envía, si no guarda offline
- Offline: se sincroniza automáticamente al recuperar conexión

---

## Flujo suplente

- Código de 6 dígitos, válido 24 horas, un solo uso
- El líder lo genera desde botón "Suplente" en el nav superior del Home
- Suplente abre app → "Ingresar como suplente" → ingresa código → ve bienvenida con datos del grupo
- Suplente registra reunión igual que líder (Paso 1, 2, 3) pero con banner negro "Modo suplente"
- Al enviar → va al líder para aprobar, NO al pastor directamente
- En el historial queda registrado "Registrado por [nombre suplente] · Aprobado por [líder]"

---

## Actividades y jerarquía

- Solo el nivel que creó la actividad o superior puede modificar el contador
- El líder solo puede editar actividades de nivel GP y agregar extras propias
- Las de Unión/Misión/Pastor son de solo lectura para el líder

---

## Nombres de miembros

- Campos separados: primer nombre, segundo nombre (opcional), primer apellido, segundo apellido (opcional)
- Al agregar miembro → estado siempre ACTIVO (sin toggle en formulario de agregar)
- Toggle Activo/Archivado solo aparece en formulario de EDITAR

---

## Patrón desglose granular por miembro — dirección INVERTIDA

El desglose de actividades funciona en dirección **miembro → total** (no al revés):

```kotlin
// En RegistroViewModel:
fun onDesgloseChange(actividadId, miembroId, nuevaCantidad) {
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

### Patrón sinLimite en filas de desglose

`MiembroDesgloseRow` y `MiembroDesgloseMonetarioRow` aceptan `sinLimite: Boolean = false`:
- `sinLimite = false` (default): limita el valor al `totalGeneral - sumOtros` (modo Paso 2 anterior)
- `sinLimite = true`: sin límite superior, `allowDirectInput = true` (modo DetalleActividadScreen)

---

## Patrón HomeScreen — registro de asistencia

```kotlin
// HomeUiState contiene:
val reunionGpHoy:        ReunionResumen?         // GP registrado hoy → reemplaza RegistrarCard
val reunionSabadoSemana: SabbathMeetingResumen?  // Sábado esta semana → badge debajo de RegistrarCard

// Lógica en HomeScreenContent:
if (reunionGpHoy != null) {
    YaRegistrasteBadge("Ya registraste hoy tu grupo pequeño", "${gp.presentes} presentes · ...")
} else {
    RegistrarCard(onClick = { showRegistrarSheet = true })
    if (reunionSabadoSemana != null) {
        YaRegistrasteBadge("Ya registraste hoy culto de sábado", "${sabado.presentes} presentes")
    }
}
// TipoRegistroSheet también recibe reunionGpHoy y reunionSabadoSemana
// → muestra YaRegistrasteBadge para la opción ya registrada en lugar del botón

// Renovación:
// GP → diario (busca fecha == LocalDate.now())
// Sábado → semanal (busca desde ultimoSábado = previousOrSame(DayOfWeek.SATURDAY))
```

---

## AgregarActividadScreen — campos de fecha

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

---

## Patrón formatTotalValor en ActividadesListScreen

```kotlin
private fun formatTotalValor(item: ActividadConResumen): String = when (item.tipo.markerType) {
    "monetary"     -> "₡${item.montoTotal.toLong()}"
    "checkbox"     -> if (item.tipo.frecuencia == "diaria") "${item.totalCantidad} días"
                      else "${item.totalCantidad} semanas"
    "participants" -> "${item.totalCantidad} ${item.tipo.unitLabel}"
    else           -> "${item.totalCantidad} ${item.tipo.unitLabel}"
}
```

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
- ❌ No sumar `member_activity_record` con `status="draft"` en totales — solo `approved` y `pending_board`

---

## Datos de muestra (temporal — reemplazar con PowerSync)

- Código suplente válido para pruebas: **`123456`**
- Miembros en MiembrosViewModel: 10 miembros hardcoded (8 ACTIVO, 2 ARCHIVADO) ⚠️ pendiente reemplazar
- HomeViewModel: nombre del grupo viene de `session.grupoNombre`, reuniones recientes son reales; stats del grupo (iglesia, horario) ⚠️ pendiente
- Actividades: ✅ CONECTADAS A SUPABASE — carga desde `activity_type` via ActividadRepository
