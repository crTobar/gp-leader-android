import Foundation
import Supabase

final class SupabaseActivityService: ActivityService {

    func getActivityTypes(scope: ActivityScope?, campoId: UUID?, churchId: UUID?) async throws -> [ActivityType] {
        // Apply all filters first, then sort — avoids PostgrestTransformBuilder filter limitation
        var filterQuery = supabase
            .from("activity_type")
            .select()
            .eq("is_active", value: true)

        if let scope = scope {
            filterQuery = filterQuery.eq("scope", value: scope.rawValue)
        }
        if let campoId = campoId {
            filterQuery = filterQuery.eq("campo_id", value: campoId.uuidString)
        }
        if let churchId = churchId {
            filterQuery = filterQuery.eq("church_id", value: churchId.uuidString)
        }

        return try await filterQuery.order("sort_order").execute().value
    }

    func getActivityRecords(meetingId: UUID) async throws -> [ActivityRecord] {
        try await supabase
            .from("activity_record")
            .select()
            .eq("meeting_id", value: meetingId.uuidString)
            .execute()
            .value
    }

    func upsertActivityRecord(_ record: ActivityRecord) async throws {
        try await supabase
            .from("activity_record")
            .upsert(record, onConflict: "meeting_id,activity_type_id")
            .execute()
    }

    func createActivityType(_ type: ActivityType) async throws -> ActivityType {
        try await supabase
            .from("activity_type")
            .insert(type)
            .select()
            .single()
            .execute()
            .value
    }
}
