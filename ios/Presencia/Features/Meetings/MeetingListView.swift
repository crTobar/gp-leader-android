import SwiftUI

// MARK: - Meeting List View (Kotlin HistorialScreen parity)

struct MeetingListView: View {
    @Environment(\.services) private var services

    let groupId: UUID
    let onNewMeeting: () -> Void
    let onMeetingSelected: (Meeting) -> Void

    @State private var vm: MeetingListViewModel
    @State private var attendanceByMeeting: [UUID: [Attendance]] = [:]
    @State private var activityRecordsByMeeting: [UUID: [ActivityRecord]] = [:]
    @State private var activityTypes: [ActivityType] = []
    @State private var isSearchPresented = false

    init(groupId: UUID,
         onNewMeeting: @escaping () -> Void,
         onMeetingSelected: @escaping (Meeting) -> Void) {
        self.groupId = groupId
        self.onNewMeeting = onNewMeeting
        self.onMeetingSelected = onMeetingSelected
        _vm = State(initialValue: MeetingListViewModel(
            services: ServiceContainer.preview,
            groupId: groupId
        ))
    }

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()
            content
        }
        .toolbar(.hidden, for: .navigationBar)
        .refreshable { await vm.load() }
        .alert("Error", isPresented: Binding(
            get: { vm.errorMessage != nil },
            set: { if !$0 { vm.errorMessage = nil } }
        )) {
            Button("Aceptar", role: .cancel) { vm.errorMessage = nil }
        } message: {
            Text(vm.errorMessage ?? "")
        }
        .task {
            vm = MeetingListViewModel(services: services, groupId: groupId)
            await vm.load()
            await loadActivityTypes()
        }
    }

    // MARK: - Content

    @ViewBuilder
    private var content: some View {
        VStack(spacing: 0) {
            if vm.isLoading {
                ScrollView {
                    LoadingStateView(rows: 4)
                        .padding(.horizontal, 20)
                        .padding(.top, 8)
                }
            } else if vm.filteredMeetings.isEmpty && vm.searchText.isEmpty {
                historialChrome
                QuarterEmptyState(quarter: vm.selectedQuarter, showAll: vm.selectedQuarter == 0, onNewMeeting: onNewMeeting)
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 12) {
                        historialChrome

                        let stats = vm.quarterStats(attendance: attendanceByMeeting)
                        if stats.total > 0 {
                            QuarterStatsCard(
                                quarter: vm.selectedQuarter,
                                year: vm.selectedYear,
                                stats: stats
                            )
                            .padding(.horizontal, 20)
                        }

                        if vm.filteredMeetings.isEmpty {
                            Text("Sin resultados para \"\(vm.searchText)\"")
                                .font(.bodyRegular)
                                .foregroundStyle(Color.textMuted)
                                .frame(maxWidth: .infinity)
                                .padding(.top, 32)
                        } else {
                            ForEach(vm.meetingsByMonth, id: \.header) { section in
                                monthHeader(section.header)

                                ForEach(section.meetings) { meeting in
                                    MeetingCardView(
                                        meeting: meeting,
                                        attendance: attendanceByMeeting[meeting.id] ?? [],
                                        activityRecords: activityRecordsByMeeting[meeting.id] ?? [],
                                        activityTypes: activityTypes
                                    )
                                    .padding(.horizontal, 20)
                                    .contentShape(Rectangle())
                                    .onTapGesture {
                                        HapticFeedback.light()
                                        onMeetingSelected(meeting)
                                    }
                                    .contextMenu {
                                        Button {
                                            HapticFeedback.light()
                                            onMeetingSelected(meeting)
                                        } label: {
                                            Label {
                                                Text("Ver detalle")
                                            } icon: {
                                                LucideIcon(uiImage: AppIcon.file, size: 18)
                                            }
                                        }
                                        if meeting.status == .draft {
                                            Button(role: .destructive) {
                                                Task { await vm.deleteDraft(meeting) }
                                            } label: {
                                                Label {
                                                    Text("Eliminar borrador")
                                                } icon: {
                                                    LucideIcon(uiImage: AppIcon.delete, size: 18)
                                                }
                                            }
                                        }
                                    }
                                    .swipeActions(edge: .leading, allowsFullSwipe: false) {
                                        if meeting.status == .draft {
                                            Button {
                                                HapticFeedback.light()
                                                onMeetingSelected(meeting)
                                            } label: {
                                                Label {
                                                    Text("Editar")
                                                } icon: {
                                                    LucideIcon(uiImage: AppIcon.edit, size: 18)
                                                }
                                            }
                                            .tint(Color.accent)
                                        }
                                    }
                                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                        if meeting.status == .draft {
                                            Button(role: .destructive) {
                                                Task { await vm.deleteDraft(meeting) }
                                            } label: {
                                                Label {
                                                    Text("Eliminar")
                                                } icon: {
                                                    LucideIcon(uiImage: AppIcon.delete, size: 18)
                                                }
                                            }
                                        }
                                    }
                                    .task(id: meeting.id) {
                                        await loadMeetingData(for: meeting)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.bottom, 110)
                }
            }
        }
    }

    /// Kotlin HistorialTopBar + TrimestresRow (title, actions, chips).
    private var historialChrome: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isSearchPresented {
                // Inline search bar
                HStack(spacing: 10) {
                    HStack(spacing: 8) {
                        LucideIcon(uiImage: AppIcon.search, size: 16)
                            .foregroundStyle(Color.textMuted)
                        TextField("Buscar por fecha...", text: $vm.searchText)
                            .font(.bodyRegular)
                            .foregroundStyle(Color.textPrimary)
                            .autocorrectionDisabled()
                            .submitLabel(.search)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 12)
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 5, x: 3, y: 3)
                    .shadow(color: Color.neuShadowLight.opacity(0.85), radius: 5, x: -3, y: -3)

                    Button {
                        HapticFeedback.light()
                        vm.searchText = ""
                        isSearchPresented = false
                    } label: {
                        Text("Cancelar")
                            .font(.bodyRegular)
                            .foregroundStyle(Color.accent)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
            } else {
                HStack(alignment: .center) {
                    Text("Historial")
                        .font(.displayLarge)
                        .foregroundStyle(Color.textPrimary)

                    Spacer(minLength: 12)

                    Button {
                        HapticFeedback.medium()
                        onNewMeeting()
                    } label: {
                        LucideIcon(uiImage: AppIcon.plusCircle, size: 24)
                            .foregroundStyle(Color.accent)
                            .frame(width: 44, height: 44)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Nueva reunión")

                    Button {
                        HapticFeedback.light()
                        isSearchPresented = true
                    } label: {
                        LucideIcon(uiImage: AppIcon.search, size: 22)
                            .foregroundStyle(Color.textPrimary)
                            .frame(width: 44, height: 44)
                            .background(Color.neuBackground)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 2, y: 2)
                            .shadow(color: Color.neuShadowLight.opacity(0.85), radius: 6, x: -2, y: -2)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Buscar")
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
            }

            HistorialTrimesterChipsRow(
                selectedQuarter: $vm.selectedQuarter,
                year: vm.selectedYear
            )
        }
        .padding(.bottom, 4)
    }

    private func monthHeader(_ title: String) -> some View {
        HStack(spacing: 10) {
            Rectangle()
                .fill(Color.neuShadowDark.opacity(0.35))
                .frame(height: 1)
            Text(title)
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
                .fixedSize()
            Rectangle()
                .fill(Color.neuShadowDark.opacity(0.35))
                .frame(height: 1)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
    }

    // MARK: - Data helpers

    private func loadActivityTypes() async {
        do {
            activityTypes = try await services.activities.getActivityTypes(
                scope: nil, campoId: nil, churchId: nil
            )
        } catch {
            activityTypes = []
        }
    }

    private func loadMeetingData(for meeting: Meeting) async {
        async let attendanceFetch = services.attendance.getAttendance(meetingId: meeting.id)
        async let recordsFetch = services.activities.getActivityRecords(meetingId: meeting.id)
        do {
            let (att, rec) = try await (attendanceFetch, recordsFetch)
            attendanceByMeeting[meeting.id] = att
            activityRecordsByMeeting[meeting.id] = rec
        } catch {}
    }
}

// MARK: - Trimester chips (Kotlin TrimestresRow / VerTodoChip)

private struct HistorialTrimesterChipsRow: View {
    @Binding var selectedQuarter: Int
    let year: Int

    private let chips: [(quarter: Int, line1: String, line2: String)] = [
        (1, "1er", "Ene–Mar"),
        (2, "2do", "Abr–Jun"),
        (3, "3er", "Jul–Sep"),
        (4, "4to", "Oct–Dic"),
    ]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(chips, id: \.quarter) { chip in
                    TrimesterChip(
                        line1: "\(chip.line1) Trim.",
                        line2: chip.line2,
                        isSelected: selectedQuarter == chip.quarter
                    ) {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.78)) {
                            selectedQuarter = chip.quarter
                        }
                        HapticFeedback.light()
                    }
                }
                VerTodoChip(isSelected: selectedQuarter == 0) {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.78)) {
                        selectedQuarter = 0
                    }
                    HapticFeedback.light()
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 4)
        }
    }
}

private struct TrimesterChip: View {
    let line1: String
    let line2: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 3) {
                HStack(spacing: 4) {
                    Text(line1)
                        .font(.captionStyle)
                        .fontWeight(.bold)
                        .foregroundStyle(isSelected ? Color.white : Color.textPrimary)
                    if isSelected {
                        LucideIcon(uiImage: AppIcon.check, size: 12)
                            .foregroundStyle(Color.statusPresent)
                    }
                }
                Text(line2)
                    .font(.captionStyle)
                    .foregroundStyle(isSelected ? Color.white.opacity(0.65) : Color.textMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .frame(minWidth: 80)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(isSelected ? Color.textPrimary : Color.neuBackground)
            )
            .shadow(
                color: isSelected ? Color.accent.opacity(0.28) : Color.neuShadowDark.opacity(0.4),
                radius: isSelected ? 10 : 6,
                x: isSelected ? 0 : 3,
                y: isSelected ? 2 : 3
            )
            .shadow(
                color: isSelected ? Color.clear : Color.neuShadowLight.opacity(0.85),
                radius: isSelected ? 0 : 6,
                x: isSelected ? 0 : -3,
                y: isSelected ? 0 : -3
            )
        }
        .buttonStyle(.plain)
    }
}

private struct VerTodoChip: View {
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text("Ver todo")
                    .font(.captionStyle)
                    .foregroundStyle(isSelected ? Color.white : Color.accent)
                if isSelected {
                    LucideIcon(uiImage: AppIcon.check, size: 12)
                        .foregroundStyle(Color.statusPresent)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .frame(minWidth: 80)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(isSelected ? Color.textPrimary : Color.neuBackground)
            )
            .overlay {
                if !isSelected {
                    RoundedRectangle(cornerRadius: 20)
                        .strokeBorder(Color.accent, style: StrokeStyle(lineWidth: 1.2, dash: [6, 5]))
                }
            }
            .shadow(
                color: isSelected ? Color.accent.opacity(0.28) : Color.neuShadowDark.opacity(0.4),
                radius: isSelected ? 10 : 6,
                x: isSelected ? 0 : 3,
                y: isSelected ? 2 : 3
            )
            .shadow(
                color: isSelected ? Color.clear : Color.neuShadowLight.opacity(0.85),
                radius: isSelected ? 0 : 6,
                x: isSelected ? 0 : -3,
                y: isSelected ? 0 : -3
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Quarter Stats Card

private struct QuarterStatsCard: View {
    let quarter: Int
    let year: Int
    let stats: MeetingListViewModel.QuarterStats

    var body: some View {
        HStack(spacing: 8) {
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.textPrimary)
                    .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 4, y: 4)
                    .shadow(color: Color.neuShadowLight.opacity(0.6), radius: 8, x: -4, y: -4)
                VStack(spacing: 4) {
                    if let rate = stats.avgAttendanceRate {
                        Text("\(Int(rate * 100))%")
                            .font(.headlineMedium)
                            .foregroundStyle(.white)
                    } else {
                        Text("--")
                            .font(.headlineMedium)
                            .foregroundStyle(.white)
                    }
                    Text("PROMEDIO")
                        .font(.captionSmall)
                        .tracking(1)
                        .foregroundStyle(Color.statusPresent)
                }
                .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 90)

            ListStatTile(value: "\(stats.total)", label: "REUNIONES")
            ListStatTile(value: "\(stats.submitted)", label: "ENVIADAS")
            ListStatTile(value: "\(stats.drafts)", label: "PENDIENTE")
        }
    }
}

private struct ListStatTile: View {
    let value: String
    let label: String

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.neuBackground)
                .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 4, y: 4)
                .shadow(color: Color.white.opacity(0.9), radius: 6, x: -4, y: -4)
            VStack(spacing: 4) {
                Text(value)
                    .font(.titleLarge)
                    .foregroundStyle(Color.textPrimary)
                Text(label)
                    .font(.captionSmall)
                    .tracking(1)
                    .foregroundStyle(Color.textMuted)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }
            .multilineTextAlignment(.center)
            .padding(.horizontal, 4)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 90)
    }
}

// MARK: - Quarter Empty State

private struct QuarterEmptyState: View {
    let quarter: Int
    let showAll: Bool
    let onNewMeeting: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            LucideIcon(uiImage: AppIcon.calendar, size: 40)
                .foregroundStyle(Color.textMuted)
            Text(showAll ? "Sin reuniones" : "Sin reuniones en T\(quarter)")
                .font(.headingMedium)
                .foregroundStyle(Color.textPrimary)
            Text(showAll ? "Aún no hay reuniones registradas." : "Aún no hay reuniones registradas en este trimestre.")
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            NeuButton("Nueva Reunión", icon: nil, variant: .primary, action: onNewMeeting)
                .padding(.horizontal, 40)
            Spacer()
        }
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        MeetingListView(
            groupId: MockData.groups[0].id,
            onNewMeeting: {},
            onMeetingSelected: { _ in }
        )
    }
    .environment(\.services, .preview)
}
