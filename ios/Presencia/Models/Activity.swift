import Foundation

// MARK: - Activity Type

struct ActivityType: Identifiable, Codable, Hashable {
    let id: UUID
    var name: String
    var description: String?
    var icon: String?
    var unitLabel: String
    var scope: ActivityScope
    var level: ActivityLevel = .myGroup  // UI hierarchy: union (locked) / pastor / myGroup
    var campoId: UUID?
    var churchId: UUID?
    var isActive: Bool
    var sortOrder: Int
    var createdBy: UUID?
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - Activity Record

struct ActivityRecord: Identifiable, Codable, Hashable {
    let id: UUID
    let meetingId: UUID
    let activityTypeId: UUID
    var count: Int
    var notes: String?
}
