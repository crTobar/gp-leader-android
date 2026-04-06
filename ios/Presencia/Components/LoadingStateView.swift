import SwiftUI

// MARK: - Shimmer Loading Skeleton

struct LoadingStateView: View {
    var rows: Int = 4
    var showAvatar: Bool = true

    var body: some View {
        VStack(spacing: 12) {
            ForEach(0..<rows, id: \.self) { _ in
                SkeletonRow(showAvatar: showAvatar)
            }
        }
    }
}

private struct SkeletonRow: View {
    let showAvatar: Bool
    @State private var phase: CGFloat = 0

    var body: some View {
        HStack(spacing: 12) {
            if showAvatar {
                Circle()
                    .fill(shimmerGradient)
                    .frame(width: 40, height: 40)
            }

            VStack(alignment: .leading, spacing: 6) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerGradient)
                    .frame(maxWidth: .infinity)
                    .frame(height: 14)

                RoundedRectangle(cornerRadius: 4)
                    .fill(shimmerGradient)
                    .frame(maxWidth: 160)
                    .frame(height: 10)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
        .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 5, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.8), radius: 5, x: -4, y: -4)
        .onAppear {
            withAnimation(.linear(duration: 1.4).repeatForever(autoreverses: false)) {
                phase = 1
            }
        }
    }

    private var shimmerGradient: LinearGradient {
        LinearGradient(
            stops: [
                .init(color: Color.neuShadowDark.opacity(0.2), location: 0),
                .init(color: Color.neuShadowDark.opacity(0.08), location: 0.4 + phase * 0.2),
                .init(color: Color.neuShadowDark.opacity(0.2), location: 0.8 + phase * 0.2)
            ],
            startPoint: .leading,
            endPoint: .trailing
        )
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        LoadingStateView(rows: 5)
            .padding()
    }
}
