import Foundation

final class MockActivityLogService: ActivityLogService {
    func getLogs(groupId: UUID) async throws -> [ActivityLogEntry] {
        try await Task.sleep(for: .seconds(0.3))
        return [
            ActivityLogEntry(
                id: UUID(), profileId: MockData.profileId, smallGroupId: groupId,
                actionType: "meeting_submitted",
                description: "María González envió la reunión del Lunes 6 de Abril",
                createdAt: Date()
            ),
            ActivityLogEntry(
                id: UUID(), profileId: MockData.profileId, smallGroupId: groupId,
                actionType: "member_added",
                description: "María González agregó al miembro Juan Pérez",
                createdAt: Date().addingTimeInterval(-3600)
            ),
        ]
    }

    func log(profileId: UUID, groupId: UUID, actionType: String, description: String) async throws {
        try await Task.sleep(for: .seconds(0.1))
    }
}
