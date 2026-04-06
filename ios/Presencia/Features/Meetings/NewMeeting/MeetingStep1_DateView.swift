import SwiftUI
import LucideIcons

// MARK: - Step 1: Date, Notes & Visitors

struct MeetingStep1_DateView: View {
    @Bindable var vm: NewMeetingViewModel

    @State private var showingAddVisitor = false
    @State private var priorVisitorsExpanded = false

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 16) {
                    StepIndicatorView(
                        currentStep: 0, totalSteps: 4,
                        labels: ["Fecha", "Asistir", "Activid.", "Resumen"]
                    )
                    .padding(.top, 16)

                    // Date card
                    NeuCard {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("Fecha de la Reunión")
                                .font(.displayItalic)
                                .foregroundStyle(Color.textPrimary)

                            DatePicker("", selection: $vm.meetingDate, displayedComponents: .date)
                                .datePickerStyle(.graphical)
                                .tint(Color.accent)
                                .environment(\.locale, Locale(identifier: "es"))
                                .padding(8)
                                .background(
                                    RoundedRectangle(cornerRadius: NeuStyle.cardRadius)
                                        .fill(Color.neuBackground)
                                        .shadow(color: Color.neuShadowDark.opacity(0.25), radius: 6, x: 0, y: 3)
                                )
                                .disabled(vm.noMeetingToday)
                                .opacity(vm.noMeetingToday ? 0.4 : 1)

                            // No meeting toggle
                            Button {
                                withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                                    vm.noMeetingToday.toggle()
                                    HapticFeedback.medium()
                                }
                            } label: {
                                HStack(spacing: 10) {
                                    Image(uiImage: vm.noMeetingToday ? AppIcon.checkCircle : AppIcon.close)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 16, height: 16)
                                        .foregroundStyle(vm.noMeetingToday ? Color.blush : Color.textMuted)
                                    Text("No hubo reunión hoy")
                                        .font(.bodyRegular)
                                        .foregroundStyle(vm.noMeetingToday ? Color.blush : Color.textMuted)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                    }
                    .padding(.horizontal, 16)

                    // Notes card
                    if !vm.noMeetingToday {
                        NeuCard {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Notas (opcional)")
                                    .font(.headingMedium)
                                    .foregroundStyle(Color.textPrimary)

                                ZStack(alignment: .topLeading) {
                                    if vm.notes.isEmpty {
                                        Text("Alguna observación de esta reunión...")
                                            .font(.bodyRegular)
                                            .foregroundStyle(Color.textMuted)
                                            .padding(.top, 8)
                                            .padding(.leading, 4)
                                            .allowsHitTesting(false)
                                    }
                                    TextEditor(text: $vm.notes)
                                        .font(.bodyRegular)
                                        .foregroundStyle(Color.textPrimary)
                                        .scrollContentBackground(.hidden)
                                        .frame(minHeight: 80)
                                }
                                .padding(12)
                                .background(
                                    RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                                        .fill(Color.neuBackground
                                            .shadow(.inner(color: Color(hex: "c2c8d4"), radius: 10, x: 4, y: 4))
                                            .shadow(.inner(color: Color.white, radius: 10, x: -4, y: -4))
                                        )
                                )
                            }
                        }
                        .padding(.horizontal, 16)

                        // Visitors card
                        visitorsCard
                    }

                    Spacer(minLength: 80)
                }
            }

            bottomBar
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .sheet(isPresented: $showingAddVisitor) {
            AddVisitorSheetView(vm: vm)
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
        }
    }

    // MARK: - Visitors Card

    private var visitorsCard: some View {
        NeuCard {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    Text("Visitantes")
                        .font(.headingMedium)
                        .foregroundStyle(Color.textPrimary)
                    Spacer()
                    Button {
                        HapticFeedback.light()
                        showingAddVisitor = true
                    } label: {
                        HStack(spacing: 4) {
                            Image(uiImage: AppIcon.plus)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 14, height: 14)
                            Text("Agregar")
                                .font(.captionStyle)
                        }
                        .foregroundStyle(Color.accent)
                    }
                }

                if vm.visitorsForThisMeeting.isEmpty {
                    Text("Sin visitantes por ahora")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .padding(.vertical, 8)
                } else {
                    ForEach(vm.visitorsForThisMeeting) { visitor in
                        HStack(spacing: 12) {
                            ZStack {
                                Circle()
                                    .fill(Color.accentGlow)
                                    .frame(width: 36, height: 36)
                                Text(visitor.initials)
                                    .font(.captionStyle)
                                    .foregroundStyle(Color.accent)
                            }
                            Text(visitor.displayName)
                                .font(.bodyLarge)
                                .foregroundStyle(Color.textPrimary)
                            Spacer()
                            Button {
                                withAnimation { vm.removeVisitorFromMeeting(visitor) }
                            } label: {
                                Image(uiImage: AppIcon.close)
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 14, height: 14)
                                    .foregroundStyle(Color.textMuted)
                            }
                        }
                    }
                }

                // Prior visitors collapsible
                if !vm.priorVisitors.isEmpty {
                    Divider().opacity(0.3)

                    Button {
                        withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                            priorVisitorsExpanded.toggle()
                        }
                    } label: {
                        HStack(spacing: 6) {
                            Image(uiImage: priorVisitorsExpanded ? AppIcon.chevronUp : AppIcon.chevronDown)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 12, height: 12)
                            Text("Visitantes anteriores (\(vm.priorVisitors.count))")
                                .font(.captionStyle)
                        }
                        .foregroundStyle(Color.textMuted)
                    }

                    if priorVisitorsExpanded {
                        VStack(spacing: 10) {
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
                                    // Quick-add button
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
                                    } else {
                                        Image(uiImage: AppIcon.check)
                                            .renderingMode(.template)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 14, height: 14)
                                            .foregroundStyle(Color.statusPresent)
                                    }
                                }
                            }
                        }
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                }
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Bottom Bar

    private var bottomBar: some View {
        HStack {
            Spacer()
            NeuButton("Siguiente →", icon: AppIcon.arrowRight, variant: .primary) {
                vm.nextStep()
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            Color.neuBackground
                .shadow(color: Color(hex: "c2c8d4").opacity(0.3), radius: 8, x: 0, y: -4)
                .ignoresSafeArea(edges: .bottom)
        )
    }
}

#Preview {
    let vm = NewMeetingViewModel(
        services: .preview,
        groupId: MockData.group1Id,
        profileId: MockData.profileId
    )
    vm.members = MockData.members
    return NavigationStack {
        MeetingStep1_DateView(vm: vm)
    }
}
