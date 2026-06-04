# Base de datos — GP Leader

## Supabase — tablas principales

**Nombres reales en Supabase (en inglés):**

| Tabla Supabase | Descripción |
|---------------|-------------|
| `campo` | Unión/campo eclesiástico |
| `district` | Distrito (hijo de campo) |
| `church` | Iglesia (hija de distrito) |
| `small_group` | Grupo pequeño (hijo de iglesia) |
| `member` | Miembro del GP |
| `meeting` | Reunión registrada |
| `attendance` | Asistencia por reunión |
| `activity_type` | Tipo de actividad (level, marker_type, scope, start_date, end_date) |
| `activity_record` | Registro de actividad por reunión (count nullable, monto) |
| `member_activity_record` | Registro individual de actividad por miembro (count, is_done, status) |
| `deputy_code` | Código de suplente |
| `missionary_duo` | Parejas misioneras (member1_id, member2_id, is_active) |
| `duo_activity_type` | Actividades propias de cada dúo (nombre, marker_type, unit_label) |
| `duo_activity_record` | Registro compartido por dúo — UNIQUE(duo_id, activity_type_id, record_date) |
| `duo_bible_study` | Estudios bíblicos compartidos del dúo (student_name, completed_lessons[]) |

---

## Columnas importantes por tabla

### `small_group`

- `gp_username` — username para auth (ej: `ucn-cat-icb-gp-los-olivos`)
- `gp_password_set` — boolean: si ya cambió la contraseña temporal
- `gp_password` — contraseña temporal (primera vez)
- `gp_code` — código suplente activo
- `church_id` — FK a `church`

### `member`

- `is_visitor` — boolean: `true` = visita puntual, `false` = miembro permanente
- `is_active` — boolean: `true` = activo, `false` = archivado
- Los miembros con `is_visitor=true` NO aparecen en la lista de miembros del grupo
- Columnas de nombre: `first_name`, `middle_name`, `last_name`, `second_last_name`
  ← NO usar `second_name/first_surname/second_surname`

### `attendance`

```
attendance: id, meeting_id, member_id (NOT NULL), status (attendance_status), note
```

- NO tiene `is_visitor` ni `visitor_name`
- Las visitas se guardan en `member` con `is_visitor=true`. Su `id` va en `attendance.member_id`
- Unique constraint en `meeting`: `UNIQUE(small_group_id, meeting_date, registry_kind)`
  → permite una `gp_meeting` Y una `saturday_worship` en el mismo día para el mismo grupo

### `member_activity_record` — columnas confirmadas

```
id, member_id, activity_type_id, record_date (date), count (int, nullable),
is_done (boolean), marked_at (timestamp with time zone, nullable),
created_at (timestamp with time zone, nullable), status (text NOT NULL DEFAULT 'draft')
```

⚠️ **NO existe columna `monto`** en esta tabla — no enviar `monto` en upserts.

- `status`: `"draft"` | `"approved"` | `"pending_board"` | `"rejected"`
  - `draft` → recién enviado por miembro, pendiente de aprobación del líder
  - `approved` → aprobado por líder (actividades no monetarias)
  - `pending_board` → aprobado por líder, pendiente de Junta de Iglesia (monetarias)
  - `rejected` → rechazado por líder
- Unique constraint en `(member_id, activity_type_id, record_date)`
- Los totales en `getActividadesConTotales` solo suman `status IN ('approved', 'pending_board')`
- DDL aplicado:
  ```sql
  ALTER TABLE member_activity_record
    ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'draft'
    CHECK (status IN ('draft','approved','pending_board','rejected'));
  ```

### `activity_type` — columnas confirmadas

```
id, name, level, marker_type, unit_label, sort_order, scope, church_id,
start_date (date, nullable), end_date (date, nullable), is_active (boolean)
```

- `level`: `"union"` | `"pastor"` | `"my_group"`
- `marker_type`: `"counter"` | `"monetary"` | `"checkbox"` | `"participants"`
- `scope`: `"global"` | `"church"` (church-scoped filtra por `church_id == iglesiaId`)
- `start_date` / `end_date`: definen período de vigencia. NULL = sin restricción.
- DDL aplicado:
  ```sql
  ALTER TABLE activity_type
    ADD COLUMN IF NOT EXISTS start_date date,
    ADD COLUMN IF NOT EXISTS end_date date;
  ```

**Dos métodos en ActividadRepository:**
- `getActividadesTipo(iglesiaId)` — aplica filtro scope + filtro período (solo activas hoy) → Paso 2
- `getTodasActividadesTipo(iglesiaId)` — aplica filtro scope, sin filtro período → ActividadesListScreen

---

## Auth — mecanismo real

El login usa Supabase Auth con email sintético:

```
email:    "{gp_username}@login.presencia.app"
password: contraseña del grupo
```

- **Primera vez** (`gp_password_set = false`): navegar a `CAMBIAR_CONTRASENA`
- **Login exitoso normal**: navegar a `QUIEN_ERES`
- La carga de datos del login requiere una sesión anónima previa (`loginAnonymously()` en Application startup)

---

## Jerarquía eclesiástica (para login)

```
campo → district → church → small_group
```

El LoginViewModel carga las 4 tablas en paralelo al init y aplica filtrado cliente:

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

Todos los dropdowns están **siempre habilitados** — no dependen del superior para activarse.

---

## RLS (Row Level Security)

- Lider solo accede a datos de su grupo
- Suplente accede solo durante vigencia del código (24h)
- Pastor accede a todos los grupos de su iglesia

---

## Schema Supabase — enums confirmados

```
meeting_status    = {draft, submitted}           -- usar "submitted" al guardar
attendance_status = {present, absent, justified} -- mapear desde dominio español
```

---

## Patrones de acceso a datos

### Patrón ReunionRepository — saveReunion

```kotlin
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

### Patrón MiembroRepository — filtrado is_visitor

```kotlin
// member.is_visitor = true  → visita puntual, aparece en lista de visitas del Paso 1
// member.is_visitor = false → miembro real, aparece en MiembrosListaScreen
// member.is_active  = false → archivado, aparece en sección ARCHIVADOS

// getMiembros()        → filtra is_visitor=false (activos + archivados reales)
// getMiembrosActivos() → filtra is_visitor=false AND is_active=true
```

### Patrón JsonNull en respuestas PostgREST

Cuando una FK es NULL, PostgREST devuelve `"church": null` en el JSON.
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

### Patrón MemberActivitySubmission — flujo de aprobación

```kotlin
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
//   isMonetary = false → status = "approved"      (suma en totales del GP)
//   isMonetary = true  → status = "pending_board" (espera Junta de Iglesia)
// Líder rechaza → status = "rejected"
```

### Patrón MiembroMarcado — incluye marcadaEn

```kotlin
data class MiembroMarcado(
    val id:        String,
    val nombre:    String,
    val marcado:   Boolean,
    val marcadaEn: java.time.Instant? = null,  // hora marcada, del campo marked_at en Supabase
)
```

`getDiasCompletionStats` selecciona `marked_at` y lo parsea.

### Patrón toggleMiembroActividad — autoApprove

```kotlin
// Actividades diarias → siempre autoApprove=true (no requieren aprobación del líder)
// Actividades semanales/contador/monetario → autoApprove=false (van a "draft", líder aprueba)
actividadRepo.toggleMiembroActividad(miembroId, tipoId, fecha, isDone, autoApprove = true)
// → status = "approved" inmediatamente, suma en totales del GP
```

### SuplementeViewModel — responsabilidades

Un solo ViewModel cubre dos flujos:
1. **Suplente entrante** (SuplementeCodigoScreen → SuplementeBienvenidaScreen): valida código, recibe grupoInfo, almacena nombreSuplente
2. **Líder generando código** (SheetGenerarCodigoSuplente): genera/revoca código, muestra vigencia

El ViewModel vive en el scope de `SUPLENTE_GRAPH` (flujo suplente) y como instancia independiente en HomeScreen (generación de código por el líder).

---

## Tablas Dúos Misioneros (agregado 2026-06-04)

```sql
-- Dúos activos del grupo
CREATE TABLE missionary_duo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    small_group_id UUID NOT NULL REFERENCES small_group(id),
    member1_id UUID NOT NULL REFERENCES member(id),
    member2_id UUID NOT NULL REFERENCES member(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tipos de actividad propios del dúo (creados por el líder)
CREATE TABLE duo_activity_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    duo_id UUID NOT NULL REFERENCES missionary_duo(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL,
    marker_type TEXT NOT NULL DEFAULT 'counter' CHECK (marker_type IN ('counter', 'checkbox')),
    unit_label TEXT NOT NULL DEFAULT 'veces',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Registro COMPARTIDO — un registro por (duo, actividad, fecha)
-- Cualquier miembro del dúo puede actualizarlo (updated_by = quien tocó último)
CREATE TABLE duo_activity_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    duo_id UUID NOT NULL REFERENCES missionary_duo(id) ON DELETE CASCADE,
    activity_type_id UUID NOT NULL REFERENCES duo_activity_type(id) ON DELETE CASCADE,
    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
    count INTEGER,
    is_done BOOLEAN NOT NULL DEFAULT false,
    updated_by UUID REFERENCES member(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(duo_id, activity_type_id, record_date)
);

-- Estudios bíblicos del dúo (distintos de los individuales)
CREATE TABLE duo_bible_study (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    duo_id UUID NOT NULL REFERENCES missionary_duo(id) ON DELETE CASCADE,
    student_name TEXT NOT NULL,
    completed_lessons INTEGER[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### DuoRepository — métodos

```kotlin
// Consultas
getDuosByGrupo(grupoId)             → List<DuoMisioneroData>
getDuoPorMiembro(miembroId)         → DuoMisioneroData?   // activo o null
crearDuo(grupoId, m1Id, m2Id)       → String (duoId)
desactivarDuo(duoId)

getActividadesDuo(duoId)            → List<DuoActividadTipo>
crearActividadDuo(duoId, nombre, markerType, unitLabel)
getRegistrosDuo(duoId, tipoId, desde) → List<DuoActividadRecord>
upsertRegistroDuo(duoId, tipoId, fecha, count, isDone, updatedBy)

getEstudiosDuo(duoId)               → List<DuoBibleStudy>
crearEstudioDuo(duoId, studentName)
toggleLeccionDuo(estudioId, leccion, completado)
```

### Patrón registro compartido

`duo_activity_record` tiene `UNIQUE(duo_id, activity_type_id, record_date)`.
Cualquier miembro hace upsert al mismo registro. El líder y ambos miembros siempre ven el valor más reciente. No hay suma de partes — es un solo valor editable por cualquiera.
