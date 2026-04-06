import Foundation
import Observation

@Observable
final class MemberListViewModel {
    var members: [Member] = []
    var archivedMembers: [Member] = []
    var searchText: String = ""
    var isLoading: Bool = true
    var errorMessage: String? = nil
    /// Member ids currently calling `toggleMemberActive` (disables switch + avoids double taps).
    private(set) var togglingMemberIds: Set<UUID> = []

    private let services: ServiceContainer
    let groupId: UUID
    let profileId: UUID?

    init(services: ServiceContainer, groupId: UUID, profileId: UUID? = nil) {
        self.services = services
        self.groupId = groupId
        self.profileId = profileId
    }

    var filteredMembers: [Member] {
        members.filter { matchesQuery($0) }
    }

    var filteredArchived: [Member] {
        archivedMembers.filter { matchesQuery($0) }
    }

    private func matchesQuery(_ member: Member) -> Bool {
        let q = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return true }
        if member.fullName.localizedCaseInsensitiveContains(q) { return true }
        if let p = member.phone, p.localizedCaseInsensitiveContains(q) { return true }
        if let e = member.email, e.localizedCaseInsensitiveContains(q) { return true }
        return false
    }

    @MainActor
    func load() async {
        isLoading = true
        do {
            async let activeFetch   = services.members.getMembers(groupId: groupId)
            async let archivedFetch = services.members.getArchivedMembers(groupId: groupId)
            (members, archivedMembers) = try await (activeFetch, archivedFetch)
        } catch {
            errorMessage = "Error al cargar los miembros."
        }
        isLoading = false
    }

    @MainActor
    func toggleActive(member: Member) async {
        guard !togglingMemberIds.contains(member.id) else { return }
        togglingMemberIds.insert(member.id)
        defer { togglingMemberIds.remove(member.id) }
        do {
            try await services.members.toggleMemberActive(id: member.id)
            if member.isActive {
                members.removeAll { $0.id == member.id }
                var archived = member
                archived.isActive = false
                archivedMembers.append(archived)
                if let pid = profileId {
                    try? await services.activityLog.log(
                        profileId: pid, groupId: groupId,
                        actionType: "member_archived",
                        description: "\(member.fullName) fue archivado"
                    )
                }
            } else {
                archivedMembers.removeAll { $0.id == member.id }
                var active = member
                active.isActive = true
                members.append(active)
                if let pid = profileId {
                    try? await services.activityLog.log(
                        profileId: pid, groupId: groupId,
                        actionType: "member_unarchived",
                        description: "\(member.fullName) fue restaurado"
                    )
                }
            }
        } catch {
            errorMessage = "No se pudo actualizar el miembro."
            await load()
        }
    }
}
