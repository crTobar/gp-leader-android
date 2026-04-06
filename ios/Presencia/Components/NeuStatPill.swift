import SwiftUI

// MARK: - Neumorphic Stat Pill

struct NeuStatPill: View {
    let value: String
    let label: String
    var valueColor: Color = .textPrimary

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.displayMedium)
                .foregroundStyle(valueColor)
                .minimumScaleFactor(0.7)
                .lineLimit(1)

            Text(label.uppercased())
                .font(.captionSmall)
                .foregroundStyle(Color.textMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .padding(.horizontal, 12)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.buttonRadius))
        .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 5, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 5, x: -4, y: -4)
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        HStack(spacing: 12) {
            NeuStatPill(value: "15", label: "Miembros")
            NeuStatPill(value: "12", label: "Presentes", valueColor: .statusPresent)
            NeuStatPill(value: "3", label: "Ausentes", valueColor: .statusAbsent)
        }
        .padding()
    }
}
