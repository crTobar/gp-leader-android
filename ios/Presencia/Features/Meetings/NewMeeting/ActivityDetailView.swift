import SwiftUI

// MARK: - Activity Detail Sheet (Kotlin DetalleActividadScreen parity)

struct ActivityDetailView: View {
    let activityType: ActivityType
    @Binding var count: Int?
    @Binding var noteText: String

    @Environment(\.dismiss) private var dismiss
    @FocusState private var notesFocused: Bool

    private var showOficialBadge: Bool {
        activityType.level != .myGroup
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.neuBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    detalleTopBar
                        .padding(.horizontal, 4)
                        .padding(.vertical, 4)

                    levelHeaderRow

                    Spacer(minLength: 16)

                    nameCard
                        .padding(.horizontal, 16)

                    Spacer(minLength: 20)

                    totalSection

                    Spacer(minLength: 20)

                    notesSection
                        .padding(.horizontal, 16)

                    Spacer(minLength: 96)
                }
            }

            guardarBar
        }
    }

    // MARK: - Top bar

    private var detalleTopBar: some View {
        HStack(spacing: 0) {
            Button {
                HapticFeedback.light()
                dismiss()
            } label: {
                LucideIcon(uiImage: AppIcon.chevronLeft, size: 18)
                    .foregroundStyle(Color.textPrimary)
                    .frame(width: 48, height: 48)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Atrás")

            Text("Detalle actividad")
                .font(.headingLarge)
                .foregroundStyle(Color.textPrimary)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)

            Color.clear
                .frame(width: 48, height: 48)
        }
    }

    // MARK: - Level header (full width)

    private var levelHeaderRow: some View {
        HStack {
            Text(levelBannerTitle)
                .font(.captionStyle)
                .foregroundStyle(levelBannerForeground)
            Spacer(minLength: 8)
            if showOficialBadge {
                Text("Oficial")
                    .font(.captionSmall)
                    .foregroundStyle(Color.statusJustified)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.statusJustified.opacity(0.15))
                    .clipShape(RoundedRectangle(cornerRadius: 4, style: .continuous))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(levelBannerBackground)
    }

    private var levelBannerTitle: String {
        switch activityType.level {
        case .union: return "NIVEL UNIÓN"
        case .pastor: return "NIVEL PASTOR"
        case .myGroup: return "NIVEL MI GP"
        }
    }

    private var levelBannerBackground: Color {
        switch activityType.level {
        case .pastor: return Color.textSecondary
        case .union, .myGroup: return Color.neuBackgroundDeep
        }
    }

    private var levelBannerForeground: Color {
        switch activityType.level {
        case .pastor: return .white
        case .union, .myGroup: return Color.textPrimary
        }
    }

    // MARK: - Name card

    private var nameCard: some View {
        NeuCard(padding: 0, cornerRadius: NeuStyle.cardRadius) {
            HStack(alignment: .center, spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(Color.neuBackgroundDeep)
                        .frame(width: 44, height: 44)
                        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                        .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                    Text("◆")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(activityType.name)
                        .font(.headingLarge)
                        .foregroundStyle(Color.textPrimary)
                    Text("Unidad: \(activityType.unitLabel)")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }

    // MARK: - Total + counter

    private var totalSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("TOTAL")
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)
                .padding(.horizontal, 16)

            NeuCard(padding: 0, cornerRadius: NeuStyle.cardRadius) {
                HStack {
                    counterSideButton(isPlus: false)
                    Spacer(minLength: 8)
                    Text(count.map { "\($0)" } ?? "—")
                        .font(.displayLarge)
                        .foregroundStyle(count != nil ? Color.textPrimary : Color.textMuted)
                        .animation(.easeInOut(duration: 0.15), value: count)
                    Spacer(minLength: 8)
                    counterSideButton(isPlus: true)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)
            }
            .padding(.horizontal, 16)
        }
    }

    private func counterSideButton(isPlus: Bool) -> some View {
        Button {
            HapticFeedback.light()
            if isPlus {
                count = (count ?? -1) + 1
            } else {
                guard let c = count else { return }
                count = c <= 0 ? nil : c - 1
            }
        } label: {
            Text(isPlus ? "+" : "−")
                .font(.headingLarge)
                .foregroundStyle(isPlus ? Color.white : (count != nil ? Color.textPrimary : Color.textMuted))
                .frame(width: 52, height: 52)
                .background(
                    Group {
                        if isPlus {
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .fill(Color.accent)
                        } else {
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .fill(Color.neuBackground)
                                .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                                .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
                        }
                    }
                )
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .buttonStyle(ScalePressButtonStyle())
        .disabled(!isPlus && count == nil)
    }

    // MARK: - Notes

    private var notesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("NOTAS (OPCIONAL)")
                .font(.captionStyle)
                .foregroundStyle(Color.textMuted)

            ZStack(alignment: .topLeading) {
                if noteText.isEmpty {
                    Text("Agrega contexto o detalles…")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                        .padding(.top, 12)
                        .padding(.leading, 14)
                        .allowsHitTesting(false)
                }
                TextField("", text: $noteText, axis: .vertical)
                    .lineLimit(3...10)
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textPrimary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .focused($notesFocused)
            }
            .background(RoundedRectangle(cornerRadius: 14, style: .continuous).fill(Color.neuBackground))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .strokeBorder(
                        notesFocused ? Color.accent : Color.textMuted.opacity(0.4),
                        lineWidth: notesFocused ? 1.5 : 1
                    )
            )
        }
    }

    // MARK: - Bottom bar

    private var guardarBar: some View {
        VStack(spacing: 0) {
            NeuButton("Guardar", variant: .primary) {
                HapticFeedback.light()
                dismiss()
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 12)
        }
        .frame(maxWidth: .infinity)
        .background(Color.neuBackground.ignoresSafeArea(edges: .bottom))
    }
}

// MARK: - Press animation (matches NeuButton)

private struct ScalePressButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .animation(.easeInOut(duration: 0.15), value: configuration.isPressed)
    }
}

#Preview("Pastor") {
    ActivityDetailPreviewHost(activity: MockData.activityTypes[2], initialCount: 3)
}

#Preview("Mi GP") {
    ActivityDetailPreviewHost(activity: MockData.activityTypes[4], initialCount: nil)
}

private struct ActivityDetailPreviewHost: View {
    let activity: ActivityType
    let initialCount: Int?
    @State private var count: Int?
    @State private var note = ""

    init(activity: ActivityType, initialCount: Int?) {
        self.activity = activity
        self.initialCount = initialCount
        _count = State(initialValue: initialCount)
    }

    var body: some View {
        ActivityDetailView(activityType: activity, count: $count, noteText: $note)
    }
}
