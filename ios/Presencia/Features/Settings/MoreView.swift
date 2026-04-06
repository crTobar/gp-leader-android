import SwiftUI

// MARK: - Perfil Tab (Kotlin PerfilPrincipalScreen parity)

struct MoreView: View {
    let profile: Profile
    let role: UserRole
    let group: SmallGroup?
    let groupId: UUID

    let onPersonalData: () -> Void
    var onSwitchSessionMember: (() -> Void)? = nil
    let onMembers: () -> Void
    let onActivityLog: () -> Void
    let onReports: () -> Void
    let onGroupSettings: () -> Void
    let onActivityTypes: () -> Void
    let onAdmin: () -> Void
    let onSignOut: () -> Void

    @Environment(\.services) private var services

    @State private var activeMemberCount = 0
    @State private var showingSignOutDialog = false
    @State private var showingNotificationsPlaceholder = false

    private var permissions: Permissions { Permissions(role: role) }

    private var groupDisplayName: String {
        if let name = group?.name, !name.isEmpty { return name }
        return "Mi grupo"
    }

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Text("Perfil")
                        .font(.displayLarge)
                        .foregroundStyle(Color.textPrimary)
                        .padding(.horizontal, 20)
                        .padding(.top, 12)
                        .padding(.bottom, 8)

                    avatarCard
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)

                    sectionLabel("MI CUENTA")
                    sectionCard {
                        profileMenuRow(label: "Datos personales", action: onPersonalData)
                        if let onSwitchSessionMember {
                            sectionDivider
                            profileMenuRow(label: "Cambiar quién usa la app", action: onSwitchSessionMember)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 4)

                    sectionLabel("MI GRUPO")
                    sectionCard {
                        profileMenuRow(label: "Datos del grupo", action: onGroupSettings)
                        sectionDivider
                        profileMenuRow(
                            label: "Miembros",
                            badgeText: "\(activeMemberCount)",
                            action: onMembers
                        )
                        sectionDivider
                        profileMenuRow(label: "Registro de actividad", action: onActivityLog)
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 4)

                    sectionLabel("PREFERENCIAS")
                    sectionCard {
                        profileMenuRow(label: "Notificaciones") {
                            showingNotificationsPlaceholder = true
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 4)

                    sectionLabel("HERRAMIENTAS")
                    toolsSectionCard
                        .padding(.horizontal, 20)
                        .padding(.bottom, 4)

                    Spacer(minLength: 16)

                    sectionCard {
                        signOutRow
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 8)

                    Text("Presencia v1.0.0")
                        .font(.captionSmall)
                        .foregroundStyle(Color.textMuted)
                        .frame(maxWidth: .infinity)
                        .multilineTextAlignment(.center)
                        .padding(.top, 24)
                        .padding(.bottom, 8)
                }
                .padding(.bottom, 24)
            }

            signOutOverlay
        }
        .toolbar(.hidden, for: .navigationBar)
        .task(id: groupId) { await loadMemberCount() }
        .alert("Próximamente", isPresented: $showingNotificationsPlaceholder) {
            Button("Aceptar", role: .cancel) {}
        } message: {
            Text("Las notificaciones estarán disponibles en una próxima versión.")
        }
    }

    // MARK: - Avatar

    private var avatarCard: some View {
        ZStack(alignment: .topTrailing) {
            NeuCard(padding: 24, cornerRadius: NeuStyle.cardRadius) {
                VStack(spacing: 16) {
                    Text(profile.initials)
                        .font(.headlineMedium)
                        .foregroundStyle(.white)
                        .frame(width: 80, height: 80)
                        .background(Color.textPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))

                    Text(profile.fullName)
                        .font(.headingLarge)
                        .foregroundStyle(Color.textPrimary)
                        .multilineTextAlignment(.center)

                    Text(role.displayName)
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textSecondary)

                    Text(groupDisplayName)
                        .font(.bodyRegular)
                        .foregroundStyle(Color.accent)
                }
                .frame(maxWidth: .infinity)
            }

            Button {
                HapticFeedback.light()
                onPersonalData()
            } label: {
                LucideIcon(uiImage: AppIcon.edit, size: 16)
                    .foregroundStyle(Color.textMuted)
                    .frame(width: 36, height: 36)
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                    .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                    .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Editar perfil")
            .padding(.top, 12)
            .padding(.trailing, 12)
        }
    }

    // MARK: - Herramientas (extra vs Kotlin; keeps app features)

    @ViewBuilder
    private var toolsSectionCard: some View {
        sectionCard {
            if permissions.canAccessAdmin {
                profileMenuRow(label: "Administración", action: onAdmin)
                sectionDivider
            }
            profileMenuRow(label: "Reportes", action: onReports)
            if permissions.canManageActivityTypes {
                sectionDivider
                profileMenuRow(label: "Tipos de Actividades", action: onActivityTypes)
            }
        }
    }

    // MARK: - Cerrar sesión row

    private var signOutRow: some View {
        Button {
            HapticFeedback.light()
            withAnimation(.easeOut(duration: 0.2)) { showingSignOutDialog = true }
        } label: {
            HStack {
                Text("Cerrar sesión")
                    .font(.headingLarge)
                    .foregroundStyle(Color.blush)
                Spacer(minLength: 0)
                LucideIcon(uiImage: AppIcon.chevronRight, size: 18)
                    .foregroundStyle(Color.blush)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    // MARK: - Sign-out overlay

    @ViewBuilder
    private var signOutOverlay: some View {
        if showingSignOutDialog {
            ZStack {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .contentShape(Rectangle())
                    .onTapGesture {
                        withAnimation(.easeOut(duration: 0.2)) { showingSignOutDialog = false }
                    }

                SignOutDialogCard(
                    onConfirm: {
                        onSignOut()
                        showingSignOutDialog = false
                    },
                    onCancel: {
                        withAnimation(.easeOut(duration: 0.2)) { showingSignOutDialog = false }
                    }
                )
                .padding(.horizontal, 32)
                .transition(.scale(scale: 0.92).combined(with: .opacity))
            }
            .animation(.easeOut(duration: 0.2), value: showingSignOutDialog)
        }
    }

    // MARK: - Section chrome

    private func sectionLabel(_ text: String) -> some View {
        Text(text)
            .font(.captionStyle)
            .foregroundStyle(Color.textMuted)
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 8)
    }

    private var sectionDivider: some View {
        Rectangle()
            .fill(Color.neuShadowDark.opacity(0.35))
            .frame(height: 1)
            .padding(.leading, 20)
    }

    private func sectionCard<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        content()
            .background(Color.neuBackground)
            .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
            .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
            .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
    }

    private func profileMenuRow(
        label: String,
        badgeText: String? = nil,
        action: @escaping () -> Void
    ) -> some View {
        Button {
            HapticFeedback.light()
            action()
        } label: {
            HStack(spacing: 8) {
                Text(label)
                    .font(.headingLarge)
                    .foregroundStyle(Color.textPrimary)
                Spacer(minLength: 8)
                if let badgeText {
                    Text(badgeText)
                        .font(.captionSmall)
                        .bold()
                        .foregroundStyle(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.textPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                }
                LucideIcon(uiImage: AppIcon.chevronRight, size: 16)
                    .foregroundStyle(Color.textMuted)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    // MARK: - Data

    @MainActor
    private func loadMemberCount() async {
        do {
            let members = try await services.members.getMembers(groupId: groupId)
            activeMemberCount = members.filter(\.isActive).count
        } catch {
            activeMemberCount = 0
        }
    }
}

// MARK: - Sign-out dialog (Kotlin CerrarSesionDialog parity)

private struct SignOutDialogCard: View {
    let onConfirm: () -> Void
    let onCancel: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text("¿Cerrar sesión?")
                .font(.titleLarge)
                .foregroundStyle(Color.textPrimary)
                .multilineTextAlignment(.center)

            Text("Tu progreso sin sincronizar se mantendrá guardado en el dispositivo.")
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
                .multilineTextAlignment(.center)

            Spacer(minLength: 16)

            Button {
                HapticFeedback.medium()
                onConfirm()
            } label: {
                Text("Cerrar sesión")
                    .font(.headingLarge)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.blush)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            }
            .buttonStyle(ScalePressButtonStyle())

            Spacer(minLength: 10)

            NeuButton("Cancelar", variant: .secondary) {
                HapticFeedback.light()
                onCancel()
            }
        }
        .padding(24)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
    }
}

private struct ScalePressButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .animation(.easeInOut(duration: 0.15), value: configuration.isPressed)
    }
}

#Preview {
    NavigationStack {
        MoreView(
            profile: MockData.currentProfile,
            role: .leader,
            group: MockData.groups.first,
            groupId: MockData.group1Id,
            onPersonalData: {},
            onSwitchSessionMember: {},
            onMembers: {},
            onActivityLog: {},
            onReports: {},
            onGroupSettings: {},
            onActivityTypes: {},
            onAdmin: {},
            onSignOut: {}
        )
    }
    .environment(\.services, .preview)
}
