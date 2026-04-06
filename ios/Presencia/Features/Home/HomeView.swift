import SwiftUI
import Charts

struct HomeView: View {
    @Environment(\.services) private var services
    @State private var vm: HomeViewModel?

    let onTakeAttendance: () -> Void
    let onViewHistory: () -> Void
    let onMeetingSelected: (Meeting) -> Void

    init(
        onTakeAttendance: @escaping () -> Void,
        onViewHistory: @escaping () -> Void,
        onMeetingSelected: @escaping (Meeting) -> Void
    ) {
        self.onTakeAttendance = onTakeAttendance
        self.onViewHistory = onViewHistory
        self.onMeetingSelected = onMeetingSelected
    }

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            if let vm {
                HomeContentView(
                    vm: vm,
                    onTakeAttendance: onTakeAttendance,
                    onViewHistory: onViewHistory,
                    onMeetingSelected: onMeetingSelected
                )
            } else {
                Color.neuBackground.ignoresSafeArea()
            }
        }
        .navigationBarHidden(true)
        .toolbarBackground(Color.neuBackground, for: .navigationBar)
        .toolbarColorScheme(.light, for: .navigationBar)
        .task {
            if vm == nil {
                vm = HomeViewModel(services: services)
            }
            await vm?.load()
        }
    }
}

// MARK: - Content view

private struct HomeContentView: View {
    @Bindable var vm: HomeViewModel

    let onTakeAttendance: () -> Void
    let onViewHistory: () -> Void
    let onMeetingSelected: (Meeting) -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // 1. Welcome header
                HomeWelcomeHeader(
                    vm: vm,
                    onTakeAttendance: onTakeAttendance
                )
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 20)

                if vm.isLoading {
                    LoadingStateView(rows: 3).padding(.horizontal, 20)
                } else {
                    VStack(spacing: 16) {
                        // 2. Group card
                        if let group = vm.activeGroup {
                            GroupInfoCard(group: group, attendanceRate: vm.quarterSummary.avgRate)
                        }
                        // 3. Stats row
                        HomeStatsRow(vm: vm)
                        // 4. Recent meetings
                        RecentMeetingsSection(
                            meetings: vm.recentMeetings,
                            attendance: vm.recentMeetingAttendance,
                            onViewAll: onViewHistory,
                            onMeetingSelected: onMeetingSelected
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 110)
                }
            }
        }
        .refreshable { await vm.load() }
        .background(Color.neuBackground)
    }
}

// MARK: - Welcome Header

private struct HomeWelcomeHeader: View {
    @Environment(\.sessionMember) private var sessionMember: Member?

    let vm: HomeViewModel
    let onTakeAttendance: () -> Void

    private var welcomeName: String {
        if let sessionMember {
            return sessionMember.displayName
        }
        return vm.profile?.fullName ?? vm.profile?.firstName ?? "Líder"
    }

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text("Hola,")
                    .font(.bodyLarge)
                    .foregroundStyle(Color.textSecondary)
                Text(welcomeName)
                    .font(.headingLarge)
                    .foregroundStyle(Color.textPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }

            Spacer(minLength: 8)

            HStack(spacing: 8) {
                // Kotlin SmallButtonPrimary — glow + Accent fill
                Button(action: onTakeAttendance) {
                    Text("+ Registrar")
                        .font(.system(size: 16, weight: .semibold, design: .rounded))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(Color.accent)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 8, x: 4, y: 4)
                        .shadow(color: Color.neuShadowLight.opacity(0.85), radius: 8, x: -4, y: -4)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

// MARK: - Group Info Card

private struct GroupInfoCard: View {
    let group: SmallGroup
    let attendanceRate: Double?

    private func meetingSchedule(_ g: SmallGroup) -> String {
        var parts: [String] = []
        if let day = g.meetingDay { parts.append(day) }
        if let time = g.meetingTime {
            let fmt = DateFormatter()
            fmt.dateFormat = "h:mm a"
            parts.append(fmt.string(from: time))
        }
        return parts.isEmpty ? "Grupo activo" : parts.joined(separator: " · ")
    }

    var body: some View {
        NeuCard {
            VStack(alignment: .leading, spacing: 8) {
                Text("TU GRUPO PEQUEÑO")
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)

                Text(group.name)
                    .font(.displayItalic)
                    .foregroundStyle(Color.textPrimary)

                Text(meetingSchedule(group))
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textSecondary)

                if let avgRate = attendanceRate {
                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 12) {
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    Capsule()
                                        .fill(Color.neuBackgroundDeep)
                                        .frame(height: 8)
                                    Capsule()
                                        .fill(Color.statusPresent)
                                        .frame(width: max(0, geo.size.width * CGFloat(avgRate)), height: 8)
                                }
                            }
                            .frame(height: 8)

                            Text("\(Int(avgRate * 100))%")
                                .font(.system(size: 16, weight: .semibold, design: .rounded))
                                .foregroundStyle(Color.statusPresent)
                        }

                        Text("Asistencia periodo actual")
                            .font(.captionStyle)
                            .foregroundStyle(Color.textMuted)
                    }
                    .padding(.top, 4)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

// MARK: - Stats Row

private struct HomeStatsRow: View {
    let vm: HomeViewModel

    var body: some View {
        HStack(spacing: 0) {
            // Dark hero tile (Kotlin StatCellDark — Ink fill)
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.textPrimary)
                    .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 4, y: 4)
                    .shadow(color: Color.neuShadowLight.opacity(0.6), radius: 8, x: -4, y: -4)
                VStack(spacing: 4) {
                    if let rate = vm.quarterSummary.avgRate {
                        Text("\(Int(rate * 100))%")
                            .font(.displayLarge)
                            .foregroundStyle(.white)
                    } else {
                        Text("--")
                            .font(.displayLarge)
                            .foregroundStyle(.white)
                    }
                    Text("ASISTENCIA")
                        .font(.captionStyle)
                        .foregroundStyle(.white.opacity(0.7))
                }
                .padding(.vertical, 20)
                .padding(.horizontal, 8)
            }
            .padding(6)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 90)

            // Light stat — Presentes
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.neuBackground)
                    .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 4, y: 4)
                    .shadow(color: Color.white.opacity(0.9), radius: 6, x: -4, y: -4)
                VStack(spacing: 4) {
                    Text("\(vm.presentCount)")
                        .font(.displayMedium)
                        .foregroundStyle(Color.textPrimary)
                    Text("PRESENTES")
                        .font(.captionStyle)
                        .foregroundStyle(Color.textMuted)
                }
                .padding(.vertical, 20)
                .padding(.horizontal, 8)
            }
            .padding(6)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 90)

            // Light stat — Ausentes
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.neuBackground)
                    .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 4, y: 4)
                    .shadow(color: Color.white.opacity(0.9), radius: 6, x: -4, y: -4)
                VStack(spacing: 4) {
                    Text("\(vm.absentCount)")
                        .font(.displayMedium)
                        .foregroundStyle(Color.textPrimary)
                    Text("AUSENTES")
                        .font(.captionStyle)
                        .foregroundStyle(Color.textMuted)
                }
                .padding(.vertical, 20)
                .padding(.horizontal, 8)
            }
            .padding(6)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 90)
        }
    }
}

// MARK: - Recent Meetings Section

private struct RecentMeetingsSection: View {
    let meetings: [Meeting]
    let attendance: [UUID: (present: Int, absent: Int)]
    let onViewAll: () -> Void
    let onMeetingSelected: (Meeting) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Section header with lines
            HStack(spacing: 10) {
                Rectangle()
                    .fill(Color.textMuted.opacity(0.35))
                    .frame(height: 1)
                Text("REUNIONES RECIENTES")
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)
                    .fixedSize()
                Rectangle()
                    .fill(Color.textMuted.opacity(0.35))
                    .frame(height: 1)
            }

            if meetings.isEmpty {
                HomeRecentEmptyState()
            } else {
                ForEach(meetings) { meeting in
                    let counts = attendance[meeting.id]
                    HomeRecentMeetingRow(
                        meeting: meeting,
                        presentCount: counts?.present,
                        absentCount: counts?.absent,
                        onTap: {
                            HapticFeedback.light()
                            onMeetingSelected(meeting)
                        }
                    )
                }
            }

            Button(action: onViewAll) {
                Text("Ver todas las reuniones →")
                    .font(Font.titleLarge)
                    .foregroundStyle(Color.accent)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 8, x: 4, y: 4)
                    .shadow(color: Color.neuShadowLight.opacity(0.85), radius: 8, x: -4, y: -4)
            }
            .buttonStyle(.plain)
        }
    }
}

// MARK: - Empty recent meetings (Kotlin HomeScreen)

private struct HomeRecentEmptyState: View {
    var body: some View {
        VStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.neuBackgroundDeep)
                .frame(width: 72, height: 72)
                .overlay {
                    Text("📅")
                        .font(.system(size: 32))
                }
            Text("Aún no hay reuniones registradas.")
                .font(Font.titleLarge)
                .foregroundStyle(Color.textPrimary)
                .multilineTextAlignment(.center)
            Text("¡Registra la primera!")
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
    }
}

// MARK: - Recent Meeting Row

private struct HomeRecentMeetingRow: View {
    let meeting: Meeting
    let presentCount: Int?
    let absentCount: Int?
    let onTap: () -> Void

    private var dayNumber: String {
        let day = Calendar.current.component(.day, from: meeting.meetingDate)
        return "\(day)"
    }

    private var monthAbbrev: String {
        let fmt = DateFormatter()
        fmt.locale = Locale(identifier: "es_CO")
        fmt.dateFormat = "MMM"
        return fmt.string(from: meeting.meetingDate).uppercased()
    }

    private var progressRatio: CGFloat {
        guard let p = presentCount, let a = absentCount, p + a > 0 else { return 0 }
        return CGFloat(p) / CGFloat(p + a)
    }

    var body: some View {
        Button(action: onTap) {
            NeuCard(padding: 16) {
                HStack(alignment: .center, spacing: 12) {
                    // Date column
                    VStack(spacing: 2) {
                        Text(dayNumber)
                            .font(Font.headlineMedium)
                            .foregroundStyle(Color.textPrimary)
                        Text(monthAbbrev)
                            .font(.captionStyle)
                            .foregroundStyle(Color.textMuted)
                    }
                    .frame(width: 44)

                    Rectangle()
                        .fill(Color.textMuted.opacity(0.3))
                        .frame(width: 1, height: 56)

                    VStack(alignment: .leading, spacing: 6) {
                        Text("Reunión Semanal")
                            .font(Font.titleLarge)
                            .foregroundStyle(Color.textPrimary)
                            .lineLimit(1)

                        HStack(spacing: 6) {
                            statusBadge
                            if let p = presentCount {
                                Text("\(p) pres.")
                                    .font(.captionSmall)
                                    .foregroundStyle(Color.textSecondary)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color.neuBackgroundDeep)
                                    .clipShape(RoundedRectangle(cornerRadius: 4))
                            }
                        }

                        if presentCount != nil || absentCount != nil {
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    Capsule()
                                        .fill(Color.neuBackgroundDeep)
                                        .frame(height: 5)
                                    Capsule()
                                        .fill(Color.statusPresent)
                                        .frame(width: max(0, geo.size.width * progressRatio), height: 5)
                                }
                            }
                            .frame(height: 5)
                        }
                    }

                    Spacer(minLength: 4)

                    Text("›")
                        .font(Font.headlineMedium)
                        .foregroundStyle(Color.textMuted)
                }
            }
        }
        .buttonStyle(.plain)
        .contentShape(Rectangle())
        .accessibilityLabel("Reunión del \(dayNumber) \(monthAbbrev)")
        .accessibilityHint("Abrir detalle")
    }

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
}

// MARK: - Preview

#Preview {
    HomeView(
        onTakeAttendance: {},
        onViewHistory: {},
        onMeetingSelected: { _ in }
    )
    .environment(\.services, .preview)
    .environment(\.sessionMember, MockData.members.first)
}
