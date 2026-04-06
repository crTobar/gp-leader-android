import SwiftUI

// MARK: - Member List (Kotlin MiembrosListaScreen parity)

struct MemberListView: View {
    @Environment(\.services) private var services
    @State private var vm: MemberListViewModel?

    let groupId: UUID
    var profileId: UUID? = nil
    let onMemberSelected: (Member) -> Void

    @State private var showingAddSheet = false
    @State private var memberToEdit: Member? = nil

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            if let vm {
                MemberListContentView(
                    vm: vm,
                    showingAddSheet: $showingAddSheet,
                    memberToEdit: $memberToEdit,
                    onMemberSelected: onMemberSelected
                )
            }
        }
        .task {
            if vm == nil {
                vm = MemberListViewModel(services: services, groupId: groupId, profileId: profileId)
                await vm?.load()
            }
        }
    }
}

// MARK: - Content

private struct MemberListContentView: View {
    @Bindable var vm: MemberListViewModel
    @Binding var showingAddSheet: Bool
    @Binding var memberToEdit: Member?
    let onMemberSelected: (Member) -> Void

    @Environment(\.dismiss) private var dismiss

    private var showActivosSection: Bool {
        vm.searchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || !vm.filteredMembers.isEmpty
    }

    private var showNoSearchResults: Bool {
        !vm.searchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && vm.filteredMembers.isEmpty
            && vm.filteredArchived.isEmpty
    }

    private var isTotallyEmpty: Bool {
        vm.members.isEmpty && vm.archivedMembers.isEmpty
    }

    var body: some View {
        VStack(spacing: 0) {
            miembrosTopBar

            Group {
                if vm.isLoading {
                    ScrollView {
                        LoadingStateView(rows: 5)
                            .padding(.horizontal, 20)
                            .padding(.top, 16)
                    }
                } else if isTotallyEmpty {
                    emptyState
                } else if showNoSearchResults {
                    searchEmptyState
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    memberList
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .toolbar(.hidden, for: .navigationBar)
        .sheet(isPresented: $showingAddSheet) {
            MemberFormView(
                member: nil,
                groupId: vm.groupId
            ) { newMember in
                vm.members.append(newMember)
                showingAddSheet = false
            }
        }
        .sheet(item: $memberToEdit) { member in
            MemberFormView(
                member: member,
                groupId: vm.groupId
            ) { updated in
                if let i = vm.members.firstIndex(where: { $0.id == updated.id }) {
                    vm.members[i] = updated
                }
                if let j = vm.archivedMembers.firstIndex(where: { $0.id == updated.id }) {
                    vm.archivedMembers[j] = updated
                }
                memberToEdit = nil
            }
        }
        .alert("Error", isPresented: Binding(
            get: { vm.errorMessage != nil },
            set: { if !$0 { vm.errorMessage = nil } }
        )) {
            Button("Aceptar", role: .cancel) { vm.errorMessage = nil }
        } message: {
            Text(vm.errorMessage ?? "")
        }
    }

    // MARK: - Top bar (back + title + + Agregar)

    private var miembrosTopBar: some View {
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
                    showingAddSheet = true
                } label: {
                    Text("+ Agregar")
                        .font(.captionStyle)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.textPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.2), radius: 4, x: -2, y: -2)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Agregar miembro")
            }

            Text("Miembros")
                .font(.headingLarge)
                .foregroundStyle(Color.textPrimary)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    // MARK: - Search + list

    private var memberList: some View {
        VStack(spacing: 0) {
            miembrosSearchField
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, 4)

            ScrollView {
                LazyVStack(spacing: 10) {
                    if showActivosSection {
                        membersSectionHeader(
                            text: String(format: "ACTIVOS (%d)", vm.filteredMembers.count)
                        )
                        ForEach(vm.filteredMembers) { member in
                            memberSwipeRow(member)
                        }
                    }

                    if !vm.filteredArchived.isEmpty {
                        membersSectionHeader(
                            text: String(format: "ARCHIVADOS (%d)", vm.filteredArchived.count)
                        )
                        .padding(.top, showActivosSection ? 8 : 0)

                        ForEach(vm.filteredArchived) { member in
                            memberSwipeRow(member)
                        }
                    }

                    Color.clear.frame(height: 24)
                }
                .padding(.horizontal, 20)
            }
            .refreshable { await vm.load() }
        }
    }

    private func membersSectionHeader(text: String) -> some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(Color.neuShadowDark.opacity(0.35))
                .frame(height: 1)
            Text(text)
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
                .padding(.horizontal, 12)
            Rectangle()
                .fill(Color.neuShadowDark.opacity(0.35))
                .frame(height: 1)
        }
        .padding(.vertical, 8)
    }

    private func memberSwipeRow(_ member: Member) -> some View {
        MemberListaSwipeRow(
            member: member,
            isToggling: vm.togglingMemberIds.contains(member.id),
            onToggleArchive: {
                Task { await vm.toggleActive(member: member) }
            },
            onOpenDetail: {
                HapticFeedback.light()
                onMemberSelected(member)
            },
            onEdit: { memberToEdit = member },
            canEdit: member.isActive
        )
    }

    private var miembrosSearchField: some View {
        HStack(spacing: 12) {
            LucideIcon(uiImage: AppIcon.search, size: 18)
                .foregroundStyle(Color.textMuted)
            TextField("Buscar miembro…", text: $vm.searchText)
                .font(.bodyLarge)
                .foregroundStyle(Color.textPrimary)
        }
        .padding(.horizontal, 16)
        .frame(minHeight: NeuStyle.touchTargetMin)
        .background(
            RoundedRectangle(cornerRadius: NeuStyle.inputRadius, style: .continuous)
                .fill(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.35), radius: 5, x: 3, y: 3)))
        )
        .overlay(
            RoundedRectangle(cornerRadius: NeuStyle.inputRadius, style: .continuous)
                .strokeBorder(Color.textMuted.opacity(0.2), lineWidth: 1)
        )
    }

    // MARK: - Empty states

    private var emptyState: some View {
        ScrollView {
            VStack(spacing: 16) {
                ContentUnavailableView {
                    Label {
                        Text("Sin miembros")
                            .font(.headingLarge)
                            .foregroundStyle(Color.textPrimary)
                    } icon: {
                        LucideIcon(uiImage: AppIcon.usersGroup, size: 44)
                            .foregroundStyle(Color.textMuted)
                            .accessibilityHidden(true)
                    }
                } description: {
                    Text("Aún no hay miembros. ¡Agrega el primero!")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textSecondary)
                        .multilineTextAlignment(.center)
                } actions: {
                    NeuButton("+ Agregar", variant: .primary) {
                        showingAddSheet = true
                    }
                    .frame(maxWidth: 220)
                }
                .padding(.top, 32)
            }
        }
    }

    private var searchEmptyState: some View {
        ContentUnavailableView {
            Label {
                Text("Sin resultados")
                    .font(.headingLarge)
                    .foregroundStyle(Color.textPrimary)
            } icon: {
                LucideIcon(uiImage: AppIcon.search, size: 40)
                    .foregroundStyle(Color.textMuted)
            }
        } description: {
            Text("No hay coincidencias para «\(vm.searchText)».")
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
                .multilineTextAlignment(.center)
        }
    }
}

// MARK: - Swipe row (deslizar a la izquierda → botón Archivar / Activar a la derecha)

private struct MemberListaSwipeRow: View {
    let member: Member
    var isToggling: Bool
    var onToggleArchive: () -> Void
    var onOpenDetail: () -> Void
    var onEdit: () -> Void
    var canEdit: Bool

    @State private var dragOffset: CGFloat = 0
    @State private var isPulsing: Bool = false

    /// Trailing action column: icon + label (Spanish).
    private let archiveBandWidth: CGFloat = 108
    private var maxDragOffset: CGFloat { archiveBandWidth + 24 }

    private var revealWidth: CGFloat {
        min(-dragOffset, archiveBandWidth)
    }

    var body: some View {
        ZStack(alignment: .trailing) {
            HStack(spacing: 0) {
                Spacer(minLength: 0)
                Button {
                    guard !isToggling else { return }
                    commitArchiveToggle()
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                        dragOffset = 0
                    }
                } label: {
                    ZStack {
                        if member.isActive {
                            Color.blush
                            VStack(spacing: 6) {
                                Image(uiImage: AppIcon.archive)
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 22, height: 22)
                                    .foregroundStyle(.white)
                                Text("Archivar")
                                    .font(.captionStyle)
                                    .foregroundStyle(.white)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.85)
                            }
                        } else {
                            Color.statusPresent
                            VStack(spacing: 6) {
                                Image(uiImage: AppIcon.archiveRestore)
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 22, height: 22)
                                    .foregroundStyle(.white)
                                Text("Activar")
                                    .font(.captionStyle)
                                    .foregroundStyle(.white)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.85)
                            }
                        }
                    }
                    .frame(width: archiveBandWidth)
                    .frame(maxHeight: .infinity)
                }
                .buttonStyle(.plain)
                .disabled(isToggling)
                .opacity(isToggling ? 0.5 : 1)
                .accessibilityLabel(member.isActive ? "Archivar miembro" : "Activar miembro")
            }
            .frame(width: revealWidth, alignment: .trailing)
            .frame(maxWidth: .infinity, alignment: .trailing)
            .clipped()

            MemberListaCardRow(
                member: member,
                canEdit: canEdit,
                isToggling: isToggling,
                isPulsing: isPulsing,
                dragOffset: $dragOffset,
                archiveBandWidth: archiveBandWidth,
                maxDragOffset: maxDragOffset,
                onToggleArchive: commitArchiveToggle,
                onOpenDetail: onOpenDetail,
                onEdit: onEdit,
                onLongPressRevealArchive: {
                    guard !isToggling else { return }
                    HapticFeedback.medium()
                    withAnimation(.spring(response: 0.42, dampingFraction: 0.82)) {
                        dragOffset = -archiveBandWidth
                    }
                }
            )
        }
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private func commitArchiveToggle() {
        guard !isToggling else { return }
        HapticFeedback.light()
        withAnimation(.spring(response: 0.15, dampingFraction: 0.5)) {
            isPulsing = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
            withAnimation(.spring(response: 0.2, dampingFraction: 0.6)) {
                isPulsing = false
            }
        }
        onToggleArchive()
    }
}

// MARK: - Card row (Kotlin MiembroCard)

private struct MemberListaCardRow: View {
    let member: Member
    var canEdit: Bool = false
    var isToggling: Bool = false
    var isPulsing: Bool = false
    @Binding var dragOffset: CGFloat
    var archiveBandWidth: CGFloat
    var maxDragOffset: CGFloat
    var onToggleArchive: () -> Void
    var onOpenDetail: () -> Void
    var onEdit: () -> Void
    var onLongPressRevealArchive: () -> Void

    /// Vertical scroll wins only on clearly vertical drags; slight diagonals count as horizontal so the center of the row swipes easily.
    private enum SwipeAxis {
        case undecided
        case horizontal
        case vertical
    }

    @State private var swipeAxis: SwipeAxis = .undecided

    private var archived: Bool { !member.isActive }

    var body: some View {
        HStack(spacing: 10) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .fill(Color.neuBackgroundDeep)
                        .frame(width: 44, height: 44)
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)

                    Text(member.initials)
                        .font(.captionStyle)
                        .bold()
                        .foregroundStyle(archived ? Color.textMuted : Color.textSecondary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(member.fullName)
                            .font(.headingLarge)
                            .foregroundStyle(archived ? Color.textMuted : Color.textPrimary)
                            .lineLimit(1)

                        if member.isVisitor {
                            Text("Visitante")
                                .font(.captionSmall)
                                .foregroundStyle(Color.accent)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.accentGlow)
                                .clipShape(Capsule())
                                .opacity(archived ? 0.7 : 1)
                        }
                    }

                    if let phone = member.phone, !phone.isEmpty {
                        Text(phone)
                            .font(.bodyRegular)
                            .foregroundStyle(archived ? Color.textMuted.opacity(0.85) : Color.textMuted)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if canEdit {
                Menu {
                    Button("Editar") {
                        HapticFeedback.light()
                        onEdit()
                    }
                } label: {
                    LucideIcon(uiImage: AppIcon.more, size: 18)
                        .foregroundStyle(Color.textMuted)
                        .frame(width: 40, height: NeuStyle.touchTargetMin)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel("Más opciones")
            }

            Button {
                onOpenDetail()
            } label: {
                LucideIcon(uiImage: AppIcon.chevronRight, size: 16)
                    .foregroundStyle(archived ? Color.textMuted.opacity(0.75) : Color.textMuted)
                    .frame(width: NeuStyle.touchTargetMin, height: NeuStyle.touchTargetMin)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Ver información, \(member.fullName)")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .opacity(archived ? 0.58 : 1)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 8, x: -4, y: -4)
        .scaleEffect(isPulsing ? 1.05 : 1.0)
        .offset(x: dragOffset)
        .animation(.spring(response: 0.32, dampingFraction: 0.78), value: dragOffset)
        .contentShape(Rectangle())
        .accessibilityHint("Desliza hacia la izquierda para archivar o activar.")
        .simultaneousGesture(
            DragGesture(minimumDistance: 12)
                .onChanged { value in
                    guard !isToggling else { return }
                    let w = value.translation.width
                    let h = value.translation.height
                    if swipeAxis == .undecided {
                        let distance = hypot(w, h)
                        if distance > 14 {
                            if abs(h) > abs(w) + 14 {
                                swipeAxis = .vertical
                            } else {
                                swipeAxis = .horizontal
                            }
                        }
                    }
                    guard swipeAxis == .horizontal else { return }
                    dragOffset = max(-maxDragOffset, min(0, w))
                }
                .onEnded { value in
                    defer { swipeAxis = .undecided }
                    guard !isToggling else { return }
                    let w = value.translation.width
                    if swipeAxis == .horizontal, -w >= archiveBandWidth * 0.45 {
                        onToggleArchive()
                    }
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.78)) {
                        dragOffset = 0
                    }
                }
        )
        .simultaneousGesture(
            LongPressGesture(minimumDuration: 0.4)
                .onEnded { _ in
                    guard !isToggling else { return }
                    onLongPressRevealArchive()
                }
        )
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        MemberListView(
            groupId: MockData.group1Id,
            onMemberSelected: { _ in }
        )
    }
    .environment(\.services, .preview)
}
