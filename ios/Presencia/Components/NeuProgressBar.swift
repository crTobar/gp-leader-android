import SwiftUI

// MARK: - Neumorphic Progress Bar

struct NeuProgressBar: View {
    let value: Double // 0.0 – 1.0
    var height: CGFloat = 10
    var cornerRadius: CGFloat = 6

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                // Track (inset)
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.4), radius: 4, x: 3, y: 3)))
                    .overlay(
                        RoundedRectangle(cornerRadius: cornerRadius)
                            .fill(Color.clear.shadow(.inner(color: Color.white.opacity(0.7), radius: 4, x: -3, y: -3)))
                    )

                // Fill
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(
                        LinearGradient(
                            colors: [Color.accent, Color.accentSoft],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: max(0, geo.size.width * min(1, max(0, value))))
                    .shadow(color: Color.accent.opacity(0.3), radius: 4, x: 2, y: 2)
            }
        }
        .frame(height: height)
        .accessibilityValue(Text("\(Int(min(1, max(0, value)) * 100))%"))
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        VStack(spacing: 20) {
            NeuProgressBar(value: 0.8)
            NeuProgressBar(value: 0.5)
            NeuProgressBar(value: 0.25)
        }
        .padding()
    }
}
