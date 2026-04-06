import SwiftUI
import LucideIcons

// MARK: - Activity Type Management

struct ActivityTypeManagementView: View {
    @Environment(\.services) private var services
    @State private var vm: ActivityTypeManagementViewModel?

    private let iconOptions = [
        "book.fill", "figure.walk", "newspaper.fill", "dollarsign.circle.fill",
        "hands.sparkles.fill", "heart.fill", "star.fill", "drop.fill",
        "flame.fill", "person.2.fill"
    ]

    var body: some View {
        Group {
            if let vm = vm {
                contentView(vm: vm)
            } else {
                LoadingStateView(rows: 4).padding()
            }
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Tipos de Actividades")
        .toolbar(.visible, for: .navigationBar)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            let viewModel = ActivityTypeManagementViewModel(
                services: services,
                campoId: MockData.campo1Id,
                churchId: MockData.church1Id
            )
            vm = viewModel
            await viewModel.load()
        }
    }

    @ViewBuilder
    private func contentView(vm: ActivityTypeManagementViewModel) -> some View {
        ScrollView {
            VStack(spacing: 12) {
                if vm.isLoading {
                    LoadingStateView(rows: 4)
                } else if vm.activityTypes.isEmpty {
                    EmptyStateView(
                        icon: AppIcon.activities,
                        title: "Sin actividades",
                        subtitle: "No hay tipos de actividad. Crea el primero.",
                        ctaTitle: "Agregar"
                    ) { vm.showingAddForm = true }
                } else {
                    ForEach(vm.activityTypes) { type in
                        NeuCard(padding: 14, cornerRadius: NeuStyle.memberRowRadius) {
                            HStack(spacing: 12) {
                                NeuIconBadge(icon: AppIcon.star)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(type.name)
                                        .font(.bodyLarge).foregroundStyle(Color.textPrimary)
                                    Text(type.scope.displayName)
                                        .font(.captionSmall)
                                        .foregroundStyle(Color.accent)
                                        .padding(.horizontal, 6).padding(.vertical, 1)
                                        .background(Color.accentGlow)
                                        .clipShape(Capsule())
                                }
                                Spacer()
                                Circle()
                                    .fill(type.isActive ? Color.statusPresent : Color.textMuted)
                                    .frame(width: 10, height: 10)
                            }
                        }
                    }
                }
            }
            .padding()
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    vm.showingAddForm = true
                } label: {
                    Image(uiImage: AppIcon.plusCircle)
                        .renderingMode(.template)
                        .foregroundStyle(Color.accent)
                }
            }
        }
        .sheet(isPresented: Binding(
            get: { vm.showingAddForm },
            set: { vm.showingAddForm = $0 }
        )) {
            addForm(vm: vm)
        }
    }

    @ViewBuilder
    private func addForm(vm: ActivityTypeManagementViewModel) -> some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    NeuCard {
                        VStack(alignment: .leading, spacing: 16) {
                            NeuTextField(placeholder: "Nombre *", text: Binding(
                                get: { vm.formName }, set: { vm.formName = $0 }
                            ), icon: AppIcon.tag)
                            NeuTextField(placeholder: "Etiqueta de unidad (ej: estudios)", text: Binding(
                                get: { vm.formUnitLabel }, set: { vm.formUnitLabel = $0 }
                            ), icon: AppIcon.tag)
                            NeuTextField(placeholder: "Descripción (opcional)", text: Binding(
                                get: { vm.formDescription }, set: { vm.formDescription = $0 }
                            ), icon: AppIcon.note)
                        }
                    }
                    .padding(.horizontal)

                    NeuCard {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Ícono").font(.headingMedium).foregroundStyle(Color.textPrimary)
                            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 5), spacing: 12) {
                                ForEach(iconOptions, id: \.self) { icon in
                                    Button {
                                        vm.formIcon = icon
                                    } label: {
                                        ZStack {
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(vm.formIcon == icon ? Color.accentGlow : Color.neuBackground)
                                                .frame(width: 52, height: 52)
                                                .shadow(
                                                    color: Color.neuShadowDark.opacity(0.4),
                                                    radius: 4, x: 3, y: 3
                                                )
                                                .shadow(
                                                    color: Color.neuShadowLight.opacity(0.9),
                                                    radius: 4, x: -3, y: -3
                                                )
                                            Image(uiImage: AppIcon.star)
                                                .renderingMode(.template)
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 22, height: 22)
                                                .foregroundStyle(vm.formIcon == icon ? Color.accent : Color.textMuted)
                                        }
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
                    .padding(.horizontal)

                    NeuCard {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Alcance").font(.headingMedium).foregroundStyle(Color.textPrimary)
                            Picker("Alcance", selection: Binding(
                                get: { vm.formScope }, set: { vm.formScope = $0 }
                            )) {
                                Text("Global").tag(ActivityScope.global)
                                Text("Campo").tag(ActivityScope.campo)
                                Text("Iglesia").tag(ActivityScope.church)
                            }
                            .pickerStyle(.segmented)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .background(Color.neuBackground.ignoresSafeArea())
            .navigationTitle("Nueva Actividad")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { vm.showingAddForm = false }
                        .foregroundStyle(Color.textSecondary)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Guardar") { Task { await vm.createType() } }
                        .foregroundStyle(Color.accent)
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        ActivityTypeManagementView()
            .environment(\.services, .preview)
    }
    .background(Color.neuBackground.ignoresSafeArea())
}
