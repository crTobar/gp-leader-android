import SwiftUI

// MARK: - Choose which GP member is using the device after login

struct MemberSessionPickerView: View {
    let groupId: UUID
    let onSelected: (Member) -> Void

    @Environment(\.services) private var services

    @State private var members: [Member] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    private var selectableMembers: [Member] {
        members
            .filter { $0.isActive && !$0.isVisitor }
            .sorted { $0.displayName.localizedCaseInsensitiveCompare($1.displayName) == .orderedAscending }
    }

    var body: some View {
        ZStack {
            Color.neuBackground
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    header
                        .padding(.horizontal, 20)
                        .padding(.top, 28)
                        .padding(.bottom, 16)

                    if isLoading {
                        ProgressView()
                            .tint(Color.accent)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 48)
                    } else if let errorMessage {
                        VStack(spacing: 16) {
                            Text(errorMessage)
                                .font(.bodyRegular)
                                .foregroundStyle(Color.blush)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 20)
                            NeuButton("Reintentar", variant: .primary, action: { Task { await load() } })
                                .padding(.horizontal, 20)
                        }
                        .padding(.vertical, 24)
                    } else if selectableMembers.isEmpty {
                        EmptyStateView(
                            icon: AppIcon.members,
                            title: "Sin miembros para elegir",
                            subtitle: "No hay miembros activos del grupo en la lista. Pide al líder que registre al grupo o revisa la conexión.",
                            ctaTitle: "Reintentar"
                        ) {
                            Task { await load() }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 16)
                    } else {
                        NeuCard(padding: 18, cornerRadius: NeuStyle.cardRadius) {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("MIEMBROS DEL GRUPO")
                                    .font(.captionStyle)
                                    .foregroundStyle(Color.textMuted)

                                ForEach(selectableMembers) { member in
                                    Button {
                                        HapticFeedback.success()
                                        onSelected(member)
                                    } label: {
                                        NeuMemberRow(member: member) {
                                            Image(uiImage: AppIcon.chevronRight)
                                                .renderingMode(.template)
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 18, height: 18)
                                                .foregroundStyle(Color.textMuted)
                                        }
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 32)
                    }
                }
            }
        }
        .task { await load() }
    }

    private var header: some View {
        VStack(spacing: 12) {
            NeuIconBadge(icon: AppIcon.members, size: 56)

            Text("¿Quién entra?")
                .font(.displayMedium)
                .foregroundStyle(Color.textPrimary)

            Text("Elige tu nombre solo para saber quién tiene el dispositivo. Te lo volveremos a pedir cada vez que inicies sesión. Todos los miembros ven la misma información del grupo pequeño: reuniones, asistencia, lista de miembros y reportes.")
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
    }

    @MainActor
    private func load() async {
        isLoading = true
        errorMessage = nil
        do {
            members = try await services.members.getMembers(groupId: groupId)
        } catch let decoding as DecodingError {
            errorMessage = "El servidor respondió bien (200) pero el formato de los miembros no coincide con la app. \(decoding.presenciaDebugDescription)"
            members = []
        } catch {
            errorMessage = "No se pudieron cargar los miembros. \(error.localizedDescription)"
            members = []
        }
        isLoading = false
    }
}

#Preview {
    MemberSessionPickerView(groupId: MockData.group1Id) { _ in }
        .environment(\.services, .preview)
}
