import SwiftUI
import LucideIcons

// MARK: - Admin: Reporting Periods

struct AdminPeriodsView: View {
    let campoId: UUID

    @Environment(\.services) private var services
    @State private var periods: [ReportingPeriod] = []
    @State private var isLoading = true
    @State private var showingAddForm = false
    @State private var formName = ""
    @State private var formStart: Date = Date()
    @State private var formEnd: Date = Calendar.current.date(byAdding: .day, value: 6, to: Date()) ?? Date()
    @State private var errorMessage: String? = nil

    var body: some View {
        Group {
            if isLoading {
                LoadingStateView(rows: 4).padding()
            } else if periods.isEmpty {
                EmptyStateView(
                    icon: AppIcon.calendarClock,
                    title: "Sin períodos",
                    subtitle: "No hay períodos de reporte configurados.",
                    ctaTitle: "Nuevo Período"
                ) { showingAddForm = true }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(periods) { period in
                            PeriodCard(period: period) {
                                closePeriod(period)
                            }
                        }
                    }
                    .padding()
                }
            }
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Períodos de Reporte")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    showingAddForm = true
                } label: {
                    Image(uiImage: AppIcon.plusCircle)
                        .renderingMode(.template)
                        .foregroundStyle(Color.accent)
                }
            }
        }
        .alert("Error", isPresented: Binding(get: { errorMessage != nil }, set: { if !$0 { errorMessage = nil } })) {
            Button("Aceptar", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
        .sheet(isPresented: $showingAddForm) { addSheet }
        .task { await loadPeriods() }
    }

    private func closePeriod(_ period: ReportingPeriod) {
        if let index = periods.firstIndex(where: { $0.id == period.id }) {
            periods[index].isClosed = true
            periods[index].closedAt = Date()
        }
        HapticFeedback.medium()
    }

    private var addSheet: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    NeuCard {
                        VStack(alignment: .leading, spacing: 16) {
                            NeuTextField(placeholder: "Nombre (ej: Semana 12)", text: $formName, icon: AppIcon.tag)
                            DatePicker("Inicio", selection: $formStart, displayedComponents: .date)
                                .font(.bodyRegular).foregroundStyle(Color.textPrimary)
                            DatePicker("Fin", selection: $formEnd, displayedComponents: .date)
                                .font(.bodyRegular).foregroundStyle(Color.textPrimary)
                        }
                    }
                    .padding(.horizontal)
                    NeuButton("Crear Período", icon: AppIcon.plusCircle, variant: .primary) { createPeriod() }
                        .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .background(Color.neuBackground.ignoresSafeArea())
            .navigationTitle("Nuevo Período")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { showingAddForm = false }
                        .foregroundStyle(Color.textSecondary)
                }
            }
        }
    }

    private func loadPeriods() async {
        isLoading = true
        // Note: a read endpoint for reporting periods is not yet in the service protocol.
        // Showing empty state until the service is extended.
        isLoading = false
    }

    private func createPeriod() {
        // Note: persisting new periods requires a write endpoint not yet in the service.
        let period = ReportingPeriod(
            id: UUID(), campoId: campoId,
            name: formName.isEmpty ? nil : formName,
            weekStart: formStart, weekEnd: formEnd,
            isClosed: false, closedAt: nil,
            createdAt: Date()
        )
        periods.insert(period, at: 0)
        formName = ""
        showingAddForm = false
        HapticFeedback.success()
    }
}

private struct PeriodCard: View {
    let period: ReportingPeriod
    let onClose: () -> Void

    private let fmt = DateFormatters.dayMonth

    var body: some View {
        NeuCard(padding: 16, cornerRadius: NeuStyle.memberRowRadius) {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(period.name ?? "Período")
                        .font(.headingMedium)
                        .foregroundStyle(Color.textPrimary)
                    Spacer()
                    Text(period.isClosed ? "Cerrado" : "Abierto")
                        .font(.captionSmall)
                        .foregroundStyle(period.isClosed ? Color.textMuted : Color.statusPresent)
                        .padding(.horizontal, 8).padding(.vertical, 2)
                        .background((period.isClosed ? Color.textMuted : Color.statusPresent).opacity(0.15))
                        .clipShape(Capsule())
                }
                Label {
                    Text("\(fmt.string(from: period.weekStart)) – \(fmt.string(from: period.weekEnd))")
                } icon: {
                    Image(uiImage: AppIcon.calendar).renderingMode(.template)
                }
                .font(.captionStyle)
                .foregroundStyle(Color.textSecondary)

                if !period.isClosed {
                    NeuButton("Cerrar período", icon: AppIcon.password, variant: .secondary) { onClose() }
                }
            }
        }
    }
}

#Preview {
    NavigationStack { AdminPeriodsView(campoId: MockData.campo1Id) }
        .background(Color.neuBackground.ignoresSafeArea())
        .environment(\.services, .preview)
}
