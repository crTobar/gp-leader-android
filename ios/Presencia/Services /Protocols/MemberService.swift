import Foundation

protocol MemberService {
    func getMembers(groupId: UUID) async throws -> [Member]
    func getArchivedMembers(groupId: UUID) async throws -> [Member]
    func createMember(_ member: Member) async throws -> Member
    func updateMember(_ member: Member) async throws -> Member
    func toggleMemberActive(id: UUID) async throws
}
