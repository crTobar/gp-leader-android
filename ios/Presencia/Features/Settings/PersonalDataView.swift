import SwiftUI
import LucideIcons

struct PersonalDataView: View {
    let profile: Profile
    let onSaved: (Profile) -> Void

    @Environment(\.services) private var services
    @Environment(\.dismiss) private var dismiss

    @State private var firstName: String
    @State private var lastName: String
    @State private var phone: String
    @State private var isSaving = false
    @State private var errorMessage: String? = nil

    init(profile: Profile, onSaved: @escaping (Profile) -> Void) {
        self.profile = profile
        self.onSaved = onSaved
        _firstName = State(initialValue: profile.firstName)
        _lastName  = State(initialValue: profile.lastName)
        _phone     = State(initialValue: profile.phone ?? "")
    }

    private var hasChanges: Bool {
        firstName != profile.firstName ||
        lastName  != profile.lastName  ||
        phone     != (profile.phone ?? "")
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                NeuCard {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Información Personal")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        NeuTextField(
                            placeholder: "Nombre",
                            text: $firstName,
                            label: "Nombre",
                            icon: AppIcon.user
                        )

                        NeuTextField(
                            placeholder: "Apellido",
                            text: $lastName,
                            label: "Apellido",
                            icon: AppIcon.user
                        )

                        NeuTextField(
                            placeholder: "Teléfono (opcional)",
                            text: $phone,
                            label: "Teléfono",
                            keyboardType: .phonePad,
                            icon: AppIcon.phone
                        )
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
                    "Guardar Cambios",
                    icon: AppIcon.checkCircle,
                    variant: .primary,
                    isLoading: isSaving
                ) {
                    Task { await save() }
                }
                .disabled(!hasChanges)
                .opacity(hasChanges ? 1 : 0.5)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Información Personal")
        .toolbar(.visible, for: .navigationBar)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func save() async {
        guard !firstName.trimmingCharacters(in: .whitespaces).isEmpty,
              !lastName.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "El nombre y apellido son obligatorios."
            return
        }
        isSaving = true
        errorMessage = nil
        var updated = profile
        updated.firstName = firstName.trimmingCharacters(in: .whitespaces)
        updated.lastName  = lastName.trimmingCharacters(in: .whitespaces)
        updated.phone     = phone.isEmpty ? nil : phone
        do {
            let saved = try await services.auth.updateProfile(updated)
            HapticFeedback.success()
            onSaved(saved)
            dismiss()
        } catch {
            errorMessage = "No se pudo guardar. Intenta de nuevo."
        }
        isSaving = false
    }
}

#Preview {
    NavigationStack {
        PersonalDataView(profile: MockData.currentProfile, onSaved: { _ in })
            .environment(\.services, .preview)
    }
}
