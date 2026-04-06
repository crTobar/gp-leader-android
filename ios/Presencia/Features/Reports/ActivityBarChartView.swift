import SwiftUI
import Charts

// MARK: - Activity Bar Chart

struct ActivityBarChartView: View {
    let summaries: [ActivitySummary]

    var body: some View {
        NeuCard(padding: 20) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Actividades del Período")
                    .font(.headingMedium)
                    .foregroundStyle(Color.textPrimary)

                if summaries.isEmpty {
                    Text("Sin actividades registradas.")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                        .frame(height: 160)
                        .frame(maxWidth: .infinity)
                } else {
                    Chart {
                        ForEach(summaries) { summary in
                            BarMark(
                                x: .value("Actividad", shortName(summary.activityType.name)),
                                y: .value("Total", summary.totalCount)
                            )
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [Color.accent, Color.accentSoft],
                                    startPoint: .bottom,
                                    endPoint: .top
                                )
                            )
                            .cornerRadius(6)
                        }
                    }
                    .chartXAxis {
                        AxisMarks { _ in
                            AxisValueLabel()
                                .font(.captionSmall)
                        }
                    }
                    .chartYAxis {
                        AxisMarks { _ in
                            AxisGridLine(stroke: StrokeStyle(dash: [4]))
                                .foregroundStyle(Color.neuShadowDark.opacity(0.3))
                            AxisValueLabel()
                                .font(.captionSmall)
                        }
                    }
                    .frame(height: 160)
                }
            }
        }
    }

    private func shortName(_ name: String) -> String {
        let words = name.split(separator: " ")
        if words.count >= 2 {
            return "\(words[0].prefix(3))."
        }
        return String(name.prefix(6))
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        ActivityBarChartView(summaries: MockData.activityTypes.prefix(5).map {
            ActivitySummary(activityType: $0, totalCount: Int.random(in: 5...30))
        })
        .padding()
    }
}
