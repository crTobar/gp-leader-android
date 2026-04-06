import SwiftUI
import Charts

// MARK: - Reports Screen

struct ReportsView: View {
    let groupId: UUID
    let churchId: UUID
    let role: UserRole

    @Environment(\.services) private var services
    @State private var vm: ReportsViewModel?

    var body: some View {
        Group {
            if let vm = vm {
                ReportsContentView(vm: vm)
            } else {
                LoadingStateView(rows: 4).padding()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Reportes")
        .navigationBarTitleDisplayMode(.large)
        .toolbar(.visible, for: .navigationBar)
        .task {
            let viewModel = ReportsViewModel(
                services: services,
                groupId: groupId,
                churchId: churchId,
                role: role
            )
            vm = viewModel
            await viewModel.load()
        }
    }
}

private struct ReportsContentView: View {
    @Bindable var vm: ReportsViewModel

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if vm.isLoading {
                    LoadingStateView(rows: 4).padding()
                } else {
                    // Stats row
                    HStack(spacing: 12) {
                        NeuStatPill(value: vm.averageRate.asPercent, label: "Promedio",
                                    valueColor: .accent)
                        NeuStatPill(value: "\(vm.totalMembers)", label: "Miembros")
                        NeuStatPill(value: "\(vm.trend.count)", label: "Semanas")
                    }
                    .padding(.horizontal)

                    // Attendance trend chart
                    AttendanceChartView(trend: vm.trend)
                        .padding(.horizontal)

                    // Activity bar chart
                    if !vm.activitySummary.isEmpty {
                        ActivityBarChartView(summaries: vm.activitySummary)
                            .padding(.horizontal)
                    }

                    // Church-level ranking (pastor+)
                    if vm.permissions.canViewChurchReports && !vm.groupRankings.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Ranking por Grupo")
                                .font(.headingLarge)
                                .foregroundStyle(Color.textPrimary)
                                .padding(.horizontal)

                            GroupRankingListView(rankings: vm.groupRankings)
                                .padding(.horizontal)
                        }
                    }
                }
            }
            .padding(.vertical)
        }
        .refreshable { await vm.load() }
        .alert("Error", isPresented: Binding(
            get: { vm.errorMessage != nil },
            set: { if !$0 { vm.errorMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(vm.errorMessage ?? "")
        }
    }
}

#Preview {
    NavigationStack {
        ReportsView(groupId: MockData.group1Id, churchId: MockData.church1Id, role: .leader)
    }
    .environment(\.services, .preview)
}
