import SwiftUI
import Charts

// MARK: - Group Ranking List

struct GroupRankingListView: View {
    let rankings: [GroupRanking]

    var body: some View {
        VStack(spacing: 10) {
            ForEach(Array(rankings.enumerated()), id: \.element.id) { index, ranking in
                GroupRankingRow(index: index + 1, ranking: ranking)
            }
        }
    }
}

private struct GroupRankingRow: View {
    let index: Int
    let ranking: GroupRanking

    var rateColor: Color {
        if ranking.attendanceRate >= 0.8 { return .statusPresent }
        if ranking.attendanceRate >= 0.6 { return .statusJustified }
        return .blush
    }

    var body: some View {
        NeuCard(padding: 14, cornerRadius: NeuStyle.memberRowRadius) {
            HStack(spacing: 12) {
                // Rank circle
                ZStack {
                    Circle()
                        .fill(Color.neuBackground)
                        .frame(width: 32, height: 32)
                        .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 3, x: 2, y: 2)
                        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 3, x: -2, y: -2)
                    Text("\(index)")
                        .font(.captionStyle)
                        .foregroundStyle(index <= 3 ? Color.accent : Color.textMuted)
                }

                // Names
                VStack(alignment: .leading, spacing: 2) {
                    Text(ranking.group.name)
                        .font(.bodyLarge)
                        .foregroundStyle(Color.textPrimary)
                    Text(ranking.leaderName)
                        .font(.captionStyle)
                        .foregroundStyle(Color.textMuted)
                }

                Spacer()

                // Sparkline
                if !ranking.trend.isEmpty {
                    Chart {
                        ForEach(Array(ranking.trend.enumerated()), id: \.offset) { i, value in
                            LineMark(
                                x: .value("Semana", i),
                                y: .value("Asistencia", value)
                            )
                            .foregroundStyle(rateColor)
                            .lineStyle(StrokeStyle(lineWidth: 1.5))
                        }
                    }
                    .chartXAxis(.hidden)
                    .chartYAxis(.hidden)
                    .frame(width: 56, height: 28)
                }

                // Rate percentage
                Text(ranking.attendanceRate.asPercent)
                    .font(.headingMedium)
                    .foregroundStyle(rateColor)
                    .frame(width: 52, alignment: .trailing)
            }
        }
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        ScrollView {
            GroupRankingListView(rankings: MockData.groups.map { group in
                GroupRanking(
                    group: group,
                    leaderName: "Líder de \(group.name)",
                    attendanceRate: Double.random(in: 0.5...0.95),
                    trend: (0..<6).map { _ in Double.random(in: 0.4...1.0) }
                )
            })
            .padding()
        }
    }
}
