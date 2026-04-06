import Foundation

// MARK: - Activity Log

struct ActivityLogEntry: Identifiable, Codable {
    let id: UUID
    let profileId: UUID
    let smallGroupId: UUID
    let actionType: String
    let description: String
    let createdAt: Date
}

protocol ActivityLogService {
    func getLogs(groupId: UUID) async throws -> [ActivityLogEntry]
    func log(profileId: UUID, groupId: UUID, actionType: String, description: String) async throws
}
