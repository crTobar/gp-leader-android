import Foundation
import Supabase

// MARK: - Live member API

/// Some PostgREST views (or older schemas) omit `small_group_id` on `member` rows even though the row
/// belongs to the filtered group. We decode via `JSONObject` and inject the query `groupId` when missing.
final class SupabaseMemberService: MemberService {

    private let rowDecoder: JSONDecoder = {
        let d = JSONDecoder()
        d.dateDecodingStrategy = .iso8601
        return d
    }()

    func getMembers(groupId: UUID) async throws -> [Member] {
        let rows: [JSONObject] = try await supabase
            .from("member")
            .select()
            .eq("small_group_id", value: groupId.uuidString)
            .eq("is_active", value: true)
            .order("first_name")
            .execute()
            .value
        return try decodeMemberRows(rows, fallbackSmallGroupId: groupId)
    }

    func createMember(_ member: Member) async throws -> Member {
        let row: JSONObject = try await supabase
            .from("member")
            .insert(member)
            .select()
            .single()
            .execute()
            .value
        return try decodeMemberRow(row, fallbackSmallGroupId: member.smallGroupId)
    }

    func updateMember(_ member: Member) async throws -> Member {
        let row: JSONObject = try await supabase
            .from("member")
            .update(member)
            .eq("id", value: member.id.uuidString)
            .select()
            .single()
            .execute()
            .value
        return try decodeMemberRow(row, fallbackSmallGroupId: member.smallGroupId)
    }

    func getArchivedMembers(groupId: UUID) async throws -> [Member] {
        let rows: [JSONObject] = try await supabase
            .from("member")
            .select()
            .eq("small_group_id", value: groupId.uuidString)
            .eq("is_active", value: false)
            .order("first_name")
            .execute()
            .value
        return try decodeMemberRows(rows, fallbackSmallGroupId: groupId)
    }

    func toggleMemberActive(id: UUID) async throws {
        let rows: [JSONObject] = try await supabase
            .from("member")
            .select("is_active")
            .eq("id", value: id.uuidString)
            .limit(1)
            .execute()
            .value
        guard let row = rows.first else {
            throw NSError(
                domain: "PresenciaMemberService",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "Miembro no encontrado."]
            )
        }
        let active = row["is_active"]?.boolValue ?? true
        try await supabase
            .from("member")
            .update(["is_active": !active])
            .eq("id", value: id.uuidString)
            .execute()
    }

    // MARK: - Decode rows (inject small_group_id)

    private func decodeMemberRows(_ rows: [JSONObject], fallbackSmallGroupId: UUID) throws -> [Member] {
        try rows.map { try decodeMemberRow($0, fallbackSmallGroupId: fallbackSmallGroupId) }
    }

    private func decodeMemberRow(_ row: JSONObject, fallbackSmallGroupId: UUID) throws -> Member {
        var dict = row.mapValues { anyJSONToFoundation($0) }
        if dict["small_group_id"] == nil || dict["small_group_id"] is NSNull {
            if let alt = dict["group_id"] as? String, UUID(uuidString: alt) != nil {
                dict["small_group_id"] = alt
            } else {
                dict["small_group_id"] = fallbackSmallGroupId.uuidString
            }
        }
        let data = try JSONSerialization.data(withJSONObject: dict)
        return try rowDecoder.decode(Member.self, from: data)
    }

    private func anyJSONToFoundation(_ json: AnyJSON) -> Any {
        switch json {
        case .null: NSNull()
        case let .bool(b): b
        case let .integer(i): i
        case let .double(d): d
        case let .string(s): s
        case let .object(o): o.mapValues { anyJSONToFoundation($0) }
        case let .array(a): a.map { anyJSONToFoundation($0) }
        }
    }
}
