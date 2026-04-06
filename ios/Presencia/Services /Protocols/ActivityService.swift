import Foundation

protocol ActivityService {
    func getActivityTypes(scope: ActivityScope?, campoId: UUID?, churchId: UUID?) async throws -> [ActivityType]
    func getActivityRecords(meetingId: UUID) async throws -> [ActivityRecord]
    func upsertActivityRecord(_ record: ActivityRecord) async throws
    func createActivityType(_ type: ActivityType) async throws -> ActivityType
}
