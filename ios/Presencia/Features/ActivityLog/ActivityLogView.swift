import SwiftUI
import LucideIcons

// MARK: - Activity Log View

struct ActivityLogView: View {
    @Environment(\.services) private var services
    @Environment(\.dismiss) private var dismiss

    let groupId: UUID

    @State private var vm: ActivityLogViewModel?

    var body: some View {
        ZStack {
            Color.neuBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                topBar
                    .padding(.horizontal, 20)
                    .padding(.top, 8)
                    .padding(.bottom, 12)

                if let vm {
                    content(vm: vm)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            if vm == nil {
                vm = ActivityLogViewModel(services: services, groupId: groupId)
                await vm?.load()
            }
        }
    }

    // MARK: - Top bar

    private var topBar: some View {
        HStack {
            Button {
                HapticFeedback.light()
                dismiss()
            } label: {
                LucideIcon(uiImage: AppIcon.chevronLeft, size: 18)
                    .foregroundStyle(Color.textPrimary)
                    .padding(10)
                    .background(Color.neuBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 4, x: 2, y: 2)
                    .shadow(color: Color.white.opacity(0.85), radius: 4, x: -2, y: -2)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Atrás")

            Spacer()

            Text("Registro de actividad")
                .font(.headingMedium)
                .foregroundStyle(Color.textPrimary)

            Spacer()

            // Invisible spacer to centre the title
            Color.clear
                .frame(width: 38, height: 38)
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func content(vm: ActivityLogViewModel) -> some View {
        if vm.isLoading {
            ScrollView {
                LoadingStateView(rows: 6)
                    .padding(.horizontal)
                    .padding(.top, 16)
            }
        } else if vm.entries.isEmpty {
            EmptyStateView(
                icon: AppIcon.clock,
                title: "Sin actividad aún",
                subtitle: "Las acciones del grupo aparecerán aquí."
            )
        } else {
            ScrollView {
                LazyVStack(spacing: 10) {
                    ForEach(vm.entries) { entry in
                        ActivityLogRow(entry: entry)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 8)
                .padding(.bottom, 40)
            }
            .refreshable {
                await vm.load()
            }
        }
    }
}

// MARK: - Log Row

private struct ActivityLogRow: View {
    let entry: ActivityLogEntry

    var body: some View {
        NeuCard(padding: 14) {
            HStack(alignment: .top, spacing: 12) {
                NeuIconBadge(icon: icon(for: entry.actionType), color: color(for: entry.actionType), size: 36)

                VStack(alignment: .leading, spacing: 4) {
                    Text(entry.description)
                        .font(.bodyRegular)
                        .foregroundStyle(Color.textPrimary)
                        .fixedSize(horizontal: false, vertical: true)

                    Text(relativeDate(entry.createdAt))
                        .font(.captionStyle)
                        .foregroundStyle(Color.textMuted)
                }

                Spacer(minLength: 0)
            }
        }
    }

    private func icon(for actionType: String) -> UIImage {
        switch actionType {
        case "meeting_submitted": return AppIcon.calendar
        case "member_added":      return AppIcon.userPlus
        case "member_archived":   return AppIcon.archive
        case "member_unarchived": return AppIcon.archiveRestore
        default:                  return AppIcon.clock
        }
    }

    private func color(for actionType: String) -> Color {
        switch actionType {
        case "meeting_submitted": return .accent
        case "member_added":      return .statusPresent
        case "member_archived":   return .statusJustified
        case "member_unarchived": return .statusPresent
        default:                  return .textMuted
        }
    }

    private func relativeDate(_ date: Date) -> String {
        let now = Date()
        let diff = now.timeIntervalSince(date)

        if diff < 60 {
            return "Ahora mismo"
        } else if diff < 3600 {
            let mins = Int(diff / 60)
            return "Hace \(mins) min"
        } else if diff < 86400 {
            let hours = Int(diff / 3600)
            return "Hace \(hours) h"
        } else if diff < 172800 {
            return "Ayer"
        } else {
            return DateFormatters.formatShortDate(date)
        }
    }
}

#Preview {
    NavigationStack {
        ActivityLogView(groupId: MockData.group1Id)
    }
    .environment(\.services, .preview)
}
