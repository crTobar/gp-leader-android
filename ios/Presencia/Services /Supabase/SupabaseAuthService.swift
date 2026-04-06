import Auth
import Foundation
import Supabase

final class SupabaseAuthService: AuthService {

    func signIn(compositeUsername: String, password: String) async throws -> Profile {
        let email = LoginUsernameBuilder.authEmail(fromComposite: compositeUsername)
        return try await signIn(email: email, password: password)
    }

    func signIn(email: String, password: String) async throws -> Profile {
        let session = try await signInReturningSession(email: email, password: password)
        return try await fetchProfile(userId: session.user.id)
    }

    func signInHierarchy(union: UnionOrg, campo: Campo, church: Church, group: SmallGroup, password: String) async throws -> Profile {
        let emails = LoginUsernameBuilder.loginEmailCandidates(union: union, campo: campo, church: church, group: group)
        for (index, email) in emails.enumerated() {
            do {
                let session = try await signInReturningSession(email: email, password: password)
                return try await fetchProfile(userId: session.user.id)
            } catch let e as PresenciaLoginError {
                if e == .invalidCredentials, index < emails.count - 1 {
                    continue
                }
                throw e
            }
        }
        throw PresenciaLoginError.invalidCredentials
    }

    func signOut() async throws {
        try await supabase.auth.signOut()
    }

    func getCurrentProfile() async -> Profile? {
        guard let user = try? await supabase.auth.user() else { return nil }
        return try? await fetchProfile(userId: user.id)
    }

    func getRoleAssignments(profileId: UUID) async throws -> [RoleAssignment] {
        try await supabase
            .from("role_assignment")
            .select()
            .eq("profile_id", value: profileId.uuidString)
            .execute()
            .value
    }

    func updateProfile(_ profile: Profile) async throws -> Profile {
        try await supabase
            .from("profile")
            .update(profile)
            .eq("id", value: profile.id.uuidString)
            .select()
            .single()
            .execute()
            .value
    }

    func updatePassword(newPassword: String) async throws {
        try await supabase.auth.update(user: UserAttributes(password: newPassword))
    }

    func getProfiles() async throws -> [Profile] {
        try await supabase
            .from("profile")
            .select()
            .order("last_name", ascending: true)
            .execute()
            .value
    }

    // MARK: - Private

    private func signInReturningSession(email: String, password: String) async throws -> Session {
        do {
            return try await supabase.auth.signIn(email: email, password: password)
        } catch let error as AuthError {
            if case let .api(_, code, _, _) = error, code == .invalidCredentials {
                throw PresenciaLoginError.invalidCredentials
            }
            let text = error.localizedDescription
            if let mapped = PresenciaLoginError.mapServerMessage(text) {
                throw mapped
            }
            throw PresenciaLoginError.networkOrServer(text)
        } catch {
            let text = error.localizedDescription
            if let mapped = PresenciaLoginError.mapServerMessage(text) {
                throw mapped
            }
            throw PresenciaLoginError.networkOrServer(text)
        }
    }

    private func fetchProfile(userId: UUID) async throws -> Profile {
        do {
            let profile: Profile = try await supabase
                .from("profile")
                .select()
                .eq("id", value: userId.uuidString)
                .single()
                .execute()
                .value
            return profile
        } catch {
            throw PresenciaLoginError.profileNotAvailable
        }
    }
}
