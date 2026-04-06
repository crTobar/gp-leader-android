import SwiftUI
import LucideIcons

// MARK: - 3-State Attendance Toggle
// Cycles: absent → present → justified → absent

struct NeuToggle: View {
    @Binding var status: AttendanceStatus

    var body: some View {
        Button {
            withAnimation(.easeInOut(duration: 0.2)) {
                status = status.next
            }
            HapticFeedback.medium()
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(backgroundFill)
                    .frame(width: 52, height: 32)

                Image(uiImage: status.icon)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 14, height: 14)
                    .foregroundStyle(status.iconColor)
            }
        }
        .accessibilityLabel(status.displayName)
        .accessibilityHint("Toca para cambiar el estado de asistencia")
    }

    private var backgroundFill: some ShapeStyle {
        switch status {
        case .absent:
            return AnyShapeStyle(Color.neuBackground.shadow(.inner(color: Color.neuShadowDark.opacity(0.0), radius: 0, x: 0, y: 0)))
        case .present:
            return AnyShapeStyle(Color.statusPresent.opacity(0.15).shadow(.inner(color: Color.statusPresent.opacity(0.3), radius: 4, x: 2, y: 2)))
        case .justified:
            return AnyShapeStyle(Color.statusJustified.opacity(0.15).shadow(.inner(color: Color.statusJustified.opacity(0.3), radius: 4, x: 2, y: 2)))
        }
    }
}

// MARK: - AttendanceStatus Extensions

extension AttendanceStatus {
    var next: AttendanceStatus {
        switch self {
        case .absent: return .present
        case .present: return .justified
        case .justified: return .absent
        }
    }

    var icon: UIImage {
        switch self {
        case .absent: return AppIcon.close
        case .present: return AppIcon.check
        case .justified: return AppIcon.info
        }
    }

    var iconColor: Color {
        switch self {
        case .absent: return .textMuted
        case .present: return .statusPresent
        case .justified: return .statusJustified
        }
    }

    var badgeColor: Color {
        switch self {
        case .absent: return .blush
        case .present: return .statusPresent
        case .justified: return .statusJustified
        }
    }
}

#Preview {
    @Previewable @State var status1: AttendanceStatus = .absent
    @Previewable @State var status2: AttendanceStatus = .present
    @Previewable @State var status3: AttendanceStatus = .justified

    ZStack {
        Color.neuBackground.ignoresSafeArea()
        HStack(spacing: 20) {
            NeuToggle(status: $status1)
            NeuToggle(status: $status2)
            NeuToggle(status: $status3)
        }
    }
}
