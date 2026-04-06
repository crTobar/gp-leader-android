import SwiftUI
import UIKit

// MARK: - Neumorphic tab bar (matches Kotlin HomeScreen / Historial bottom bar)
// Elevated bar; the selected tab column stays inset (hundido) so the choice reads clearly.

struct NeuTabItem {
    let icon: UIImage
    let label: String
}

struct NeuTabBar: View {
    @Binding var selectedTab: Int
    let items: [NeuTabItem]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(items.indices, id: \.self) { index in
                NeuTabBarItem(
                    icon: items[index].icon,
                    label: items[index].label,
                    isSelected: selectedTab == index
                ) {
                    if selectedTab != index {
                        HapticFeedback.light()
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                            selectedTab = index
                        }
                    }
                }
            }
        }
        .padding(.vertical, 6)
        .frame(maxWidth: .infinity)
        .background(Color.neuBackground)
        .clipShape(RoundedRectangle(cornerRadius: NeuStyle.cardRadius))
        .shadow(color: Color.neuShadowDark.opacity(0.5), radius: 10, x: 8, y: 8)
        .shadow(color: Color.neuShadowLight.opacity(0.9), radius: 10, x: -8, y: -8)
        .padding(.horizontal, 4)
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 4)
        .background(
            Color.neuBackground
                .shadow(color: Color.neuShadowDark.opacity(0.35), radius: 8, x: 0, y: -3)
                .ignoresSafeArea(edges: .bottom)
        )
    }
}

// MARK: - Tab column (Kotlin NavTabItem)

private struct NeuTabBarItem: View {
    let icon: UIImage
    let label: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 3) {
                Image(uiImage: icon)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 22, height: 22)
                    .foregroundStyle(isSelected ? Color.accent : Color.textMuted)

                Text(label)
                    .font(.captionStyle)
                    .foregroundStyle(isSelected ? Color.accent : Color.textMuted)
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .modifier(NeuTabBarSelectedInset(isSelected: isSelected))
            .contentShape(Rectangle())
        }
        .buttonStyle(NeuTabBarItemButtonStyle(isSelected: isSelected))
    }
}

// MARK: - Persistent inset on selected tab

private struct NeuTabBarSelectedInset: ViewModifier {
    let isSelected: Bool
    private let corner: CGFloat = 14

    func body(content: Content) -> some View {
        Group {
            if isSelected {
                content.neuInset(cornerRadius: corner)
            } else {
                content
            }
        }
        .animation(.spring(response: 0.28, dampingFraction: 0.78), value: isSelected)
    }
}

// MARK: - Press feedback on tabs that are not already selected (avoids double-inset)

private struct NeuTabBarItemButtonStyle: ButtonStyle {
    let isSelected: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .modifier(NeuTabBarItemPressModifier(isPressed: configuration.isPressed, isSelected: isSelected))
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .animation(.easeInOut(duration: 0.15), value: configuration.isPressed)
    }
}

private struct NeuTabBarItemPressModifier: ViewModifier {
    let isPressed: Bool
    let isSelected: Bool

    func body(content: Content) -> some View {
        Group {
            if isPressed, !isSelected {
                content.neuPressed(cornerRadius: 14)
            } else {
                content
            }
        }
    }
}

#Preview {
    @Previewable @State var selected = 0
    VStack {
        Spacer()
        NeuTabBar(
            selectedTab: $selected,
            items: [
                NeuTabItem(icon: AppIcon.home, label: "INICIO"),
                NeuTabItem(icon: AppIcon.calendar, label: "HISTORIAL"),
                NeuTabItem(icon: AppIcon.userCircle, label: "PERFIL"),
            ]
        )
    }
    .background(Color.neuBackground.ignoresSafeArea())
}
