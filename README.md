# Presencia

A cross-platform mobile app for Seventh-day Adventist small-group leaders to record attendance, track activities, and report data up the church hierarchy.

## What it does

Presencia streamlines the weekly small-group reporting process. Leaders take attendance on their phone during or after a meeting, log activity counts, and submit the report — which flows up the org hierarchy:

```
Small Group → Church → District → Campo → Union
```

## Platforms

| Platform | Folder | Stack |
|---|---|---|
| iOS | [`ios/`](./ios/) | SwiftUI · iOS 18+ · Swift async/await |
| Android | [`android/`](./android/) | Jetpack Compose · Kotlin · Coroutines |

Both apps share the same Supabase backend and the same feature set.

## Features

- **Attendance tracking** — 3-state per member (Present / Absent / Justified) with swipe gestures and bulk actions
- **Meeting registration** — Step-by-step wizard: Attendance → Activities → Summary, with draft save and submit to pastor
- **Draft meetings** — Save in progress and come back; submitted meetings are editable within 7 days
- **Duplicate submission guard** — Prevents two leaders from submitting separate reports for the same date
- **Activity log** — Per-group audit trail of actions (meetings submitted, members added/archived/restored)
- **Meeting history** — Browse past meetings with status badges (draft / submitted / approved)
- **Member management** — Add, view, archive, and restore members
- **Reports & charts** — Attendance trends, activity breakdowns, and group rankings
- **Deputy submission** — Unauthenticated deputy records attendance on behalf of the leader
- **Role-based UI** — Different capabilities for leaders, church admins, campo admins, and union overseers
- **Admin panel** — Manage churches, districts, users, and reporting periods
- **Hierarchical login** — Sign in by picking Unión → Campo → Iglesia → Grupo Pequeño + password (no email needed)

## Backend

Both apps connect to the same **Supabase** project (PostgreSQL + Auth + Row Level Security).

Key tables: `union_org`, `campo`, `district`, `church`, `small_group`, `profile`, `role_assignment`, `member`, `meeting`, `attendance`, `activity_type`, `activity_record`, `activity_log`, `reporting_period`, `deputy_submission`.

Credentials are never committed — each platform reads from a local config file (see platform-specific setup below).

## Repository structure

```
PresenciaApp/
├── android/          # Kotlin / Jetpack Compose Android app
│   ├── app/
│   ├── build.gradle.kts
│   └── ...
├── ios/              # SwiftUI iOS app
│   ├── Presencia/
│   ├── Presencia.xcodeproj
│   ├── project.yml   # XcodeGen spec
│   └── ...
└── README.md
```

## Getting started

### iOS

Requirements: Xcode 16+, iOS 18+ target, [`xcodegen`](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`).

```bash
cd ios
cp Presencia/Config.example.swift Presencia/Config.swift
# Fill in supabaseURL and supabaseAnonKey in Config.swift
xcodegen generate
open Presencia.xcodeproj
```

See [`ios/README.md`](./ios/README.md) for full setup details.

### Android

Requirements: Android Studio Hedgehog+, JDK 17+.

```bash
cd android
# Add your Supabase credentials to local.properties or the relevant config file
```

Open the `android/` folder in Android Studio and run on a device or emulator.

## Design system

Both apps use a **neumorphic** design language — soft raised/inset shadows on a muted gray background (`#E8EDF2`). Shared design tokens:

| Token | Value |
|---|---|
| Background | `#E8EDF2` |
| Accent | `#3D6BBF` |
| Text primary | `#1a2130` |
| Present (green) | `#4CAF7D` |
| Absent (red/blush) | soft blush |
| Justified (amber) | warm amber |

Typography uses **DM Sans** (rounded, sans-serif only — no serif fonts in the UI).

## Contributing

1. Branch off `main`
2. Work inside `ios/` or `android/` — changes to one platform don't affect the other
3. Keep all UI text in **Spanish**; all code, variable names, and comments in **English**
4. Never commit `Config.swift` (iOS) or credentials files (Android)
