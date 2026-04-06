import SwiftUI
import LucideIcons
import Supabase

// MARK: - App Entry Point

@main
struct PresenciaApp: App {

    init() {
        configureTabBarAppearance()
    }

    var body: some Scene {
        WindowGroup {
            AppRootView()
        }
    }
}

// MARK: - Root (auth + services)

private struct AppRootView: View {
    @State private var authState: AuthState = .loading
    @State private var currentProfile: Profile?
    @State private var activeRoleAssignment: RoleAssignment?

    /// Small-group member using this device for the current authenticated session.
    @State private var sessionMember: Member?
    @State private var isResolvingSessionMember = true

    /// Stable id so we re-resolve when profile or assigned group changes.
    private var sessionResolutionId: String? {
        guard authState == .authenticated,
              let profile = currentProfile,
              let role = activeRoleAssignment,
              let groupId = role.smallGroupId else { return nil }
        return "\(profile.id.uuidString)_\(groupId.uuidString)"
    }

    var body: some View {
        Group {
            switch authState {
            case .loading:
                SplashView()
            case .unauthenticated:
                LoginView(
                    onSignedIn: {
                        Task { await refreshAuthenticatedSession() }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .authenticated:
                authenticatedShell
            }
        }
        .environment(\.services, .live)
        // Run in parallel: (1) restore session + profile with a hard cap so `.loading` cannot hang forever
        // on slow/hung Supabase calls; (2) subscribe to auth stream without blocking on (1).
        .task {
            await runInitialAuthResolution(timeoutSeconds: 12)
        }
        .task {
            await observeAuthState()
        }
        .task(id: sessionResolutionId) {
            guard sessionResolutionId != nil else { return }
            await resolveSessionMemberFromServer()
        }
    }

    @ViewBuilder
    private var authenticatedShell: some View {
        if let profile = currentProfile, let role = activeRoleAssignment {
            let groupId = role.smallGroupId ?? UUID()
            let storageKey = SessionMemberStorage.userDefaultsKey(profileId: profile.id, groupId: groupId)

            if isResolvingSessionMember {
                SplashView()
            } else if let member = sessionMember {
                MainTabView(
                    profile: profile,
                    roleAssignment: role,
                    onSwitchSessionMember: {
                        UserDefaults.standard.removeObject(forKey: storageKey)
                        sessionMember = nil
                    }
                )
                .environment(\.sessionMember, member)
            } else {
                MemberSessionPickerView(groupId: groupId) { selected in
                    sessionMember = selected
                }
            }
        } else {
            SplashView()
        }
    }

    /// Resolves Keychain session and `profile`/`role_assignment`. If Supabase never returns, still leaves `.loading`.
    @MainActor
    private func runInitialAuthResolution(timeoutSeconds: UInt64) async {
        await withTaskGroup(of: Bool.self) { group in
            group.addTask {
                await refreshAuthenticatedSession()
                return false
            }
            group.addTask {
                try? await Task.sleep(nanoseconds: timeoutSeconds * 1_000_000_000)
                return true
            }
            guard let first = await group.next() else {
                group.cancelAll()
                return
            }
            group.cancelAll()
            if first, case .loading = authState {
                currentProfile = nil
                activeRoleAssignment = nil
                sessionMember = nil
                isResolvingSessionMember = true
                authState = .unauthenticated
            }
        }
    }

    @MainActor
    private func observeAuthState() async {
        for await (_, session) in supabase.auth.authStateChanges {
            if let session, !session.isExpired {
                await refreshAuthenticatedSession()
            } else {
                currentProfile = nil
                activeRoleAssignment = nil
                sessionMember = nil
                isResolvingSessionMember = true
                authState = .unauthenticated
            }
        }
    }

    @MainActor
    private func refreshAuthenticatedSession() async {
        let container = ServiceContainer.live
        guard let profile = await container.auth.getCurrentProfile() else {
            currentProfile = nil
            activeRoleAssignment = nil
            sessionMember = nil
            isResolvingSessionMember = true
            authState = .unauthenticated
            return
        }
        do {
            let roles = try await container.auth.getRoleAssignments(profileId: profile.id)
            currentProfile = profile
            guard let firstRole = roles.first else {
                sessionMember = nil
                isResolvingSessionMember = true
                authState = .unauthenticated
                return
            }
            activeRoleAssignment = firstRole
            authState = .authenticated
        } catch {
            sessionMember = nil
            isResolvingSessionMember = true
            authState = .unauthenticated
        }
    }

    /// Clears prior device-user choice so the member picker always runs for this authenticated session.
    /// (No UserDefaults restore — shared devices should confirm who is using the app after each sign-in.)
    @MainActor
    private func resolveSessionMemberFromServer() async {
        isResolvingSessionMember = true
        defer { isResolvingSessionMember = false }

        guard let profile = currentProfile, let role = activeRoleAssignment else { return }
        let groupId = role.smallGroupId ?? UUID()
        let key = SessionMemberStorage.userDefaultsKey(profileId: profile.id, groupId: groupId)
        UserDefaults.standard.removeObject(forKey: key)
        sessionMember = nil
    }
}

// MARK: - Tab Bar Appearance

private func configureTabBarAppearance() {
    let bg = UIColor(red: 0xec/255.0, green: 0xee/255.0, blue: 0xf1/255.0, alpha: 1)
    let accent = UIColor(red: 0x4a/255.0, green: 0x7f/255.0, blue: 0xd4/255.0, alpha: 1)
    let inactive = UIColor(red: 0x9a/255.0, green: 0xa4/255.0, blue: 0xb2/255.0, alpha: 1)

    let appearance = UITabBarAppearance()
    appearance.configureWithOpaqueBackground()
    appearance.backgroundColor = bg
    appearance.shadowColor = UIColor(white: 0.7, alpha: 0.4)

    let normal = appearance.stackedLayoutAppearance
    normal.normal.iconColor = inactive
    normal.normal.titleTextAttributes = [.foregroundColor: inactive]
    normal.selected.iconColor = accent
    normal.selected.titleTextAttributes = [.foregroundColor: accent]

    UITabBar.appearance().standardAppearance = appearance
    UITabBar.appearance().scrollEdgeAppearance = appearance
}

// MARK: - Auth State

private enum AuthState {
    case loading
    case unauthenticated
    case authenticated
}

// MARK: - Splash Screen

struct SplashView: View {
    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()
            VStack(spacing: 20) {
                Image(uiImage: AppIcon.book)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .foregroundStyle(Color.accent)
                Text("Presencia")
                    .font(.displayLarge)
                    .foregroundStyle(Color.textPrimary)
                Text("Grupos Pequeños")
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textSecondary)
                ProgressView()
                    .tint(Color.accent)
                    .padding(.top, 8)
            }
        }
    }
}

#Preview {
    SplashView()
}
