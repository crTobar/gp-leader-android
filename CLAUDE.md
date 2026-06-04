# GP Leader — CLAUDE.md

App móvil Android para líderes de grupos pequeños SDA. Registra asistencia semanal, actividades y envía reportes al pastor. Incluye flujo de suplente (código 6 dígitos, 24h).

## Documentación completa

- @docs/design-system.md — colores, tipografía, sombras neumórficas, componentes UI
- @docs/architecture.md — stack, estructura de carpetas, roles, rutas NavGraph, convenciones
- @docs/database.md — tablas Supabase, RLS, auth, patrones de repositorio
- @docs/rules.md — reglas de negocio, flujos críticos, lo que NO hacer
- @docs/screens.md — estado actual de cada pantalla y pendientes

## Stack

| Capa | Tecnología |
|------|-----------|
| UI | Kotlin + Jetpack Compose |
| Estado | ViewModel + StateFlow |
| DB local | PowerSync (offline-first) |
| Backend | Supabase (PostgreSQL + Auth) |
| DI | Hilt |

## Comandos

```bash
./gradlew assembleDebug   # Build debug
./gradlew installDebug    # Instalar en dispositivo
./gradlew test            # Tests
./gradlew lint            # Lint
```

## Regla principal

**Estructura del wireframe + apariencia del style board SDA.**
Nunca inventar layouts nuevos ni ignorar el diseño visual neumórfico.
