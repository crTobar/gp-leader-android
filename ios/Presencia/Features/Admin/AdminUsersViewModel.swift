import Foundation
import Observation

@Observable
final class AdminUsersViewModel {
    var profiles: [Profile] = []
    var isLoading: Bool = true
    var errorMessage: String? = nil

    private let services: ServiceContainer

    init(services: ServiceContainer) {
        self.services = services
    }

    @MainActor
    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            profiles = try await services.auth.getProfiles()
        } catch {
            errorMessage = "No se pudieron cargar los usuarios."
        }
        isLoading = false
    }
}
