import SwiftUI

// MARK: - Step 3 Summary (Kotlin RegistroPaso3Screen parity)

struct MeetingStep4_SummaryView: View {
    @Bindable var vm: NewMeetingViewModel
    let onSubmitted: () -> Void

    @State private var showingSubmitAlert = false

    private var unionWithCounts: [ActivityType] {
        vm.activityTypes.filter { $0.level == .union && (vm.activityCounts[$0.id] ?? nil) != nil }
    }
    private var pastorWithCounts: [ActivityType] {
        vm.activityTypes.filter { $0.level == .pastor && (vm.activityCounts[$0.id] ?? nil) != nil }
    }
    private var gpWithCounts: [ActivityType] {
        vm.activityTypes.filter { $0.level == .myGroup && (vm.activityCounts[$0.id] ?? nil) != nil }
    }

    private var hasAnyActivities: Bool {
        !unionWithCounts.isEmpty || !pastorWithCounts.isEmpty || !gpWithCounts.isEmpty
            || vm.extraActivities.contains(where: { $0.count > 0 })
    }

    private var visitasLabel: String {
        let v = vm.visitorsForThisMeeting.count
        if v == 1 { return "1 persona" }
        return "\(v) personas"
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    resumenDarkCard
                        .padding(.horizontal, 16)

                    actividadesSeparator
                        .padding(.horizontal, 16)

                    actividadesGrouped
                        .padding(.horizontal, 16)
                }
                .padding(.top, 8)
                .padding(.bottom, 120)
            }

            VStack(spacing: 8) {
                NeuButton("Enviar al pastor", icon: nil, variant: .primary, isLoading: vm.isSubmitting) {
                    showingSubmitAlert = true
                }
                NeuButton("Guardar como borrador", icon: AppIcon.save, variant: .secondary, isLoading: false) {
                    Task {
                        await vm.saveDraft()
                        if vm.errorMessage == nil { onSubmitted() }
                    }
                }
                NeuButton("← Editar actividades", icon: nil, variant: .ghost, isLoading: false) {
                    vm.prevStep()
                }
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
        .alert("Enviar al pastor", isPresented: $showingSubmitAlert) {
            Button("Cancelar", role: .cancel) {}
            Button("Enviar", role: .destructive) {
                Task {
                    await vm.submit()
                    if vm.isSubmitted { onSubmitted() }
                }
            }
        } message: {
            Text("No podrás editar la reunión después de enviarla.")
        }
        .alert("No se pudo enviar", isPresented: Binding(
            get: { vm.errorMessage != nil },
            set: { if !$0 { vm.errorMessage = nil } }
        )) {
            Button("Entendido", role: .cancel) {}
        } message: {
            Text(vm.errorMessage ?? "")
        }
    }

    // MARK: - Dark resumen card

    private var resumenDarkCard: some View {
        let groupTitle = vm.groupName.isEmpty ? "—" : vm.groupName

        return VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Resumen")
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)
                Spacer()
                Text(groupTitle)
                    .font(.bodyRegular)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
            }

            Rectangle()
                .fill(Color.textSecondary.opacity(0.3))
                .frame(height: 1)

            resumenRow(label: "FECHA", value: DateFormatters.formatShortDate(vm.meetingDate))

            HStack(alignment: .center) {
                Text("MIEMBROS")
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)
                Spacer()
                HStack(spacing: 4) {
                    resumenBadge("\(vm.presentCount)P", background: Color.statusPresent)
                    resumenBadge("\(vm.absentCount)A", background: Color.blush)
                    resumenBadge("\(vm.justifiedCount)J", background: Color.textSecondary)
                }
            }

            resumenRow(label: "% ASISTENCIA", value: "\(Int(vm.attendanceRate * 100))%")

            resumenRow(label: "VISITAS", value: visitasLabel)
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.textPrimary)
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func resumenRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
            Spacer()
            Text(value)
                .font(.bodyRegular)
                .foregroundStyle(.white)
        }
    }

    private func resumenBadge(_ text: String, background: Color) -> some View {
        Text(text)
            .font(.captionSmall)
            .foregroundStyle(.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: 6))
    }

    // MARK: - ACTIVIDADES separator

    private var actividadesSeparator: some View {
        HStack(spacing: 8) {
            Rectangle()
                .fill(Color.textMuted.opacity(0.35))
                .frame(height: 1)
            Text("ACTIVIDADES")
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
            Rectangle()
                .fill(Color.textMuted.opacity(0.35))
                .frame(height: 1)
        }
    }

    // MARK: - Grouped activities

    private var actividadesGrouped: some View {
        VStack(alignment: .leading, spacing: 12) {
            if !hasAnyActivities {
                Text("Sin actividades registradas")
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textMuted)
                    .frame(maxWidth: .infinity, alignment: .center)
            } else {
                if !unionWithCounts.isEmpty {
                    actividadSection(title: "UNIÓN", types: unionWithCounts, headerBg: Color.textPrimary, headerFg: .white)
                }
                if !pastorWithCounts.isEmpty {
                    actividadSection(title: "PASTOR", types: pastorWithCounts, headerBg: Color.textSecondary, headerFg: .white)
                }
                if !gpWithCounts.isEmpty {
                    actividadSection(title: "MI GP", types: gpWithCounts, headerBg: Color.neuBackgroundDeep, headerFg: Color.textPrimary)
                }
                ForEach(vm.extraActivities.filter { $0.count > 0 }) { extra in
                    HStack {
                        Text(extra.name)
                            .font(.bodyRegular)
                            .foregroundStyle(Color.textPrimary)
                        Spacer()
                        Text("\(extra.count) \(extra.unitLabel)")
                            .font(.bodyLarge)
                            .foregroundStyle(Color.accent)
                    }
                    .padding(.vertical, 4)
                }
            }
        }
    }

    private func actividadSection(title: String, types: [ActivityType], headerBg: Color, headerFg: Color) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(.captionStyle)
                .foregroundStyle(headerFg)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(headerBg)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(spacing: 8) {
                ForEach(types) { type in
                    if let count = vm.activityCounts[type.id] ?? nil {
                        HStack {
                            Text(type.name)
                                .font(.bodyRegular)
                                .foregroundStyle(Color.textPrimary)
                            Spacer()
                            Text("\(count) \(type.unitLabel)")
                                .font(.bodyLarge)
                                .foregroundStyle(Color.accent)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .padding(.top, 8)
        }
    }
}

#Preview {
    @Previewable @State var vm = NewMeetingViewModel(
        services: .preview,
        groupId: MockData.group1Id,
        profileId: MockData.profileId
    )
    NavigationStack {
        MeetingStep4_SummaryView(vm: vm, onSubmitted: {})
    }
    .task { await vm.load() }
    .background(Color.neuBackground.ignoresSafeArea())
}
