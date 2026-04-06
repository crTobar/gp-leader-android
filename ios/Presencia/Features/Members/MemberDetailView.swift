import SwiftUI
import LucideIcons

// MARK: - Member Detail (Kotlin MiembroDetalleScreen parity)

struct MemberDetailView: View {
    let groupId: UUID
    let onEdit: (Member) -> Void
    var onMemberUpdated: ((Member) -> Void)? = nil

    @Environment(\.services) private var services
    @Environment(\.dismiss) private var dismiss

    @State private var displayedMember: Member
    @State private var attendance: [Attendance] = []
    @State private var meetings: [Meeting] = []
    @State private var isLoading: Bool = true
    @State private var isTogglingArchive = false
    @State private var showArchiveConfirm = false
    @State private var archiveError: String?
    @State private var loadError: String?

    init(
        member: Member,
        groupId: UUID,
        onEdit: @escaping (Member) -> Void,
        onMemberUpdated: ((Member) -> Void)? = nil
    ) {
        self.groupId = groupId
        self.onEdit = onEdit
        self.onMemberUpdated = onMemberUpdated
        _displayedMember = State(initialValue: member)
    }

    private var archived: Bool { !displayedMember.isActive }

    private var heroFullName: String {
        [displayedMember.firstName, displayedMember.secondName, displayedMember.lastName, displayedMember.secondLastName]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
    }

    private var memberSinceLine: String {
        let m = DateFormatters.monthYear.string(from: displayedMember.createdAt).capitalized
        return "Miembro desde \(m)"
    }

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                detalleTopBar

                ScrollView {
                    VStack(spacing: 16) {
                        if isLoading {
                            LoadingStateView(rows: 4)
                                .padding(.top, 24)
                        } else {
                            heroCard
                            archiveToggleSection
                            infoCard
                            historialCard
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
                    .padding(.bottom, 24)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task { await loadData() }
        .alert("Archivar miembro", isPresented: $showArchiveConfirm) {
            Button("Cancelar", role: .cancel) {}
            Button("Archivar", role: .destructive) {
                Task { await performArchiveToggle() }
            }
        } message: {
            Text("Podrás volver a activarlo cuando quieras desde aquí o desde la lista.")
        }
        .alert("Error", isPresented: Binding(
            get: { archiveError != nil },
            set: { if !$0 { archiveError = nil } }
        )) {
            Button("Aceptar", role: .cancel) { archiveError = nil }
        } message: {
            Text(archiveError ?? "")
        }
        .alert("Error al cargar", isPresented: Binding(
            get: { loadError != nil },
            set: { if !$0 { loadError = nil } }
        )) {
            Button("Aceptar", role: .cancel) { loadError = nil }
        } message: {
            Text(loadError ?? "")
        }
    }

    // MARK: - Archive / activate

    private var archiveToggleSection: some View {
        NeuButton(
            displayedMember.isActive ? "Archivar miembro" : "Activar miembro",
            icon: displayedMember.isActive ? AppIcon.archive : AppIcon.archiveRestore,
            variant: displayedMember.isActive ? .secondary : .primary,
            isLoading: isTogglingArchive
        ) {
            if displayedMember.isActive {
                showArchiveConfirm = true
            } else {
                Task { await performArchiveToggle() }
            }
        }
    }

    @MainActor
    private func performArchiveToggle() async {
        guard !isTogglingArchive else { return }
        isTogglingArchive = true
        defer { isTogglingArchive = false }
        showArchiveConfirm = false
        do {
            try await services.members.toggleMemberActive(id: displayedMember.id)
            var updated = displayedMember
            updated.isActive.toggle()
            displayedMember = updated
            onMemberUpdated?(updated)
            HapticFeedback.success()
        } catch {
            archiveError = "No se pudo actualizar el estado del miembro."
        }
    }

    // MARK: - Top bar

    private var detalleTopBar: some View {
        ZStack {
            HStack {
                Button {
                    HapticFeedback.light()
                    dismiss()
                } label: {
                    LucideIcon(uiImage: AppIcon.chevronLeft, size: 18)
                        .foregroundStyle(Color.textPrimary)
                        .padding(10)
                        .background(Color.neuBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Atrás")

                Spacer()

                Button {
                    HapticFeedback.light()
                    onEdit(displayedMember)
                } label: {
                    Text("Editar")
                        .font(.captionStyle)
                        .foregroundStyle(Color.accent)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.neuBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                }
                .buttonStyle(.plain)
            }

            Text("Detalle miembro")
                .font(.headingLarge)
                .foregroundStyle(Color.textPrimary)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    // MARK: - Hero

    private var heroCard: some View {
        VStack(spacing: 14) {
            Text(displayedMember.initials)
                .font(.titleLarge)
                .foregroundStyle(.white)
                .frame(width: 64, height: 64)
                .background(Color.textPrimary)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)

            Text(heroFullName)
                .font(.headingLarge)
                .foregroundStyle(Color.textPrimary)
                .multilineTextAlignment(.center)

            if displayedMember.isVisitor {
                Text("Visitante")
                    .font(.captionSmall)
                    .foregroundStyle(Color.accent)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.accentGlow)
                    .clipShape(Capsule())
            }

            Text(memberSinceLine)
                .font(.bodyRegular)
                .foregroundStyle(Color.textMuted)

            Text(archived ? "Archivado" : "Activo")
                .font(.captionStyle)
                .foregroundStyle(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 5)
                .background(archived ? Color.textMuted : Color.textPrimary)
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
        .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
    }

    // MARK: - Info card

    private var infoCard: some View {
        VStack(spacing: 0) {
            ForEach(Array(infoRows.enumerated()), id: \.offset) { index, row in
                infoFila(label: row.label, value: row.value)
                if index < infoRows.count - 1 {
                    Rectangle()
                        .fill(Color.neuShadowDark.opacity(0.35))
                        .frame(height: 0.5)
                }
            }
        }
        .padding(24)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
        .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
    }

    private var infoRows: [(label: String, value: String?)] {
        [
            ("PRIMER NOMBRE", displayedMember.firstName),
            ("SEGUNDO NOMBRE", displayedMember.secondName.nilIfEmptyTrimmed),
            ("PRIMER APELLIDO", displayedMember.lastName),
            ("SEGUNDO APELLIDO", displayedMember.secondLastName.nilIfEmptyTrimmed),
            ("TELÉFONO", displayedMember.phone.nilIfEmptyTrimmed),
            ("CORREO ELECTRÓNICO", displayedMember.email.nilIfEmptyTrimmed),
        ]
    }

    private func infoFila(label: String, value: String?) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
            Text(value ?? "—")
                .font(.bodyLarge)
                .fontWeight(.medium)
                .foregroundStyle(value != nil ? Color.textPrimary : Color.textMuted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 12)
    }

    // MARK: - Historial

    private var historialCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("HISTORIAL DE ASISTENCIA")
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)

            if historialRows.isEmpty {
                Text("—")
                    .font(.bodyLarge)
                    .foregroundStyle(Color.textMuted)
            } else {
                VStack(spacing: 10) {
                    ForEach(historialRows) { row in
                        historialFila(row: row)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius, style: .continuous))
        .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
    }

    private struct HistorialRow: Identifiable {
        var id: UUID { meetingId }
        let meetingId: UUID
        let fecha: String
        let letter: String
        let badgeColor: Color
    }

    private var historialRows: [HistorialRow] {
        meetings.map { meeting in
            let record = attendance.first { $0.meetingId == meeting.id && $0.memberId == displayedMember.id }
            let (letter, color): (String, Color) = {
                switch record?.status {
                case .present: return ("P", .statusPresent)
                case .absent: return ("A", .blush)
                case .justified: return ("J", .textMuted)
                case .none: return ("—", .textMuted)
                }
            }()
            return HistorialRow(
                meetingId: meeting.id,
                fecha: DateFormatters.dayMonth.string(from: meeting.meetingDate).capitalized,
                letter: letter,
                badgeColor: color
            )
        }
    }

    private func historialFila(row: HistorialRow) -> some View {
        HStack {
            Text(row.fecha)
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
            Spacer()
            Text(row.letter)
                .font(.captionStyle.bold())
                .foregroundStyle(row.letter == "—" ? Color.textMuted : .white)
                .frame(width: 28, height: 28)
                .background(row.letter == "—" ? Color.neuShadowDark.opacity(0.2) : row.badgeColor)
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        }
    }

    // MARK: - Data

    private func loadData() async {
        do {
            let allMeetings = try await services.meetings.getMeetings(groupId: groupId, dateRange: nil)
            meetings = allMeetings
                .sorted { $0.meetingDate > $1.meetingDate }
                .prefix(12)
                .map { $0 }

            var allAttendance: [Attendance] = []
            for meeting in meetings {
                let records = try await services.attendance.getAttendance(meetingId: meeting.id)
                allAttendance.append(contentsOf: records.filter { $0.memberId == displayedMember.id })
            }
            attendance = allAttendance
        } catch {
            loadError = "No se pudieron cargar los datos. Intenta de nuevo."
        }
        isLoading = false
    }
}

// MARK: - String helper

private extension Optional where Wrapped == String {
    var nilIfEmptyTrimmed: String? {
        switch self {
        case .none: return nil
        case .some(let s):
            let t = s.trimmingCharacters(in: .whitespacesAndNewlines)
            return t.isEmpty ? nil : t
        }
    }
}

#Preview {
    NavigationStack {
        MemberDetailView(
            member: MockData.members[0],
            groupId: MockData.group1Id,
            onEdit: { _ in }
        )
        .environment(\.services, .preview)
    }
}
