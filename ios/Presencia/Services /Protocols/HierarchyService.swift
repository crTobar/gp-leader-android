import Foundation

// MARK: - Organization hierarchy (login pickers + Supabase)

protocol HierarchyService: Sendable {
    func fetchUnions() async throws -> [UnionOrg]
    func fetchCampos(unionId: UUID) async throws -> [Campo]
    func fetchDistricts(campoId: UUID) async throws -> [District]
    /// Churches under all districts that belong to the campo.
    func fetchChurches(campoId: UUID) async throws -> [Church]
    func fetchSmallGroups(churchId: UUID) async throws -> [SmallGroup]
    /// Resolves `campo_id` for a small group (church → district → campo).
    func fetchCampoId(forSmallGroupId: UUID) async throws -> UUID
}
