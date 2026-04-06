import SwiftUI
import LucideIcons

// MARK: - Step 3: Activity Counts (grouped by level)

struct MeetingStep3_ActivitiesView: View {
    @Bindable var vm: NewMeetingViewModel

    @State private var selectedActivity: ActivityType? = nil
    @State private var showingAddExtra = false

    // Activities grouped by level in display order
    private var unionActivities:   [ActivityType] { vm.activityTypes.filter { $0.level == .union } }
    private var pastorActivities:  [ActivityType] { vm.activityTypes.filter { $0.level == .pastor } }
    private var myGroupActivities: [ActivityType] { vm.activityTypes.filter { $0.level == .myGroup } }

    var body: some View {
        VStack(spacing: 0) {
            if vm.activityTypes.isEmpty {
                EmptyStateView(
                    icon: AppIcon.activities,
                    title: "Sin actividades",
                    subtitle: "No hay tipos de actividad configurados."
                )
                .frame(maxHeight: .infinity)
            } else {
                ScrollView {
                    LazyVStack(spacing: 20, pinnedViews: .sectionHeaders) {
                        // Union (locked)
                        if !unionActivities.isEmpty {
                            Section {
                                ForEach(unionActivities) { type in
                                    ActivityStepRow(
                                        activityType: type,
                                        count: countBinding(for: type.id),
                                        noteText: noteBinding(for: type.id),
                                        isLocked: true,
                                        onIncrement: { vm.increment(typeId: type.id) },
                                        onDecrement: { vm.decrement(typeId: type.id) },
                                        onTap: nil
                                    )
                                }
                            } header: {
                                levelHeader("UNIÓN", icon: AppIcon.locked, background: Color.textPrimary, foreground: .white)
                            }
                        }

                        // Pastor
                        if !pastorActivities.isEmpty {
                            Section {
                                ForEach(pastorActivities) { type in
                                    ActivityStepRow(
                                        activityType: type,
                                        count: countBinding(for: type.id),
                                        noteText: noteBinding(for: type.id),
                                        isLocked: false,
                                        onIncrement: { vm.increment(typeId: type.id) },
                                        onDecrement: { vm.decrement(typeId: type.id) },
                                        onTap: { selectedActivity = type }
                                    )
                                }
                            } header: {
                                levelHeader("PASTOR", icon: AppIcon.star, background: Color.textSecondary, foreground: .white)
                            }
                        }

                        // Mi GP + extra
                        Section {
                            ForEach(myGroupActivities) { type in
                                ActivityStepRow(
                                    activityType: type,
                                    count: countBinding(for: type.id),
                                    noteText: noteBinding(for: type.id),
                                    isLocked: false,
                                    onIncrement: { vm.increment(typeId: type.id) },
                                    onDecrement: { vm.decrement(typeId: type.id) },
                                    onTap: { selectedActivity = type }
                                )
                            }

                            // Extra activities
                            ForEach(vm.extraActivities) { extra in
                                ExtraActivityRow(
                                    extra: extra,
                                    onRemove: { vm.removeExtraActivity(id: extra.id) }
                                )
                            }

                            // Add extra button
                            Button {
                                HapticFeedback.light()
                                showingAddExtra = true
                            } label: {
                                HStack(spacing: 10) {
                                    Image(uiImage: AppIcon.plus)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 16, height: 16)
                                    Text("+ Agregar actividad extra")
                                        .font(.bodyLarge)
                                }
                                .foregroundStyle(Color.accent)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(16)
                                .background(Color.neuBackground)
                                .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
                                .overlay(
                                    RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius)
                                        .strokeBorder(Color.accent.opacity(0.25), lineWidth: 1.5)
                                )
                            }
                            .buttonStyle(.plain)
                        } header: {
                            levelHeader("MI GP", icon: AppIcon.sparkles, background: Color.neuBackgroundDeep, foreground: Color.textPrimary)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 16)
                }
            }

            NeuButton("Siguiente → Resumen", icon: AppIcon.arrowRight, variant: .primary) {
                vm.nextStep()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                Color.neuBackground
                    .shadow(color: Color.neuShadowDark.opacity(0.2), radius: 8, x: 0, y: -4)
                    .ignoresSafeArea(edges: .bottom)
            )
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .sheet(item: $selectedActivity) { type in
            ActivityDetailView(
                activityType: type,
                count: countBinding(for: type.id),
                noteText: noteBinding(for: type.id)
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showingAddExtra) {
            AddExtraActivityView { name, unitLabel, count in
                vm.addExtraActivity(name: name, unitLabel: unitLabel, count: count)
            }
            .presentationDetents([.medium])
            .presentationDragIndicator(.visible)
        }
    }

    // MARK: - Helpers

    private func countBinding(for id: UUID) -> Binding<Int?> {
        Binding(
            get: { vm.activityCounts[id] ?? nil },
            set: { vm.activityCounts[id] = $0 }
        )
    }

    private func noteBinding(for id: UUID) -> Binding<String> {
        Binding(
            get: { vm.activityNotes[id] ?? "" },
            set: { vm.activityNotes[id] = $0 }
        )
    }

    private func levelHeader(
        _ title: String,
        icon: UIImage,
        background: Color,
        foreground: Color
    ) -> some View {
        HStack(spacing: 8) {
            Image(uiImage: icon)
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 12, height: 12)
                .foregroundStyle(foreground.opacity(0.85))
            Text(title)
                .font(.captionStyle)
                .foregroundStyle(foreground)
            Spacer()
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(background)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Activity Row

private struct ActivityStepRow: View {
    let activityType: ActivityType
    @Binding var count: Int?
    @Binding var noteText: String
    let isLocked: Bool
    let onIncrement: () -> Void
    let onDecrement: () -> Void
    let onTap: (() -> Void)?

    @State private var noteExpanded = false

    var body: some View {
        NeuCard(padding: 14) {
            VStack(spacing: 10) {
                HStack(spacing: 12) {
                    NeuIconBadge(
                        icon: isLocked ? AppIcon.locked : AppIcon.star,
                        color: isLocked ? .textMuted : .accent
                    )

                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 6) {
                            Text(activityType.name)
                                .font(.bodyLarge)
                                .foregroundStyle(isLocked ? Color.textMuted : Color.textPrimary)
                            if activityType.level != .myGroup {
                                Text("Oficial")
                                    .font(.captionSmall)
                                    .foregroundStyle(Color.statusJustified)
                                    .padding(.horizontal, 7)
                                    .padding(.vertical, 2)
                                    .background(Color.statusJustified.opacity(0.15))
                                    .clipShape(Capsule())
                            }
                        }
                        Text(isLocked ? activityType.unitLabel : "Total del GP · \(activityType.unitLabel)")
                            .font(.captionStyle)
                            .foregroundStyle(Color.textMuted)
                    }

                    Spacer()

                    // Stepper
                    HStack(spacing: 0) {
                        Button {
                            if !isLocked { onDecrement(); HapticFeedback.light() }
                        } label: {
                            Image(uiImage: AppIcon.minus)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 18, height: 18)
                                .foregroundStyle(isLocked ? Color.textMuted.opacity(0.4) : Color.textSecondary)
                                .frame(width: 38, height: 38)
                        }
                        .disabled(isLocked)

                        Text(count.map { "\($0)" } ?? "—")
                            .font(.headingMedium)
                            .foregroundStyle(count != nil ? (isLocked ? Color.textMuted : Color.accent) : Color.textMuted)
                            .frame(width: 40)

                        Button {
                            if !isLocked { onIncrement(); HapticFeedback.light() }
                        } label: {
                            Image(uiImage: AppIcon.plus)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 18, height: 18)
                                .foregroundStyle(isLocked ? Color.textMuted.opacity(0.4) : Color.accent)
                                .frame(width: 38, height: 38)
                        }
                        .disabled(isLocked)
                    }
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
                    .shadow(color: Color(hex: "c2c8d4"), radius: 6, x: 2, y: 2)
                    .shadow(color: Color.white, radius: 6, x: -2, y: -2)
                }

                if !isLocked {
                    HStack {
                        Button {
                            withAnimation(.easeInOut(duration: 0.2)) { noteExpanded.toggle() }
                        } label: {
                            HStack(spacing: 4) {
                                Image(uiImage: noteExpanded ? AppIcon.chevronUp : AppIcon.chevronDown)
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 11, height: 11)
                                Text(noteExpanded ? "Ocultar nota" : "Agregar nota")
                                    .font(.captionStyle)
                            }
                            .foregroundStyle(Color.textMuted)
                        }

                        Spacer()

                        if let tap = onTap {
                            Button {
                                tap()
                            } label: {
                                HStack(spacing: 4) {
                                    Image(uiImage: AppIcon.edit)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 11, height: 11)
                                    Text("Detalle")
                                        .font(.captionStyle)
                                }
                                .foregroundStyle(Color.accent)
                            }
                        }
                    }

                    if noteExpanded {
                        NeuTextField(placeholder: "Nota sobre esta actividad...", text: $noteText)
                            .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                }
            }
        }
    }
}

// MARK: - Extra Activity Row

private struct ExtraActivityRow: View {
    let extra: ExtraActivity
    let onRemove: () -> Void

    var body: some View {
        NeuCard(padding: 14) {
            HStack(spacing: 12) {
                NeuIconBadge(icon: AppIcon.sparkles, color: .accent)

                VStack(alignment: .leading, spacing: 2) {
                    Text(extra.name)
                        .font(.bodyLarge)
                        .foregroundStyle(Color.textPrimary)
                    Text(extra.unitLabel)
                        .font(.captionStyle)
                        .foregroundStyle(Color.textMuted)
                }

                Spacer()

                Text("\(extra.count)")
                    .font(.headingMedium)
                    .foregroundStyle(Color.accent)
                    .frame(width: 40)

                Button {
                    withAnimation { onRemove() }
                } label: {
                    Image(uiImage: AppIcon.close)
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 14, height: 14)
                        .foregroundStyle(Color.textMuted)
                        .frame(width: 32, height: 32)
                }
            }
        }
    }
}

#Preview {
    @Previewable @State var vm = NewMeetingViewModel(
        services: .preview,
        groupId: MockData.group1Id,
        profileId: MockData.profileId
    )
    NavigationStack { MeetingStep3_ActivitiesView(vm: vm) }
        .task { await vm.load() }
        .background(Color.neuBackground.ignoresSafeArea())
}
