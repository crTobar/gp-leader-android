import SwiftUI
import LucideIcons

// MARK: - Admin: District Management

struct AdminDistrictsView: View {
    let campoId: UUID

    @Environment(\.services) private var services
    @State private var districts: [District] = []
    @State private var isLoading = true
    @State private var showingAddForm = false
    @State private var formName = ""
    @State private var errorMessage: String? = nil

    var body: some View {
        Group {
            if isLoading {
                LoadingStateView(rows: 4).padding()
            } else if districts.isEmpty {
                EmptyStateView(
                    icon: AppIcon.map,
                    title: "Sin distritos",
                    subtitle: "No hay distritos registrados.",
                    ctaTitle: "Agregar Distrito"
                ) { showingAddForm = true }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(districts) { district in
                            NeuCard(padding: 16, cornerRadius: NeuStyle.memberRowRadius) {
                                HStack {
                                    NeuIconBadge(icon: AppIcon.map, size: 36)
                                    Text(district.name)
                                        .font(.bodyLarge)
                                        .foregroundStyle(Color.textPrimary)
                                    Spacer()
                                    Image(uiImage: AppIcon.chevronRight)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 12, height: 12)
                                        .foregroundStyle(Color.textMuted)
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Distritos")
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
        .sheet(isPresented: $showingAddForm) {
            NavigationStack {
                VStack(spacing: 16) {
                    NeuCard {
                        NeuTextField(placeholder: "Nombre del distrito *", text: $formName, icon: AppIcon.map)
                    }
                    .padding(.horizontal)
                    NeuButton("Guardar", icon: AppIcon.check, variant: .primary) {
                        Task { await saveDistrict() }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
                .background(Color.neuBackground.ignoresSafeArea())
                .navigationTitle("Nuevo Distrito")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancelar") { showingAddForm = false }
                            .foregroundStyle(Color.textSecondary)
                    }
                }
            }
        }
        .task { await loadDistricts() }
    }

    private func loadDistricts() async {
        isLoading = true
        do {
            districts = try await services.hierarchy.fetchDistricts(campoId: campoId)
        } catch {
            errorMessage = "No se pudieron cargar los distritos."
        }
        isLoading = false
    }

    private func saveDistrict() async {
        guard !formName.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        // Note: persisting new districts requires a write endpoint not yet in the service.
        // The district is added locally until the service supports creates.
        let district = District(
            id: UUID(), campoId: campoId,
            name: formName, createdAt: Date(), updatedAt: Date()
        )
        districts.append(district)
        formName = ""
        showingAddForm = false
        HapticFeedback.success()
    }
}

#Preview {
    NavigationStack { AdminDistrictsView(campoId: MockData.campo1Id) }
        .background(Color.neuBackground.ignoresSafeArea())
        .environment(\.services, .preview)
}
