import Foundation

final class MockActivityService: ActivityService {
    private var types = MockData.activityTypes
    private var recordStore: [UUID: [ActivityRecord]] = [:]

    init() {
        for meeting in MockData.meetings {
            recordStore[meeting.id] = MockData.activityRecords(for: meeting)
        }
    }

    func getActivityTypes(scope: ActivityScope?, campoId: UUID?, churchId: UUID?) async throws -> [ActivityType] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        return types.filter { type in
            if let scope = scope, type.scope != scope { return false }
            return true
        }
    }

    func getActivityRecords(meetingId: UUID) async throws -> [ActivityRecord] {
        try await Task.sleep(for: .seconds(0.3))
        return recordStore[meetingId] ?? []
    }

    func upsertActivityRecord(_ record: ActivityRecord) async throws {
        try await Task.sleep(for: .seconds(0.3))
        var records = recordStore[record.meetingId] ?? []
        if let index = records.firstIndex(where: { $0.activityTypeId == record.activityTypeId }) {
            records[index] = record
        } else {
            records.append(record)
        }
        recordStore[record.meetingId] = records
    }

    func createActivityType(_ type: ActivityType) async throws -> ActivityType {
        try await Task.sleep(for: .seconds(0.5))
        types.append(type)
        return type
    }
}
