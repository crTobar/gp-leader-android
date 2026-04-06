import SwiftUI
import LucideIcons

// MARK: - Add Extra Activity Sheet

struct AddExtraActivityView: View {
    let onAdd: (String, String, Int) -> Void   // name, unitLabel, count

    @Environment(\.dismiss) private var dismiss

    @State private var name: String = ""
    @State private var count: Int = 1
    @State private var unitIndex: Int = 0

    private let unitOptions = ["personas", "visitas", "sesiones", "otro"]

    private var selectedUnit: String { unitOptions[unitIndex] }
    private var canAdd: Bool { !name.trimmingCharacters(in: .whitespaces).isEmpty && count > 0 }

    var body: some View {
        NavigationStack {
            ZStack {
                Color.neuBackground.ignoresSafeArea()

                VStack(spacing: 24) {
                    // Name field
                    NeuTextField(
                        placeholder: "Nombre de la actividad *",
                        text: $name,
                        icon: AppIcon.sparkles
                    )
                    .padding(.horizontal, 20)

                    // Unit selector
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Unidad de medida")
                            .font(.captionStyle)
                            .foregroundStyle(Color.textMuted)
                            .padding(.horizontal, 20)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 10) {
                                ForEach(unitOptions.indices, id: \.self) { i in
                                    Button {
                                        HapticFeedback.light()
                                        unitIndex = i
                                    } label: {
                                        Text(unitOptions[i])
                                            .font(.captionStyle)
                                            .foregroundStyle(unitIndex == i ? Color.white : Color.textSecondary)
                                            .padding(.horizontal, 16)
                                            .padding(.vertical, 8)
                                            .background(
                                                Capsule()
                                                    .fill(unitIndex == i ? Color.accent : Color.neuBackground)
                                                    .shadow(color: Color(hex: "c2c8d4"), radius: unitIndex == i ? 0 : 6,
                                                            x: unitIndex == i ? 0 : 2, y: unitIndex == i ? 0 : 2)
                                                    .shadow(color: Color.white, radius: unitIndex == i ? 0 : 6,
                                                            x: unitIndex == i ? 0 : -2, y: unitIndex == i ? 0 : -2)
                                            )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 20)
                        }
                    }

                    // Counter
                    VStack(spacing: 16) {
                        Text("\(count)")
                            .font(.custom("CormorantGaramond-Regular", size: 64))
                            .foregroundStyle(Color.textPrimary)
                            .animation(.easeInOut(duration: 0.15), value: count)

                        Text(selectedUnit)
                            .font(.captionStyle)
                            .foregroundStyle(Color.textMuted)

                        HStack(spacing: 20) {
                            Button {
                                if count > 1 { count -= 1 }
                                HapticFeedback.light()
                            } label: {
                                ZStack {
                                    Circle()
                                        .fill(Color.neuBackground)
                                        .frame(width: 52, height: 52)
                                        .shadow(color: Color(hex: "c2c8d4"), radius: 16, x: 6, y: 6)
                                        .shadow(color: Color.white, radius: 16, x: -6, y: -6)
                                    Image(uiImage: AppIcon.minus)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 20, height: 20)
                                        .foregroundStyle(Color.textSecondary)
                                }
                            }

                            Button {
                                count += 1
                                HapticFeedback.light()
                            } label: {
                                ZStack {
                                    Circle()
                                        .fill(Color.accent)
                                        .frame(width: 52, height: 52)
                                        .shadow(
                                            color: Color(red: 74/255, green: 127/255, blue: 212/255, opacity: 0.25),
                                            radius: 20, x: 0, y: 0
                                        )
                                    Image(uiImage: AppIcon.plus)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 20, height: 20)
                                        .foregroundStyle(Color.white)
                                }
                            }
                        }
                    }

                    Spacer()

                    NeuButton("Agregar Actividad", icon: AppIcon.check, variant: .primary) {
                        let trimmed = name.trimmingCharacters(in: .whitespaces)
                        onAdd(trimmed, selectedUnit, count)
                        dismiss()
                    }
                    .padding(.horizontal, 20)
                    .disabled(!canAdd)
                    .opacity(canAdd ? 1 : 0.5)
                }
                .padding(.top, 24)
                .padding(.bottom, 20)
            }
            .navigationTitle("Actividad Extra")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                        .foregroundStyle(Color.textSecondary)
                }
            }
        }
    }
}
