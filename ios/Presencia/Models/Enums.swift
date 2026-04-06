import Foundation

// MARK: - User Roles

enum UserRole: String, Codable, CaseIterable, Hashable {
    case leader
    case coLeader = "co_leader"
    case anciano
    case pastor
    case campoAdmin = "campo_admin"
    case unionAdmin = "union_admin"

    var displayName: String {
        switch self {
        case .leader: return "Líder"
        case .coLeader: return "Co-Líder"
        case .anciano: return "Anciano"
        case .pastor: return "Pastor"
        case .campoAdmin: return "Admin de Campo"
        case .unionAdmin: return "Admin de Unión"
        }
    }
}

// MARK: - Meeting

enum MeetingStatus: String, Codable, Hashable {
    case draft
    case pendingSync = "pending_sync"
    case submitted
    case approved

    var displayName: String {
        switch self {
        case .draft:        return "Borrador"
        case .pendingSync:  return "Sincronizando"
        case .submitted:    return "Enviada"
        case .approved:     return "Aprobada"
        }
    }
}

// MARK: - Attendance

enum AttendanceStatus: String, Codable, Hashable {
    case present
    case absent
    case justified

    var displayName: String {
        switch self {
        case .present:   return "Presente"
        case .absent:    return "Ausente"
        case .justified: return "Justificado"
        }
    }
}

// MARK: - Activity Scope (administrative — who manages the activity type)

enum ActivityScope: String, Codable, Hashable {
    case global
    case campo
    case church

    var displayName: String {
        switch self {
        case .global: return "Global"
        case .campo:  return "Campo"
        case .church: return "Iglesia"
        }
    }
}

// MARK: - Activity Level (UI hierarchy in meeting registration)

enum ActivityLevel: String, Codable, Hashable, CaseIterable {
    case union            = "union"    // locked — set by Union, not editable
    case pastor           = "pastor"   // editable count, not customizable
    case myGroup          = "my_group" // editable + leader can add custom activities

    var displayName: String {
        switch self {
        case .union:   return "Unión"
        case .pastor:  return "Pastor"
        case .myGroup: return "Mi GP"
        }
    }

    var isLocked: Bool { self == .union }
}
