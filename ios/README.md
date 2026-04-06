# Presencia

A native iOS app for Seventh-day Adventist small-group leaders to record attendance, track activities, and report data up the church hierarchy.

## Overview

Presencia streamlines the weekly small-group reporting process. Leaders take attendance on their phone during or after a meeting, log activity counts, and submit the report — which flows up from Small Group → Church → District → Campo → Union.

## Features

- **Attendance tracking** — 3-state toggle per member (Present / Absent / Justified) with swipe-to-justify gesture and one-tap bulk actions
- **Activity logging** — Dynamic activity types (Union / Pastor / Mi GP levels) with count tracking, notes, and custom extra activities per meeting
- **Meeting registration** — 3-step wizard: Attendance → Activities → Summary, with draft save and submit to pastor
- **Draft meetings** — Save a meeting as a draft and edit it any time; submitted meetings are editable within 7 days
- **Duplicate submission guard** — Blocks a second submission for the same date; surfaces a clear message when another leader has already submitted
- **Activity log** — Per-group audit trail of actions (meetings submitted, members added/archived/restored)
- **Meeting history** — Browse past meetings with status badges (draft / submitted / approved) and full detail view
- **Member management** — Add, view, archive, and restore small group members; actions are logged to the activity log
- **Reports & charts** — Attendance trends, activity breakdowns, and group rankings via Swift Charts
- **Deputy submission** — Unauthenticated deputy submits attendance on behalf of the leader; leader reviews and approves
- **Role-based UI** — Different capabilities for leaders, church admins, campo admins, and district overseers
- **Admin panel** — Manage churches, districts, users, and reporting periods
- **Hierarchical login** — Leaders sign in by choosing Unión → Campo → Iglesia → Grupo pequeño and a password; the app builds the login identifier from those selections (Supabase Auth still uses a synthetic `@login.presencia.app` address under the hood)

## Tech Stack

| Layer | Technology |
|---|---|
| UI | SwiftUI (iOS 18+) |
| State | `@Observable` (Swift Observation framework) |
| Backend | Supabase (PostgreSQL + Auth + RLS) |
| Networking | supabase-swift 2.x via SPM |
| Charts | Swift Charts |
| Project gen | xcodegen |
| Concurrency | Swift async/await |

## Requirements

- Xcode 16+
- iOS 18.0+ deployment target
- [xcodegen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)
- A Supabase project with the schema deployed

## Setup

**1. Clone the repo**

```bash
git clone <repo-url>
cd "Presencia App/Presencia"
```

**2. Add your Supabase credentials**

Copy the example config and fill in your project URL and anon key:

```bash
cp Presencia/Config.example.swift Presencia/Config.swift
```

Edit `Presencia/Config.swift`:

```swift
enum Config {
    static let supabaseURL = "https://YOUR-PROJECT-ID.supabase.co"
    static let supabaseAnonKey = "YOUR-ANON-KEY"
}
```

`Config.swift` is gitignored and never committed.

**3. Generate the Xcode project**

```bash
xcodegen generate
```

**4. Open and run**

```bash
open Presencia.xcodeproj
```

Select a simulator and press Run.

**5. Supabase SQL (development)**

From the repo root (`Presencia App/`), the `Design/` folder holds SQL you can run in the Supabase SQL Editor after your main schema (DDL) is deployed:

| File | Purpose |
|------|---------|
| `Design/supabase_login_hierarchy.sql` | RLS / policies so the **anon** role can `SELECT` org tables (`union_org`, `campo`, `district`, `church`, `small_group`) for the login pickers |
| `Design/supabase_seed.sql` | Reference data, demo meeting, and a demo leader user — **safe to re-run** (`ON CONFLICT` upserts; demo attendance rows for the sample meeting are reset) |

Paths relative to the Xcode project directory (`Presencia/`): `../Design/supabase_login_hierarchy.sql` and `../Design/supabase_seed.sql`.

**`seed.sql` (project root)**

A test seed at `Presencia/seed.sql` creates the `activity_log` table with RLS and seeds sample log entries + activity records against your existing real group/members. Run it once in the Supabase SQL Editor to populate test data. Re-running is safe for `activity_record` (uses `ON CONFLICT DO UPDATE`); `activity_log` entries will duplicate on re-run — prepend `TRUNCATE activity_log;` if you want a clean slate.

### Demo login (after seed)

In the app, pick **Unión Centro Norte → Campo Central → Iglesia Central → GP Los Olivos**, then password:

- **Password:** `PresenciaDemo123!`

Users do not type an email. The seed creates the matching Auth user as `ucn-cat-icb-gp-los-olivos@login.presencia.app` (same string the app builds from Unión **UCN** → Campo **CAT** → Iglesia **ICB** → **GP Los Olivos**).

## Project Structure

```
Presencia/
├── PresenciaApp.swift              # Entry point, auth state machine
├── Config.swift                    # Supabase credentials (gitignored)
├── Config.example.swift            # Credentials template
├── Models/
│   ├── Enums.swift                 # UserRole, MeetingStatus, AttendanceStatus, etc.
│   ├── Organization.swift          # Union, Campo, District, Church, SmallGroup
│   ├── Profile.swift               # App users + RoleAssignment
│   ├── Member.swift                # Small group attendees (no login)
│   ├── Meeting.swift               # Meeting + Attendance
│   ├── Activity.swift              # ActivityType + ActivityRecord
│   ├── Deputy.swift                # Deputy submission flow
│   └── Reporting.swift             # Report DTOs
├── Services/
│   ├── Protocols/                  # AuthService, HierarchyService, MemberService,
│   │                               # MeetingService, AttendanceService, ActivityService,
│   │                               # ActivityLogService, ReportService, DeputyService
│   ├── Supabase/                   # Live implementations (Auth, Hierarchy, members, meetings, …)
│   ├── Mocks/                      # MockData + mock implementations for previews
│   └── ServiceContainer.swift      # DI container, injected via @Environment
├── Features/
│   ├── Auth/                       # LoginView (hierarchy pickers + password), deputy code entry
│   ├── Home/                       # HomeView + HomeViewModel (trend chart)
│   ├── ActivityLog/                # ActivityLogView + ActivityLogViewModel
│   ├── Meetings/
│   │   ├── MeetingListView + ViewModel
│   │   ├── MeetingCardView
│   │   ├── MeetingDetailView
│   │   └── NewMeeting/             # 3-step wizard (Attendance → Activities → Summary):
│   │       ├── MeetingStep2_AttendanceView
│   │       ├── MeetingStep3_ActivitiesView
│   │       ├── MeetingStep4_SummaryView  # submit or save as draft
│   │       ├── NewMeetingView
│   │       └── NewMeetingViewModel
│   ├── Members/
│   │   ├── MemberListView + ViewModel
│   │   ├── MemberDetailView
│   │   └── MemberFormView
│   ├── Reports/
│   │   ├── ReportsView + ReportsViewModel
│   │   ├── AttendanceChartView
│   │   ├── ActivityBarChartView
│   │   └── GroupRankingListView
│   ├── Deputy/
│   │   ├── DeputySubmissionView + ViewModel
│   │   └── DeputyReviewView
│   ├── Activities/                 # ActivityTypeManagementView + ViewModel
│   ├── Settings/                   # MoreView, GroupSettingsView
│   └── Admin/                      # AdminChurchesView, AdminDistrictsView,
│                                   # AdminUsersView, AdminPeriodsView
├── Navigation/
│   ├── AppRoute.swift              # Typed navigation enum (Hashable)
│   ├── MainTabView.swift           # Root 4-tab container
│   └── Permissions.swift           # Role-based feature gating
├── Components/
│   ├── NeuCard.swift
│   ├── NeuButton.swift
│   ├── NeuTextField.swift
│   ├── NeuToggle.swift             # 3-state attendance toggle
│   ├── NeuStatPill.swift
│   ├── NeuProgressBar.swift
│   ├── NeuIconBadge.swift
│   ├── NeuMemberRow.swift
│   ├── EmptyStateView.swift
│   ├── LoadingStateView.swift
│   ├── StepIndicatorView.swift
│   └── ConfirmationOverlay.swift
├── Theme/
│   ├── Color+Hex.swift             # Color(hex:) initializer
│   ├── Color+Theme.swift           # Full app color palette
│   ├── Font+Theme.swift            # Typography scale (rounded sans-serif only)
│   ├── NeuModifiers.swift          # .neuRaised(), .neuInset(), .neuPressed()
│   └── NeuStyle.swift              # Layout constants (radii, spacing, touch targets)
└── Utilities/
    ├── LoginUsernameBuilder.swift  # Composite login id + synthetic Auth email mapping
    ├── DateFormatters.swift        # Spanish locale date formatting
    └── Extensions.swift            # HapticFeedback, Array[safe:], View.if()
```

## Architecture

MVVM with protocol-based dependency injection:

- **Models** are plain `Codable` structs — no business logic
- **ViewModels** are `@Observable` classes — own async loading and state mutation
- **Services** are protocol-typed — swap live for mock at the `ServiceContainer` level (`AuthService`, `HierarchyService`, and the rest are composed on `ServiceContainer`)
- **Views** typically receive a ViewModel and render; `LoginView` loads hierarchy via `HierarchyService` and calls `AuthService.signIn(compositeUsername:password:)`

```
View  →  ViewModel  →  ServiceContainer  →  Service Protocol
                                         ↳  SupabaseXxxService (live)
                                         ↳  MockXxxService (preview)
```

`ServiceContainer` is injected at the root via:

```swift
.environment(\.services, .live)   // in PresenciaApp
.environment(\.services, .preview) // in #Preview blocks
```

## Design System

Neumorphic UI built on a soft gray background (`#E8EDF2`). All interactive surfaces use dual-shadow raised/inset technique. Typography uses `.rounded` font design throughout — no serif fonts.

Key modifiers:
- `.neuRaised()` — elevated card surface
- `.neuInset()` — recessed input field
- `.neuPressed()` — pressed button state

## Database Schema

The app expects these Supabase tables (among others): `union_org`, `campo`, `district`, `church`, `small_group`, `profile`, `role_assignment`, `member`, `meeting`, `attendance`, `activity_type`, `activity_record`, `activity_log`, `reporting_period`, `deputy_submission`, `deputy_attendance_entry`, `deputy_activity_entry`, `group_request`.

All tables use snake_case column names. The app handles camelCase ↔ snake_case conversion globally via `JSONEncoder.keyEncodingStrategy = .convertToSnakeCase` and `JSONDecoder.keyDecodingStrategy = .convertFromSnakeCase`.

> **Note:** Models that define their own `CodingKeys` must use camelCase raw values (or omit `CodingKeys` entirely) — explicit snake_case raw values conflict with `convertFromSnakeCase` and will silently break decoding.
