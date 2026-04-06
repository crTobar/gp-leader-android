import Foundation

// MARK: - Meeting

struct Meeting: Identifiable, Codable, Hashable {
    let id: UUID
    let smallGroupId: UUID
    var meetingDate: Date
    var status: MeetingStatus
    var submittedAt: Date?
    var submittedBy: UUID?
    var notes: String?
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - Attendance

struct Attendance: Identifiable, Codable, Hashable {
    let id: UUID
    let meetingId: UUID
    let memberId: UUID
    var status: AttendanceStatus
    var note: String?
}
