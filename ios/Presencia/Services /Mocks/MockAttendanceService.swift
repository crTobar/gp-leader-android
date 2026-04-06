import Foundation

final class MockAttendanceService: AttendanceService {
    private var attendanceStore: [UUID: [Attendance]] = [:]

    init() {
        let meetings = MockData.meetings
        for meeting in meetings {
            attendanceStore[meeting.id] = MockData.attendance(for: meeting)
        }
    }

    func getAttendance(meetingId: UUID) async throws -> [Attendance] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        return attendanceStore[meetingId] ?? []
    }

    func prepopulateAttendance(meetingId: UUID, members: [Member]) async throws -> [Attendance] {
        try await Task.sleep(for: .seconds(0.5))
        let records = members.map { member in
            Attendance(id: UUID(), meetingId: meetingId, memberId: member.id, status: .absent, note: nil)
        }
        attendanceStore[meetingId] = records
        return records
    }

    func updateAttendance(_ attendance: Attendance) async throws {
        try await Task.sleep(for: .seconds(0.2))
        if var records = attendanceStore[attendance.meetingId],
           let index = records.firstIndex(where: { $0.id == attendance.id }) {
            records[index] = attendance
            attendanceStore[attendance.meetingId] = records
        }
    }

    func batchUpdateAttendance(_ records: [Attendance]) async throws {
        try await Task.sleep(for: .seconds(0.5))
        for record in records {
            if var existing = attendanceStore[record.meetingId],
               let index = existing.firstIndex(where: { $0.memberId == record.memberId }) {
                existing[index] = record
                attendanceStore[record.meetingId] = existing
            }
        }
    }
}
