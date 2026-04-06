import SwiftUI

// MARK: - Member Create / Edit (Kotlin MiembroAgregar / MiembroEditar parity)

struct MemberFormView: View {
    let member: Member?
    let groupId: UUID
    let onSave: (Member) -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(\.services) private var services

    @State private var firstName: String = ""
    @State private var secondName: String = ""
    @State private var lastName: String = ""
    @State private var secondLastName: String = ""
    @State private var phone: String = ""
    @State private var email: String = ""

    @State private var showSecondNameField = false
    @State private var showSecondLastNameField = false

    @State private var isVisitor: Bool = false
    @State private var isLoading: Bool = false
    @State private var showingArchiveConfirm: Bool = false

    @State private var firstNameError = false
    @State private var lastNameError = false
    @State private var validationError: String? = nil

    private var isEditing: Bool { member != nil }

    private var initialsPreview: String {
        let f = firstName.trimmingCharacters(in: .whitespaces).first
        let l = lastName.trimmingCharacters(in: .whitespaces).first
        if let f, let l { return "\(f)\(l)".uppercased() }
        if let f { return String(f).uppercased() }
        return "?"
    }

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                formTopBar

                ScrollView {
                    VStack(spacing: 12) {
                        if !isEditing {
                            avatarPreviewSection
                        }

                        sectionLabel(isEditing ? "NOMBRE" : "NOMBRE")

                        nameRow

                        expandSecondNameRow
                        if showSecondNameField {
                            NeuTextField(
                                placeholder: "Segundo nombre",
                                text: $secondName,
                                label: "SEGUNDO NOMBRE",
                                hasError: false
                            )
                            .transition(.opacity.combined(with: .move(edge: .top)))
                        }

                        expandSecondLastNameRow
                        if showSecondLastNameField {
                            NeuTextField(
                                placeholder: "Segundo apellido",
                                text: $secondLastName,
                                label: "SEGUNDO APELLIDO",
                                hasError: false
                            )
                            .transition(.opacity.combined(with: .move(edge: .top)))
                        }

                        if !isEditing {
                            Text("El miembro se agregará como Activo automáticamente")
                                .font(.captionStyle)
                                .foregroundStyle(Color.textMuted)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        sectionLabel("CONTACTO")

                        NeuTextField(
                            placeholder: "0000-0000",
                            text: $phone,
                            label: "TELÉFONO",
                            keyboardType: .phonePad,
                            hasError: false
                        )

                        NeuTextField(
                            placeholder: "correo@ejemplo.com",
                            text: $email,
                            label: "CORREO (opcional)",
                            keyboardType: .emailAddress,
                            hasError: false
                        )

                        if isEditing {
                            visitorCard
                            archiveButton
                        }

                        if let error = validationError {
                            Text(error)
                                .font(.captionStyle)
                                .foregroundStyle(Color.blush)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 8)
                    .padding(.bottom, 120)
                    .animation(.easeInOut(duration: 0.2), value: showSecondNameField)
                    .animation(.easeInOut(duration: 0.2), value: showSecondLastNameField)
                }

                bottomActionBar
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .onAppear(perform: hydrateFromMember)
        .confirmationDialog(
            "¿Archivar miembro?",
            isPresented: $showingArchiveConfirm,
            titleVisibility: .visible
        ) {
            Button("Archivar", role: .destructive) {
                Task { await archiveAction() }
            }
            Button("Cancelar", role: .cancel) {}
        } message: {
            Text("El miembro no aparecerá en nuevas reuniones. Puedes restaurarlo después.")
        }
    }

    // MARK: - Top bar

    private var formTopBar: some View {
        ZStack {
            HStack {
                Button {
                    HapticFeedback.light()
                    dismiss()
                } label: {
                    LucideIcon(uiImage: AppIcon.chevronLeft, size: 18)
                        .foregroundStyle(Color.textPrimary)
                        .padding(10)
                        .background(Color.neuBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Atrás")

                Spacer()

                if isEditing {
                    Button {
                        Task { await saveAction() }
                    } label: {
                        Text("Guardar")
                            .font(.captionStyle)
                            .foregroundStyle(Color.accent)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .background(Color.neuBackground)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                            .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                    }
                    .buttonStyle(.plain)
                    .disabled(isLoading)
                }
            }

            Text(isEditing ? "Editar miembro" : "Agregar miembro")
                .font(.headingLarge)
                .foregroundStyle(Color.textPrimary)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    // MARK: - Sections

    private func sectionLabel(_ text: String) -> some View {
        Text(text)
            .font(.captionStyle)
            .foregroundStyle(Color.textMuted)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 4)
    }

    private var avatarPreviewSection: some View {
        VStack(spacing: 10) {
            Text(initialsPreview)
                .font(.titleLarge)
                .foregroundStyle(.white)
                .frame(width: 72, height: 72)
                .background(Color.textPrimary)
                .clipShape(Circle())

            Text("Las iniciales se generan automáticamente")
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
        .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 8, x: -4, y: -4)
    }

    private var nameRow: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                NeuTextField(
                    placeholder: "María",
                    text: $firstName,
                    label: isEditing ? "PRIMER NOMBRE" : "PRIMER NOMBRE *",
                    hasError: firstNameError
                )
                if firstNameError {
                    Text("El primer nombre es obligatorio")
                        .font(.captionSmall)
                        .foregroundStyle(Color.blush)
                        .padding(.leading, 4)
                }
            }
            .frame(maxWidth: .infinity)

            VStack(alignment: .leading, spacing: 4) {
                NeuTextField(
                    placeholder: "García",
                    text: $lastName,
                    label: isEditing ? "PRIMER APELLIDO" : "PRIMER APELLIDO *",
                    hasError: lastNameError
                )
                if lastNameError {
                    Text("El primer apellido es obligatorio")
                        .font(.captionSmall)
                        .foregroundStyle(Color.blush)
                        .padding(.leading, 4)
                }
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var expandSecondNameRow: some View {
        Button {
            withAnimation { showSecondNameField.toggle() }
        } label: {
            HStack(spacing: 6) {
                LucideIcon(uiImage: showSecondNameField ? AppIcon.chevronUp : AppIcon.chevronDown, size: 12)
                Text("Agregar segundo nombre (opcional)")
                    .font(.captionStyle)
            }
            .foregroundStyle(Color.textMuted)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .buttonStyle(.plain)
    }

    private var expandSecondLastNameRow: some View {
        Button {
            withAnimation { showSecondLastNameField.toggle() }
        } label: {
            HStack(spacing: 6) {
                LucideIcon(uiImage: showSecondLastNameField ? AppIcon.chevronUp : AppIcon.chevronDown, size: 12)
                Text("Agregar segundo apellido (opcional)")
                    .font(.captionStyle)
            }
            .foregroundStyle(Color.textMuted)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .buttonStyle(.plain)
    }

    private var visitorCard: some View {
        NeuCard {
            HStack {
                LucideIcon(uiImage: AppIcon.userKey, size: 20)
                    .foregroundStyle(Color.accent)
                Text("Es visitante")
                    .font(.bodyLarge)
                    .foregroundStyle(Color.textPrimary)
                Spacer()
                Toggle("", isOn: $isVisitor)
                    .tint(Color.accent)
                    .labelsHidden()
            }
        }
    }

    private var archiveButton: some View {
        NeuButton("Archivar miembro", variant: .secondary) {
            showingArchiveConfirm = true
        }
    }

    // MARK: - Bottom bar

    private var bottomActionBar: some View {
        VStack(spacing: 10) {
            if isEditing {
                NeuButton("Guardar cambios", variant: .primary) {
                    Task { await saveAction() }
                }
                .disabled(isLoading)
            } else {
                NeuButton("+ Agregar al grupo", variant: .primary) {
                    Task { await saveAction() }
                }
                .disabled(isLoading)

                NeuButton("Cancelar", variant: .secondary) {
                    dismiss()
                }
                .disabled(isLoading)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity)
        .background(Color.neuBackground.ignoresSafeArea(edges: .bottom))
    }

    // MARK: - Lifecycle

    private func hydrateFromMember() {
        guard let m = member else { return }
        firstName = m.firstName
        secondName = m.secondName ?? ""
        lastName = m.lastName
        secondLastName = m.secondLastName ?? ""
        phone = m.phone ?? ""
        email = m.email ?? ""
        isVisitor = m.isVisitor
        showSecondNameField = !(m.secondName ?? "").trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        showSecondLastNameField = !(m.secondLastName ?? "").trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    // MARK: - Actions

    private func saveAction() async {
        let trimFirst = firstName.trimmingCharacters(in: .whitespaces)
        let trimLast = lastName.trimmingCharacters(in: .whitespaces)
        firstNameError = trimFirst.isEmpty
        lastNameError = trimLast.isEmpty
        guard !trimFirst.isEmpty, !trimLast.isEmpty else {
            validationError = nil
            HapticFeedback.warning()
            return
        }
        validationError = nil
        isLoading = true

        let newMember = Member(
            id: member?.id ?? UUID(),
            smallGroupId: groupId,
            firstName: trimFirst,
            secondName: secondName.trimmingCharacters(in: .whitespaces).nilIfEmptyString,
            lastName: trimLast,
            secondLastName: secondLastName.trimmingCharacters(in: .whitespaces).nilIfEmptyString,
            phone: phone.trimmingCharacters(in: .whitespaces).nilIfEmptyString,
            email: email.trimmingCharacters(in: .whitespaces).nilIfEmptyString,
            isVisitor: isEditing ? isVisitor : false,
            isActive: member?.isActive ?? true,
            createdAt: member?.createdAt ?? Date(),
            updatedAt: Date()
        )
        do {
            let saved: Member = isEditing
                ? try await services.members.updateMember(newMember)
                : try await services.members.createMember(newMember)
            HapticFeedback.success()
            onSave(saved)
        } catch {
            validationError = "No se pudo guardar. Intenta de nuevo."
            HapticFeedback.warning()
        }
        isLoading = false
    }

    private func archiveAction() async {
        guard let m = member else { return }
        isLoading = true
        do {
            try await services.members.toggleMemberActive(id: m.id)
            HapticFeedback.success()
            dismiss()
        } catch {
            validationError = "No se pudo archivar el miembro."
            HapticFeedback.warning()
        }
        isLoading = false
    }
}

// MARK: - String helper

private extension String {
    var nilIfEmptyString: String? {
        let t = trimmingCharacters(in: .whitespacesAndNewlines)
        return t.isEmpty ? nil : t
    }
}

#Preview("Nuevo") {
    MemberFormView(member: nil, groupId: MockData.group1Id) { _ in }
        .environment(\.services, .preview)
}

#Preview("Editar") {
    MemberFormView(member: MockData.members[0], groupId: MockData.group1Id) { _ in }
        .environment(\.services, .preview)
}
