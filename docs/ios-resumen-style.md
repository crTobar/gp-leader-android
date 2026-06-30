# Porting the Summary screen (Step 3) to iOS — neumorphic SDA style

This guide explains how to replicate the **Registro Paso 3 / Resumen** (meeting summary) screen
in iOS / SwiftUI. All values are taken from the Android implementation
(`RegistroPaso3Screen.kt` + `Elevation.kt`). Points (`pt`) map 1:1 to Android `dp`.

---

## 1. Color palette

```swift
extension Color {
    static let background     = Color(hex: 0xECEEF1) // general background
    static let backgroundDeep = Color(hex: 0xE0E3E9) // dividers / progress track
    static let shadowDark     = Color(hex: 0xC2C8D4) // neu dark shadow
    static let shadowLight    = Color(hex: 0xFFFFFF) // neu light shadow
    static let accent         = Color(hex: 0x4A7FD4) // primary blue / "My GP" level
    static let gold           = Color(hex: 0xC9A84C) // "Unión" level
    static let sage           = Color(hex: 0x6AAB8E) // present
    static let blush          = Color(hex: 0xD4836A) // absent
    static let ink            = Color(hex: 0x1E2733) // primary text / "Pastor" level
    static let mid            = Color(hex: 0x5A6577) // secondary text
    static let muted          = Color(hex: 0x9AA4B2) // disabled text / zero values
}
```

Use a standard `Color(hex:)` initializer (UInt-based).

**Activity level color:** Unión → `gold`, Pastor → `ink`, My GP → `accent`.

---

## 2. Typography

Fonts are bundled (same files as Android):

- **Cormorant Garamond** (serif) → display / large titles
- **DM Sans** (sans-serif) → body / UI
- **DM Mono** (monospaced, tracking ~2pt, UPPERCASE) → labels / eyebrows

| Usage | Font | Size / weight |
|-------|------|---------------|
| Group name | DM Sans | 20pt Bold |
| Eyebrow label (FECHA, MIEMBROS, ACTIVIDADES, NIVEL…) | DM Mono | 11pt, tracking 2, uppercase, color `muted` |
| Stat number (present/absent…) | DM Sans | ~24pt Bold |
| Activity rows | DM Sans | 16pt |
| Activity value | DM Sans | 16pt SemiBold |

---

## 3. Neumorphic shadows (the core of the style)

Dual shadow: **light top-left + dark bottom-right**. In SwiftUI chain two `.shadow` calls.

```swift
extension View {
    // neuElevated — floating card (radius 20)
    func neuElevated(corner: CGFloat = 20) -> some View {
        self
            .background(Color.background)
            .clipShape(RoundedRectangle(cornerRadius: corner))
            .shadow(color: .shadowDark,  radius: 10, x: 8,  y: 8)
            .shadow(color: .shadowLight, radius: 10, x: -8, y: -8)
    }

    // neuElevatedSm — small floating pill (radius 14)
    func neuElevatedSm(corner: CGFloat = 14) -> some View {
        self
            .background(Color.background)
            .clipShape(RoundedRectangle(cornerRadius: corner))
            .shadow(color: .shadowDark,  radius: 6, x: 5,  y: 5)
            .shadow(color: .shadowLight, radius: 6, x: -5, y: -5)
    }
}
```

> Conversion: Android uses `blurRadius=20, offset=8` → SwiftUI `radius ≈ 10` (Android's
> blur is roughly 2× SwiftUI's radius), `x/y = 8`. For `Sm`: blur 12 / offset 5 → radius 6.

**Pressed (inset) effect — the non-clickable activity cards:**
SwiftUI has no native inner shadow; simulate it with gradients drawn **on top** of the content
(same as Android's `neuInsetInner`: dark top/left, light bottom/right):

```swift
struct NeuInsetInner: ViewModifier {
    var corner: CGFloat = 20
    var size: CGFloat = 16
    func body(content: Content) -> some View {
        content.overlay(
            ZStack {
                LinearGradient(colors: [Color.shadowDark.opacity(0.31), .clear],
                               startPoint: .top, endPoint: .init(x: 0.5, y: size/120))
                LinearGradient(colors: [Color.shadowDark.opacity(0.31), .clear],
                               startPoint: .leading, endPoint: .init(x: size/200, y: 0.5))
                LinearGradient(colors: [.clear, Color.shadowLight.opacity(0.38)],
                               startPoint: .init(x: 0.5, y: 1 - size/120), endPoint: .bottom)
                LinearGradient(colors: [.clear, Color.shadowLight.opacity(0.38)],
                               startPoint: .init(x: 1 - size/200, y: 0.5), endPoint: .trailing)
            }
            .allowsHitTesting(false)
        )
        .clipShape(RoundedRectangle(cornerRadius: corner))
    }
}
```

> Exact alpha from Android: dark `0x50` = 0.31, light `0x60` = 0.38. `size` = 16pt of
> gradient from each edge. **No outer shadow** → communicates "pressed / read-only".
> Critical order: `background → clip → overlay-gradients`. Never add an outer shadow to these cards.

---

## 4. Screen structure (top to bottom)

```
ScrollView
 ├─ TopBar: back button (neuElevatedSm 12) + title "Registrar reunión" / "Paso 3 de 3 · Resumen"
 ├─ Stepper: 3 circles (active = filled accent; pending = muted border)
 ├─ ResumenCard  (neuElevated 28, padding 20)
 ├─ Separator "— ACTIVIDADES —"
 └─ Activity sections per level (Unión / Pastor / My GP)
Pinned bottom button: "Enviar al pastor" (accent + neuGlow) + "Editar actividades"
```

---

## 5. ResumenCard (top card)

`neuElevated(corner: 28)`, padding 20pt. Vertical content with `backgroundDeep` 1pt dividers:

1. **Header:** group name (DM Sans 20 Bold) + `person.3.fill` icon in accent.
2. **FECHA** (eyebrow muted) ··· value on the right (DM Sans 16 Medium ink).
3. **MIEMBROS** → row of **3 equal tiles** (`HStack`, spacing 8):

```swift
// AsistenciaStat
VStack(spacing: 2) {
    Text("\(count)").font(.dmSans(24, .bold))
    Text(label).font(.dmSans(12, .medium)).lineLimit(1)
}
.frame(maxWidth: .infinity)
.padding(.vertical, 12).padding(.horizontal, 6)
.background((active ? color : .muted).opacity(active ? 0.12 : 0.08))
.clipShape(RoundedRectangle(cornerRadius: 14))
.foregroundColor(active ? color : .muted)  // grey when count == 0
```

Colors: present `sage`, absent `blush`, justified `gold`. Labels in **singular/plural**
based on the count (1 → "Presente", 0 / 2+ → "Presentes").

4. **ASISTENCIA** → bar only: `"\(present)/\(total)"` · `ProgressView`
   (tint accent, track `backgroundDeep`, height 8, radius 4) · `"\(pct)%"`.
   (No big number above the bar.)
5. **VISITAS** ··· "N personas".

---

## 6. Activity sections (the key part)

Each level = **floating pill outside** + **sunken card below**:

```swift
VStack(alignment: .leading, spacing: 8) {
    // Floating level pill (sticks out, indented 12pt from the left)
    HStack(spacing: 9) {
        RoundedRectangle(cornerRadius: 2)
            .fill(levelColor).frame(width: 3, height: 14)        // accent bar
        Text("NIVEL UNIÓN").font(.dmMono(11)).foregroundColor(.ink).bold()
        Text("\(count)").font(.dmMono(11)).bold()                // count badge
            .padding(.horizontal, 7).padding(.vertical, 1)
            .background(levelColor.opacity(0.14)).clipShape(Capsule())
            .foregroundColor(levelColor == .ink ? .mid : levelColor)
    }
    .padding(.leading, 12).padding(.trailing, 16).padding(.vertical, 9)
    .neuElevatedSm(corner: 14)          // ← the pill IS elevated
    .padding(.leading, 12)              // indent so it "sticks out"

    // Sunken card with the rows
    HStack(spacing: 0) {
        Rectangle().fill(levelColor).frame(width: 4)             // full-height accent strip
        VStack(spacing: 0) {
            ForEach(activities) { act in
                if act != first { Divider().background(Color.backgroundDeep).padding(.horizontal, 16) }
                HStack {
                    Text(act.nombre).font(.dmSans(16)).foregroundColor(.ink)
                    Spacer()
                    Text("\(act.cantidad) \(act.unidad)")
                        .font(.dmSans(16, .semibold))
                        .foregroundColor(act.cantidad > 0 ? levelColor : .muted) // grey if 0
                }
                .padding(.horizontal, 16).padding(.vertical, 13)
            }
        }
        .padding(.vertical, 6)
    }
    .background(levelColor.opacity(0.05))   // subtle level tint
    .modifier(NeuInsetInner(corner: 20, size: 16))  // ← SUNKEN card
}
```

**Visual contrast summary:** the level pill is **elevated** (sticks out), the data card is
**sunken** (not clickable). Both share the level color (accent bar + 5% tint + value text).

---

## 7. Bottom button

`"Enviar al pastor"`: `accent` background, white DM Sans 17 SemiBold text, radius 24, plus a
blue glow `neuGlow` (= neuElevated but with an extra blue shadow `rgba(74,127,212,0.25)`,
blur ~30, no offset). Below it, "Editar actividades" as flat `muted` text.

```swift
extension View {
    func neuGlow(corner: CGFloat = 24) -> some View {
        self
            .clipShape(RoundedRectangle(cornerRadius: corner))
            .shadow(color: Color.accent.opacity(0.25), radius: 15, x: 0, y: 0)
            .shadow(color: .shadowDark,  radius: 10, x: 8,  y: 8)
            .shadow(color: .shadowLight, radius: 10, x: -8, y: -8)
    }
}
```

---

## Quick reference (Android → SwiftUI)

| Android | SwiftUI equivalent |
|---------|--------------------|
| `neuElevated(20.dp)` | `.neuElevated(corner: 20)` |
| `neuElevatedSm(14.dp)` | `.neuElevatedSm(corner: 14)` |
| `neuInsetInner(shadowSize = 16.dp)` | `.modifier(NeuInsetInner(corner: 20, size: 16))` |
| `neuGlow` | `.neuGlow()` |
| `dp` | `pt` (1:1) |
| `MaterialTheme.typography.labelSmall` | DM Mono 11pt, tracking 2, uppercase |
| `pluralStringResource` | `String.localizedStringKey` with stringsdict plurals |
