import SwiftUI
import LucideIcons

// MARK: - Meeting Detail View

struct MeetingDetailView: View {
    let meeting: Meeting
    let onEdit: () -> Void

    @Environment(\.services) private var services

    @State private var attendance: [Attendance] = []
    @State private var activityRecords: [ActivityRecord] = []
    @State private var members: [Member] = []
    @State private var activityTypes: [ActivityType] = []
    @State private var isLoading = true
    @State private var submitterName: String? = nil

    // MARK: Derived counts

    private var presentCount: Int {
        attendance.filter { $0.status == .present }.count
    }

    private var justifiedCount: Int {
        attendance.filter { $0.status == .justified }.count
    }

    private var absentCount: Int {
        attendance.filter { $0.status == .absent }.count
    }

    // MARK: Sorted attendance rows — present, justified, then absent

    private var sortedAttendance: [Attendance] {
        attendance.sorted { lhs, rhs in
            func order(_ s: AttendanceStatus) -> Int {
                switch s {
                case .present:   return 0
                case .justified: return 1
                case .absent:    return 2
                }
            }
            return order(lhs.status) < order(rhs.status)
        }
    }

    // MARK: Activity highlights paired with their types

    private var activityHighlights: [(record: ActivityRecord, type: ActivityType)] {
        activityRecords.compactMap { record in
            guard let type = activityTypes.first(where: { $0.id == record.activityTypeId }) else {
                return nil
            }
            return (record: record, type: type)
        }
    }

    var body: some View {
        ZStack {
            Color.neuBackground
                .ignoresSafeArea()

            if isLoading {
                ScrollView {
                    LoadingStateView(rows: 5)
                        .padding(.horizontal)
                        .padding(.top, 16)
                }
            } else {
                detailContent
            }
        }
        .navigationTitle(DateFormatters.formatShortDate(meeting.meetingDate))
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadData()
        }
    }

    // MARK: Detail content

    private var detailContent: some View {
        ScrollView {
            VStack(spacing: 20) {

                // MARK: Header card
                NeuCard {
                    VStack(alignment: .leading, spacing: 12) {
                        statusBadge

                        Text(DateFormatters.formatMeetingDate(meeting.meetingDate))
                            .font(.displayMedium)
                            .foregroundStyle(Color.textPrimary)

                        if let submitterName, let submittedAt = meeting.submittedAt {
                            HStack(spacing: 4) {
                                LucideIcon(uiImage: AppIcon.user, size: 12)
                                    .foregroundStyle(Color.textMuted)
                                Text("Enviada por \(submitterName) · \(DateFormatters.formatShortDate(submittedAt))")
                                    .font(.captionStyle)
                                    .foregroundStyle(Color.textMuted)
                            }
                        }

                        if let notes = meeting.notes, !notes.isEmpty {
                            Text(notes)
                                .font(.bodyRegular)
                                .foregroundStyle(Color.textSecondary)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }

                // MARK: Stats row
                HStack(spacing: 12) {
                    NeuStatPill(
                        value: "\(presentCount)",
                        label: "Presentes",
                        valueColor: .statusPresent
                    )
                    NeuStatPill(
                        value: "\(justifiedCount)",
                        label: "Justificados",
                        valueColor: .statusJustified
                    )
                    NeuStatPill(
                        value: "\(absentCount)",
                        label: "Ausentes",
                        valueColor: .blush
                    )
                    NeuStatPill(
                        value: attendance.isEmpty ? "--" : "\(Int(Double(presentCount) / Double(attendance.count) * 100))%",
                        label: "Asist.",
                        valueColor: .accent
                    )
                }

                // MARK: Attendance section
                if !sortedAttendance.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Asistencia")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)
                            .padding(.horizontal, 4)

                        LazyVStack(spacing: 10) {
                            ForEach(sortedAttendance) { record in
                                let member = members.first(where: { $0.id == record.memberId })
                                if let member = member {
                                    NeuMemberRow(member: member) {
                                        attendanceTrailing(status: record.status)
                                    }
                                }
                            }
                        }
                    }
                }

                // MARK: Activities section
                if !activityHighlights.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Actividades")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)
                            .padding(.horizontal, 4)

                        let levels: [ActivityLevel] = [.union, .pastor, .myGroup]
                        ForEach(levels, id: \.self) { level in
                            let items = activityHighlights.filter { $0.type.level == level }
                            if !items.isEmpty {
                                VStack(spacing: 0) {
                                    // Dark level header
                                    HStack(spacing: 8) {
                                        Text(level.displayName.uppercased())
                                            .font(.captionStyle)
                                            .foregroundStyle(.white)
                                        Spacer()
                                    }
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 10)
                                    .background(Color.neuBackgroundDark)

                                    // Activity rows
                                    VStack(spacing: 12) {
                                        ForEach(items, id: \.record.id) { pair in
                                            HStack(spacing: 12) {
                                                NeuIconBadge(icon: AppIcon.star, color: .accent, size: 36)
                                                Text(pair.type.name)
                                                    .font(.bodyRegular)
                                                    .foregroundStyle(Color.textPrimary)
                                                    .lineLimit(1)
                                                Spacer(minLength: 4)
                                                Text("\(pair.record.count) \(pair.type.unitLabel)")
                                                    .font(.captionStyle)
                                                    .foregroundStyle(Color.accent)
                                            }
                                        }
                                    }
                                    .padding(12)
                                    .background(Color.neuBackground)
                                }
                                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                                .shadow(color: Color.neuShadowDark.opacity(0.3), radius: 4, x: 2, y: 2)
                                .shadow(color: Color.white.opacity(0.8), radius: 4, x: -2, y: -2)
                            }
                        }
                    }
                }

                // MARK: Edit button — drafts always, submitted within 7 days
                let canEdit: Bool = {
                    if meeting.status == .draft { return true }
                    if meeting.status == .submitted {
                        let age = Date().timeIntervalSince(meeting.submittedAt ?? meeting.createdAt)
                        return age < 7 * 24 * 3600
                    }
                    return false
                }()
                if canEdit {
                    NeuButton("Editar Reunión", icon: AppIcon.edit, variant: .secondary) {
                        HapticFeedback.medium()
                        onEdit()
                    }
                    .padding(.top, 4)
                }
            }
            .padding(.horizontal)
            .padding(.top, 16)
            .padding(.bottom, 40)
        }
    }

    // MARK: Attendance trailing view

    private func attendanceTrailing(status: AttendanceStatus) -> some View {
        let letter: String
        let color: Color
        switch status {
        case .present:   letter = "P"; color = .statusPresent
        case .absent:    letter = "A"; color = .blush
        case .justified: letter = "J"; color = .statusJustified
        }
        return RoundedRectangle(cornerRadius: 8)
            .fill(color)
            .frame(width: 32, height: 32)
            .overlay(
                Text(letter)
                    .font(.bodyLarge)
                    .foregroundStyle(.white)
            )
    }

    // MARK: Status badge

    @ViewBuilder
    private var statusBadge: some View {
        switch meeting.status {
        case .draft:
            Text("Borrador")
                .font(.captionSmall)
                .foregroundStyle(Color.statusJustified)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color.statusJustified.opacity(0.15))
                .clipShape(Capsule())
        case .submitted:
            Text("Enviada")
                .font(.captionSmall)
                .foregroundStyle(Color.statusPresent)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color.statusPresent.opacity(0.15))
                .clipShape(Capsule())
        case .pendingSync:
            Text("Sincronizando")
                .font(.captionSmall)
                .foregroundStyle(Color.textMuted)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color.textMuted.opacity(0.15))
                .clipShape(Capsule())
        case .approved:
            Text("Aprobada")
                .font(.captionSmall)
                .foregroundStyle(Color.accent)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color.accent.opacity(0.15))
                .clipShape(Capsule())
        }
    }

    // MARK: Data loading

    private func loadData() async {
        isLoading = true
        async let attendanceFetch = services.attendance.getAttendance(meetingId: meeting.id)
        async let recordsFetch = services.activities.getActivityRecords(meetingId: meeting.id)
        async let membersFetch = services.members.getMembers(groupId: meeting.smallGroupId)
        async let typesFetch = services.activities.getActivityTypes(scope: nil, campoId: nil, churchId: nil)

        attendance = (try? await attendanceFetch) ?? []
        activityRecords = (try? await recordsFetch) ?? []
        members = (try? await membersFetch) ?? []
        activityTypes = (try? await typesFetch) ?? []

        if let submitterId = meeting.submittedBy,
           let profile = try? await services.auth.getProfiles().first(where: { $0.id == submitterId }) {
            submitterName = profile.firstName + " " + profile.lastName
        }

        isLoading = false
    }
}

#Preview {
    NavigationStack {
        MeetingDetailView(
            meeting: MockData.meetings[0],
            onEdit: {}
        )
        .environment(\.services, .preview)
    }
}
