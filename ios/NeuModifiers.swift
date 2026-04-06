import SwiftUI

// MARK: - Neumorphic Shadow Modifiers
// Exact values from style board (section 13 · Light & Shadow Anatomy):
//
//   Raised · Resting   → 6px 6px 16px #c2c8d4,   -6px -6px 16px #ffffff
//   Inset · Pressed    → inset 4px 4px 10px #c2c8d4, inset -4px -4px 10px #ffffff
//   Glow · Accent      → 0 0 20px rgba(74,127,212,.25) + raised
//   Flat · Minimal     → 2px 2px 6px #c2c8d4,    -2px -2px 6px #ffffff

// MARK: - Raised · Resting
// Default state for all interactive elements. Light from top-left.

struct NeuRaised: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(Color.neuBackground)
            .shadow(color: Color.neuShadowDark,        radius: 16, x: 6,  y: 6)
            .shadow(color: Color.white,                 radius: 16, x: -6, y: -6)
    }
}

// MARK: - Flat · Minimal
// Dividers, labels, secondary text containers.

struct NeuRaisedSmall: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(Color.neuBackground)
            .shadow(color: Color.neuShadowDark,        radius: 6, x: 2,  y: 2)
            .shadow(color: Color.white,                 radius: 6, x: -2, y: -2)
    }
}

// MARK: - Inset · Pressed
// Active inputs, toggles ON, selected members.

struct NeuInset: ViewModifier {
    var cornerRadius: CGFloat = NeuStyle.cardRadius

    func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(Color.neuBackground
                        .shadow(.inner(color: Color.neuShadowDark, radius: 10, x: 4, y: 4))
                        .shadow(.inner(color: Color.white,          radius: 10, x: -4, y: -4))
                    )
            )
    }
}

struct NeuPressed: ViewModifier {
    var cornerRadius: CGFloat = NeuStyle.buttonRadius

    func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(Color.neuBackground
                        .shadow(.inner(color: Color.neuShadowDark, radius: 10, x: 4, y: 4))
                        .shadow(.inner(color: Color.white,          radius: 10, x: -4, y: -4))
                    )
            )
    }
}

// MARK: - View Extensions

extension View {
    func neuRaised() -> some View      { modifier(NeuRaised()) }
    func neuRaisedSmall() -> some View { modifier(NeuRaisedSmall()) }
    func neuInset(cornerRadius: CGFloat = NeuStyle.cardRadius) -> some View {
        modifier(NeuInset(cornerRadius: cornerRadius))
    }
    func neuPressed(cornerRadius: CGFloat = NeuStyle.buttonRadius) -> some View {
        modifier(NeuPressed(cornerRadius: cornerRadius))
    }
}
