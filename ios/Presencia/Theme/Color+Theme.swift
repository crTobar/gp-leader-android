import SwiftUI

extension Color {
    // MARK: - Surfaces
    static let neuBackground  = Color(hex: "eceef1")
    /// Deeper section / track (Kotlin `BackgroundDeep`)
    static let neuBackgroundDeep = Color(hex: "e0e3e9")
    static let neuShadowDark  = Color(hex: "c2c8d4")
    static let neuShadowLight = Color.white

    // MARK: - Primary Accent
    static let accent     = Color(hex: "4a7fd4")
    static let accentSoft = Color(hex: "6497e0")
    static let accentGlow = Color(hex: "4a7fd4").opacity(0.25)

    // MARK: - Text Hierarchy
    static let textPrimary   = Color(hex: "1e2733")
    static let textSecondary = Color(hex: "5a6577")
    static let textMuted     = Color(hex: "9aa4b2")

    // MARK: - Semantic Status
    static let statusPresent   = Color(hex: "6aab8e")  // sage
    static let statusAbsent    = Color(hex: "9fb3c8")  // slate gray — neutral, not alarming
    static let statusJustified = Color(hex: "c9a84c")  // gold
    static let blush           = Color(hex: "d4836a")  // orange — errors, alerts, absent counts

    // MARK: - Dark Overlay (used on dark header surfaces)
    static let neuBackgroundDark = Color(hex: "1a2130")
}
