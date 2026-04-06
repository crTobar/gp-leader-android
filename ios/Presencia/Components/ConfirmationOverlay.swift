import SwiftUI
import LucideIcons

// MARK: - Success Confirmation Overlay

struct ConfirmationOverlay: View {
    @Binding var isShowing: Bool
    var title: String = "¡Asistencia enviada!"
    var subtitle: String = "El reporte fue enviado exitosamente."
    var autoDismissAfter: Double = 2.0

    @State private var scale: CGFloat = 0.5
    @State private var opacity: Double = 0

    var body: some View {
        ZStack {
            Color.neuBackground.opacity(0.95)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                ZStack {
                    Circle()
                        .fill(Color.statusPresent.opacity(0.15))
                        .frame(width: 120, height: 120)

                    Circle()
                        .strokeBorder(Color.statusPresent.opacity(0.3), lineWidth: 2)
                        .frame(width: 100, height: 100)

                    Image(uiImage: AppIcon.checkCircle)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 64, height: 64)
                        .foregroundStyle(Color.statusPresent)
                }
                .scaleEffect(scale)

                VStack(spacing: 8) {
                    Text(title)
                        .font(.displayMedium)
                        .foregroundStyle(Color.textPrimary)
                        .multilineTextAlignment(.center)

                    Text(subtitle)
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textSecondary)
                        .multilineTextAlignment(.center)
                }
            }
            .opacity(opacity)
        }
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                scale = 1.0
                opacity = 1.0
            }
            Task {
                try? await Task.sleep(for: .seconds(autoDismissAfter))
                withAnimation(.easeOut(duration: 0.3)) {
                    opacity = 0
                }
                try? await Task.sleep(for: .seconds(0.3))
                isShowing = false
            }
        }
    }
}

#Preview {
    @Previewable @State var showing = true
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        if showing {
            ConfirmationOverlay(isShowing: $showing)
        }
    }
}
