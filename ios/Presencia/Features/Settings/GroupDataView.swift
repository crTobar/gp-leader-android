import SwiftUI
import UIKit
import LucideIcons

struct GroupDataView: View {
    let group: SmallGroup
    let onSaved: (SmallGroup) -> Void

    @Environment(\.services) private var services
    @Environment(\.dismiss) private var dismiss

    @State private var name: String
    @State private var meetingDay: String
    @State private var isSaving = false
    @State private var errorMessage: String? = nil

    init(group: SmallGroup, onSaved: @escaping (SmallGroup) -> Void) {
        self.group = group
        self.onSaved = onSaved
        _name       = State(initialValue: group.name)
        _meetingDay = State(initialValue: group.meetingDay ?? "Sábado")
    }

    private var hasChanges: Bool {
        name != group.name || meetingDay != (group.meetingDay ?? "Sábado")
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                NeuCard {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Nombre del Grupo")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        NeuTextField(
                            placeholder: "Nombre del grupo",
                            text: $name,
                            label: "Nombre del grupo",
                            icon: AppIcon.usersGroup
                        )
                    }
                }
                .padding(.horizontal)

                NeuCard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Día de Reunión")
                            .font(.headingMedium)
                            .foregroundStyle(Color.textPrimary)

                        MeetingDayWheelPicker(selection: $meetingDay)
                            .frame(height: 148)
                            .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
                    }
                }
                .padding(.horizontal)

                if let errorMessage {
                    Text(errorMessage)
                        .font(.captionStyle)
                        .foregroundStyle(Color.blush)
                        .padding(.horizontal)
                }

                NeuButton(
                    "Guardar Cambios",
                    icon: AppIcon.checkCircle,
                    variant: .primary,
                    isLoading: isSaving
                ) {
                    Task { await save() }
                }
                .disabled(!hasChanges)
                .opacity(hasChanges ? 1 : 0.5)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color.neuBackground.ignoresSafeArea())
        .navigationTitle("Datos del Grupo")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func save() async {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "El nombre del grupo es obligatorio."
            return
        }
        isSaving = true
        errorMessage = nil
        var updated = group
        updated.name = name.trimmingCharacters(in: .whitespaces)
        updated.meetingDay = meetingDay
        do {
            let saved = try await services.group.updateGroup(updated)
            HapticFeedback.success()
            onSaved(saved)
            dismiss()
        } catch {
            errorMessage = "No se pudo guardar. Intenta de nuevo."
        }
        isSaving = false
    }
}

#Preview {
    NavigationStack {
        GroupDataView(group: MockData.groups[0], onSaved: { _ in })
            .environment(\.services, .preview)
    }
}

// MARK: - MeetingDayWheelPicker (shared with GroupSettingsView)

/// High-contrast weekday wheel; lives here so it always compiles with the Settings target (avoids orphan file / XcodeGen drift).
struct MeetingDayWheelPicker: UIViewRepresentable {
    @Binding var selection: String

    static let days = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"]

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> UIPickerView {
        let picker = UIPickerView()
        picker.delegate = context.coordinator
        picker.dataSource = context.coordinator
        picker.backgroundColor = UIColor(Color.neuBackground)
        return picker
    }

    func updateUIView(_ uiView: UIPickerView, context: Context) {
        context.coordinator.parent = self
        if let idx = Self.days.firstIndex(of: selection) {
            if uiView.selectedRow(inComponent: 0) != idx {
                uiView.selectRow(idx, inComponent: 0, animated: false)
            }
        }
    }

    final class Coordinator: NSObject, UIPickerViewDelegate, UIPickerViewDataSource {
        var parent: MeetingDayWheelPicker

        init(_ parent: MeetingDayWheelPicker) {
            self.parent = parent
        }

        func numberOfComponents(in pickerView: UIPickerView) -> Int { 1 }

        func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
            MeetingDayWheelPicker.days.count
        }

        func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {
            let label = (view as? UILabel) ?? UILabel()
            let day = MeetingDayWheelPicker.days[row]
            label.text = day
            if let descriptor = UIFont.systemFont(ofSize: 20, weight: .semibold).fontDescriptor.withDesign(.rounded) {
                label.font = UIFont(descriptor: descriptor, size: 20)
            } else {
                label.font = UIFont.systemFont(ofSize: 20, weight: .semibold)
            }
            label.textColor = UIColor(Color.textPrimary)
            label.textAlignment = .center
            return label
        }

        func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
            parent.selection = MeetingDayWheelPicker.days[row]
        }
    }
}
