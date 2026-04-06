import Foundation
import Observation

@Observable
final class ActivityLogViewModel {
    var entries: [ActivityLogEntry] = []
    var isLoading = true
    var errorMessage: String? = nil

    private let services: ServiceContainer
    let groupId: UUID

    init(services: ServiceContainer, groupId: UUID) {
        self.services = services
        self.groupId = groupId
    }

    @MainActor
    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            entries = try await services.activityLog.getLogs(groupId: groupId)
        } catch {
            errorMessage = "No se pudo cargar el registro de actividad."
        }
        isLoading = false
    }
}
