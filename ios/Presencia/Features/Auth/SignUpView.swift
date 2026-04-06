import SwiftUI
import Supabase
import LucideIcons

// MARK: - Temporary sign-up screen (remove once real users exist in DB)

struct SignUpView: View {
    @Environment(\.dismiss) private var dismiss

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var didSucceed = false

    var body: some View {
        NavigationStack {
            ZStack {
                Color.neuBackground.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 24) {
                        Spacer(minLength: 32)

                        // Header
                        VStack(spacing: 6) {
                            Image(uiImage: AppIcon.signup)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 56, height: 56)
                                .foregroundStyle(Color.accent)
                            Text("Crear Cuenta")
                                .font(.displayMedium)
                                .foregroundStyle(Color.textPrimary)
                            Text("Solo para configuración inicial")
                                .font(.captionStyle)
                                .foregroundStyle(Color.textMuted)
                        }

                        Spacer(minLength: 8)

                        // Fields
                        VStack(spacing: 14) {
                            NeuTextField(placeholder: "Nombre", text: $firstName, icon: AppIcon.user)
                            NeuTextField(placeholder: "Apellido", text: $lastName, icon: AppIcon.user)
                            NeuTextField(
                                placeholder: "Correo electrónico",
                                text: $email,
                                keyboardType: .emailAddress,
                                icon: AppIcon.email
                            )
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            NeuTextField(
                                placeholder: "Contraseña (mín. 6 caracteres)",
                                text: $password,
                                isSecure: true,
                                icon: AppIcon.password
                            )
                        }

                        // Success message
                        if didSucceed {
                            NeuCard(padding: 14) {
                                Label { Text("Cuenta creada. Ya puedes iniciar sesión.") } icon: { Image(uiImage: AppIcon.checkCircle).renderingMode(.template) }
                                    .font(.bodyRegular)
                                    .foregroundStyle(Color.statusPresent)
                            }
                        }

                        // Error
                        if let error = errorMessage {
                            Text(error)
                                .font(.captionStyle)
                                .foregroundStyle(Color.blush)
                                .multilineTextAlignment(.center)
                        }

                        NeuButton(
                            didSucceed ? "Volver al Login" : "Crear Cuenta",
                            icon: didSucceed ? AppIcon.arrowLeft : AppIcon.signup,
                            variant: .primary,
                            isLoading: isLoading
                        ) {
                            if didSucceed {
                                dismiss()
                            } else {
                                Task { await signUp() }
                            }
                        }

                        Spacer(minLength: 32)
                    }
                    .padding(.horizontal, 32)
                    .frame(maxWidth: 375)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                        .font(.bodyRegular)
                        .foregroundStyle(Color.accent)
                }
            }
        }
    }

    @MainActor
    private func signUp() async {
        let trimmedFirst = firstName.trimmingCharacters(in: .whitespaces)
        let trimmedLast = lastName.trimmingCharacters(in: .whitespaces)
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)

        guard !trimmedFirst.isEmpty, !trimmedLast.isEmpty,
              !trimmedEmail.isEmpty, password.count >= 6 else {
            errorMessage = "Completa todos los campos. La contraseña debe tener al menos 6 caracteres."
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            // 1. Create auth user
            let response = try await supabase.auth.signUp(email: trimmedEmail, password: password)
            let userId = response.user.id

            // 2. Insert profile row
            let now = ISO8601DateFormatter().string(from: Date())
            try await supabase.from("profile").insert([
                "id": userId.uuidString,
                "first_name": trimmedFirst,
                "last_name": trimmedLast,
                "email": trimmedEmail,
                "is_active": "true",
                "created_at": now,
                "updated_at": now
            ]).execute()

            // 3. Insert a basic leader role assignment (no group assigned yet)
            try await supabase.from("role_assignment").insert([
                "id": UUID().uuidString,
                "profile_id": userId.uuidString,
                "role": "leader",
                "created_at": now
            ]).execute()

            HapticFeedback.success()
            didSucceed = true
        } catch {
            errorMessage = "Error: \(error.localizedDescription)"
            HapticFeedback.warning()
        }

        isLoading = false
    }
}

#Preview {
    SignUpView()
}
