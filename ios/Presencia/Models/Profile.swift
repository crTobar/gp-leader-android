import Foundation

// MARK: - Profile (App users with login)

struct Profile: Identifiable, Codable, Hashable {
    let id: UUID
    var firstName: String
    var lastName: String
    var email: String?
    var phone: String?
    var avatarUrl: String?
    var isActive: Bool
    let createdAt: Date
    var updatedAt: Date

    var fullName: String { "\(firstName) \(lastName)" }
    var initials: String { "\(firstName.prefix(1))\(lastName.prefix(1))".uppercased() }
}

// MARK: - Role Assignment

struct RoleAssignment: Identifiable, Codable, Hashable {
    let id: UUID
    let profileId: UUID
    let role: UserRole
    var smallGroupId: UUID?
    var churchId: UUID?
    var districtId: UUID?
    var campoId: UUID?
    var unionId: UUID?
    var title: String?
    let createdAt: Date
}
