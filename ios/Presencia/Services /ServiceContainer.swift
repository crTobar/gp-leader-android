import SwiftUI

// MARK: - Service Container

struct ServiceContainer: @unchecked Sendable {
    let auth: any AuthService
    let hierarchy: any HierarchyService
    let members: any MemberService
    let meetings: any MeetingService
    let attendance: any AttendanceService
    let activities: any ActivityService
    let reports: any ReportService
    let group: any GroupService
    let activityLog: any ActivityLogService
}

// MARK: - Default (Live Supabase) Container

extension ServiceContainer {
    static let live = ServiceContainer(
        auth: SupabaseAuthService(),
        hierarchy: SupabaseHierarchyService(),
        members: SupabaseMemberService(),
        meetings: SupabaseMeetingService(),
        attendance: SupabaseAttendanceService(),
        activities: SupabaseActivityService(),
        reports: SupabaseReportService(),
        group: SupabaseGroupService(),
        activityLog: SupabaseActivityLogService()
    )

    // Mock container for SwiftUI Previews
    static let preview = ServiceContainer(
        auth: MockAuthService(),
        hierarchy: MockHierarchyService(),
        members: MockMemberService(),
        meetings: MockMeetingService(),
        attendance: MockAttendanceService(),
        activities: MockActivityService(),
        reports: MockReportService(),
        group: MockGroupService(),
        activityLog: MockActivityLogService()
    )
}

// MARK: - SwiftUI Environment

struct ServiceContainerKey: EnvironmentKey {
    static let defaultValue: ServiceContainer = .live
}

// MARK: - Session member environment (device user — GP attendee on a shared login)

/// Legacy key used only to clear old saved member ids (app no longer restores session member from disk).
enum SessionMemberStorage {
    static func userDefaultsKey(profileId: UUID, groupId: UUID) -> String {
        "presencia.sessionMember.\(profileId.uuidString.lowercased()).\(groupId.uuidString.lowercased())"
    }
}

private struct SessionMemberKey: EnvironmentKey {
    static let defaultValue: Member? = nil
}

extension EnvironmentValues {
    var services: ServiceContainer {
        get { self[ServiceContainerKey.self] }
        set { self[ServiceContainerKey.self] = newValue }
    }

    /// Active small-group member using this device for the current login session.
    var sessionMember: Member? {
        get { self[SessionMemberKey.self] }
        set { self[SessionMemberKey.self] = newValue }
    }
}
