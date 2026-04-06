import SwiftUI

// Handles sub-navigation for ProfileRoute within the existing NavigationStack
struct ProfileContainerView: View {
    @State private var profile: Profile
    @State private var group: SmallGroup?
    let role: UserRole

    init(profile: Profile, group: SmallGroup?, role: UserRole) {
        _profile = State(initialValue: profile)
        _group   = State(initialValue: group)
        self.role = role
    }

    var body: some View {
        ProfileView(profile: profile, group: group, role: role)
            .navigationDestination(for: ProfileRoute.self) { route in
                switch route {
                case .personalData(let p):
                    PersonalDataView(profile: p) { updated in
                        profile = updated
                    }
                case .groupData(let g):
                    GroupDataView(group: g) { updated in
                        group = updated
                    }
                }
            }
    }
}
