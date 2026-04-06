import SwiftUI

// MARK: - Neumorphic Card Container

struct NeuCard<Content: View>: View {
    let content: () -> Content
    var padding: CGFloat = NeuStyle.cardPadding
    var cornerRadius: CGFloat = NeuStyle.cardRadius

    init(padding: CGFloat = NeuStyle.cardPadding,
         cornerRadius: CGFloat = NeuStyle.cardRadius,
         @ViewBuilder content: @escaping () -> Content) {
        self.padding = padding
        self.cornerRadius = cornerRadius
        self.content = content
    }

    var body: some View {
        content()
            .padding(padding)
            .background(Color.neuBackground)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
            .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
            .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        NeuCard {
            VStack(alignment: .leading, spacing: 8) {
                Text("Tarjeta de Ejemplo")
                    .font(.headingMedium)
                    .foregroundStyle(Color.textPrimary)
                Text("Contenido de la tarjeta aquí.")
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textSecondary)
            }
        }
        .padding()
    }
}
