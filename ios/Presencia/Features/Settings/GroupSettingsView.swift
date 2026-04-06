import SwiftUI
import LucideIcons

// MARK: - Group Settings

struct GroupSettingsView: View {
    let group: SmallGroup
    var onSaved: ((SmallGroup) -> Void)? = nil

    @Environment(\.services) private var services
    @State private var name: String = ""
    @State private var meetingDay: String = "Sábado"
    @State private var showingSuccess: Bool = false
    @State private var isSaving: Bool = false
    @State private var errorMessage: String?

    private var hasChanges: Bool {
        name != group.name || meetingDay != (group.meetingDay ?? "Sábado")
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                NeuCard {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Datos del Grupo")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)
                        NeuTextField(placeholder: "Nombre del grupo", text: $name, icon: AppIcon.usersGroup)
                    }
                }
                .padding(.horizontal)

                NeuCard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Día de Reunión")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        MeetingDayWheelPicker(selection: $meetingDay)
                            .frame(height: 148)
                            .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
                    }
                }
                .padding(.horizontal)

                if let errorMessage {
                    Text(errorMessage)
                        .font(.captionStyle)
                        .foregroundStyle(Color.blush)
                        .padding(.horizontal)
                }

                NeuButton("Guardar Cambios", icon: AppIcon.checkCircle,
                          variant: .primary, isLoading: isSaving) {
                    Task { await save() }
                }
                .disabled(!hasChanges)
                .opacity(hasChanges ? 1 : 0.5)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Configuración del Grupo")
        .toolbar(.visible, for: .navigationBar)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            name = group.name
            meetingDay = group.meetingDay ?? "Sábado"
        }
        .alert("¡Guardado!", isPresented: $showingSuccess) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Los cambios fueron guardados exitosamente.")
        }
    }

    private func save() async {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "El nombre del grupo es obligatorio."
            return
        }
        isSaving = true
        errorMessage = nil
        var updated = group
        updated.name = name.trimmingCharacters(in: .whitespaces)
        updated.meetingDay = meetingDay
        do {
            let saved = try await services.group.updateGroup(updated)
            HapticFeedback.success()
            onSaved?(saved)
            showingSuccess = true
        } catch {
            errorMessage = "No se pudo guardar. Intenta de nuevo."
        }
        isSaving = false
    }
}

#Preview {
    NavigationStack {
        GroupSettingsView(group: MockData.groups[0])
            .environment(\.services, .preview)
    }
    .background(Color.neuBackground.ignoresSafeArea())
}
