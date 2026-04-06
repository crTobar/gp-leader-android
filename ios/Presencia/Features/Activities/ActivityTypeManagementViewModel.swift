import Foundation
import Observation

@Observable
final class ActivityTypeManagementViewModel {
    var activityTypes: [ActivityType] = []
    var isLoading: Bool = true
    var showingAddForm: Bool = false
    var errorMessage: String? = nil

    // Form state
    var formName: String = ""
    var formDescription: String = ""
    var formIcon: String = "star.fill"
    var formUnitLabel: String = ""
    var formScope: ActivityScope = .global

    private let services: ServiceContainer
    let campoId: UUID?
    let churchId: UUID?

    init(services: ServiceContainer, campoId: UUID?, churchId: UUID?) {
        self.services = services
        self.campoId = campoId
        self.churchId = churchId
    }

    @MainActor
    func load() async {
        isLoading = true
        do {
            activityTypes = try await services.activities.getActivityTypes(
                scope: nil, campoId: campoId, churchId: churchId
            )
        } catch {
            errorMessage = "Error al cargar las actividades."
        }
        isLoading = false
    }

    @MainActor
    func createType() async {
        guard !formName.trimmingCharacters(in: .whitespaces).isEmpty,
              !formUnitLabel.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        let newType = ActivityType(
            id: UUID(), name: formName,
            description: formDescription.isEmpty ? nil : formDescription,
            icon: formIcon, unitLabel: formUnitLabel,
            scope: formScope,
            campoId: formScope == .campo ? campoId : nil,
            churchId: formScope == .church ? churchId : nil,
            isActive: true, sortOrder: activityTypes.count + 1,
            createdBy: nil, createdAt: Date(), updatedAt: Date()
        )
        do {
            let created = try await services.activities.createActivityType(newType)
            activityTypes.append(created)
            resetForm()
            showingAddForm = false
            HapticFeedback.success()
        } catch {
            errorMessage = "No se pudo crear el tipo de actividad."
        }
    }

    func resetForm() {
        formName = ""; formDescription = ""; formIcon = "star.fill"
        formUnitLabel = ""; formScope = .global
    }

    func move(from offsets: IndexSet, to destination: Int) {
        activityTypes.move(fromOffsets: offsets, toOffset: destination)
    }
}
