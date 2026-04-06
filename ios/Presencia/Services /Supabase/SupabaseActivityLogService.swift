import Foundation
import Supabase

final class SupabaseActivityLogService: ActivityLogService {

    func getLogs(groupId: UUID) async throws -> [ActivityLogEntry] {
        try await supabase
            .from("activity_log")
            .select()
            .eq("small_group_id", value: groupId.uuidString)
            .order("created_at", ascending: false)
            .limit(50)
            .execute()
            .value
    }

    func log(profileId: UUID, groupId: UUID, actionType: String, description: String) async throws {
        struct Payload: Encodable {
            let profile_id: String
            let small_group_id: String
            let action_type: String
            let description: String
        }
        try await supabase
            .from("activity_log")
            .insert(Payload(
                profile_id: profileId.uuidString,
                small_group_id: groupId.uuidString,
                action_type: actionType,
                description: description
            ))
            .execute()
    }
}
