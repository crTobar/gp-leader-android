import SwiftUI
import LucideIcons

struct ChangePasswordView: View {
    @Environment(\.services) private var services
    @Environment(\.dismiss) private var dismiss

    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    @State private var isSaving = false
    @State private var errorMessage: String? = nil
    @State private var showingSuccess = false

    private var req8Chars: Bool    { newPassword.count >= 8 }
    private var reqUppercase: Bool { newPassword.contains(where: { $0.isUppercase }) }
    private var reqNumber: Bool    { newPassword.contains(where: { $0.isNumber }) }
    private var reqMatch: Bool     { !newPassword.isEmpty && newPassword == confirmPassword }

    private var isValid: Bool {
        !currentPassword.isEmpty && req8Chars && reqUppercase && reqNumber && reqMatch
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Fields card
                NeuCard {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Cambiar Contraseña")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        NeuTextField(
                            placeholder: "••••••••",
                            text: $currentPassword,
                            label: "Contraseña actual",
                            isSecure: true,
                            icon: AppIcon.password
                        )

                        NeuTextField(
                            placeholder: "••••••••",
                            text: $newPassword,
                            label: "Nueva contraseña",
                            isSecure: true,
                            icon: AppIcon.password
                        )

                        NeuTextField(
                            placeholder: "••••••••",
                            text: $confirmPassword,
                            label: "Confirmar contraseña",
                            isSecure: true,
                            icon: AppIcon.password
                        )
                    }
                }
                .padding(.horizontal)

                // Requirements checklist
                NeuCard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Requisitos")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        RequirementRow(met: req8Chars,    label: "Mínimo 8 caracteres")
                        RequirementRow(met: reqUppercase, label: "Al menos una mayúscula")
                        RequirementRow(met: reqNumber,    label: "Al menos un número")
                        RequirementRow(met: reqMatch,     label: "Las contraseñas coinciden")
                    }
                }
                .padding(.horizontal)

                if let errorMessage {
                    Text(errorMessage)
                        .font(.captionStyle)
                        .foregroundStyle(Color.blush)
                        .padding(.horizontal)
                }

                NeuButton(
                    "Actualizar contraseña",
                    icon: AppIcon.checkCircle,
                    variant: .primary,
                    isLoading: isSaving
                ) {
                    Task { await changePassword() }
                }
                .disabled(!isValid)
                .opacity(isValid ? 1 : 0.5)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Cambiar Contraseña")
        .navigationBarTitleDisplayMode(.inline)
        .alert("¡Contraseña actualizada!", isPresented: $showingSuccess) {
            Button("OK", role: .cancel) { dismiss() }
        } message: {
            Text("Tu contraseña ha sido cambiada exitosamente.")
        }
    }

    private func changePassword() async {
        isSaving = true
        errorMessage = nil
        do {
            try await services.auth.updatePassword(newPassword: newPassword)
            HapticFeedback.success()
            showingSuccess = true
        } catch {
            errorMessage = "No se pudo cambiar la contraseña. Intenta de nuevo."
        }
        isSaving = false
    }
}

// MARK: - Requirement Row

private struct RequirementRow: View {
    let met: Bool
    let label: String

    var body: some View {
        HStack(spacing: 10) {
            ZStack {
                RoundedRectangle(cornerRadius: 4)
                    .fill(met ? Color.statusPresent : Color.neuBackground)
                    .frame(width: 20, height: 20)
                    .shadow(color: met ? Color.clear : Color.neuShadowDark.opacity(0.4), radius: 3, x: 2, y: 2)
                    .shadow(color: met ? Color.clear : Color.white.opacity(0.9), radius: 3, x: -2, y: -2)
                if met {
                    Image(uiImage: AppIcon.check)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 11, height: 11)
                        .foregroundStyle(.white)
                }
            }
            .animation(.spring(response: 0.3, dampingFraction: 0.6), value: met)

            Text(label)
                .font(.bodyRegular)
                .foregroundStyle(met ? Color.statusPresent : Color.textSecondary)
                .animation(.easeInOut(duration: 0.2), value: met)
        }
    }
}

#Preview {
    NavigationStack {
        ChangePasswordView()
            .environment(\.services, .preview)
    }
}
