# GP Leader — Pantallas, flujo y diseño

Documento de referencia para replicar el diseño y la navegación de la app.

**Última actualización:** 2026-05-25

---

## Historial de cambios

### 2026-05-25
- **MiembrosViewModel**: conectado a Supabase — datos reales, agregar/editar/archivar miembros
- **MiembrosListaScreen**: avatares con fondo `BackgroundDeep` + texto `Mid`; tarjetas con `neuElevated`; badge de sección con count en `Accent`; botón "+ Agregar" en `Accent`
- **MiembroDetalleScreen**: avatar en `Accent` (azul); botón "Editar" con fondo `Accent` + texto blanco; badge "Activo" en `Sage` (verde)
- **PerfilPrincipalScreen**: sección "MI CUENTA" eliminada; avatar en `Accent`; badge de miembros activos en `AccentLight` sobresaliendo del borde de la tarjeta; `SeccionCard` sin `clip` para permitir el overflow del badge
- **PerfilViewModel**: `totalMiembros` cargado desde `getMiembrosActivos()` real
- **HomeViewModel + SabadoCultoViewModel**: lógica del sábado usa `previousOrSame` — lunes a viernes muestra el sábado anterior; el borrador solo se crea el día que es sábado
- **docs/pantallas.md**: creado este documento

---

## Sistema de diseño

### Filosofía visual

**Neumorfismo suave** — sin bordes duros, sombras dobles (luz arriba-izquierda, sombra abajo-derecha). Fondo gris perla uniforme. Las tarjetas "flotan" (`neu-up`) o "se hunden" (`neu-in`) según su estado.

### Paleta de colores

| Token | Hex | Uso |
|-------|-----|-----|
| `Background` | `#ECEEF1` | Fondo general de todas las pantallas |
| `BackgroundDeep` | `#E0E3E9` | Fondos de secciones internas, avatares de miembros |
| `Shadow` | `#C2C8D4` | Sombra oscura neumórfica / divisores |
| `Light` | `#FFFFFF` | Sombra clara neumórfica |
| `Accent` | `#4A7FD4` | Azul principal — botones primarios, avatares, selección activa |
| `AccentLight` | `#6497E0` | Azul claro — badges de conteo, hover |
| `Gold` | `#C9A84C` | Dorado — actividades nivel Unión, badges oficiales |
| `Sage` | `#6AAB8E` | Verde — éxito, estado Activo, presentes |
| `Blush` | `#D4836A` | Naranja — alertas, ausentes, cerrar sesión |
| `Ink` | `#1E2733` | Texto principal |
| `Mid` | `#5A6577` | Texto secundario |
| `Muted` | `#9AA4B2` | Texto deshabilitado / hints |

### Tipografía

| Estilo | Fuente | Tamaño | Uso |
|--------|--------|--------|-----|
| `displayLarge` | Cormorant Garamond SemiBold | 40sp | Títulos hero |
| `headlineMedium` | Cormorant Garamond SemiBold | 28sp | Nombres, totales grandes |
| `titleLarge` | DM Sans SemiBold | 20sp | Filas de menú, labels de card |
| `bodyLarge` | DM Sans Regular | 16sp | Párrafos, descripciones |
| `bodyMedium` | DM Sans Regular | 14sp | Texto secundario |
| `labelSmall` | DM Mono Regular | 11sp + 2sp letterSpacing | Etiquetas, badges, caps |

### Radios de esquinas

| Token | Radio | Uso |
|-------|-------|-----|
| `extraLarge` | 28dp | Cards principales, secciones |
| `large` | 20dp | Cards internas, filas de miembro |
| `medium` | 14dp | Botones, avatares de perfil |
| `small` | 8dp | Chips, badges pequeños |
| `extraSmall` | 4dp | Tags muy pequeños |

### Sombras neumórficas

```
neu-up     → card flotante normal
neu-up-sm  → elemento pequeño flotante (botones, chips)
neu-in     → campo de entrada / botón presionado
neu-in-sm  → campo pequeño presionado
neu-glow   → botón primario con resplandor Accent
```

---

## Roles de usuario

```
LIDER    — cuenta permanente asignada por el pastor. Acceso completo.
SUPLENTE — sesión temporal vía código de 6 dígitos. Solo registra la reunión del día.
```

---

## Mapa de navegación

```
┌─────────────────────────────────────────────────────────────┐
│  AUTH GRAPH                                                 │
│  LoginScreen → (éxito con password cambiada) → QuienEres   │
│             → (primera vez)                  → CambiarContr.│
│             → "Ingresar como suplente"       → SuplementeGraph│
│                                                             │
│  SUPLENTE GRAPH                                             │
│  SuplementeCodigo → SuplementeBienvenida → Registro (paso1)│
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  LIDER GRAPH (bottom nav: INICIO / HISTORIAL / PERFIL)      │
│                                                             │
│  HOME ──────────────────────────────────────────────────── │
│    HomeScreen → "+ Registrar"       → RegistroGraph         │
│              → "Ver todas"          → HistorialScreen        │
│              → "Suplente"           → SheetCodigoSuplente   │
│              → tarjeta reunión      → DetalleReunionScreen  │
│                                                             │
│  HISTORIAL ─────────────────────────────────────────────── │
│    HistorialScreen → tarjeta reunión → DetalleReunionScreen │
│                  → "+ Registrar"    → RegistroGraph         │
│                                                             │
│  REGISTRO GRAPH ────────────────────────────────────────── │
│    Paso1 (Asistencia) → Paso2 (Actividades) → Paso3 (Resumen)│
│                       → AgregarActividadScreen              │
│                       → DetalleActividadScreen              │
│    Paso3 → (con red)  → ExitoEnviadoScreen                  │
│         → (sin red)   → ExitoOfflineScreen                  │
│                                                             │
│  PERFIL GRAPH ──────────────────────────────────────────── │
│    PerfilPrincipal → DatosPersonales                        │
│                   → CambiarContrasena                       │
│                   → DatosGrupo                              │
│                   → MiembrosGraph                           │
│                   → ActividadesListaScreen                  │
│                                                             │
│  MIEMBROS GRAPH ────────────────────────────────────────── │
│    MiembrosLista → MiembroDetalle → MiembroEditar           │
│                 → MiembroAgregar                            │
│                                                             │
│  ACTIVIDADES ───────────────────────────────────────────── │
│    ActividadesLista → ActividadHistorial                    │
└─────────────────────────────────────────────────────────────┘
```

---

## Pantallas — detalle

---

### LOGIN

**Título:** "Bienvenido" / subtítulo "Elige tu GP y luego accede como líder o suplente."

**Elementos:**
- 4 dropdowns en cascada con buscador interno: **Campo → Distrito → Iglesia → Grupo Pequeño**
  - Todos siempre habilitados (no dependen del superior para activarse)
  - Filtrado inteligente: seleccionar uno filtra los siguientes
  - GP muestra "nombre · Iglesia · Distrito · Campo"
- Campo de **contraseña** (campo neuInset, toggle show/hide)
- Botón primario **"Acceder"** (fondo Accent, neuGlow)
- Separador "o"
- Botón secundario **"Ingresar como suplente"** (fondo Background, neuElevated)
- Nota informativa en Muted: "Tu usuario sigue el formato que te dio tu pastor o administrador"

**Interacciones:**
- Login exitoso con contraseña ya cambiada → QuienEres
- Login exitoso primera vez → CambiarContrasena
- "Ingresar como suplente" → SuplementeGraph

---

### QUIEN ERES

Pantalla post-login para seleccionar quién está usando la app en este momento.

**Elementos:**
- Tarjeta del grupo (nombre, iglesia)
- Opción **Líder** (card neuElevated seleccionable)
- Opción **Miembro** (card neuElevated seleccionable, disponible solo sábados)
- Botón "Continuar"
- Nota si no es sábado: "El acceso de miembros está disponible el sábado"

---

### SUPLENTE — CÓDIGO

**Elementos:**
- Instrucción: "Ingresa el código de 6 dígitos que te compartió tu líder."
- 6 cajas neuInset para cada dígito (teclado numérico)
- Animación shake en error
- Validación automática al completar los 6 dígitos
- Spinner "Validando…" mientras verifica

---

### SUPLENTE — BIENVENIDA

**Elementos:**
- Hero Ink 35% de altura con ícono/título "¡Bienvenido! · Modo suplente activado"
- Card con datos del grupo: nombre del GP, nombre del líder
- Nota en Gold (dashed border): "El líder recibirá una copia del registro para revisarlo"
- Campo texto **"TU NOMBRE COMPLETO"** (neuInset) — aparecerá en el reporte
- Botón primario **"Comenzar registro"**

---

### HOME

Bottom nav compartido: **INICIO · HISTORIAL · PERFIL** (iconos + label DM Mono, activo en Accent)

**Header:**
- Saludo: "Hola, [nombre]"
- Botón **"Suplente"** (top-right, chip neuElevatedSm)
- Botón **"+ Registrar"** (Accent, neuGlow)

**Card del grupo** (neuElevated, extraLarge):
- Nombre e iglesia del GP
- Stats del período actual en chips horizontales:
  - ASISTENCIA · PRESENTES · AUSENTES · JUSTIFICAR · PROMEDIO

**Sección "REUNIONES RECIENTES"** (labelSmall Muted):
- Máximo 2 tarjetas de reunión recientes
- Cada tarjeta: fecha · estado (badge) · conteo P/A/J
  - Badges: `Enviada` (Sage) · `Pendiente` (Gold) · `Borrador` (Muted)
- Botón "Ver todas las reuniones →" (si hay más de 2)

**Estado vacío:** título "Aún no hay reuniones registradas. ¡Registra la primera!"

---

### SHEET — CÓDIGO SUPLENTE

Bottom sheet accesible desde el botón "Suplente" en Home.

**Elementos:**
- Título + subtítulo: "Válido por 24 horas"
- 6 cajas Ink (neuElevated) con los dígitos del código
- Barra de vigencia: Sage (mucho tiempo) → Gold (poco) → Blush (por vencer)
- Nota con borde dashed: "Permite registrar una sola reunión. Expira en 24h o al revocar."
- Botones: **Compartir código** · **Generar nuevo código** · **Revocar código** (Blush)
- AlertDialog de confirmación al revocar

---

### REGISTRO — PASO 1 (Asistencia)

**Header:** "Paso 1 de 3 · Fecha y asistencia" + badge "1/3"
**Banner suplente** (si aplica): barra negra "MODO SUPLENTE" en la parte superior

**Elementos:**
- Fila FECHA DE REUNIÓN: fecha auto-llenada con hoy + botón ✎ para editar
- Botón **"No hubo reunión"** → modal de confirmación → envía reporte vacío

**Sección MIEMBROS:**
- Selector rápido "Sel. todos" con opciones P / A / J
- Lista de miembros (cada uno en tarjeta neuElevated):
  - Avatar cuadrado con iniciales (fondo Accent)
  - Nombre completo
  - Toggle 3 estados: **P** (Sage) · **A** (Blush) · **J** (Gold)
- Si todos = Ausente al continuar → modal de confirmación

**Sección VISITAS ANTERIORES** (colapsable, conteo en badge):
- Misma UI que miembros, badge "Visita"
- Botón "+ Agregar" por sección

**Agregar visita nueva:**
- Campo de texto con dropdown hacia ARRIBA con coincidencias de visitas anteriores
- Nueva visita → se marca como Presente automáticamente (sin toggle)

**Botón:** "Continuar" (Accent, neuGlow)

---

### REGISTRO — PASO 2 (Actividades)

**Header:** "Paso 2 de 3 · Actividades" + badge "2/3"

**Actividades agrupadas por nivel:**

| Nivel | Color header | Editable |
|-------|-------------|---------|
| UNIÓN | Gold | No — 🔒 solo lectura |
| PASTOR | Ink | Sí |
| MI GP | Accent | Sí |

**Cada actividad** (tarjeta neuElevated):
- Nombre de la actividad
- Badge de nivel (Gold/Ink/Accent)
- Tipo de marcador:
  - **Contador** → campo numérico, inicia en `—` (no en 0)
  - **Checkbox** → toggle marcado/desmarcado
  - **Monto ₡** → campo numérico con prefijo ₡ en Gold
  - **Participantes** → campo numérico con desglose opcional por miembro
- Actividades de Unión: ícono 🔒, no editables

**Botón** "+ Agregar actividad extra" → AgregarActividadScreen (solo nivel Mi GP)

**Validación:** si hay actividad obligatoria en `—` → error en Blush al intentar continuar

**Botón:** "Continuar" (Accent, neuGlow)

---

### AGREGAR ACTIVIDAD (pantalla completa)

**Título:** "Nueva Actividad"

**Elementos:**
- Campo **Nombre de la actividad** (BasicTextField, neuInsetSm)
- Chips horizontales scroll **Tipo de marcador**: Contador · Completado · Monto ₡ · Participantes
- Preview visual del tipo seleccionado
- Si monetario: campo **Monto ₡** grande centrado (titleLarge, prefijo en Gold)
- Campo **Unidad** (personas / visitas / sesiones / otro)
- Toggle **Desglose por miembro** (si aplica)
- Sección **PERÍODO** (opcional):
  - Inicio: DatePickerDialog, default hoy, sin botón limpiar
  - Vencimiento: DatePickerDialog, default vacío, con botón limpiar
- Botón **"Guardar actividad"** (Accent, neuGlow)

---

### REGISTRO — PASO 3 (Resumen)

**Header:** "Paso 3 de 3 · Resumen" + badge "3/3"

**Card resumen (neuElevated):**
- Nombre del GP + fecha
- Stats chips: Presentes · Ausentes · Justificados · Visitas · % Asistencia
- Sección ACTIVIDADES: lista por nivel (Unión → Pastor → Mi GP)
  - Cada actividad: nombre + valor registrado
  - Sin actividades: "Sin actividades registradas"

**Botones:**
- **"Enviar al pastor"** (Accent, neuGlow) — si hay conexión
- **"Guardar como borrador"** — si sin conexión → ExitoOfflineScreen
- **"← Editar actividades"** → vuelve al paso 2

---

### ÉXITO — ENVIADO

**Hero negro** (35% de pantalla):
- "¡Reporte enviado!" + timestamp "Enviado hoy, HH:mm"

**Card pastor:**
- Nombre del pastor + rol e iglesia

**Card resumen stats:**
- Presentes · Ausentes · Justificados · Asistencia % · Visitas

**Botones:**
- "Volver al inicio"
- "Ver historial"

---

### ÉXITO — OFFLINE

**Hero gris:**
- "Reporte guardado" + "Sin conexión. Se enviará automáticamente al recuperar señal."

**Card estado:**
- Badge "Pendiente de envío" + "Esperando conexión"
- Nota tranquilizadora en Muted

**Botón:** "Volver al inicio"

---

### HISTORIAL

**Header:** título "Historial" + botón "+ Registrar reunión"

**Tabs de trimestre** (horizontalScroll):
- 1er Trim · 2do Trim · 3er Trim · 4to Trim · Ver todo
- Tab activo: texto Accent + subrayado Accent

**Stats del período** (4 chips horizontales):
- PROMEDIO · REUNIONES · ENVIADAS · PENDIENTE

**Lista de reuniones** (cada una en tarjeta neuElevated):
- Fecha (día bold + mes/año labelSmall)
- Título "Reunión de GP"
- Badge estado: Enviada (Sage) · Pendiente (Gold) · Borrador (Muted)
- Conteo: `3P · 1A · 0J · 75%`
- Acción deslizar: "Editar"

---

### DETALLE REUNIÓN

**Header:** "← Reunión [fecha]"

**Stats 4 chips** (P / A / J / %):
- Presentes (Sage) · Ausentes (Blush) · Justificados (Gold) · Porcentaje

**Card Asistencia** (neuElevated):
- Lista de miembros con badge P/A/J a la derecha

**Card Actividades** por nivel:
- Sección UNIÓN / PASTOR / MI GP
- Cada fila: nombre de actividad + valor registrado

---

### MIEMBROS — LISTA

**Header:** "Miembros" + botón **"+ Agregar"** (Accent, neuElevatedSm)

**Buscador** (neuInset, full width): "Buscar miembro…"

**Sección ACTIVOS** — separador con label + badge count (AccentLight):
- Cada miembro en tarjeta neuElevated (cornerRadius 20dp):
  - Avatar cuadrado con iniciales (fondo BackgroundDeep, texto Mid)
  - Nombre completo (titleLarge, Ink)
  - Punto verde Sage si activo
  - Flecha derecha (Muted)
  - Deslizar izquierda → "Archivar" (Blush)

**Sección ARCHIVADOS** — separador con badge count:
- Misma UI, sin punto de estado
- Deslizar izquierda → "Activar" (Sage)

---

### MIEMBRO — DETALLE

**Header:** "← Detalle miembro" + botón **"Editar"** (fondo Accent, texto blanco)

**Hero card** (neuElevated, centrada):
- Avatar cuadrado grande con iniciales (fondo Accent, texto blanco)
- Nombre completo (headlineMedium, serif)
- "Miembro desde [Mes Año]" (bodyMedium, Muted)
- Badge estado: **Activo** (Sage) · **Archivado** (Muted)

**Card Contacto** (neuElevated):
- Teléfono · Correo · Dirección
- Campos vacíos muestran "—" en Muted

**Card Historial de Asistencia** (neuElevated):
- Lista de últimas reuniones
- Cada fila: fecha + badge P (Sage) / A (Blush) / J (Gold)

---

### MIEMBRO — EDITAR

**Header:** "← Editar miembro" + botón **"Guardar"** (Accent)

**Sección NOMBRE** (neuElevated):
- Primer nombre * (campo neuInset)
- Primer apellido * (campo neuInset)
- Expandible "+ Segundo nombre (opcional)"
- Expandible "+ Segundo apellido (opcional)"

**Sección CONTACTO** (neuElevated):
- Teléfono (+506 prefijo) · Correo · Dirección

**Sección ESTADO** (neuElevated):
- Toggle **Activo / Archivado** (neuInset cuando inactivo, neuElevated cuando activo)
- ⚠️ Solo aparece al EDITAR, nunca al agregar

**Card Historial de Asistencia** (igual que detalle)

---

### MIEMBRO — AGREGAR

**Header:** "← Agregar miembro"

**Avatar animado** (arriba, centrado):
- Estado inicial: borde dashed, ícono +
- Al escribir nombre: aparecen iniciales con fondo Accent

**Formulario** (igual que Editar pero SIN toggle de estado)

**Nota** (fondo BackgroundDeep, borde Sage dashed):
- "El miembro se agregará como Activo automáticamente"

**Botón:** "+ Agregar al grupo" (Accent, neuGlow)

---

### PERFIL — PRINCIPAL

**Avatar card** (neuElevated):
- Avatar cuadrado con iniciales (fondo Accent, texto blanco)
- Nombre completo (headlineMedium, serif)
- Rol: "Líder de grupo pequeño" (Mid)
- Nombre del GP (Accent)
- Botón ✎ editar (top-right, neuElevatedSm, Muted)

**Sección MI GRUPO** (SeccionCard neuElevated, cornerRadius 28dp):
- **Editar información de miembros** → MiembrosGraph
  - Badge count de miembros activos (AccentLight) sobresaliendo del borde superior-derecho
- **Editar datos del grupo pequeño** → DatosGrupo
- **Registro de actividad** → (dev)
- **Reportes** → (dev)
- **Actividades (dev)** → ActividadesLista

**Sección PREFERENCIAS:**
- **Cerrar sesión** (texto Blush) → AlertDialog de confirmación

---

### PERFIL — DATOS PERSONALES

**Sección NOMBRE** (neuElevated):
- Primer nombre * · Primer apellido *
- Expandibles para segundo nombre y segundo apellido

**Sección CONTACTO:**
- Teléfono (prefijo +506) · Correo (nota "Usado para iniciar sesión")

**Sección IGLESIA** (readonly):
- Campo deshabilitado + nota "Contacta a tu pastor para cambiar esto"

**Botón:** "Guardar cambios" (Accent, neuGlow)

---

### PERFIL — CAMBIAR CONTRASEÑA

**Hero:** "Nueva contraseña" + subtítulo explicativo

**Campos** (neuInset, toggle show/hide):
- Contraseña actual *
- Nueva contraseña *
- Confirmar nueva contraseña *

**Checklist** (labelSmall, DM Mono) — se activa en Sage al cumplirse:
- ✓ Al menos 8 caracteres
- ✓ Una letra mayúscula
- ✓ Un número
- ✓ Las contraseñas coinciden

**Botón:** "Actualizar contraseña" (Accent, neuGlow, habilitado solo si checklist completo)

---

### PERFIL — DATOS DEL GRUPO

**Sección IDENTIFICACIÓN** (neuElevated):
- Nombre del grupo * (editable)
- Descripción · Lugar de reunión · Canto favorito · Versículo favorito · Personaje bíblico

**Sección UBICACIÓN ECLESIÁSTICA** (readonly):
- Iglesia · Campo · Distrito
- Nota: "Contacta a tu pastor para cambiar la ubicación eclesiástica"

**Sección HORARIO** (dropdowns):
- Día de la semana · Hora inicio · Hora fin

**Botón:** "Guardar cambios" (Accent, neuGlow)

---

### ACTIVIDADES — LISTA

**Filtros en NeuCard:**
- Row 1 — Nivel (horizontalScroll): Todo · Unión · Pastor · Mi GP
- Divider
- Row 2 — Estado: Todas · Activas · Inactivas

**Cada actividad** (NeuCard):
- Nombre (titleLarge)
- Badge nivel: Gold=Unión · Ink=Pastor · Accent=Mi GP
- Badge estado: Sage=Activa · Blush=Vencida
- Total acumulado (headlineMedium, color del nivel)
  - Formato: `₡12000` (monetario) · `8 semanas` (checkbox) · `45 personas` (otros)

---

### ACTIVIDAD — HISTORIAL

**Card resumen** (neuElevated):
- Nombre de la actividad (headlineMedium, serif)
- 3 stats: total acumulado · promedio · semanas activas

**Lista de registros semanales:**
- Cada fila: bloque de fecha (día bold + "MMM yyyy") + chip valor
  - Accent = contador/participantes con valor
  - Sage = checkbox completado ✓
  - BackgroundDeep = valor 0 o vacío
- Tap en fila → modo edición inline: campo numérico + botones Cancelar / Guardar

---

## Componentes reutilizables

| Componente | Descripción |
|-----------|-------------|
| `NeuCard` | Card neumórfica elevada, cornerRadius 28dp por defecto |
| `NeuButtonPrimary` | Botón fondo Accent + neuGlow, texto blanco |
| `NeuButtonSecondary` | Botón fondo Background + neuElevated, texto Ink |
| `NeuTextField` | Campo con estados: normal / error (Blush) / success (Sage) / readOnly |
| `SwipeableItem` | Fila con acción al deslizar, clip para el ripple |
| `BadgeNumero` | Chip redondeado AccentLight con número, sobresale del borde de la card |
| `HorizontalDivider` | Línea Shadow 1dp entre filas de una SeccionCard |

---

## Reglas de UX importantes

- **Contador de actividades:** siempre inicia en `—`, nunca en `0`
- **Toggle Activo/Archivado:** solo en formulario de **editar** miembro, nunca al agregar
- **Visita nueva** en asistencia: se marca como Presente automáticamente
- **Dropdown de visitas anteriores:** se despliega hacia ARRIBA (sobre el campo)
- **Actividades Unión:** solo lectura para el líder (ícono 🔒)
- **Sábado:** la sesión del culto usa el sábado anterior (lunes–viernes muestran el sábado pasado; el sábado actual solo se abre ese mismo día)
- **Suplente:** su registro va al líder para aprobar antes de llegar al pastor
