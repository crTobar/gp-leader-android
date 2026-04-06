import Foundation
import Observation

@Observable
final class ReportsViewModel {
    var trend: [WeeklyAttendance] = []
    var groupRankings: [GroupRanking] = []
    var activitySummary: [ActivitySummary] = []
    var isLoading: Bool = true
    var errorMessage: String? = nil

    private let services: ServiceContainer
    let groupId: UUID
    let churchId: UUID
    let role: UserRole

    init(services: ServiceContainer, groupId: UUID, churchId: UUID, role: UserRole) {
        self.services = services
        self.groupId = groupId
        self.churchId = churchId
        self.role = role
    }

    var permissions: Permissions { Permissions(role: role) }

    var averageRate: Double {
        guard !trend.isEmpty else { return 0 }
        return trend.map(\.rate).reduce(0, +) / Double(trend.count)
    }

    var totalMembers: Int { trend.last?.totalCount ?? 0 }

    @MainActor
    func load() async {
        isLoading = true
        do {
            trend = try await services.reports.getAttendanceTrend(groupId: groupId, weeks: 8)
            if permissions.canViewChurchReports {
                groupRankings = try await services.reports.getGroupRanking(churchId: churchId)
            }
            let end = Date()
            let start = Calendar.current.date(byAdding: .weekOfYear, value: -7, to: end) ?? end
            activitySummary = try await services.reports.getActivitySummary(
                groupId: groupId, dateRange: start...end
            )
        } catch {
            errorMessage = "Error al cargar los reportes."
        }
        isLoading = false
    }
}
