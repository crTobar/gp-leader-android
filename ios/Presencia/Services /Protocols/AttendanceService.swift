import Foundation

protocol AttendanceService {
    func getAttendance(meetingId: UUID) async throws -> [Attendance]
    func prepopulateAttendance(meetingId: UUID, members: [Member]) async throws -> [Attendance]
    func updateAttendance(_ attendance: Attendance) async throws
    func batchUpdateAttendance(_ records: [Attendance]) async throws
}
