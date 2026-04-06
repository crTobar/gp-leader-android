import Foundation

// MARK: - Role Permission Matrix

struct Permissions {
    let role: UserRole

    var canTakeAttendance: Bool {
        role == .leader || role == .coLeader
    }

    var canManageMembers: Bool {
        role == .leader || role == .coLeader
    }

    var canViewChurchReports: Bool {
        switch role {
        case .anciano, .pastor, .campoAdmin, .unionAdmin: return true
        default: return false
        }
    }

    var canViewCampoReports: Bool {
        role == .campoAdmin || role == .unionAdmin
    }

    var canManageActivityTypes: Bool {
        switch role {
        case .pastor, .campoAdmin, .unionAdmin: return true
        default: return false
        }
    }

    var canAccessAdmin: Bool {
        role == .campoAdmin || role == .unionAdmin
    }
}
