import Foundation

final class MockAuthService: AuthService {
    func signIn(compositeUsername: String, password: String) async throws -> Profile {
        let email = LoginUsernameBuilder.authEmail(fromComposite: compositeUsername)
        return try await signIn(email: email, password: password)
    }

    func signIn(email: String, password: String) async throws -> Profile {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        if email.isEmpty || password.isEmpty {
            throw NSError(domain: "auth", code: 401, userInfo: [NSLocalizedDescriptionKey: "Credenciales inválidas"])
        }
        return MockData.currentProfile
    }

    func signOut() async throws {
        try await Task.sleep(for: .seconds(0.3))
    }

    func getCurrentProfile() async -> Profile? {
        MockData.currentProfile
    }

    func getRoleAssignments(profileId: UUID) async throws -> [RoleAssignment] {
        try await Task.sleep(for: .seconds(0.3))
        return [MockData.leaderRoleAssignment]
    }

    func updateProfile(_ profile: Profile) async throws -> Profile {
        try await Task.sleep(for: .seconds(0.5))
        return profile
    }

    func updatePassword(newPassword: String) async throws {
        try await Task.sleep(for: .seconds(0.5))
    }

    func getProfiles() async throws -> [Profile] {
        try await Task.sleep(for: .seconds(0.3))
        return [MockData.currentProfile]
    }
}
