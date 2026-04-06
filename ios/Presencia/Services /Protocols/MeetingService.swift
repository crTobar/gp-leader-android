import Foundation

protocol MeetingService {
    func getMeetings(groupId: UUID, dateRange: ClosedRange<Date>?) async throws -> [Meeting]
    func getMeeting(id: UUID) async throws -> Meeting
    func createMeeting(_ meeting: Meeting) async throws -> Meeting
    func updateMeeting(_ meeting: Meeting) async throws -> Meeting
    func submitMeeting(id: UUID, submittedBy: UUID) async throws
    func deleteDraft(id: UUID) async throws
}
