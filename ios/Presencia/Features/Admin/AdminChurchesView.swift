import SwiftUI
import LucideIcons

// MARK: - Admin: Church Management

struct AdminChurchesView: View {
    let campoId: UUID

    @Environment(\.services) private var services
    @State private var churches: [Church] = []
    @State private var districts: [District] = []
    @State private var isLoading = true
    @State private var showingAddForm = false
    @State private var formName = ""
    @State private var formCode = ""
    @State private var formAddress = ""
    @State private var selectedDistrictId: UUID? = nil
    @State private var errorMessage: String? = nil

    var body: some View {
        Group {
            if isLoading {
                LoadingStateView(rows: 4).padding()
            } else if churches.isEmpty {
                EmptyStateView(
                    icon: AppIcon.building,
                    title: "Sin iglesias",
                    subtitle: "No hay iglesias registradas.",
                    ctaTitle: "Agregar Iglesia"
                ) { showingAddForm = true }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(churches) { church in
                            NeuCard(padding: 16, cornerRadius: NeuStyle.memberRowRadius) {
                                VStack(alignment: .leading, spacing: 6) {
                                    HStack {
                                        Text(church.name)
                                            .font(.headingMedium)
                                            .foregroundStyle(Color.textPrimary)
                                        Spacer()
                                        if let code = church.code {
                                            Text(code)
                                                .font(.captionSmall)
                                                .foregroundStyle(Color.accent)
                                                .padding(.horizontal, 6).padding(.vertical, 2)
                                                .background(Color.accent.opacity(0.12))
                                                .clipShape(Capsule())
                                        }
                                    }
                                    if let address = church.address {
                                        Label {
                                            Text(address).font(.captionStyle).foregroundStyle(Color.textMuted)
                                        } icon: {
                                            Image(uiImage: AppIcon.mapPin).renderingMode(.template)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Iglesias")
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
        .task { await loadData() }
    }

    private var addSheet: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    NeuCard {
                        VStack(spacing: 14) {
                            NeuTextField(placeholder: "Nombre *", text: $formName, icon: AppIcon.building)
                            NeuTextField(placeholder: "Código", text: $formCode, icon: AppIcon.hash)
                            NeuTextField(placeholder: "Dirección", text: $formAddress, icon: AppIcon.mapPin)
                            if !districts.isEmpty {
                                Picker("Distrito", selection: $selectedDistrictId) {
                                    Text("Seleccionar distrito").tag(UUID?.none)
                                    ForEach(districts) { d in
                                        Text(d.name).tag(UUID?.some(d.id))
                                    }
                                }
                                .font(.bodyRegular)
                                .foregroundStyle(Color.textPrimary)
                            }
                        }
                    }
                    .padding(.horizontal)
                    NeuButton("Guardar", icon: AppIcon.check, variant: .primary) { saveChurch() }
                        .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .background(Color.neuBackground.ignoresSafeArea())
            .navigationTitle("Nueva Iglesia")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { showingAddForm = false }
                        .foregroundStyle(Color.textSecondary)
                }
            }
        }
    }

    private func loadData() async {
        isLoading = true
        do {
            async let churchFetch = services.hierarchy.fetchChurches(campoId: campoId)
            async let districtFetch = services.hierarchy.fetchDistricts(campoId: campoId)
            churches = try await churchFetch
            districts = try await districtFetch
            selectedDistrictId = districts.first?.id
        } catch {
            errorMessage = "No se pudieron cargar las iglesias."
        }
        isLoading = false
    }

    private func saveChurch() {
        guard !formName.trimmingCharacters(in: .whitespaces).isEmpty,
              let districtId = selectedDistrictId else { return }
        // Note: persisting new churches requires a write endpoint not yet in the service.
        let church = Church(
            id: UUID(), districtId: districtId,
            name: formName,
            code: formCode.isEmpty ? nil : formCode,
            address: formAddress.isEmpty ? nil : formAddress,
            createdAt: Date(), updatedAt: Date()
        )
        churches.append(church)
        formName = ""; formCode = ""; formAddress = ""
        showingAddForm = false
        HapticFeedback.success()
    }
}

#Preview {
    NavigationStack { AdminChurchesView(campoId: MockData.campo1Id) }
        .background(Color.neuBackground.ignoresSafeArea())
        .environment(\.services, .preview)
}
