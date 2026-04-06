import Foundation

final class MockHierarchyService: HierarchyService {

    func fetchUnions() async throws -> [UnionOrg] {
        try await Task.sleep(for: .milliseconds(200))
        return [MockData.union]
    }

    func fetchCampos(unionId: UUID) async throws -> [Campo] {
        try await Task.sleep(for: .milliseconds(200))
        return MockData.campos.filter { $0.unionId == unionId }
    }

    func fetchDistricts(campoId: UUID) async throws -> [District] {
        try await Task.sleep(for: .milliseconds(200))
        return MockData.districts.filter { $0.campoId == campoId }
    }

    func fetchChurches(campoId: UUID) async throws -> [Church] {
        try await Task.sleep(for: .milliseconds(200))
        let districtIds = Set(MockData.districts.filter { $0.campoId == campoId }.map(\.id))
        return MockData.churches.filter { districtIds.contains($0.districtId) }
    }

    func fetchSmallGroups(churchId: UUID) async throws -> [SmallGroup] {
        try await Task.sleep(for: .milliseconds(200))
        return MockData.groups.filter { $0.churchId == churchId && $0.isActive }
    }

    func fetchCampoId(forSmallGroupId smallGroupId: UUID) async throws -> UUID {
        try await Task.sleep(for: .milliseconds(100))
        guard let group = MockData.groups.first(where: { $0.id == smallGroupId }),
              let church = MockData.churches.first(where: { $0.id == group.churchId }),
              let district = MockData.districts.first(where: { $0.id == church.districtId })
        else {
            throw NSError(
                domain: "MockHierarchyService",
                code: 404,
                userInfo: [NSLocalizedDescriptionKey: "No se encontró el campo para el GP."]
            )
        }
        return district.campoId
    }
}
