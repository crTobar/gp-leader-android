import SwiftUI

// MARK: - Step 2: Attendance Marking

struct MeetingStep2_AttendanceView: View {
    @Bindable var vm: NewMeetingViewModel
    @State private var searchText = ""
    @State private var showingAddVisitor = false
    @State private var priorVisitorsExpanded = false

    // Tracks how many times the user has used "Justificado" — stops bounce after 5
    @AppStorage("justifiedActivationCount") private var justifiedActivationCount: Int = 0

    private var filteredMembers: [Member] {
        let base = vm.regularMembers + vm.visitorsForThisMeeting
        guard !searchText.isEmpty else { return base }
        return base.filter { $0.fullName.localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 16) {
                    // Header card
                    NeuCard {
                        VStack(alignment: .leading, spacing: 14) {
                            HStack(alignment: .center) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("MIEMBROS")
                                        .font(.captionStyle)
                                        .foregroundStyle(Color.textMuted)
                                    Text(DateFormatters.formatMeetingDate(vm.meetingDate))
                                        .font(.displayItalic)
                                        .foregroundStyle(Color.accent)
                                }
                                Spacer()
                                // Count badge
                                Text("\(vm.presentCount)/\(vm.totalCount)")
                                    .font(.headingMedium)
                                    .foregroundStyle(Color.accent)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.accent.opacity(0.1))
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                            }

                            // Apply-to-all dropdown menu
                            Menu {
                                Button {
                                    vm.setAllStatus(.justified)
                                    if justifiedActivationCount < 5 { justifiedActivationCount += 1 }
                                } label: {
                                    Label {
                                        Text("Todos Justificados")
                                    } icon: {
                                        LucideIcon(uiImage: AppIcon.clock, size: 18)
                                    }
                                }
                                Button {
                                    vm.setAllStatus(.present)
                                } label: {
                                    Label {
                                        Text("Todos Presentes")
                                    } icon: {
                                        LucideIcon(uiImage: AppIcon.checkCircle, size: 18)
                                    }
                                }
                                Button {
                                    vm.setAllStatus(.absent)
                                } label: {
                                    Label {
                                        Text("Todos Ausentes")
                                    } icon: {
                                        LucideIcon(uiImage: AppIcon.xCircle, size: 18)
                                    }
                                }
                            } label: {
                                HStack(spacing: 6) {
                                    Text("Sel. todos")
                                        .font(.bodyLarge)
                                    Image(uiImage: AppIcon.chevronDown)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 11, height: 11)
                                }
                                .foregroundStyle(Color.textSecondary)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color.neuBackground)
                                .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
                                .shadow(color: Color(hex: "c2c8d4"), radius: 6, x: 2, y: 2)
                                .shadow(color: Color.white, radius: 6, x: -2, y: -2)
                            }

                            NeuProgressBar(value: vm.attendanceRate)
                                .animation(.easeInOut(duration: 0.4), value: vm.attendanceRate)

                            Text("\(vm.presentCount) de \(vm.totalCount) presentes")
                                .font(.captionStyle)
                                .foregroundStyle(Color.textSecondary)

                            Divider().opacity(0.3)

                            // Date row (read-only)
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("FECHA DE REUNIÓN")
                                        .font(.captionStyle)
                                        .foregroundStyle(Color.textMuted)
                                    Text(DateFormatters.formatMeetingDate(vm.meetingDate))
                                        .font(.bodyLarge)
                                        .foregroundStyle(Color.textPrimary)
                                }
                                Spacer()
                            }

                            // No meeting toggle
                            Button {
                                withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                                    vm.noMeetingToday.toggle()
                                    HapticFeedback.medium()
                                }
                            } label: {
                                HStack(spacing: 8) {
                                    RoundedRectangle(cornerRadius: 4)
                                        .stroke(vm.noMeetingToday ? Color.blush : Color.textMuted, lineWidth: 1.5)
                                        .background(vm.noMeetingToday ? Color.blush : Color.clear)
                                        .frame(width: 18, height: 18)
                                        .clipShape(RoundedRectangle(cornerRadius: 4))
                                    Text("No hubo reunión")
                                        .font(.captionStyle)
                                        .foregroundStyle(vm.noMeetingToday ? Color.blush : Color.textMuted)
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)

                    // Member list
                    LazyVStack(spacing: 10) {
                        ForEach(filteredMembers) { member in
                            AttendanceRow(
                                member: member,
                                status: Binding(
                                    get: { vm.attendanceMap[member.id] ?? .absent },
                                    set: { vm.attendanceMap[member.id] = $0 }
                                ),
                                justifiedActivationCount: $justifiedActivationCount
                            )
                        }
                    }
                    .padding(.horizontal, 16)

                    // Visitors section
                    VStack(alignment: .leading, spacing: 0) {
                        // Dark header
                        HStack {
                            Text("VISITAS DE HOY")
                                .font(.captionStyle)
                                .foregroundStyle(.white)
                            Spacer()
                            if !vm.priorVisitors.isEmpty {
                                Button {
                                    withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                                        priorVisitorsExpanded.toggle()
                                    }
                                } label: {
                                    HStack(spacing: 4) {
                                        Text("Anteriores (\(vm.priorVisitors.count))")
                                            .font(.captionSmall)
                                            .foregroundStyle(.white.opacity(0.8))
                                        Image(uiImage: priorVisitorsExpanded ? AppIcon.chevronUp : AppIcon.chevronDown)
                                            .renderingMode(.template)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 10, height: 10)
                                            .foregroundStyle(.white.opacity(0.8))
                                    }
                                }
                            }
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.neuBackgroundDark)
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                        // Today's visitors
                        VStack(spacing: 10) {
                            if vm.visitorsForThisMeeting.isEmpty && !priorVisitorsExpanded {
                                Text("Sin visitantes")
                                    .font(.captionStyle)
                                    .foregroundStyle(Color.textMuted)
                                    .padding(.vertical, 8)
                            }

                            ForEach(vm.visitorsForThisMeeting) { visitor in
                                HStack(spacing: 12) {
                                    ZStack {
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(Color.neuBackgroundDark)
                                            .frame(width: 36, height: 36)
                                        Text(visitor.initials)
                                            .font(.captionStyle)
                                            .foregroundStyle(.white)
                                    }
                                    Text(visitor.displayName)
                                        .font(.bodyLarge)
                                        .foregroundStyle(Color.textPrimary)
                                    Spacer()
                                    Text("Visita")
                                        .font(.captionSmall)
                                        .foregroundStyle(Color.textMuted)
                                    Button {
                                        withAnimation { vm.removeVisitorFromMeeting(visitor) }
                                    } label: {
                                        Image(uiImage: AppIcon.close)
                                            .renderingMode(.template)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 13, height: 13)
                                            .foregroundStyle(Color.textMuted)
                                    }
                                }
                            }

                            if priorVisitorsExpanded {
                                ForEach(vm.priorVisitors) { visitor in
                                    HStack(spacing: 12) {
                                        ZStack {
                                            Circle()
                                                .fill(Color.neuBackground)
                                                .frame(width: 32, height: 32)
                                                .shadow(color: Color(hex: "c2c8d4"), radius: 6, x: 2, y: 2)
                                                .shadow(color: Color.white, radius: 6, x: -2, y: -2)
                                            Text(visitor.initials)
                                                .font(.captionSmall)
                                                .foregroundStyle(Color.textSecondary)
                                        }
                                        Text(visitor.displayName)
                                            .font(.bodyRegular)
                                            .foregroundStyle(Color.textSecondary)
                                        Spacer()
                                        if !vm.visitorsForThisMeeting.contains(where: { $0.id == visitor.id }) {
                                            Button {
                                                vm.addVisitorToMeeting(visitor)
                                            } label: {
                                                Image(uiImage: AppIcon.plus)
                                                    .renderingMode(.template)
                                                    .resizable()
                                                    .scaledToFit()
                                                    .frame(width: 14, height: 14)
                                                    .foregroundStyle(Color.accent)
                                            }
                                        }
                                    }
                                }
                            }

                            Button {
                                HapticFeedback.light()
                                showingAddVisitor = true
                            } label: {
                                HStack(spacing: 8) {
                                    Image(uiImage: AppIcon.plus)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 14, height: 14)
                                    Text("Agregar visita")
                                        .font(.captionStyle)
                                }
                                .foregroundStyle(Color.accent)
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 10)
                        .background(Color.neuBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .shadow(color: Color.neuShadowDark.opacity(0.25), radius: 4, x: 0, y: 2)
                    }
                    .padding(.horizontal, 16)

                    Spacer(minLength: 80)
                }
            }
            .searchable(text: $searchText, prompt: "Buscar miembro...")

            bottomBar
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .alert("¿Todos ausentes?", isPresented: $vm.showAllAbsentAlert) {
            Button("Continuar de todas formas", role: .destructive) {
                vm.confirmNextStepDespiteAllAbsent()
            }
            Button("Revisar asistencia", role: .cancel) {}
        } message: {
            Text("Todos los miembros están marcados como ausentes. ¿Deseas continuar?")
        }
        .sheet(isPresented: $showingAddVisitor) {
            AddVisitorSheetView(vm: vm)
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
        }
    }

    private var bottomBar: some View {
        VStack(spacing: 8) {
            if vm.noMeetingToday {
                HStack(spacing: 6) {
                    Image(uiImage: AppIcon.info)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 13)
                    Text("Se omitirán las actividades porque no hubo reunión")
                        .font(.captionStyle)
                }
                .foregroundStyle(Color.blush)
                .frame(maxWidth: .infinity, alignment: .center)
                .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
            NeuButton(
                vm.noMeetingToday ? "Continuar → Resumen" : "Continuar → Actividades",
                icon: AppIcon.arrowRight,
                variant: .primary
            ) {
                vm.nextStep()
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .animation(.easeInOut(duration: 0.2), value: vm.noMeetingToday)
        .background(
            Color.neuBackground
                .shadow(color: Color.neuShadowDark.opacity(0.2), radius: 8, x: 0, y: -4)
                .ignoresSafeArea(edges: .bottom)
        )
    }
}

// MARK: - Attendance Row

private struct AttendanceRow: View {
    let member: Member
    @Binding var status: AttendanceStatus
    @Binding var justifiedActivationCount: Int

    @State private var dragOffset: CGFloat = 0
    @State private var isPulsing: Bool = false
    @State private var showMenu: Bool = false
    @State private var showJustifiedBounce: Bool = false

    /// Toque en la cajita = alternar presente / ausente. Arrastre a la derecha = justificado. Pulsación larga = menú (justificado / ausente).
    private let justifiedBandWidth: CGFloat = 88

    private var maxDragOffset: CGFloat { justifiedBandWidth + 20 }

    var body: some View {
        ZStack(alignment: .leading) {
            // Fondo: al arrastrar a la derecha solo se revela Justificado
            HStack(spacing: 0) {
                ZStack {
                    Color.statusJustified
                    Image(uiImage: AppIcon.calendarClock)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.white)
                }
                .frame(width: justifiedBandWidth)

                Spacer(minLength: 0)
            }
            .frame(width: min(dragOffset, justifiedBandWidth), alignment: .leading)
            .clipped()

            NeuMemberRow(member: member) {
                presentTapBox
            }
            .scaleEffect(isPulsing ? 1.05 : 1.0)
            .offset(x: dragOffset)
            .animation(.spring(response: 0.3, dampingFraction: 0.6), value: dragOffset)
            .gesture(
                DragGesture(minimumDistance: 14)
                    .onChanged { value in
                        let x = value.translation.width
                        dragOffset = max(0, min(x, maxDragOffset))
                    }
                    .onEnded { value in
                        let w = value.translation.width
                        let commitJustified = w >= justifiedBandWidth * 0.45
                        if commitJustified {
                            applyStatus(.justified)
                            if justifiedActivationCount < 5 { justifiedActivationCount += 1 }
                        }
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                            dragOffset = 0
                        }
                    }
            )
            .simultaneousGesture(
                LongPressGesture(minimumDuration: 0.45)
                    .onEnded { _ in
                        HapticFeedback.medium()
                        if justifiedActivationCount < 5 {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.4).repeatCount(2)) {
                                showJustifiedBounce = true
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                                showJustifiedBounce = false
                            }
                        }
                        showMenu = true
                    }
            )
            .confirmationDialog("Estado de asistencia", isPresented: $showMenu, titleVisibility: .visible) {
                Button {
                    applyStatus(.justified)
                    if justifiedActivationCount < 5 { justifiedActivationCount += 1 }
                } label: {
                    HStack {
                        Text("Justificado")
                        if justifiedActivationCount < 5 { Text(" ✨") }
                    }
                }
                Button("Ausente") { applyStatus(.absent) }
                Button("Cancelar", role: .cancel) {}
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
    }

    /// Tap alternates presente ↔ ausente; swipe a la derecha sigue siendo justificado; pulsación larga abre el diálogo.
    private var presentTapBox: some View {
        Button {
            togglePresentOrAbsent()
        } label: {
            statusIndicator
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Presente o ausente")
        .accessibilityHint("Toca para alternar presente y ausente. Desliza a la derecha para justificado. Mantén pulsada la fila para más opciones.")
    }

    private func togglePresentOrAbsent() {
        if status == .present {
            applyStatus(.absent)
        } else {
            applyStatus(.present)
        }
    }

    // MARK: - Status indicator (44×44 touch target)

    private var statusIndicator: some View {
        ZStack {
            switch status {
            case .absent:
                RoundedRectangle(cornerRadius: 5)
                    .stroke(Color.textMuted, lineWidth: 1.5)
                    .frame(width: 26, height: 26)
            case .present:
                RoundedRectangle(cornerRadius: 5)
                    .fill(Color.statusPresent)
                    .frame(width: 26, height: 26)
                Image(uiImage: AppIcon.check)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 14, height: 14)
                    .foregroundStyle(.white)
            case .justified:
                RoundedRectangle(cornerRadius: 5)
                    .fill(Color.statusJustified)
                    .frame(width: 26, height: 26)
                Image(uiImage: AppIcon.calendarClock)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 14, height: 14)
                    .foregroundStyle(.white)
                    .scaleEffect(showJustifiedBounce ? 1.3 : 1.0)
                    .animation(.spring(response: 0.2, dampingFraction: 0.4), value: showJustifiedBounce)
            }
        }
        .frame(width: 44, height: 44)
    }

    // MARK: - Apply with pulse animation

    private func applyStatus(_ newStatus: AttendanceStatus) {
        status = newStatus
        HapticFeedback.light()
        withAnimation(.spring(response: 0.15, dampingFraction: 0.5)) {
            isPulsing = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
            withAnimation(.spring(response: 0.2, dampingFraction: 0.6)) {
                isPulsing = false
            }
        }
    }
}

#Preview {
    let vm = NewMeetingViewModel(
        services: .preview,
        groupId: MockData.group1Id,
        profileId: MockData.profileId
    )
    vm.members = MockData.members
    for member in MockData.members {
        vm.attendanceMap[member.id] = .absent
    }
    return MeetingStep2_AttendanceView(vm: vm)
}
