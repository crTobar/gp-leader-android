import Foundation

protocol AuthService {
    /// Standard Supabase email (or legacy login).
    func signIn(email: String, password: String) async throws -> Profile

    /// Hierarchy username `union-campo-church-gp` (converted to auth email internally).
    func signIn(compositeUsername: String, password: String) async throws -> Profile

    /// Login after pickers: tries composite email, then `gp_username` if present and different (legacy Auth rows).
    func signInHierarchy(union: UnionOrg, campo: Campo, church: Church, group: SmallGroup, password: String) async throws -> Profile
    func signOut() async throws
    func getCurrentProfile() async -> Profile?
    func getRoleAssignments(profileId: UUID) async throws -> [RoleAssignment]
    func updateProfile(_ profile: Profile) async throws -> Profile
    func updatePassword(newPassword: String) async throws
    func getProfiles() async throws -> [Profile]
}

extension AuthService {
    func signInHierarchy(union: UnionOrg, campo: Campo, church: Church, group: SmallGroup, password: String) async throws -> Profile {
        let composite = LoginUsernameBuilder.loginIdentifier(union: union, campo: campo, church: church, group: group)
        return try await signIn(compositeUsername: composite, password: password)
    }
}
