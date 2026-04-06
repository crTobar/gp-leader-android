import Foundation

protocol GroupService {
    func getGroup(id: UUID) async throws -> SmallGroup
    func updateGroup(_ group: SmallGroup) async throws -> SmallGroup
}
