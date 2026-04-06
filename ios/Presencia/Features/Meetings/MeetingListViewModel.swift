import Foundation
import Observation

@Observable
final class MeetingListViewModel {
    var meetings: [Meeting] = []
    var isLoading: Bool = true
    var errorMessage: String? = nil
    var searchText: String = ""
    var selectedQuarter: Int = 0  // 0 = all, 1-4 = quarters
    var selectedYear: Int = Calendar.current.component(.year, from: Date())

    private let services: ServiceContainer
    let groupId: UUID

    init(services: ServiceContainer, groupId: UUID) {
        self.services = services
        self.groupId = groupId
    }

    // MARK: - Quarter helpers

    static func dateRange(quarter: Int, year: Int) -> ClosedRange<Date>? {
        var comps = DateComponents()
        comps.year = year
        comps.month = (quarter - 1) * 3 + 1
        comps.day = 1
        comps.hour = 0; comps.minute = 0; comps.second = 0
        let cal = Calendar.current
        guard let start = cal.date(from: comps),
              let end = cal.date(byAdding: DateComponents(month: 3, second: -1), to: start)
        else { return nil }
        return start...end
    }

    // MARK: - Filtered lists

    var meetingsInQuarter: [Meeting] {
        guard let range = Self.dateRange(quarter: selectedQuarter, year: selectedYear) else { return [] }
        return meetings.filter { range.contains($0.meetingDate) }
    }

    var quarterFiltered: [Meeting] {
        selectedQuarter == 0 ? meetings : meetingsInQuarter
    }

    var filteredMeetings: [Meeting] {
        let base = quarterFiltered
        guard !searchText.isEmpty else { return base }
        return base.filter {
            DateFormatters.formatMeetingDate($0.meetingDate)
                .localizedCaseInsensitiveContains(searchText)
        }
    }

    // Meetings grouped by "MMMM yyyy" month header, sorted newest first
    var meetingsByMonth: [(header: String, meetings: [Meeting])] {
        let fmt = DateFormatter()
        fmt.locale = Locale(identifier: "es_CO")
        fmt.dateFormat = "MMMM yyyy"
        let grouped = Dictionary(grouping: filteredMeetings) { meeting in
            fmt.string(from: meeting.meetingDate).uppercased()
        }
        return grouped
            .map { (header: $0.key, meetings: $0.value) }
            .sorted { a, b in
                guard let da = a.meetings.first?.meetingDate,
                      let db = b.meetings.first?.meetingDate else { return false }
                return da > db
            }
    }

    // MARK: - Quarter stats

    struct QuarterStats {
        let total: Int
        let submitted: Int
        let drafts: Int
        let avgAttendanceRate: Double?   // nil if no attendance data yet
    }

    func quarterStats(attendance: [UUID: [Attendance]]) -> QuarterStats {
        let list = quarterFiltered
        let submitted = list.filter { $0.status == .submitted || $0.status == .approved }.count
        let drafts = list.filter { $0.status == .draft }.count

        let rates: [Double] = list.compactMap { m in
            guard let att = attendance[m.id], !att.isEmpty else { return nil }
            let present = att.filter { $0.status == .present }.count
            return Double(present) / Double(att.count)
        }
        let avg: Double? = rates.isEmpty ? nil : rates.reduce(0, +) / Double(rates.count)

        return QuarterStats(total: list.count, submitted: submitted, drafts: drafts, avgAttendanceRate: avg)
    }

    // MARK: - Load / delete

    @MainActor
    func load() async {
        isLoading = true
        do {
            meetings = try await services.meetings.getMeetings(groupId: groupId, dateRange: nil)
        } catch {
            errorMessage = "Error al cargar las reuniones."
        }
        isLoading = false
    }

    @MainActor
    func deleteDraft(_ meeting: Meeting) async {
        guard meeting.status == .draft else { return }
        do {
            try await services.meetings.deleteDraft(id: meeting.id)
            meetings.removeAll { $0.id == meeting.id }
        } catch {
            errorMessage = "No se pudo eliminar el borrador."
        }
    }
}
