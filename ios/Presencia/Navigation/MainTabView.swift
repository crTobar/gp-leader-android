import SwiftUI

// MARK: - Root Tab View

struct MainTabView: View {
    let profile: Profile
    let roleAssignment: RoleAssignment
    /// Clears the device “who is using the app” choice so the member picker shows again.
    var onSwitchSessionMember: (() -> Void)? = nil

    @Environment(\.services) private var services

    @State private var selectedTab: Int = 0

    // Tab navigation paths
    @State private var homeNavPath      = NavigationPath()
    @State private var meetingsNavPath  = NavigationPath()
    @State private var moreNavPath      = NavigationPath()

    // Sheet presentation
    @State private var showingNewMeeting = false

    // Loaded from Supabase
    @State private var currentGroup: SmallGroup? = nil

    private var groupId:  UUID { roleAssignment.smallGroupId ?? UUID() }
    private var churchId: UUID { roleAssignment.churchId ?? UUID() }
    private var campoId:  UUID { roleAssignment.campoId ?? UUID() }
    private var permissions: Permissions { Permissions(role: roleAssignment.role) }

    private let tabItems: [NeuTabItem] = [
        NeuTabItem(icon: AppIcon.home, label: "INICIO"),
        NeuTabItem(icon: AppIcon.calendar, label: "HISTORIAL"),
        NeuTabItem(icon: AppIcon.userCircle, label: "PERFIL"),
    ]

    var body: some View {
        ZStack(alignment: .bottom) {
            // No `TabView` + `.page` — that style is a horizontal pager and steals sideways drags
            // (e.g. member archive swipe), shifting the whole screen. Switch tabs only via NeuTabBar.
            Group {
                switch selectedTab {
                case 0:
                    NavigationStack(path: $homeNavPath) {
                        HomeView(
                            onTakeAttendance: { showingNewMeeting = true },
                            onViewHistory: { selectedTab = 1 },
                            onMeetingSelected: { homeNavPath.append(AppRoute.meetingDetail($0)) }
                        )
                        .navigationDestination(for: AppRoute.self) { route in routeDestination(route, path: $homeNavPath) }
                    }
                case 1:
                    NavigationStack(path: $meetingsNavPath) {
                        MeetingListView(
                            groupId: groupId,
                            onNewMeeting: { showingNewMeeting = true },
                            onMeetingSelected: { meetingsNavPath.append(AppRoute.meetingDetail($0)) }
                        )
                        .navigationDestination(for: AppRoute.self) { route in routeDestination(route, path: $meetingsNavPath) }
                    }
                case 2:
                    NavigationStack(path: $moreNavPath) {
                        MoreView(
                            profile: profile,
                            role: roleAssignment.role,
                            group: currentGroup,
                            groupId: groupId,
                            onPersonalData: {
                                moreNavPath.append(AppRoute.personalData(profile))
                            },
                            onSwitchSessionMember: onSwitchSessionMember,
                            onMembers: {
                                moreNavPath.append(AppRoute.memberList)
                            },
                            onActivityLog: { moreNavPath.append(AppRoute.activityLog) },
                            onReports:       { moreNavPath.append(AppRoute.reports) },
                            onGroupSettings: { moreNavPath.append(AppRoute.groupSettings) },
                            onActivityTypes: { moreNavPath.append(AppRoute.activityTypes) },
                            onAdmin:         { moreNavPath.append(AppRoute.adminChurches) },
                            onSignOut:       { Task { try? await services.auth.signOut() } }
                        )
                        .navigationDestination(for: AppRoute.self) { route in routeDestination(route, path: $moreNavPath) }
                    }
                default:
                    NavigationStack(path: $homeNavPath) {
                        HomeView(
                            onTakeAttendance: { showingNewMeeting = true },
                            onViewHistory: { selectedTab = 1 },
                            onMeetingSelected: { homeNavPath.append(AppRoute.meetingDetail($0)) }
                        )
                        .navigationDestination(for: AppRoute.self) { route in routeDestination(route, path: $homeNavPath) }
                    }
                }
            }
            .ignoresSafeArea(edges: .bottom)

            NeuTabBar(selectedTab: $selectedTab, items: tabItems)
        }
        .tint(Color.accent)
        .task {
            currentGroup = try? await services.group.getGroup(id: groupId)
        }
        .sheet(isPresented: $showingNewMeeting) {
            NewMeetingView(
                groupId: groupId,
                profileId: profile.id,
                onDone:   { showingNewMeeting = false },
                onCancel: { showingNewMeeting = false }
            )
        }
    }

    // MARK: - Route Destinations

    @ViewBuilder
    private func routeDestination(_ route: AppRoute, path: Binding<NavigationPath>) -> some View {
        switch route {
        case .memberList:
            MemberListView(
                groupId: groupId,
                profileId: profile.id,
                onMemberSelected: { moreNavPath.append(AppRoute.memberDetail($0)) }
            )
        case .meetingDetail(let meeting):
            MeetingDetailView(meeting: meeting, onEdit: {
                path.wrappedValue.append(AppRoute.newMeetingEdit(meeting))
            })
        case .newMeeting:
            NewMeetingView(
                groupId: groupId, profileId: profile.id,
                onDone:   { meetingsNavPath.removeLast() },
                onCancel: { meetingsNavPath.removeLast() }
            )
        case .memberDetail(let member):
            MemberDetailView(
                member: member,
                groupId: groupId,
                onEdit: { current in
                    moreNavPath.append(AppRoute.memberForm(current))
                }
            )
            .id(member.id)
        case .memberForm(let member):
            MemberFormView(member: member, groupId: groupId) { _ in
                moreNavPath.removeLast()
            }
        case .profile(let prof, let grp, let r):
            ProfileContainerView(profile: prof, group: grp, role: r)
        case .personalData(let prof):
            PersonalDataView(profile: prof) { _ in }
        case .reports:
            ReportsView(groupId: groupId, churchId: churchId, role: roleAssignment.role)
        case .groupSettings:
            if let group = currentGroup {
                GroupSettingsView(group: group) { updated in
                    currentGroup = updated
                }
            } else {
                LoadingStateView(rows: 3).padding()
            }
        case .activityLog:
            ActivityLogView(groupId: groupId)
        case .activityTypes:
            ActivityTypeManagementView()
        case .adminChurches:
            AdminChurchesView(campoId: campoId)
        case .adminDistricts:
            AdminDistrictsView(campoId: campoId)
        case .adminUsers:
            AdminUsersView()
        case .adminPeriods:
            AdminPeriodsView(campoId: campoId)
        case .newMeetingEdit(let meeting):
            NewMeetingView(
                groupId: meeting.smallGroupId,
                profileId: profile.id,
                editingMeeting: meeting,
                onDone: {
                    if !path.wrappedValue.isEmpty { path.wrappedValue.removeLast() }
                },
                onCancel: {
                    if !path.wrappedValue.isEmpty { path.wrappedValue.removeLast() }
                }
            )
        }
    }
}

#Preview {
    MainTabView(
        profile: MockData.currentProfile,
        roleAssignment: MockData.leaderRoleAssignment,
        onSwitchSessionMember: {}
    )
    .environment(\.services, .preview)
    .environment(\.sessionMember, MockData.members.first)
}
