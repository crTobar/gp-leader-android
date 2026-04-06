import SwiftUI
import LucideIcons

// MARK: - Neumorphic Icon Badge

struct NeuIconBadge: View {
    let icon: UIImage
    var color: Color = .accent
    var size: CGFloat = NeuStyle.iconContainerSize
    var accessibilityLabel: String? = nil

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: NeuStyle.iconContainerRadius)
                .fill(Color.neuBackground)
                .frame(width: size, height: size)
                .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 5, x: 3, y: 3)
                .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 5, x: -3, y: -3)

            Image(uiImage: icon)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: size * 0.42, height: size * 0.42)
                .foregroundStyle(color)
        }
        .accessibilityLabel(accessibilityLabel.map(Text.init) ?? Text(""))
        .accessibilityHidden(accessibilityLabel == nil)
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        HStack(spacing: 20) {
            NeuIconBadge(icon: AppIcon.book)
            NeuIconBadge(icon: AppIcon.person, color: .statusPresent)
            NeuIconBadge(icon: AppIcon.newspaper, color: .statusJustified)
        }
    }
}
