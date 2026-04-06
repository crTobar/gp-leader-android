import SwiftUI
import LucideIcons

// MARK: - Admin: User Management

struct AdminUsersView: View {
    @Environment(\.services) private var services
    @State private var vm: AdminUsersViewModel?

    var body: some View {
        Group {
            if let vm {
                adminContent(vm: vm)
            } else {
                LoadingStateView(rows: 4).padding()
            }
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Usuarios")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if vm == nil { vm = AdminUsersViewModel(services: services) }
            await vm?.load()
        }
    }

    @ViewBuilder
    private func adminContent(vm: AdminUsersViewModel) -> some View {
        Group {
            if vm.isLoading {
                LoadingStateView(rows: 4).padding()
            } else if let error = vm.errorMessage {
                EmptyStateView(
                    icon: AppIcon.usersGroup,
                    title: "Error",
                    subtitle: error
                )
            } else if vm.profiles.isEmpty {
                EmptyStateView(
                    icon: AppIcon.usersGroup,
                    title: "Sin usuarios",
                    subtitle: "No hay usuarios registrados."
                )
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(vm.profiles) { profile in
                            NeuCard(padding: 14, cornerRadius: NeuStyle.memberRowRadius) {
                                HStack(spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(Color.neuBackground)
                                            .frame(width: 42, height: 42)
                                            .shadow(color: Color.neuShadowDark.opacity(0.4),
                                                    radius: 4, x: 3, y: 3)
                                            .shadow(color: Color.neuShadowLight.opacity(0.9),
                                                    radius: 4, x: -3, y: -3)
                                        Text(profile.initials)
                                            .font(.captionStyle)
                                            .foregroundStyle(Color.accent)
                                    }
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(profile.fullName)
                                            .font(.bodyLarge)
                                            .foregroundStyle(Color.textPrimary)
                                        if let email = profile.email {
                                            Text(email)
                                                .font(.captionStyle)
                                                .foregroundStyle(Color.textMuted)
                                        }
                                    }
                                    Spacer()
                                    Circle()
                                        .fill(profile.isActive ? Color.statusPresent : Color.textMuted)
                                        .frame(width: 10, height: 10)
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
        }
    }
}

#Preview {
    NavigationStack { AdminUsersView() }
        .background(Color.neuBackground.ignoresSafeArea())
}
