import Foundation

final class MockMemberService: MemberService {
    private var members = MockData.members

    func getMembers(groupId: UUID) async throws -> [Member] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        return members.filter { $0.smallGroupId == groupId && $0.isActive }
    }

    func createMember(_ member: Member) async throws -> Member {
        try await Task.sleep(for: .seconds(0.5))
        members.append(member)
        return member
    }

    func updateMember(_ member: Member) async throws -> Member {
        try await Task.sleep(for: .seconds(0.5))
        if let index = members.firstIndex(where: { $0.id == member.id }) {
            members[index] = member
        }
        return member
    }

    func getArchivedMembers(groupId: UUID) async throws -> [Member] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.6)))
        return members.filter { $0.smallGroupId == groupId && !$0.isActive }
    }

    func toggleMemberActive(id: UUID) async throws {
        try await Task.sleep(for: .seconds(0.3))
        if let index = members.firstIndex(where: { $0.id == id }) {
            members[index].isActive.toggle()
        }
    }
}
