import SwiftUI
import LucideIcons

// MARK: - Meeting Success Screen
// Shown after a meeting report is successfully submitted.

struct MeetingSuccessView: View {
    let presentCount: Int
    let absentCount: Int
    let justifiedCount: Int
    let sentAt: Date
    let onClose: () -> Void
    let onViewHistory: () -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            // Dark top half / light bottom half background
            VStack(spacing: 0) {
                Color.neuBackgroundDark.ignoresSafeArea()
                Color.neuBackground
            }

            // Content
            VStack(spacing: 0) {
                // Dark section
                VStack(spacing: 16) {
                    Spacer().frame(height: 60)

                    // Checkmark in rounded square outline
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.white.opacity(0.4), lineWidth: 2)
                        .frame(width: 72, height: 72)
                        .overlay(
                            Image(uiImage: AppIcon.check)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 32, height: 32)
                                .foregroundStyle(.white)
                        )

                    Text("¡Reporte enviado!")
                        .font(.displayLarge)
                        .foregroundStyle(.white)

                    Text(sentAt, format: .dateTime.day().month(.wide).year())
                        .font(.captionStyle)
                        .foregroundStyle(.white.opacity(0.5))

                    Spacer().frame(height: 20)
                }
                .frame(maxWidth: .infinity)
                .background(Color.neuBackgroundDark)

                // Light section
                VStack(spacing: 0) {
                    // Stats table
                    NeuCard {
                        VStack(spacing: 0) {
                            statRow(label: "Presentes", value: "\(presentCount)", color: .statusPresent)
                            Divider().opacity(0.3)
                            statRow(label: "Ausentes", value: "\(absentCount)", color: .blush)
                            Divider().opacity(0.3)
                            statRow(label: "Justificados", value: "\(justifiedCount)", color: .statusJustified)
                            Divider().opacity(0.3)
                            statRow(
                                label: "Asistencia",
                                value: total > 0 ? "\(Int(Double(presentCount) / Double(total) * 100))%" : "--",
                                color: .accent
                            )
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 24)

                    VStack(spacing: 12) {
                        NeuButton("Volver al inicio", variant: .primary, action: onClose)
                        Button(action: onViewHistory) {
                            Text("Ver historial")
                                .font(.bodyLarge)
                                .foregroundStyle(Color.accent)
                        }
                        .buttonStyle(.plain)
                        .padding(.top, 4)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 40)
                }
                .background(Color.neuBackground)
            }
        }
        .ignoresSafeArea()
    }

    private var total: Int { presentCount + absentCount + justifiedCount }

    private func statRow(label: String, value: String, color: Color) -> some View {
        HStack {
            Text(label)
                .font(.bodyRegular)
                .foregroundStyle(Color.textSecondary)
            Spacer()
            Text(value)
                .font(.headingMedium)
                .foregroundStyle(color)
        }
        .padding(.vertical, 12)
    }
}

#Preview {
    MeetingSuccessView(
        presentCount: 8,
        absentCount: 1,
        justifiedCount: 1,
        sentAt: Date(),
        onClose: {},
        onViewHistory: {}
    )
}
