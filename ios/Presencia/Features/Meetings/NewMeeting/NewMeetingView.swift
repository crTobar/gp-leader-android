import SwiftUI

// MARK: - New Meeting Step Container (Kotlin Registro graph parity)

struct NewMeetingView: View {
    let groupId: UUID
    let profileId: UUID
    /// When non-nil, loads and updates this draft instead of creating a new meeting row.
    var editingMeeting: Meeting? = nil
    let onDone: () -> Void
    let onCancel: () -> Void

    @Environment(\.services) private var services
    @State private var vm: NewMeetingViewModel?
    @State private var showingSuccess = false
    @State private var successSentAt = Date()

    /// While the ViewModel is nil or still fetching members / activity types, show placeholders under the registro chrome.
    private var showStepLoadingPlaceholder: Bool {
        vm?.isLoading ?? true
    }

    private var chromeStep: Int {
        vm?.currentStep ?? 0
    }

    var body: some View {
        ZStack {
            NavigationStack {
                VStack(spacing: 0) {
                    RegistroMeetingTopBar(currentStep: chromeStep) {
                        if let vm {
                            if vm.currentStep == 0 {
                                onCancel()
                            } else {
                                vm.prevStep()
                            }
                        } else {
                            onCancel()
                        }
                    }
                    RegistroStepTabsRow(activeStep: chromeStep)
                    if showStepLoadingPlaceholder {
                        Spacer(minLength: 0)
                        LoadingStateView(rows: 4).padding()
                        Spacer(minLength: 0)
                    } else if let vm {
                        stepContent(vm: vm)
                    }
                }
                .background(Color.neuBackground.ignoresSafeArea())
                .toolbar(.hidden, for: .navigationBar)
            }
            .task(id: editingMeeting?.id) {
                let viewModel = NewMeetingViewModel(
                    services: services,
                    groupId: groupId,
                    profileId: profileId,
                    editingMeeting: editingMeeting
                )
                vm = viewModel
                await viewModel.load()
            }

            if showingSuccess, let vm {
                MeetingSuccessView(
                    presentCount: vm.presentCount,
                    absentCount: vm.absentCount,
                    justifiedCount: vm.justifiedCount,
                    sentAt: successSentAt,
                    onClose: onDone,
                    onViewHistory: onDone
                )
                .transition(.opacity)
                .zIndex(100)
            }
        }
        .animation(.easeInOut(duration: 0.35), value: showingSuccess)
    }

    @ViewBuilder
    private func stepContent(vm: NewMeetingViewModel) -> some View {
        ZStack {
            switch vm.currentStep {
            case 0:
                MeetingStep2_AttendanceView(vm: vm)
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            case 1:
                MeetingStep3_ActivitiesView(vm: vm)
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            case 2:
                MeetingStep4_SummaryView(
                    vm: vm,
                    onSubmitted: {
                        successSentAt = Date()
                        withAnimation { showingSuccess = true }
                    }
                )
                .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            default:
                EmptyView()
            }
        }
        .animation(.easeInOut(duration: 0.3), value: vm.currentStep)
    }
}

#Preview {
    NewMeetingView(
        groupId: MockData.group1Id,
        profileId: MockData.profileId,
        onDone: {},
        onCancel: {}
    )
    .environment(\.services, .preview)
}
