import Foundation

final class MockMeetingService: MeetingService {
    private var meetings = MockData.meetings

    func getMeetings(groupId: UUID, dateRange: ClosedRange<Date>?) async throws -> [Meeting] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        var result = meetings.filter { $0.smallGroupId == groupId }
        if let range = dateRange {
            result = result.filter { range.contains($0.meetingDate) }
        }
        return result.sorted { $0.meetingDate > $1.meetingDate }
    }

    func getMeeting(id: UUID) async throws -> Meeting {
        try await Task.sleep(for: .seconds(0.3))
        guard let meeting = meetings.first(where: { $0.id == id }) else {
            throw NSError(domain: "meetings", code: 404, userInfo: [NSLocalizedDescriptionKey: "Reunión no encontrada"])
        }
        return meeting
    }

    func createMeeting(_ meeting: Meeting) async throws -> Meeting {
        try await Task.sleep(for: .seconds(0.5))
        meetings.append(meeting)
        return meeting
    }

    func updateMeeting(_ meeting: Meeting) async throws -> Meeting {
        try await Task.sleep(for: .seconds(0.3))
        if let index = meetings.firstIndex(where: { $0.id == meeting.id }) {
            meetings[index] = meeting
        }
        return meeting
    }

    func submitMeeting(id: UUID, submittedBy: UUID) async throws {
        try await Task.sleep(for: .seconds(0.5))
        if let index = meetings.firstIndex(where: { $0.id == id }) {
            meetings[index].status = .submitted
            meetings[index].submittedAt = Date()
            meetings[index].submittedBy = submittedBy
        }
    }

    func deleteDraft(id: UUID) async throws {
        try await Task.sleep(for: .seconds(0.3))
        meetings.removeAll { $0.id == id && $0.status == .draft }
    }
}
