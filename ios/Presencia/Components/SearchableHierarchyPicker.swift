import SwiftUI

// MARK: - Searchable sheet picker (login hierarchy)

struct SearchableHierarchyPicker<Item: Identifiable & Hashable>: View {
    let title: String
    let fieldLabel: String
    let placeholder: String
    @Binding var selection: Item?
    let items: [Item]
    let displayPrimary: (Item) -> String
    let displaySecondary: (Item) -> String?
    var isEnabled: Bool = true

    @State private var showingSheet = false

    private var summary: String {
        guard let s = selection else { return placeholder }
        return displayPrimary(s)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(fieldLabel)
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)

            Button {
                guard isEnabled else { return }
                HapticFeedback.light()
                showingSheet = true
            } label: {
                HStack(spacing: 12) {
                    Text(summary)
                        .font(.bodyLarge)
                        .foregroundStyle(selection == nil ? Color.textMuted : Color.textPrimary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .multilineTextAlignment(.leading)

                    LucideIcon(uiImage: AppIcon.chevronDown, size: 18)
                        .foregroundStyle(Color.textMuted)
                }
                .padding(.horizontal, 16)
                .frame(minHeight: NeuStyle.touchTargetMin)
                .background(
                    RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                        .fill(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.4), radius: 5, x: 4, y: 4)))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                        .strokeBorder(Color.textMuted.opacity(isEnabled ? 0.2 : 0.1), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)
            .disabled(!isEnabled)
            .opacity(isEnabled ? 1 : 0.45)
        }
        .sheet(isPresented: $showingSheet) {
            SearchableHierarchySheet(
                title: title,
                items: items,
                displayPrimary: displayPrimary,
                displaySecondary: displaySecondary,
                onSelect: { item in
                    selection = item
                    showingSheet = false
                },
                onDismiss: { showingSheet = false }
            )
        }
    }
}

// MARK: - Sheet content

private struct SearchableHierarchySheet<Item: Identifiable & Hashable>: View {
    let title: String
    let items: [Item]
    let displayPrimary: (Item) -> String
    let displaySecondary: (Item) -> String?
    let onSelect: (Item) -> Void
    let onDismiss: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var searchText = ""

    private var filtered: [Item] {
        let q = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return items }
        return items.filter { item in
            displayPrimary(item).localizedCaseInsensitiveContains(q)
                || (displaySecondary(item)?.localizedCaseInsensitiveContains(q) ?? false)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    LucideIcon(uiImage: AppIcon.search, size: 18)
                        .foregroundStyle(Color.textMuted)
                    TextField("Buscar…", text: $searchText)
                        .font(.bodyLarge)
                        .foregroundStyle(Color.textPrimary)
                }
                .padding(.horizontal, 16)
                .frame(minHeight: NeuStyle.touchTargetMin)
                .background(
                    RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                        .fill(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.35), radius: 5, x: 3, y: 3)))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: NeuStyle.inputRadius)
                        .strokeBorder(Color.textMuted.opacity(0.2), lineWidth: 1)
                )
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 12)

                if filtered.isEmpty {
                    ContentUnavailableView {
                        Label {
                            Text("Sin resultados")
                                .font(.headingLarge)
                                .foregroundStyle(Color.textPrimary)
                        } icon: {
                            LucideIcon(uiImage: AppIcon.search, size: 40)
                                .foregroundStyle(Color.textMuted)
                        }
                    } description: {
                        Text("Prueba con otro texto de búsqueda.")
                            .font(.bodyRegular)
                            .foregroundStyle(Color.textSecondary)
                    }
                    .frame(maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 10) {
                            ForEach(filtered) { item in
                                hierarchyRow(for: item)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 24)
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color.neuBackground.ignoresSafeArea())
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(Color.neuBackground, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbarColorScheme(.light, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") {
                        dismiss()
                        onDismiss()
                    }
                    .font(.bodyRegular.weight(.semibold))
                    .foregroundStyle(Color.accent)
                }
            }
        }
        .preferredColorScheme(.light)
    }

    @ViewBuilder
    private func hierarchyRow(for item: Item) -> some View {
        Button {
            HapticFeedback.light()
            onSelect(item)
        } label: {
            HStack(alignment: .center, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(displayPrimary(item))
                        .font(.bodyLarge)
                        .foregroundStyle(Color.textPrimary)
                        .multilineTextAlignment(.leading)
                    if let sub = displaySecondary(item), !sub.isEmpty {
                        Text(sub)
                            .font(.captionStyle)
                            .foregroundStyle(Color.textSecondary)
                            .multilineTextAlignment(.leading)
                    }
                }
                Spacer(minLength: 8)
                LucideIcon(uiImage: AppIcon.chevronRight, size: 18)
                    .foregroundStyle(Color.textMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: NeuStyle.buttonRadius)
                    .fill(Color.neuBackground)
                    .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 8, x: 4, y: 4)
                    .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 8, x: -4, y: -4)
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
