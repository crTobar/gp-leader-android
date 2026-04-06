import SwiftUI

// MARK: - Typography Scale
// All fonts use SF Pro Rounded (system .rounded) — no serif, no custom fonts.
// Sizes tuned for elder-friendly readability.
//
//   displayLarge   40px · rounded bold
//   displayItalic  32px · rounded semibold  (replaces Cormorant italic)
//   displayMedium  26px · rounded semibold
//   headlineMedium 30px · rounded semibold
//   titleLarge     24px · rounded semibold
//   headingLarge   22px · rounded semibold
//   headingMedium  19px · rounded semibold
//   bodyLarge      17px · rounded medium
//   bodyRegular    16px · rounded regular
//   captionStyle   13px · rounded regular
//   captionSmall   11px · rounded regular

extension Font {
    // Display
    static let displayLarge   = Font.system(size: 40, weight: .bold,     design: .rounded)
    static let displayItalic  = Font.system(size: 32, weight: .semibold, design: .rounded)
    static let displayMedium  = Font.system(size: 26, weight: .semibold, design: .rounded)

    // Headings
    static let headingLarge   = Font.system(size: 22, weight: .semibold, design: .rounded)
    static let headingMedium  = Font.system(size: 19, weight: .semibold, design: .rounded)
    /// Material `headlineMedium` scale — date numerals, strong subheads
    static let headlineMedium = Font.system(size: 30, weight: .semibold, design: .rounded)
    /// Card / bar titles (Kotlin `titleLarge` scale)
    static let titleLarge     = Font.system(size: 24, weight: .semibold, design: .rounded)

    // Body
    static let bodyLarge      = Font.system(size: 17, weight: .medium,   design: .rounded)
    static let bodyRegular    = Font.system(size: 16, weight: .regular,  design: .rounded)

    // Captions / labels
    static let captionStyle   = Font.system(size: 13, weight: .regular,  design: .rounded)
    static let captionSmall   = Font.system(size: 11, weight: .regular,  design: .rounded)
}
