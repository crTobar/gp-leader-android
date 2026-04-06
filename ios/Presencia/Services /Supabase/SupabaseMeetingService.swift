import Foundation
import Supabase

final class SupabaseMeetingService: MeetingService {

    func getMeetings(groupId: UUID, dateRange: ClosedRange<Date>?) async throws -> [Meeting] {
        // Apply all filters first, then order — avoids PostgrestTransformBuilder limitation
        var filterQuery = supabase
            .from("meeting")
            .select()
            .eq("small_group_id", value: groupId.uuidString)

        if let range = dateRange {
            let formatter = ISO8601DateFormatter()
            filterQuery = filterQuery
                .gte("meeting_date", value: formatter.string(from: range.lowerBound))
                .lte("meeting_date", value: formatter.string(from: range.upperBound))
        }

        return try await filterQuery.order("meeting_date", ascending: false).execute().value
    }

    func getMeeting(id: UUID) async throws -> Meeting {
        try await supabase
            .from("meeting")
            .select()
            .eq("id", value: id.uuidString)
            .single()
            .execute()
            .value
    }

    func createMeeting(_ meeting: Meeting) async throws -> Meeting {
        try await supabase
            .from("meeting")
            .insert(meeting)
            .select()
            .single()
            .execute()
            .value
    }

    func updateMeeting(_ meeting: Meeting) async throws -> Meeting {
        try await supabase
            .from("meeting")
            .update(meeting)
            .eq("id", value: meeting.id.uuidString)
            .select()
            .single()
            .execute()
            .value
    }

    func submitMeeting(id: UUID, submittedBy: UUID) async throws {
        let now = ISO8601DateFormatter().string(from: Date())
        try await supabase
            .from("meeting")
            .update([
                "status": MeetingStatus.submitted.rawValue,
                "submitted_at": now,
                "submitted_by": submittedBy.uuidString
            ])
            .eq("id", value: id.uuidString)
            .execute()
    }

    func deleteDraft(id: UUID) async throws {
        try await supabase
            .from("meeting")
            .delete()
            .eq("id", value: id.uuidString)
            .eq("status", value: MeetingStatus.draft.rawValue)
            .execute()
    }
}
