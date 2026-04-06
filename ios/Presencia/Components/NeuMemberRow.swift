import SwiftUI
import LucideIcons

// MARK: - Neumorphic Member Row

struct NeuMemberRow<Trailing: View>: View {
    let member: Member
    let trailing: () -> Trailing

    init(member: Member, @ViewBuilder trailing: @escaping () -> Trailing) {
        self.member = member
        self.trailing = trailing
    }

    var body: some View {
        HStack(spacing: 12) {
            // Initials Avatar
            ZStack {
                Circle()
                    .fill(Color.neuBackground)
                    .frame(width: 40, height: 40)
                    .shadow(color: Color.neuShadowDark.opacity(0.45), radius: 4, x: 3, y: 3)
                    .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 4, x: -3, y: -3)

                Text(member.initials)
                    .font(.captionStyle)
                    .foregroundStyle(Color.accent)
            }

            // Name + badges
            VStack(alignment: .leading, spacing: 2) {
                Text(member.fullName)
                    .font(.bodyLarge)
                    .foregroundStyle(Color.textPrimary)

                HStack(spacing: 6) {
                    if member.isVisitor {
                        VisitorBadge()
                    }
                    if !member.isActive {
                        InactiveBadge()
                    }
                }
            }

            Spacer(minLength: 0)

            trailing()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .frame(minHeight: NeuStyle.touchTargetMin + 4)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.memberRowRadius))
        .shadow(color: Color.neuShadowDark.opacity(0.4), radius: 6, x: 4, y: 4)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 6, x: -4, y: -4)
        .accessibilityLabel(Text("\(member.fullName), \(member.isActive ? "activo" : "inactivo")"))
    }
}

// MARK: - Badge helpers

private struct VisitorBadge: View {
    var body: some View {
        Text("Visitante")
            .font(.captionSmall)
            .foregroundStyle(Color.accent)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(Color.accentGlow)
            .clipShape(Capsule())
    }
}

private struct InactiveBadge: View {
    var body: some View {
        Text("Inactivo")
            .font(.captionSmall)
            .foregroundStyle(Color.textMuted)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(Color.neuShadowDark.opacity(0.15))
            .clipShape(Capsule())
    }
}

#Preview {
    ZStack {
        Color.neuBackground.ignoresSafeArea()
        VStack(spacing: 12) {
            NeuMemberRow(member: MockData.members[0]) {
                Image(uiImage: AppIcon.chevronRight)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 12, height: 12)
                    .foregroundStyle(Color.textMuted)
            }
            NeuMemberRow(member: MockData.members[9]) {
                Image(uiImage: AppIcon.chevronRight)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 12, height: 12)
                    .foregroundStyle(Color.textMuted)
            }
        }
        .padding()
    }
}
