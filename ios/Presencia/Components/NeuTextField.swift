import SwiftUI
import LucideIcons

// MARK: - Neumorphic Text Field

struct NeuTextField: View {
    let placeholder: String
    @Binding var text: String
    var label: String? = nil
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var icon: UIImage? = nil
    /// When true, shows a blush border (validation).
    var hasError: Bool = false

    @FocusState private var isFocused: Bool
    @State private var showPassword: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let label {
                Text(label)
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)
            }

            HStack(spacing: 12) {
                if let icon = icon {
                    Image(uiImage: icon)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 16, height: 16)
                        .foregroundStyle(isFocused ? Color.accent : Color.textMuted)
                        .animation(.easeInOut(duration: 0.2), value: isFocused)
                }

                Group {
                    if isSecure && !showPassword {
                        SecureField(placeholder, text: $text)
                    } else {
                        TextField(placeholder, text: $text)
                            .keyboardType(keyboardType)
                    }
                }
                .font(.bodyLarge)
                .foregroundStyle(Color.textPrimary)

                if isSecure {
                    Button {
                        showPassword.toggle()
                    } label: {
                        Image(uiImage: showPassword ? AppIcon.eyeOff : AppIcon.eye)
                            .renderingMode(.template)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 16, height: 16)
                            .foregroundStyle(Color.textMuted)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .frame(minHeight: NeuStyle.touchTargetMin)
            .background(
                RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                    .fill(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.4), radius: 5, x: 4, y: 4)))
                    .overlay(
                        RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                            .fill(Color.clear.shadow(.inner(color: Color.white.opacity(0.7), radius: 5, x: -4, y: -4)))
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                    .strokeBorder(
                        borderStrokeColor,
                        lineWidth: 1.5
                    )
                    .animation(.easeInOut(duration: 0.2), value: isFocused)
                    .animation(.easeInOut(duration: 0.2), value: hasError)
            )
            .focused($isFocused)
        }
    }

    private var borderStrokeColor: Color {
        if hasError { return Color.blush.opacity(0.9) }
        if isFocused { return Color.accent.opacity(0.5) }
        return Color.clear
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        VStack(spacing: 16) {
            NeuTextField(placeholder: "correo@ejemplo.com", text: .constant(""), label: "Correo electrónico", icon: AppIcon.email)
            NeuTextField(placeholder: "••••••••", text: .constant(""), label: "Contraseña", isSecure: true, icon: AppIcon.password)
            NeuTextField(placeholder: "Nombre", text: .constant("María González"), icon: AppIcon.user)
        }
        .padding()
    }
}
