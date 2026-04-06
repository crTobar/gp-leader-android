import SwiftUI
import LucideIcons

// MARK: - Reusable Empty State

struct EmptyStateView: View {
    let icon: UIImage
    let title: String
    let subtitle: String
    var ctaTitle: String? = nil
    var ctaAction: (() -> Void)? = nil

    var body: some View {
        ContentUnavailableView {
            Label {
                Text(title)
                    .font(.headingLarge)
                    .foregroundStyle(Color.textPrimary)
            } icon: {
                Image(uiImage: icon)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .foregroundStyle(Color.textMuted)
            }
        } description: {
            Text(subtitle)
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
                .multilineTextAlignment(.center)
        } actions: {
            if let ctaTitle = ctaTitle, let ctaAction = ctaAction {
                NeuButton(ctaTitle, variant: .primary, action: ctaAction)
                    .frame(maxWidth: 220)
            }
        }
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        EmptyStateView(
            icon: AppIcon.calendar,
            title: "Sin reuniones",
            subtitle: "No hay reuniones esta semana. ¡Toca para crear una!",
            ctaTitle: "Nueva Reunión"
        ) {}
    }
}
