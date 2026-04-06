import Foundation
import Supabase

final class SupabaseReportService: ReportService {

    // MARK: - Attendance Trend
    // Was: 2 sequential queries per week × N weeks = 8 queries
    // Now: 2 queries total (all meetings in range, then all attendance in one IN query)

    func getAttendanceTrend(groupId: UUID, weeks: Int) async throws -> [WeeklyAttendance] {
        let calendar = Calendar.current
        let now = Date()
        let formatter = ISO8601DateFormatter()

        guard let rangeStart = calendar.date(byAdding: .weekOfYear, value: -(weeks - 1), to: now) else {
            return []
        }

        // 1 query: all meetings in the entire range
        let meetings: [Meeting] = try await supabase
            .from("meeting")
            .select()
            .eq("small_group_id", value: groupId.uuidString)
            .gte("meeting_date", value: formatter.string(from: rangeStart))
            .lte("meeting_date", value: formatter.string(from: now))
            .execute()
            .value

        guard !meetings.isEmpty else { return [] }

        // 1 query: all attendance for those meetings
        let meetingIds = meetings.map { $0.id.uuidString }
        let allAttendance: [Attendance] = try await supabase
            .from("attendance")
            .select()
            .in("meeting_id", values: meetingIds)
            .execute()
            .value

        let attendanceByMeeting = Dictionary(grouping: allAttendance, by: \.meetingId)

        // Group client-side into weekly buckets
        var results: [WeeklyAttendance] = []
        for weekOffset in (0..<weeks).reversed() {
            guard
                let weekStart = calendar.date(byAdding: .weekOfYear, value: -weekOffset, to: now),
                let weekEnd   = calendar.date(byAdding: .day, value: 6, to: weekStart)
            else { continue }

            guard let meeting = meetings.first(where: {
                $0.meetingDate >= weekStart && $0.meetingDate <= weekEnd
            }) else { continue }

            let att = attendanceByMeeting[meeting.id] ?? []
            results.append(WeeklyAttendance(
                weekStart: weekStart,
                presentCount: att.filter { $0.status == .present }.count,
                totalCount: att.count
            ))
        }
        return results
    }

    // MARK: - Group Ranking
    // Was: N groups × M meetings × 1 query = up to 80+ sequential queries
    // Now: 3 queries total (groups, all meetings, all attendance)

    func getGroupRanking(churchId: UUID) async throws -> [GroupRanking] {
        // 1 query: all active groups
        let groups: [SmallGroup] = try await supabase
            .from("small_group")
            .select()
            .eq("church_id", value: churchId.uuidString)
            .eq("is_active", value: true)
            .execute()
            .value

        guard !groups.isEmpty else { return [] }

        let groupIds = groups.map { $0.id.uuidString }

        // 1 query: recent meetings for all groups
        let allMeetings: [Meeting] = try await supabase
            .from("meeting")
            .select()
            .in("small_group_id", values: groupIds)
            .order("meeting_date", ascending: false)
            .execute()
            .value

        // Cap at 8 meetings per group (client-side)
        var meetingsByGroup = Dictionary(grouping: allMeetings, by: \.smallGroupId)
        for key in meetingsByGroup.keys {
            meetingsByGroup[key] = Array((meetingsByGroup[key] ?? []).prefix(8))
        }

        let relevantMeetings = meetingsByGroup.values.flatMap { $0 }
        guard !relevantMeetings.isEmpty else {
            return groups.map { GroupRanking(group: $0, leaderName: "", attendanceRate: 0, trend: []) }
                         .sorted { $0.attendanceRate > $1.attendanceRate }
        }

        // 1 query: all attendance for those meetings
        let meetingIds = relevantMeetings.map { $0.id.uuidString }
        let allAttendance: [Attendance] = try await supabase
            .from("attendance")
            .select()
            .in("meeting_id", values: meetingIds)
            .execute()
            .value

        let attendanceByMeeting = Dictionary(grouping: allAttendance, by: \.meetingId)

        let rankings: [GroupRanking] = groups.map { group in
            let meetings = meetingsByGroup[group.id] ?? []
            let weeklyRates: [Double] = meetings.compactMap { meeting in
                let att = attendanceByMeeting[meeting.id] ?? []
                guard !att.isEmpty else { return nil }
                return Double(att.filter { $0.status == .present }.count) / Double(att.count)
            }
            let avg = weeklyRates.isEmpty ? 0.0 : weeklyRates.reduce(0, +) / Double(weeklyRates.count)
            return GroupRanking(group: group, leaderName: "", attendanceRate: avg, trend: weeklyRates)
        }

        return rankings.sorted { $0.attendanceRate > $1.attendanceRate }
    }

    // MARK: - Activity Summary
    // Was: 1 query per meeting (N sequential queries) + 1 for types
    // Now: 3 queries total (meetings, all records in one IN query, types) — records + types in parallel

    func getActivitySummary(groupId: UUID, dateRange: ClosedRange<Date>) async throws -> [ActivitySummary] {
        let formatter = ISO8601DateFormatter()
        let meetings: [Meeting] = try await supabase
            .from("meeting")
            .select()
            .eq("small_group_id", value: groupId.uuidString)
            .gte("meeting_date", value: formatter.string(from: dateRange.lowerBound))
            .lte("meeting_date", value: formatter.string(from: dateRange.upperBound))
            .execute()
            .value

        guard !meetings.isEmpty else { return [] }

        let meetingIds = meetings.map { $0.id.uuidString }

        // Fetch all records + all activity types in parallel
        async let recordsFetch: [ActivityRecord] = supabase
            .from("activity_record")
            .select()
            .in("meeting_id", values: meetingIds)
            .execute()
            .value

        async let typesFetch: [ActivityType] = supabase
            .from("activity_type")
            .select()
            .execute()
            .value

        let records      = try await recordsFetch
        let activityTypes = try await typesFetch

        var countByType: [UUID: Int] = [:]
        for record in records {
            countByType[record.activityTypeId, default: 0] += record.count
        }

        let typeMap = Dictionary(uniqueKeysWithValues: activityTypes.map { ($0.id, $0) })

        return countByType.compactMap { typeId, total -> ActivitySummary? in
            guard let type = typeMap[typeId] else { return nil }
            return ActivitySummary(activityType: type, totalCount: total)
        }.sorted { $0.totalCount > $1.totalCount }
    }
}
