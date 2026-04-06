import Foundation

// MARK: - Type-safe navigation routes

enum AppRoute: Hashable {
    // Meetings
    case meetingDetail(Meeting)
    case newMeeting
    case newMeetingEdit(Meeting)

    // Members
    case memberList
    case memberDetail(Member)
    case memberForm(Member?)

    // Reports
    case reports

    // Profile
    case profile(Profile, SmallGroup?, UserRole)
    case personalData(Profile)
    // Activity Log
    case activityLog

    // Settings
    case groupSettings
    case activityTypes

    // Admin
    case adminChurches
    case adminDistricts
    case adminUsers
    case adminPeriods
}
