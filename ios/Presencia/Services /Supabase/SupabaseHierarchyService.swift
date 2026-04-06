import Foundation
import Supabase

// MARK: - Supabase tables (adjust in one place if your schema differs)

private enum OrgTable {
    static let unionOrg = "union_org"
    static let campo = "campo"
    static let district = "district"
    static let church = "church"
    static let smallGroup = "small_group"
}

/// Reads organizational hierarchy for the login flow. Requires RLS policies that allow `anon` SELECT on these tables (or a secure view).
final class SupabaseHierarchyService: HierarchyService {

    func fetchUnions() async throws -> [UnionOrg] {
        try await supabase
            .from(OrgTable.unionOrg)
            .select()
            .order("name")
            .execute()
            .value
    }

    func fetchCampos(unionId: UUID) async throws -> [Campo] {
        try await supabase
            .from(OrgTable.campo)
            .select()
            .eq("union_id", value: unionId)
            .order("name")
            .execute()
            .value
    }

    func fetchDistricts(campoId: UUID) async throws -> [District] {
        try await supabase
            .from(OrgTable.district)
            .select()
            .eq("campo_id", value: campoId)
            .order("name")
            .execute()
            .value
    }

    func fetchChurches(campoId: UUID) async throws -> [Church] {
        let districts: [District] = try await supabase
            .from(OrgTable.district)
            .select()
            .eq("campo_id", value: campoId)
            .execute()
            .value

        let districtIds = districts.map(\.id)
        guard !districtIds.isEmpty else { return [] }

        return try await supabase
            .from(OrgTable.church)
            .select()
            .in("district_id", values: districtIds)
            .order("name")
            .execute()
            .value
    }

    func fetchSmallGroups(churchId: UUID) async throws -> [SmallGroup] {
        try await supabase
            .from(OrgTable.smallGroup)
            .select()
            .eq("church_id", value: churchId)
            .eq("is_active", value: true)
            .order("name")
            .execute()
            .value
    }

    func fetchCampoId(forSmallGroupId smallGroupId: UUID) async throws -> UUID {
        struct ChurchRef: Decodable {
            let churchId: UUID
        }
        struct DistrictRef: Decodable {
            let districtId: UUID
        }
        struct CampoRef: Decodable {
            let campoId: UUID
        }

        let groupRow: ChurchRef = try await supabase
            .from(OrgTable.smallGroup)
            .select("church_id")
            .eq("id", value: smallGroupId.uuidString)
            .single()
            .execute()
            .value

        let churchRow: DistrictRef = try await supabase
            .from(OrgTable.church)
            .select("district_id")
            .eq("id", value: groupRow.churchId.uuidString)
            .single()
            .execute()
            .value

        let districtRow: CampoRef = try await supabase
            .from(OrgTable.district)
            .select("campo_id")
            .eq("id", value: churchRow.districtId.uuidString)
            .single()
            .execute()
            .value

        return districtRow.campoId
    }
}
