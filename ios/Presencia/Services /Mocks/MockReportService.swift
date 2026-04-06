import Foundation

final class MockReportService: ReportService {
    func getAttendanceTrend(groupId: UUID, weeks: Int) async throws -> [WeeklyAttendance] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.3...0.8)))
        return Array(MockData.attendanceTrend.suffix(weeks))
    }

    func getGroupRanking(churchId: UUID) async throws -> [GroupRanking] {
        try await Task.sleep(for: .seconds(Double.random(in: 0.5...0.8)))
        return MockData.groups.map { group in
            GroupRanking(
                group: group,
                leaderName: "Líder de \(group.name)",
                attendanceRate: Double.random(in: 0.6...0.95),
                trend: (0..<6).map { _ in Double.random(in: 0.5...1.0) }
            )
        }.sorted { $0.attendanceRate > $1.attendanceRate }
    }

    func getActivitySummary(groupId: UUID, dateRange: ClosedRange<Date>) async throws -> [ActivitySummary] {
        try await Task.sleep(for: .seconds(0.5))
        let counts = [24, 18, 12, 8, 15]
        return MockData.activityTypes.prefix(5).enumerated().map { index, type in
            ActivitySummary(activityType: type, totalCount: counts[index])
        }
    }
}
