import SwiftUI

// MARK: - Kotlin Registro top bar + StepperRow (RegistroPaso1/2/3)

struct RegistroMeetingTopBar: View {
    let currentStep: Int
    let onBack: () -> Void

    private var stepBadge: String {
        switch currentStep {
        case 0: return "1/3"
        case 1: return "2/3"
        default: return "3/3"
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            Button(action: {
                HapticFeedback.light()
                onBack()
            }) {
                LucideIcon(uiImage: AppIcon.chevronLeft, size: 20)
                    .foregroundStyle(Color.textPrimary)
                    .frame(width: 40, height: 40)
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 2, y: 2)
                    .shadow(color: Color.neuShadowLight.opacity(0.85), radius: 6, x: -2, y: -2)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Atrás")

            Text("Registrar reunión")
                .font(Font.titleLarge)
                .foregroundStyle(Color.textPrimary)
                .frame(maxWidth: .infinity)
                .multilineTextAlignment(.center)

            Text(stepBadge)
                .font(.captionStyle)
                .foregroundStyle(.white)
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .background(Color.textPrimary)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

/// Underline tabs: `1 Asistencia` | `2 Actividades` | `3 Resumen`
struct RegistroStepTabsRow: View {
    /// 0 = asistencia, 1 = actividades, 2 = resumen
    let activeStep: Int

    private let labels = ["1 Asistencia", "2 Actividades", "3 Resumen"]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<3, id: \.self) { index in
                VStack(spacing: 0) {
                    Text(labels[index])
                        .font(.captionStyle)
                        .foregroundStyle(activeStep == index ? Color.accent : Color.textMuted)
                        .padding(.vertical, 10)
                        .frame(maxWidth: .infinity)

                    Rectangle()
                        .fill(activeStep == index ? Color.accent : Color.clear)
                        .frame(height: 2)
                }
            }
        }
        .padding(.horizontal, 16)
        .background(Color.neuBackground)
    }
}

#Preview {
    VStack(spacing: 0) {
        RegistroMeetingTopBar(currentStep: 1, onBack: {})
        RegistroStepTabsRow(activeStep: 1)
    }
    .background(Color.neuBackground)
}
