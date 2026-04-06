import Foundation

protocol ReportService {
    func getAttendanceTrend(groupId: UUID, weeks: Int) async throws -> [WeeklyAttendance]
    func getGroupRanking(churchId: UUID) async throws -> [GroupRanking]
    func getActivitySummary(groupId: UUID, dateRange: ClosedRange<Date>) async throws -> [ActivitySummary]
}
