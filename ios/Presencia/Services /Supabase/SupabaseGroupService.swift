import Foundation
import Supabase

final class SupabaseGroupService: GroupService {

    func getGroup(id: UUID) async throws -> SmallGroup {
        try await supabase
            .from("small_group")
            .select()
            .eq("id", value: id.uuidString)
            .single()
            .execute()
            .value
    }

    func updateGroup(_ group: SmallGroup) async throws -> SmallGroup {
        try await supabase
            .from("small_group")
            .update(group)
            .eq("id", value: group.id.uuidString)
            .select()
            .single()
            .execute()
            .value
    }
}
