# GP Leader — Brief de diseño (contexto para Claude)

> **Cómo usar este archivo:** adjúntalo o pégalo al empezar una conversación de diseño.
> Es autocontenido: describe la app, su sistema visual y — lo importante — **cómo se aplica** en
> pantallas reales. Referencia visual navegable (renderizada): el artifact "GP Leader · Sistema de diseño".
> Al pedir una pantalla, indicá **cuál**, **para qué rol** y **qué elementos** debe tener.

---

## 1. Qué es la app

App móvil Android (Kotlin + Jetpack Compose) para **líderes de grupos pequeños** de la Iglesia
Adventista (SDA). Registran asistencia semanal, actividades misioneras y aportes de los miembros, y
envían reportes al pastor. Hay flujo de **suplente** (código de 6 dígitos, 24h) y roles superiores.
Interfaz en **español**. Moneda: colón (`₡`).

**Roles:** Líder (acceso completo) · Suplente (solo registra, banner negro "Modo suplente") ·
Miembro (sus actividades + dúo) · Líder de iglesia / niveles superiores (aprueban aportes, ven agregados).

---

## 2. Regla de oro

**Estructura del wireframe + apariencia del style board SDA.** Los wireframes definen *qué* elementos
hay y *dónde*; el style board define *cómo se ven*. Nunca inventar layouts nuevos ni romper el
lenguaje neumórfico.

---

## 3. Tipo de diseño: neumorfismo suave

Sin bordes duros. Fondo gris perla uniforme del que los elementos **flotan** (sombra clara
arriba-izquierda + oscura abajo-derecha) o se **hunden** (mismas sombras, por dentro). El estado se
comunica con elevación:

- **Flotante** → interactivo / normal / "sobresale".
- **Hundido** → pasivo / seleccionado / solo lectura.

Contraste típico de la app: una **pill elevada que sobresale** encima de una **tarjeta de datos
hundida** (ver §8, Registro Paso 3). Es la firma visual del app.

> Es **light-only por diseño**: el neumorfismo depende del fondo claro; no hay modo oscuro.

---

## 4. Paleta (hex exactos)

```
Fondos      Background #ECEEF1 · BackgroundDeep #E0E3E9 (secciones, track de progreso)
Sombras     Shadow #C2C8D4 (oscura, abajo-dcha) · Light #FFFFFF (clara, arriba-izq)
Acento      Accent #4A7FD4 · AccentLight #6497E0
Semánticos  Gold #C9A84C · Sage #6AAB8E · Blush #D4836A · Violet #7B61C4
            verde-vivo #2EA86A (check "ya registraste", ≠ Sage)
Texto       Ink #1E2733 · Mid #5A6577 · Muted #9AA4B2
```

**Semántica:** Sage = presente/éxito/aprobado · Blush = ausente/alerta/rechazado/offline ·
Gold = justificado/oficial · Violet = culto de sábado.
**Color por nivel de actividad:** `Unión → Gold` · `Pastor → Ink` · `Mi GP → Accent`.

---

## 5. Tipografía

Tres familias bundleadas (no Google Fonts): **Cormorant Garamond** (serif, títulos + números
destacados) · **DM Sans** (cuerpo/UI) · **DM Mono** (etiquetas MAYÚS, tracking 2).

| Rol | Familia | Tamaño/peso | Uso |
|-----|---------|-------------|-----|
| displayLarge | Cormorant | 40sp SemiBold · lh48 | Hero (nombre de grupo sube a 38sp Bold, 2 líneas) |
| headlineMedium | Cormorant | 28sp SemiBold · lh36 | Título de pantalla |
| headlineSmall | Cormorant | 24sp SemiBold · lh32 | Números/stats, texto italic de cards |
| titleLarge | DM Sans | 20sp SemiBold · lh28 | Encabezado de card |
| titleMedium | DM Sans | 16sp Medium · lh24 | Subtítulo |
| bodyLarge | DM Sans | 16sp · lh24 | Cuerpo, filas de lista |
| bodyMedium | DM Sans | 14sp · lh20 | Cuerpo secundario |
| bodySmall | DM Sans | 12sp · lh16 | Captions |
| labelSmall | DM Mono | 11sp · tracking 2 · UPPER · Muted | Eyebrows (FECHA, HOY, NIVEL…) |

---

## 6. Sombras (recetas)

```
neuElevated    card flotante · radio 28dp   (blur ~20, offset 8)
neuElevatedSm  pill pequeña   · radio 14dp   (blur ~12, offset 5)
neuInset       campo hundido con sombra EXTERNA · radio 14dp
neuInsetSm     campo pequeño hundido · radio 8dp
neuInsetInner  hundido SIN sombra exterior (gradientes por encima) → íconos, avatares, cards read-only
neuGlow        botón Accent con halo azul rgba(74,127,212,.25)
```
CSS aproximado para mockups:
```css
elevado:  8px 8px 20px #C2C8D4, -8px -8px 20px #fff;
pequeño:  5px 5px 12px #C2C8D4, -5px -5px 12px #fff;
hundido:  inset 6px 6px 12px #C2C8D4, inset -6px -6px 12px #fff;
glow:     0 0 30px rgba(74,127,212,.25), 8px 8px 20px #C2C8D4, -8px -8px 20px #fff;
```
> Orden crítico del efecto hundido-sin-sombra: `clip → background → neuInsetInner → padding`.

**Radios:** 28dp cards principales · 20dp cards internas · 14dp botones/avatares · 8dp chips · 4dp tags.

---

## 7. Componentes (existen — reusar, no reinventar)

`core/ui/components/`: **NeuCard** (elevada / hundida "in") · **NeuButtonPrimary** (Accent+glow,
`enabled`) · **NeuButtonSecondary** (Background+elevated) · **NeuTextField**
(isError/isSuccess/readOnly/isPassword/leadingContent) · **NeuAvatar** (circular hundido, iniciales
Cormorant Bold color Mid) · **AppBottomNavBar** (Inicio·Actividades·Perfil) · **OfflineBanner**
(Blush 12%) · **SwipeableItem** · **Skeleton**.

Patrones: chips de filtro en **scroll horizontal** (activo = Accent+blanco) · **pill de nivel** con
franja de color a la izquierda + badge de conteo · **eyebrow** DM Mono sobre el título · **stat tile**
(número Cormorant + label singular/plural, gris si es 0).

---

## 8. Recetas de pantallas clave (composición real con medidas)

### Home del líder
1. **HomeHeader** — eyebrow fecha (DM Mono 11sp Muted) · **"Grupo" / NombreGrupo** en dos `Text`
   Cormorant **38sp Bold** lh40 · subtítulo "N miembros · reunión semanal" DM Sans 15sp Mid.
2. **RegistrarCard** (NeuCard, padding `26/30`) — eyebrow "HOY" (Accent) · texto italic Cormorant 24sp
   Medium · botón Accent + neuGlow radio 14dp, "✓ Tomar Asistencia" 17sp SemiBold blanco.
3. **YaRegistrasteBadge** (reemplaza RegistrarCard si ya registró) — tarjeta **hundida** (neuInsetInner,
   sin sombra externa, clip 24dp) · círculo check 44dp **#2EA86A** blanco · título 16sp Bold · sub 13sp Mid.
4. Cards de acceso: Actividades misioneras · Aprobaciones (con badge de pendientes) · Ver historial
   — ícono **hundido** (neuInsetInner) + título + subtítulo + chevron.

### Registro Paso 2 (actividades por nivel)
- Secciones agrupadas Unión → Pastor → Mi GP. Cada actividad es una **NeuCard individual**: ícono de
  tipo + nombre + chip de valor (`—` vacío o total) + chevron (o 🔒 si es de solo lectura para el líder).
- Unión bloqueada para el líder; Pastor/GP editables. Toda edición numérica ocurre en pantalla aparte.

### Registro Paso 3 (resumen)
- **ResumenCard** (neuElevated 28dp): nombre de grupo + FECHA + **MIEMBROS** = 3 stat tiles
  (Presentes/Ausentes/Justificados, número Cormorant, gris si 0) + barra de asistencia.
- **Secciones por nivel** = firma visual: **pill de nivel elevada** (neuElevatedSm, sobresale con
  sangría 12dp, franja de color 3dp + conteo) **por fuera** + **tarjeta de datos hundida**
  (neuInsetInner) con franja lateral 4dp y tinte 5% del color de nivel. Filas nombre · valor
  (color del nivel, o Muted si es 0).

### Lista de actividades
- Filtros nivel/estado en **dos botones desplegables** neumórficos lado a lado (botón elevado
  `neuElevatedSm` 44dp con punto de color + chevron; menú en Popup con opción hundida + check Accent)
  + switch **GP / Dúo misionero** arriba.
- Card por actividad: ícono de tipo (hundido) + nombre + **NivelBadge** + valor formateado
  (`₡` monto · "X días/semanas" checkbox · "N unidad" contador). El color del valor = color del nivel.

### Aprobaciones (líder)
- Cards de acceso Historial + Movimientos (comparten un AtajoCard) · lista "MONTOS POR APROBAR"
  agrupada por actividad, cada fila con botones **✗ Rechazar** (Blush) / **✓ Aprobar suma** (Sage).
  Sin conexión los botones se reemplazan por "Necesitas conexión para aprobar o rechazar".

---

## 9. Convenciones que afectan el diseño

- **Cero se muestra como "—"** en Muted, nunca `0`.
- **Singular/plural** según el número (1→"Presente", 0/2+→"Presentes").
- **Offline es solo lectura**: los controles de escritura se deshabilitan y muestran "Necesitas conexión".
- **Tres estados por pantalla**: cargando (Skeleton), vacío (mensaje + guía, distingue "sin datos" de
  "sin conexión") y con datos.
- Móvil vertical; contenido ancho hace scroll dentro de su contenedor, el body nunca scrollea horizontal.
- `PaddingValues` siempre con direcciones individuales (`start/end/bottom`, no `horizontal=`).

---

## 10. Mapa de pantallas

```
AUTH      Login (dropdowns campo→distrito→iglesia→GP) · ¿Quién eres? · Suplente (código · bienvenida)
HOME      Header · RegistrarCard · Actividades misioneras · Aprobaciones · Ver historial
REGISTRO  Paso 1 asistencia P/A/J + visitas → Paso 2 actividades por nivel → Paso 3 resumen → envío
HISTORIAL Tabs por trimestre + stats + reuniones · Detalle reunión
MIEMBROS  Lista (activos/archivados) · detalle · editar · agregar
ACTIVIDADES  Lista (filtros + switch GP/Dúo) · historial semanal · Aportes (aprobaciones · historial · movimientos)
DÚOS      Landing · lista · crear · detalle (actividades / estudios bíblicos)
PERFIL    Principal · datos personales · cambiar contraseña · datos del grupo
```
Bottom nav (líder): **Inicio · Actividades · Perfil**. Historial se abre desde una card del Home.

---

## 11. Al pedir un diseño, incluí

1. **Pantalla** y su propósito.  2. **Rol** (líder/miembro/suplente/iglesia).
3. **Elementos** y su jerarquía.  4. Si es **nueva** o **rediseño**.
5. Formato: specs, o **artifact HTML navegable** para verla.
