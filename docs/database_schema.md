# GP Leader — Schema de Base de Datos

> Snapshot generado el 2026-05-21. Refleja el estado real de Supabase (public schema) antes de cualquier migración futura.

---

## Jerarquía eclesiástica

```
union_org
  └── campo
        └── district
              └── church
                    └── small_group
                          └── member
```

---

## Enums

| Tipo | Valores |
|------|---------|
| `activity_scope` | `global`, `campo`, `church`, `district` |
| `attendance_status` | `present`, `absent`, `justified` |
| `deputy_submission_status` | `pending`, `approved`, `rejected` |
| `edit_window_type` | `one_week`, `two_weeks`, `one_month`, `one_quarter`, `one_year`, `unlimited` |
| `group_request_status` | `pending`, `approved`, `rejected` |
| `meeting_status` | `draft`, `submitted` |
| `member_status` | `active`, `archived`, `inactive` |
| `pastoral_assignment_status` | `active`, `ended`, `on_leave` |
| `user_role` | `leader`, `co_leader`, `anciano`, `pastor`, `campo_admin`, `union_admin`, `super_admin`, `pastor_practicante`, `director_unadeca` |

---

## Tablas

### `union_org`
Unión eclesiástica (nivel más alto).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `name` | text | NO | | |
| `code` | text | SÍ | | UNIQUE |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 2 | **RLS:** activado

---

### `campo`
Campo eclesiástico (hijo de union_org).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `union_id` | uuid | NO | | FK → union_org.id |
| `name` | text | NO | | |
| `code` | text | SÍ | | UNIQUE |
| `province` | text | SÍ | | |
| `submission_edit_window` | edit_window_type | NO | `one_week` | Ventana de edición de reuniones |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 4 | **RLS:** activado

---

### `district`
Distrito (hijo de campo).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `campo_id` | uuid | NO | | FK → campo.id |
| `name` | text | NO | | |
| `province_code` | text | SÍ | | |
| `edit_window_days` | integer | SÍ | | Días hacia atrás que se pueden editar meetings. NULL = trimestre completo abierto |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 7 | **RLS:** activado

---

### `church`
Iglesia (hija de district).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `district_id` | uuid | NO | | FK → district.id |
| `name` | text | NO | | |
| `code` | text | SÍ | | |
| `address` | text | SÍ | | |
| `province` | text | SÍ | | |
| `canton` | text | SÍ | | |
| `district_name` | text | SÍ | | Desnormalizado para búsqueda |
| `zip_code` | text | SÍ | | |
| `is_active` | boolean | NO | true | |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 31 | **RLS:** activado  
**Índices:** `church_name_trgm_idx` (GIN trigram sobre `name`)

---

### `small_group`
Grupo pequeño (hijo de church).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `church_id` | uuid | NO | | FK → church.id |
| `name` | text | NO | | |
| `meeting_day` | text | SÍ | | |
| `meeting_time` | time | SÍ | | |
| `meeting_place` | text | SÍ | | |
| `hymn` | text | SÍ | | Canto favorito |
| `favorite_verse` | text | SÍ | | |
| `bible_chapter` | text | SÍ | | Personaje/capítulo bíblico |
| `is_active` | boolean | NO | true | |
| `gp_username` | text | SÍ | | Handle de login; email sintético = `{gp_username}@login.presencia.app` |
| `gp_password` | text | SÍ | | Contraseña temporal inicial |
| `gp_temp_password` | text | SÍ | | |
| `gp_password_set` | boolean | NO | false | true = ya cambió la contraseña inicial |
| `gp_code` | text | SÍ | | UNIQUE — código suplente activo |
| `created_by` | uuid | SÍ | | FK → auth.users.id |
| `last_edited_by` | uuid | SÍ | | FK → auth.users.id |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 68 | **RLS:** activado  
**Índices:** `small_group_gp_code_key` (UNIQUE sobre `gp_code`)

---

### `profile`
Perfil de usuario autenticado (espejo de auth.users).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | | PK, FK → auth.users.id |
| `first_name` | text | NO | | |
| `middle_name` | text | SÍ | | |
| `last_name` | text | NO | | |
| `second_last_name` | text | SÍ | | |
| `email` | text | SÍ | | |
| `phone` | text | SÍ | | |
| `avatar_url` | text | SÍ | | |
| `school` | text | SÍ | | |
| `temp_password` | text | SÍ | | |
| `must_change_password` | boolean | NO | false | |
| `is_active` | boolean | NO | true | |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 108 | **RLS:** activado

---

### `role_assignment`
Asignación de roles por nivel jerárquico.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `profile_id` | uuid | NO | | FK → profile.id |
| `role` | user_role | NO | | |
| `small_group_id` | uuid | SÍ | | FK → small_group.id |
| `church_id` | uuid | SÍ | | FK → church.id |
| `district_id` | uuid | SÍ | | FK → district.id |
| `campo_id` | uuid | SÍ | | FK → campo.id |
| `union_id` | uuid | SÍ | | FK → union_org.id |
| `title` | text | SÍ | | |
| `is_active` | boolean | NO | true | |
| `ended_at` | timestamptz | SÍ | | |
| `supervisor_id` | uuid | SÍ | | FK → auth.users.id |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 108 | **RLS:** activado  
**Índices:**
- UNIQUE `(profile_id, role, small_group_id, church_id, district_id, campo_id, union_id)`
- `idx_role_assignment_active` (WHERE is_active = true)

---

### `member`
Miembro de un grupo pequeño (o visita puntual).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `first_name` | text | NO | | |
| `middle_name` | text | SÍ | | |
| `last_name` | text | NO | | |
| `second_last_name` | text | SÍ | | |
| `phone` | text | SÍ | | |
| `email` | text | SÍ | | |
| `is_visitor` | boolean | NO | false | true = visita puntual (no aparece en lista de miembros) |
| `is_active` | boolean | NO | true | |
| `is_leader` | boolean | NO | false | true = muestra asterisco en QuienEresScreen y requiere contraseña |
| `status` | member_status | NO | `active` | |
| `created_by` | uuid | SÍ | | FK → auth.users.id |
| `last_edited_by` | uuid | SÍ | | FK → auth.users.id |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 400 | **RLS:** activado  
**Índices:** `idx_member_small_group` (WHERE is_active = true)

---

### `meeting`
Reunión registrada (GP o culto de sábado).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `meeting_date` | date | NO | | |
| `registry_kind` | text | NO | `gp_meeting` | CHECK: `gp_meeting` \| `saturday_worship` |
| `status` | meeting_status | NO | `draft` | |
| `no_meeting` | boolean | NO | false | true = no hubo reunión esa semana |
| `notes` | text | SÍ | | |
| `quarter_id` | uuid | SÍ | | FK → quarter.id |
| `submitted_at` | timestamptz | SÍ | | |
| `submitted_by` | uuid | SÍ | | FK → profile.id |
| `submitted_actor_display_name` | text | SÍ | | Nombre para historial (líder/suplente) |
| `last_saved_at` | timestamptz | SÍ | | |
| `last_edited_by` | uuid | SÍ | | FK → profile.id |
| `created_by` | uuid | SÍ | | FK → auth.users.id |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 196 | **RLS:** activado  
**Índices:**
- UNIQUE `meeting_unique_per_group_date_kind` sobre `(small_group_id, meeting_date, registry_kind)` — permite una `gp_meeting` Y una `saturday_worship` el mismo día para el mismo grupo
- `idx_meeting_small_group_date`, `idx_meeting_status`, `meeting_quarter_id_idx`

---

### `attendance`
Registro de asistencia por miembro por reunión.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `meeting_id` | uuid | NO | | FK → meeting.id |
| `member_id` | uuid | NO | | FK → member.id |
| `status` | attendance_status | NO | `absent` | |
| `note` | text | SÍ | | |
| `visited_church_id` | uuid | SÍ | | FK → church.id — iglesia a la que asistió (culto sábado) |

**Filas actuales:** 1114 | **RLS:** activado  
**Índices:**
- UNIQUE `attendance_meeting_id_member_id_key` sobre `(meeting_id, member_id)`
- `idx_attendance_meeting` sobre `meeting_id`

---

### `activity_type`
Tipo de actividad (definida por Unión, Campo, Distrito, Pastor o GP).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `name` | text | NO | | |
| `description` | text | SÍ | | |
| `icon` | text | SÍ | | |
| `level` | text | NO | `my_group` | `union` \| `pastor` \| `my_group` |
| `scope` | activity_scope | NO | `global` | `global` \| `campo` \| `church` \| `district` |
| `marker_type` | text | NO | `counter` | CHECK: `checkbox` \| `counter` \| `monetary` |
| `value_type` | text | SÍ | `discrete` | CHECK: `boolean` \| `discrete` \| `monetary` |
| `unit_label` | text | NO | `count` | Unidad mostrada en UI |
| `frecuencia` | text | NO | `semanal` | |
| `sort_order` | integer | NO | 0 | |
| `campo_id` | uuid | SÍ | | FK → campo.id (scope=campo) |
| `district_id` | uuid | SÍ | | FK → district.id (scope=district) |
| `church_id` | uuid | SÍ | | FK → church.id (scope=church) |
| `start_date` | date | SÍ | | Inicio de vigencia. NULL = sin restricción |
| `end_date` | date | SÍ | | Fin de vigencia. NULL = sin restricción |
| `is_indefinite` | boolean | SÍ | true | |
| `is_active` | boolean | NO | true | |
| `is_member_accessible` | boolean | SÍ | false | true = visible en flujo de miembro |
| `created_by` | uuid | SÍ | | FK → profile.id |
| `last_edited_by` | uuid | SÍ | | FK → profile.id |
| `archived_by` | uuid | SÍ | | FK → auth.users.id |
| `archived_at` | timestamptz | SÍ | | |
| `last_edited_at` | timestamptz | SÍ | | |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 14 | **RLS:** activado

---

### `activity_record`
Registro del valor de una actividad en una reunión específica.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `meeting_id` | uuid | NO | | FK → meeting.id |
| `activity_type_id` | uuid | NO | | FK → activity_type.id |
| `count` | integer | SÍ | | NULL = sin llenar (muestra "—" en UI) |
| `monto` | numeric | SÍ | | Para marker_type = monetary |
| `notes` | text | SÍ | | |

**Filas actuales:** 938 | **RLS:** activado  
**Índices:** UNIQUE `activity_record_meeting_id_activity_type_id_key` sobre `(meeting_id, activity_type_id)`

---

### `member_activity_record`
Registro de actividad personal de un miembro (flujo miembro).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `member_id` | uuid | NO | | FK → member.id |
| `activity_type_id` | uuid | NO | | FK → activity_type.id |
| `record_date` | date | NO | CURRENT_DATE | |
| `is_done` | boolean | NO | true | |
| `count` | integer | SÍ | | |
| `marked_at` | timestamptz | SÍ | | |
| `created_at` | timestamptz | SÍ | now() | |

**Filas actuales:** 1 | **RLS:** activado  
**Índices:** UNIQUE `(member_id, activity_type_id, record_date)`

---

### `quarter`
Trimestres del año para agrupación de reportes.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `year` | integer | NO | | |
| `quarter_number` | integer | NO | | CHECK: 1–4 |
| `start_date` | date | NO | | |
| `end_date` | date | NO | | |
| `is_current` | boolean | NO | false | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 4 | **RLS:** activado  
**Índices:** UNIQUE `(year, quarter_number)`

---

### `deputy_code`
Códigos de 6 dígitos para acceso temporal de suplente.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `code` | text | NO | | CHECK: regex `^[0-9]{6}$` |
| `expires_at` | timestamptz | NO | | Vigencia 24 horas |
| `used_at` | timestamptz | SÍ | | NULL = no usado aún |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 14 | **RLS:** activado  
**Índices:**
- UNIQUE `deputy_code_unique_unused_per_group_code` sobre `(small_group_id, code)` WHERE `used_at IS NULL`

---

### `deputy_assignment`
Asignación formal de suplente a un grupo.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `deputy_member_id` | uuid | NO | | FK → member.id |
| `assigned_by_member_id` | uuid | NO | | FK → member.id |
| `approved_by_member_id` | uuid | SÍ | | FK → member.id |
| `assigned_at` | timestamptz | NO | now() | |
| `expires_at` | timestamptz | NO | | |
| `session_started_at` | timestamptz | SÍ | | |
| `approved_at` | timestamptz | SÍ | | |
| `status` | text | NO | `pending` | CHECK: `pending` \| `active` \| `completed` \| `revoked` |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 1 | **RLS:** activado  
**Índices:** UNIQUE `deputy_assignment_active_unique` sobre `(small_group_id)` WHERE `status IN ('pending','active')`

---

### `deputy_submission`
Registro enviado por suplente (pendiente de aprobación del líder).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `campo_id` | uuid | NO | | FK → campo.id |
| `meeting_id` | uuid | SÍ | | FK → meeting.id |
| `meeting_date` | date | NO | | |
| `deputy_name` | text | NO | | |
| `attendance_data` | jsonb | NO | | Snapshot de asistencia |
| `activity_data` | jsonb | NO | | Snapshot de actividades |
| `notes` | text | SÍ | | |
| `status` | deputy_submission_status | NO | `pending` | |
| `reviewed_by` | uuid | SÍ | | FK → profile.id |
| `reviewed_at` | timestamptz | SÍ | | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 6 | **RLS:** activado

---

### `reporting_period`
Período de reporte semanal por campo.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `campo_id` | uuid | NO | | FK → campo.id |
| `name` | text | SÍ | | |
| `week_start` | date | NO | | |
| `week_end` | date | NO | | |
| `is_closed` | boolean | NO | false | |
| `closed_at` | timestamptz | SÍ | | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 1 | **RLS:** activado

---

### `pastoral_district`
Asignación de pastor a iglesia.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `church_id` | uuid | NO | | FK → church.id |
| `pastor_id` | uuid | NO | | FK → profile.id |
| `start_date` | date | NO | | |
| `end_date` | date | SÍ | | |
| `status` | pastoral_assignment_status | NO | `active` | |
| `notes` | text | SÍ | | |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 1 | **RLS:** activado  
**Índices:** UNIQUE `idx_pastoral_district_active` sobre `(church_id)` WHERE `status = 'active'`

---

### `group_request`
Solicitud de creación de nuevo grupo pequeño.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `church_id` | uuid | NO | | FK → church.id |
| `small_group_id` | uuid | SÍ | | FK → small_group.id (asignado al aprobar) |
| `requested_by` | uuid | NO | | FK → profile.id |
| `proposed_leader` | uuid | SÍ | | FK → profile.id |
| `proposed_leader_name` | text | SÍ | | |
| `proposed_members` | jsonb | SÍ | `[]` | |
| `name` | text | NO | | |
| `meeting_day` | text | SÍ | | |
| `meeting_time` | time | SÍ | | |
| `notes` | text | SÍ | | |
| `status` | group_request_status | NO | `pending` | |
| `reviewed_by` | uuid | SÍ | | FK → profile.id |
| `reviewed_at` | timestamptz | SÍ | | |
| `rejection_reason` | text | SÍ | | |
| `created_at` | timestamptz | NO | now() | |
| `updated_at` | timestamptz | NO | now() | |

**Filas actuales:** 2 | **RLS:** activado

---

### `visitor`
Visita puntual (no pertenece a un grupo específico).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `first_name` | text | NO | | |
| `last_name` | text | NO | | |
| `phone` | text | SÍ | | |
| `email` | text | SÍ | | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 110 | **RLS:** activado

---

### `visit_log`
Registro de visitas a un grupo pequeño.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `visitor_id` | uuid | NO | | FK → visitor.id |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `visit_date` | date | NO | | |
| `notes` | text | SÍ | | |
| `registered_by` | uuid | SÍ | | FK → auth.users.id |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 110 | **RLS:** activado

---

### `self_saturday_church_attendance`
Declaraciones anónimas de asistencia al culto de sábado (insert vía RPC `submit_self_saturday_church_attendance`).

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `church_id` | uuid | NO | | FK → church.id |
| `attendance_date` | date | NO | | |
| `first_name` | text | NO | | CHECK: 1–80 chars (trimmed) |
| `last_name` | text | NO | | CHECK: 1–80 chars (trimmed) |
| `notes` | text | SÍ | | CHECK: max 500 chars |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 21 | **RLS:** activado  
**Índices:** `self_sat_church_date_idx` sobre `(church_id, attendance_date DESC)`

---

### `gp_sessions`
Sesiones activas de grupos pequeños.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `gp_code` | text | NO | | |
| `session_token` | text | NO | | UNIQUE |
| `device_info` | text | SÍ | | |
| `is_active` | boolean | NO | true | |
| `last_active_at` | timestamptz | NO | now() | |
| `expires_at` | timestamptz | NO | | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 0 | **RLS:** activado

---

### `activity_log`
Auditoría de acciones de usuarios en grupos.

| Columna | Tipo | Nullable | Default | Notas |
|---------|------|----------|---------|-------|
| `id` | uuid | NO | gen_random_uuid() | PK |
| `profile_id` | uuid | NO | | FK → profile.id |
| `small_group_id` | uuid | NO | | FK → small_group.id |
| `action_type` | text | NO | | |
| `description` | text | NO | | |
| `created_at` | timestamptz | NO | now() | |

**Filas actuales:** 29 | **RLS:** activado

---

## Resumen de constraints únicos clave

| Tabla | Constraint | Columnas |
|-------|-----------|----------|
| `meeting` | `meeting_unique_per_group_date_kind` | `(small_group_id, meeting_date, registry_kind)` |
| `attendance` | `attendance_meeting_id_member_id_key` | `(meeting_id, member_id)` |
| `activity_record` | `activity_record_meeting_id_activity_type_id_key` | `(meeting_id, activity_type_id)` |
| `member_activity_record` | — | `(member_id, activity_type_id, record_date)` |
| `deputy_code` | `deputy_code_unique_unused_per_group_code` | `(small_group_id, code)` WHERE `used_at IS NULL` |
| `deputy_assignment` | `deputy_assignment_active_unique` | `(small_group_id)` WHERE `status IN ('pending','active')` |
| `pastoral_district` | `idx_pastoral_district_active` | `(church_id)` WHERE `status = 'active'` |
| `quarter` | — | `(year, quarter_number)` |
| `role_assignment` | — | `(profile_id, role, small_group_id, church_id, district_id, campo_id, union_id)` |
