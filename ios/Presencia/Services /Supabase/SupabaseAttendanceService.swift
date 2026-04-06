import Foundation
import Supabase

final class SupabaseAttendanceService: AttendanceService {

    func getAttendance(meetingId: UUID) async throws -> [Attendance] {
        try await supabase
            .from("attendance")
            .select()
            .eq("meeting_id", value: meetingId.uuidString)
            .execute()
            .value
    }

    func prepopulateAttendance(meetingId: UUID, members: [Member]) async throws -> [Attendance] {
        let records = members.map { member in
            Attendance(
                id: UUID(),
                meetingId: meetingId,
                memberId: member.id,
                status: .absent,
                note: nil
            )
        }
        try await supabase
            .from("attendance")
            .insert(records)
            .execute()
        return records
    }

    func updateAttendance(_ attendance: Attendance) async throws {
        try await supabase
            .from("attendance")
            .update(attendance)
            .eq("id", value: attendance.id.uuidString)
            .execute()
    }

    func batchUpdateAttendance(_ records: [Attendance]) async throws {
        try await supabase
            .from("attendance")
            .upsert(records)
            .execute()
    }
}
