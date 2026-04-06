import Foundation

// MARK: - Reporting Period

struct ReportingPeriod: Identifiable, Codable, Hashable {
    let id: UUID
    let campoId: UUID
    var name: String?
    var weekStart: Date
    var weekEnd: Date
    var isClosed: Bool
    var closedAt: Date?
    let createdAt: Date
}

// MARK: - Report DTOs

struct WeeklyAttendance: Identifiable {
    let id = UUID()
    let weekStart: Date
    let presentCount: Int
    let totalCount: Int
    var rate: Double { totalCount > 0 ? Double(presentCount) / Double(totalCount) : 0 }
}

struct GroupRanking: Identifiable {
    let id = UUID()
    let group: SmallGroup
    let leaderName: String
    let attendanceRate: Double
    let trend: [Double]
}

struct ActivitySummary: Identifiable {
    let id = UUID()
    let activityType: ActivityType
    let totalCount: Int
}
