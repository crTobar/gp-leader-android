import SwiftUI
import LucideIcons

// MARK: - Profile Hub

struct ProfileView: View {
    let profile: Profile
    let group: SmallGroup?
    let role: UserRole

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Avatar card
                NeuCard {
                    HStack(spacing: 16) {
                        ZStack {
                            Circle()
                                .fill(Color.neuBackground)
                                .frame(width: 64, height: 64)
                                .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 5, y: 5)
                                .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 8, x: -5, y: -5)
                            Text(profile.initials)
                                .font(.displayMedium)
                                .foregroundStyle(Color.accent)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text(profile.fullName)
                                .font(.headingMedium)
                                .foregroundStyle(Color.textPrimary)
                            if let email = profile.email {
                                Text(email)
                                    .font(.captionStyle)
                                    .foregroundStyle(Color.textMuted)
                            }
                        }
                        Spacer()
                    }
                }
                .padding(.horizontal)

                // Personal section
                ProfileSection(title: "Datos Personales") {
                    NavigationLink(value: ProfileRoute.personalData(profile)) {
                        ProfileMenuRow(icon: AppIcon.user, title: "Información Personal")
                    }
                }

                // Group section
                if let group {
                    ProfileSection(title: "Mi Grupo") {
                        NavigationLink(value: ProfileRoute.groupData(group)) {
                            ProfileMenuRow(icon: AppIcon.usersGroup, title: "Datos del Grupo")
                        }
                    }
                }
            }
            .padding(.vertical)
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Mi Perfil")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Profile Route (local nav)

enum ProfileRoute: Hashable {
    case personalData(Profile)
    case groupData(SmallGroup)
}

// MARK: - Section wrapper

private struct ProfileSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title.uppercased())
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
                .padding(.horizontal, 20)

            VStack(spacing: 8) {
                content()
            }
            .padding(.horizontal)
        }
    }
}

// MARK: - Menu row

struct ProfileMenuRow: View {
    let icon: UIImage
    let title: String

    var body: some View {
        HStack(spacing: 14) {
            NeuIconBadge(icon: icon)
            Text(title)
                .font(.bodyLarge)
                .foregroundStyle(Color.textPrimary)
            Spacer()
            Image(uiImage: AppIcon.chevronRight)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 12, height: 12)
                .foregroundStyle(Color.textMuted)
        }
        .padding(16)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
        .neuRaised()
    }
}

#Preview {
    NavigationStack {
        ProfileView(profile: MockData.currentProfile, group: MockData.groups.first, role: .leader)
    }
    .environment(\.services, .preview)
}
