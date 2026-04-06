import SwiftUI
import LucideIcons

// MARK: - Multi-Step Flow Indicator

struct StepIndicatorView: View {
    let currentStep: Int
    let totalSteps: Int
    var labels: [String] = []

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<totalSteps, id: \.self) { index in
                StepDot(
                    index: index,
                    currentStep: currentStep,
                    label: labels[safe: index]
                )

                if index < totalSteps - 1 {
                    Rectangle()
                        .fill(index < currentStep ? Color.accent.opacity(0.6) : Color.neuShadowDark.opacity(0.3))
                        .frame(height: 2)
                        .frame(maxWidth: .infinity)
                }
            }
        }
        .padding(.horizontal)
    }
}

private struct StepDot: View {
    let index: Int
    let currentStep: Int
    let label: String?

    private var isDone: Bool { index < currentStep }
    private var isCurrent: Bool { index == currentStep }

    var body: some View {
        VStack(spacing: 4) {
            ZStack {
                Circle()
                    .fill(dotBackground)
                    .frame(width: 28, height: 28)
                    .shadow(color: isDone || isCurrent ? Color.accent.opacity(0.3) : Color.neuShadowDark.opacity(0.3),
                            radius: 4, x: 2, y: 2)
                    .shadow(color: Color.neuShadowLight.opacity(0.8), radius: 4, x: -2, y: -2)

                if isDone {
                    Image(uiImage: AppIcon.check)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 12, height: 12)
                        .foregroundStyle(Color.white)
                } else {
                    Text("\(index + 1)")
                        .font(.captionSmall)
                        .foregroundStyle(isCurrent ? Color.white : Color.textMuted)
                }
            }

            if let label = label {
                Text(label)
                    .font(.captionSmall)
                    .foregroundStyle(isCurrent ? Color.accent : Color.textMuted)
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
                    .frame(width: 56)
            }
        }
    }

    private var dotBackground: Color {
        if isDone { return .accent }
        if isCurrent { return .accent }
        return .neuBackground
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        VStack(spacing: 32) {
            StepIndicatorView(currentStep: 0, totalSteps: 4,
                              labels: ["Fecha", "Asistencia", "Actividades", "Resumen"])
            StepIndicatorView(currentStep: 2, totalSteps: 4,
                              labels: ["Fecha", "Asistencia", "Actividades", "Resumen"])
        }
        .padding()
    }
}
