import SwiftUI

// MARK: - Meeting Card View (Kotlin Historial ReunionCard parity)

struct MeetingCardView: View {
    let meeting: Meeting
    let attendance: [Attendance]
    let activityRecords: [ActivityRecord]
    let activityTypes: [ActivityType]

    private var presentCount: Int { attendance.filter { $0.status == .present }.count }
    private var totalCount: Int { attendance.count }
    private var attendanceRatio: Double {
        totalCount > 0 ? Double(presentCount) / Double(totalCount) : 0
    }
    private var absentCount: Int { attendance.filter { $0.status == .absent }.count }
    private var justifiedCount: Int { attendance.filter { $0.status == .justified }.count }

    private var isPendingSync: Bool { meeting.status == .pendingSync }
    private var canEdit: Bool { meeting.status == .draft }

    private var dayNumber: String {
        "\(Calendar.current.component(.day, from: meeting.meetingDate))"
    }

    private var monthShortUpper: String {
        let fmt = DateFormatter()
        fmt.locale = Locale(identifier: "es_CO")
        fmt.dateFormat = "MMM"
        return fmt.string(from: meeting.meetingDate)
            .replacingOccurrences(of: ".", with: "")
            .prefix(3)
            .uppercased()
    }

    private var weekdayShort: String {
        let w = Calendar.current.component(.weekday, from: meeting.meetingDate)
        let names = ["DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB"]
        return names[(w - 1) % 7]
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            dateColumn

            VStack(alignment: .leading, spacing: 5) {
                Text("Reunión Semanal")
                    .font(Font.titleLarge)
                    .foregroundStyle(Color.textPrimary)

                HStack(spacing: 6) {
                    estadoBadge
                    if canEdit {
                        editarBadge
                    }
                }

                if totalCount > 0 {
                    HStack(alignment: .center) {
                        dotsRow
                        Spacer(minLength: 8)
                        Text("\(Int(attendanceRatio * 100))%")
                            .font(.bodyLarge)
                            .fontWeight(.bold)
                            .foregroundStyle(Color.textPrimary)
                    }

                    historialInsetProgress(value: attendanceRatio)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            LucideIcon(uiImage: AppIcon.chevronRight, size: 16)
                .foregroundStyle(Color.textMuted)
        }
        .padding(12)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 8, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 8, x: -4, y: -4)
    }

    // MARK: - Date column (recessed Kotlin style)

    private var dateColumn: some View {
        let bg = isPendingSync ? Color.textMuted.opacity(0.35) : Color.neuBackgroundDeep
        let subColor = isPendingSync ? Color.textSecondary : Color.textMuted
        let numColor = isPendingSync ? Color.textSecondary : Color.textPrimary

        return VStack(spacing: 2) {
            Text(weekdayShort)
                .font(.captionStyle)
                .foregroundStyle(subColor)
            Text(dayNumber)
                .font(Font.headlineMedium)
                .fontWeight(.bold)
                .foregroundStyle(numColor)
            Text(monthShortUpper)
                .font(.captionStyle)
                .foregroundStyle(subColor)
        }
        .frame(minWidth: 52)
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(bg)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    // MARK: - Badges

    @ViewBuilder
    private var estadoBadge: some View {
        if isPendingSync {
            Text("Pendiente")
                .font(.captionSmall)
                .foregroundStyle(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(Color.statusJustified)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        } else if meeting.status == .draft {
            Text("Borrador")
                .font(.captionSmall)
                .foregroundStyle(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(Color.textMuted)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        } else if meeting.status == .approved {
            Text("Aprobada")
                .font(.captionSmall)
                .foregroundStyle(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(Color.accent)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        } else {
            Text("Enviada")
                .font(.captionSmall)
                .foregroundStyle(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(Color.statusPresent)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        }
    }

    private var editarBadge: some View {
        Text("Editar")
            .font(.captionSmall)
            .foregroundStyle(Color.accent)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .overlay(
                RoundedRectangle(cornerRadius: 6)
                    .strokeBorder(Color.accent, style: StrokeStyle(lineWidth: 1, dash: [5, 4]))
            )
    }

    // MARK: - Dots row (Kotlin DotsAsistencia)

    private var dotsRow: some View {
        HStack(spacing: 8) {
            dotLabel(count: presentCount, color: .statusPresent, suffix: "P")
            dotLabel(count: absentCount, color: .blush, suffix: "A")
            if justifiedCount > 0 {
                dotLabel(count: justifiedCount, color: .textMuted, suffix: "J")
            }
        }
    }

    private func dotLabel(count: Int, color: Color, suffix: String) -> some View {
        HStack(spacing: 3) {
            RoundedRectangle(cornerRadius: 3)
                .fill(color)
                .frame(width: 6, height: 6)
            Text("\(count)\(suffix)")
                .font(.captionSmall)
                .foregroundStyle(color)
        }
    }
}

// MARK: - Inset progress (Sage fill, Kotlin neuInsetSm track)

private func historialInsetProgress(value: Double) -> some View {
    GeometryReader { geo in
        ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: 3)
                .fill(Color.neuBackgroundDeep)
                .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 3, x: 2, y: 2)
                .shadow(color: Color.white.opacity(0.6), radius: 3, x: -2, y: -2)

            RoundedRectangle(cornerRadius: 3)
                .fill(Color.statusPresent)
                .frame(width: max(0, geo.size.width * min(1, max(0, value))))
        }
    }
    .frame(height: 6)
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        ScrollView {
            VStack(spacing: 16) {
                MeetingCardView(
                    meeting: MockData.meetings[0],
                    attendance: MockData.attendance(for: MockData.meetings[0]),
                    activityRecords: MockData.activityRecords(for: MockData.meetings[0]),
                    activityTypes: MockData.activityTypes
                )
                MeetingCardView(
                    meeting: MockData.meetings[7],
                    attendance: MockData.attendance(for: MockData.meetings[7]),
                    activityRecords: MockData.activityRecords(for: MockData.meetings[7]),
                    activityTypes: MockData.activityTypes
                )
            }
            .padding()
        }
    }
}
