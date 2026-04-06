import SwiftUI
import LucideIcons

// MARK: - Add Visitor Bottom Sheet

struct AddVisitorSheetView: View {
    @Bindable var vm: NewMeetingViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var searchText = ""
    @State private var isCreating = false

    private var filteredPrior: [Member] {
        guard !searchText.trimmingCharacters(in: .whitespaces).isEmpty else {
            return vm.priorVisitors
        }
        return vm.priorVisitors.filter {
            $0.fullName.localizedCaseInsensitiveContains(searchText)
        }
    }

    private var searchTrimmed: String {
        searchText.trimmingCharacters(in: .whitespaces)
    }

    private var canCreateNew: Bool {
        !searchTrimmed.isEmpty &&
        !vm.priorVisitors.contains(where: {
            $0.fullName.localizedCaseInsensitiveContains(searchTrimmed)
        })
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color.neuBackground.ignoresSafeArea()

                VStack(spacing: 0) {
                    // Search field
                    HStack(spacing: 12) {
                        Image(uiImage: AppIcon.search)
                            .renderingMode(.template)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 16, height: 16)
                            .foregroundStyle(Color.textMuted)

                        TextField("Nombre del visitante...", text: $searchText)
                            .font(.bodyLarge)
                            .foregroundStyle(Color.textPrimary)
                            .autocorrectionDisabled()
                    }
                    .padding(14)
                    .background(
                        RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius)
                            .fill(Color.neuBackground
                                .shadow(.inner(color: Color(hex: "c2c8d4"), radius: 10, x: 4, y: 4))
                                .shadow(.inner(color: Color.white, radius: 10, x: -4, y: -4))
                            )
                    )
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    .padding(.bottom, 12)

                    ScrollView {
                        LazyVStack(spacing: 10) {
                            // Create new visitor option
                            if canCreateNew {
                                Button {
                                    Task { await addNew() }
                                } label: {
                                    HStack(spacing: 14) {
                                        ZStack {
                                            Circle()
                                                .fill(Color.accentGlow)
                                                .frame(width: 40, height: 40)
                                            Image(uiImage: AppIcon.plus)
                                                .renderingMode(.template)
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 16, height: 16)
                                                .foregroundStyle(Color.accent)
                                        }
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text("Agregar como nuevo visitante")
                                                .font(.bodyLarge)
                                                .foregroundStyle(Color.accent)
                                            Text("\"\(searchTrimmed)\"")
                                                .font(.captionStyle)
                                                .foregroundStyle(Color.textMuted)
                                        }
                                        Spacer()
                                        if isCreating {
                                            ProgressView()
                                                .scaleEffect(0.8)
                                        }
                                    }
                                    .padding(14)
                                    .background(Color.neuBackground)
                                    .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
                                    .shadow(color: Color(hex: "c2c8d4"), radius: 16, x: 6, y: 6)
                                    .shadow(color: Color.white, radius: 16, x: -6, y: -6)
                                }
                                .buttonStyle(.plain)
                                .disabled(isCreating)
                            }

                            // Prior visitors matching search
                            if !filteredPrior.isEmpty {
                                HStack {
                                    Text(searchTrimmed.isEmpty ? "Visitantes anteriores" : "Resultados")
                                        .font(.captionStyle)
                                        .foregroundStyle(Color.textMuted)
                                    Spacer()
                                }
                                .padding(.horizontal, 4)

                                ForEach(filteredPrior) { visitor in
                                    let alreadyAdded = vm.visitorsForThisMeeting.contains(where: { $0.id == visitor.id })
                                    Button {
                                        if !alreadyAdded {
                                            vm.addVisitorToMeeting(visitor)
                                            dismiss()
                                        }
                                    } label: {
                                        HStack(spacing: 14) {
                                            ZStack {
                                                Circle()
                                                    .fill(alreadyAdded ? Color.accentGlow : Color.neuBackground)
                                                    .frame(width: 40, height: 40)
                                                    .shadow(color: Color(hex: "c2c8d4"), radius: 6, x: 2, y: 2)
                                                    .shadow(color: Color.white, radius: 6, x: -2, y: -2)
                                                Text(visitor.initials)
                                                    .font(.captionStyle)
                                                    .foregroundStyle(alreadyAdded ? Color.accent : Color.textSecondary)
                                            }
                                            Text(visitor.displayName)
                                                .font(.bodyLarge)
                                                .foregroundStyle(alreadyAdded ? Color.textMuted : Color.textPrimary)
                                            Spacer()
                                            if alreadyAdded {
                                                Text("Agregado")
                                                    .font(.captionStyle)
                                                    .foregroundStyle(Color.statusPresent)
                                            }
                                        }
                                        .padding(14)
                                        .background(Color.neuBackground)
                                        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
                                        .shadow(color: Color(hex: "c2c8d4"), radius: alreadyAdded ? 6 : 16,
                                                x: alreadyAdded ? 2 : 6, y: alreadyAdded ? 2 : 6)
                                        .shadow(color: Color.white, radius: alreadyAdded ? 6 : 16,
                                                x: alreadyAdded ? -2 : -6, y: alreadyAdded ? -2 : -6)
                                    }
                                    .buttonStyle(.plain)
                                    .disabled(alreadyAdded)
                                }
                            } else if searchTrimmed.isEmpty {
                                Text("No hay visitantes previos")
                                    .font(.bodyRegular)
                                    .foregroundStyle(Color.textMuted)
                                    .frame(maxWidth: .infinity)
                                    .padding(.top, 20)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 24)
                    }
                }
            }
            .navigationTitle("Agregar Visitante")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                        .foregroundStyle(Color.textSecondary)
                }
            }
        }
    }

    private func addNew() async {
        isCreating = true
        let parts = searchTrimmed.split(separator: " ", maxSplits: 1)
        let first = String(parts.first ?? Substring(searchTrimmed))
        let last  = parts.count > 1 ? String(parts[1]) : "—"
        await vm.createAndAddVisitor(firstName: first, lastName: last)
        isCreating = false
        if vm.errorMessage == nil { dismiss() }
    }
}
