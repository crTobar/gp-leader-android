import Foundation

final class MockGroupService: GroupService {
    private var groups = MockData.groups

    func getGroup(id: UUID) async throws -> SmallGroup {
        try await Task.sleep(for: .seconds(0.3))
        guard let group = groups.first(where: { $0.id == id }) else {
            throw NSError(domain: "Group", code: 404)
        }
        return group
    }

    func updateGroup(_ group: SmallGroup) async throws -> SmallGroup {
        try await Task.sleep(for: .seconds(0.5))
        if let index = groups.firstIndex(where: { $0.id == group.id }) {
            groups[index] = group
        }
        return group
    }
}
