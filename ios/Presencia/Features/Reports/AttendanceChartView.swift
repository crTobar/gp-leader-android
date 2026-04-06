import SwiftUI
import Charts

// MARK: - Attendance Trend Line Chart

struct AttendanceChartView: View {
    let trend: [WeeklyAttendance]

    var body: some View {
        NeuCard(padding: 20) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Tendencia de Asistencia")
                    .font(.headingMedium)
                    .foregroundStyle(Color.textPrimary)

                if trend.isEmpty {
                    Text("Sin datos disponibles.")
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textMuted)
                        .frame(height: 160)
                        .frame(maxWidth: .infinity)
                } else {
                    Chart {
                        ForEach(trend) { week in
                            AreaMark(
                                x: .value("Semana", week.weekStart),
                                y: .value("Asistencia", week.rate * 100)
                            )
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [Color.accent.opacity(0.3), Color.accent.opacity(0.0)],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )

                            LineMark(
                                x: .value("Semana", week.weekStart),
                                y: .value("Asistencia", week.rate * 100)
                            )
                            .foregroundStyle(Color.accent)
                            .lineStyle(StrokeStyle(lineWidth: 2.5))

                            PointMark(
                                x: .value("Semana", week.weekStart),
                                y: .value("Asistencia", week.rate * 100)
                            )
                            .foregroundStyle(Color.accent)
                            .symbolSize(36)
                        }
                    }
                    .chartXAxis {
                        AxisMarks(values: .stride(by: .weekOfYear)) { value in
                            if let date = value.as(Date.self) {
                                AxisValueLabel {
                                    Text(DateFormatters.dayMonth.string(from: date))
                                        .font(.captionSmall)
                                        .foregroundStyle(Color.textMuted)
                                }
                            }
                        }
                    }
                    .chartYAxis {
                        AxisMarks(values: [0, 50, 100]) { value in
                            AxisValueLabel {
                                if let v = value.as(Double.self) {
                                    Text("\(Int(v))%")
                                        .font(.captionSmall)
                                        .foregroundStyle(Color.textMuted)
                                }
                            }
                            AxisGridLine(stroke: StrokeStyle(dash: [4]))
                                .foregroundStyle(Color.neuShadowDark.opacity(0.3))
                        }
                    }
                    .chartYScale(domain: 0...100)
                    .frame(height: 160)
                }
            }
        }
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        AttendanceChartView(trend: MockData.attendanceTrend)
            .padding()
    }
}
