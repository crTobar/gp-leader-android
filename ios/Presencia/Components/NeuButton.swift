import SwiftUI
import LucideIcons

// MARK: - Button Style Variants

enum NeuButtonVariant {
    case primary
    case secondary
    case ghost
}

// MARK: - Neumorphic Button

struct NeuButton: View {
    let title: String
    let icon: UIImage?
    let variant: NeuButtonVariant
    let isLoading: Bool
    let action: () -> Void

    @State private var isPressed = false

    init(_ title: String,
         icon: UIImage? = nil,
         variant: NeuButtonVariant = .primary,
         isLoading: Bool = false,
         action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.variant = variant
        self.isLoading = isLoading
        self.action = action
    }

    var body: some View {
        Button {
            HapticFeedback.light()
            action()
        } label: {
            buttonLabel
        }
        .buttonStyle(NeuButtonStyle(variant: variant))
        .disabled(isLoading)
        .accessibilityLabel(title)
    }

    @ViewBuilder
    private var buttonLabel: some View {
        HStack(spacing: 8) {
            if isLoading {
                ProgressView()
                    .progressViewStyle(.circular)
                    .tint(labelColor)
                    .scaleEffect(0.8)
            } else if let icon = icon {
                Image(uiImage: icon)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 18, height: 18)
            }
            Text(title)
                .font(.headingMedium)
        }
        .foregroundStyle(labelColor)
        .frame(maxWidth: .infinity)
        .frame(minHeight: NeuStyle.touchTargetMin)
        .padding(.horizontal, 20)
    }

    private var labelColor: Color {
        switch variant {
        case .primary: return .white
        case .secondary: return .accent
        case .ghost: return .textMuted
        }
    }
}

// MARK: - Button Style Implementation

struct NeuButtonStyle: ButtonStyle {
    let variant: NeuButtonVariant

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background(backgroundView(isPressed: configuration.isPressed))
            .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.easeInOut(duration: 0.15), value: configuration.isPressed)
    }

    @ViewBuilder
    private func backgroundView(isPressed: Bool) -> some View {
        switch variant {
        case .primary:
            if isPressed {
                Color.accent.opacity(0.85)
            } else {
                Color.accent
            }
        case .secondary:
            if isPressed {
                Color.neuBackground.opacity(0.9)
            } else {
                Color.neuBackground
            }
        case .ghost:
            Color.clear
        }
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        VStack(spacing: 20) {
            NeuButton("Tomar Asistencia", icon: AppIcon.checkCircle, variant: .primary) {}
            NeuButton("Ver Historial", variant: .secondary) {}
            NeuButton("Cancelar", variant: .ghost) {}
            NeuButton("Iniciando sesión...", variant: .primary, isLoading: true) {}
        }
        .padding()
    }
}
