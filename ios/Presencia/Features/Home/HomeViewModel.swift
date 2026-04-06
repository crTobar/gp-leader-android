import Foundation
import Observation

@Observable
final class HomeViewModel {
    var profile: Profile? = nil
    var activeGroup: SmallGroup? = nil
    var lastMeeting: Meeting? = nil
    var lastAttendance: [Attendance] = []
    var recentMeetings: [Meeting] = []
    /// Present / absent counts per recent meeting (Kotlin home reunion badges + progress).
    var recentMeetingAttendance: [UUID: (present: Int, absent: Int)] = [:]
    var attendanceTrend: [WeeklyAttendance] = []
    var isLoading: Bool = true
    var errorMessage: String? = nil

    private let services: ServiceContainer

    init(services: ServiceContainer) {
        self.services = services
    }

    var presentCount: Int { lastAttendance.filter { $0.status == .present }.count }
    var absentCount:  Int { lastAttendance.filter { $0.status == .absent  }.count }
    var totalCount:   Int { lastAttendance.count }

    // MARK: - Quarterly stats

    struct QuarterSummary {
        let quarter: Int
        let year: Int
        let meetingCount: Int
        let avgRate: Double?   // nil if no trend data

        var label: String { "T\(quarter) \(year)" }
    }

    var quarterSummary: QuarterSummary {
        let cal = Calendar.current
        let now = Date()
        let month = cal.component(.month, from: now)
        let q = (month - 1) / 3 + 1
        let year = cal.component(.year, from: now)
        let avg: Double? = attendanceTrend.isEmpty ? nil
            : attendanceTrend.reduce(0) { $0 + $1.rate } / Double(attendanceTrend.count)
        return QuarterSummary(quarter: q, year: year, meetingCount: attendanceTrend.count, avgRate: avg)
    }

    @MainActor
    func load() async {
        isLoading = true
        errorMessage = nil

        // --- Step 1: profile (required) ---
        profile = await services.auth.getCurrentProfile()
        guard let profileId = profile?.id else {
            errorMessage = "No se pudo cargar tu perfil. Intenta de nuevo."
            clearData(); return
        }

        // --- Step 2: role assignment → groupId (required) ---
        let roleAssignments = (try? await services.auth.getRoleAssignments(profileId: profileId)) ?? []
        guard let groupId = roleAssignments.first?.smallGroupId else {
            errorMessage = "No se encontró tu grupo. Contacta al administrador."
            clearData(); return
        }

        // --- Step 3: group + meetings + trend in parallel ---
        async let groupFetch   = services.group.getGroup(id: groupId)
        async let meetingsFetch = services.meetings.getMeetings(groupId: groupId, dateRange: nil)
        async let trendFetch   = services.reports.getAttendanceTrend(groupId: groupId, weeks: 4)

        activeGroup    = try? await groupFetch
        let meetings   = (try? await meetingsFetch) ?? []
        attendanceTrend = (try? await trendFetch) ?? []

        guard activeGroup != nil else {
            errorMessage = "No se pudo cargar tu grupo. Intenta de nuevo."
            clearData(); return
        }

        lastMeeting   = meetings.first(where: { $0.status == .submitted }) ?? meetings.first
        recentMeetings = Array(meetings.prefix(3))

        // --- Step 4: all attendance in parallel (one task per unique meeting) ---
        let idsToFetch: [UUID] = Array(
            Set(recentMeetings.map(\.id) + [lastMeeting?.id].compactMap { $0 })
        )

        var fetched: [UUID: [Attendance]] = [:]
        await withTaskGroup(of: (UUID, [Attendance]).self) { group in
            for id in idsToFetch {
                group.addTask { [services] in
                    let att = (try? await services.attendance.getAttendance(meetingId: id)) ?? []
                    return (id, att)
                }
            }
            for await (id, att) in group {
                fetched[id] = att
            }
        }

        recentMeetingAttendance = [:]
        for m in recentMeetings {
            if let att = fetched[m.id] {
                recentMeetingAttendance[m.id] = (
                    att.filter { $0.status == .present }.count,
                    att.filter { $0.status == .absent  }.count
                )
            }
        }
        lastAttendance = fetched[lastMeeting?.id ?? UUID()] ?? []

        isLoading = false
    }

    // MARK: - Helpers

    @MainActor
    private func clearData() {
        activeGroup = nil
        lastMeeting = nil
        recentMeetings = []
        lastAttendance = []
        recentMeetingAttendance = [:]
        attendanceTrend = []
        isLoading = false
    }
}
