import SwiftUI

// MARK: - Login (hierarchy username + password, Supabase Auth)

struct LoginView: View {
    var onSignedIn: (() -> Void)? = nil

    @Environment(\.services) private var services

    @State private var unions: [UnionOrg] = []
    @State private var campos: [Campo] = []
    @State private var churches: [Church] = []
    @State private var groups: [SmallGroup] = []

    @State private var selectedUnion: UnionOrg?
    @State private var selectedCampo: Campo?
    @State private var selectedChurch: Church?
    @State private var selectedGroup: SmallGroup?

    @State private var password = ""
    @State private var isLoadingHierarchy = false
    @State private var isSigningIn = false
    @State private var errorMessage: String? = nil

    var body: some View {
        ZStack {
            Color.neuBackground
                .ignoresSafeArea()
                .allowsHitTesting(false)

            ScrollView {
                VStack(spacing: 0) {
                    LoginHeader()
                        .allowsHitTesting(false)

                    LoginHierarchyBody(
                        unions: unions,
                        campos: campos,
                        churches: churches,
                        groups: groups,
                        selectedUnion: $selectedUnion,
                        selectedCampo: $selectedCampo,
                        selectedChurch: $selectedChurch,
                        selectedGroup: $selectedGroup,
                        password: $password,
                        isLoadingHierarchy: isLoadingHierarchy,
                        isSigningIn: isSigningIn,
                        errorMessage: errorMessage,
                        onRetryLoadUnions: { Task { await loadUnions() } },
                        onLogin: { Task { await signIn() } }
                    )
                }
            }
            .scrollIndicators(.visible)
            .background(Color.neuBackground)
            .ignoresSafeArea(edges: .top)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task { await loadUnions() }
        .onChange(of: selectedUnion) { _, new in
            selectedCampo = nil
            selectedChurch = nil
            selectedGroup = nil
            campos = []
            churches = []
            groups = []
            if let u = new {
                Task { await loadCampos(unionId: u.id) }
            }
        }
        .onChange(of: selectedCampo) { _, new in
            selectedChurch = nil
            selectedGroup = nil
            churches = []
            groups = []
            if let c = new {
                Task { await loadChurches(campoId: c.id) }
            }
        }
        .onChange(of: selectedChurch) { _, new in
            selectedGroup = nil
            groups = []
            if let ch = new {
                Task { await loadGroups(churchId: ch.id) }
            }
        }
    }

    @MainActor
    private func loadUnions() async {
        isLoadingHierarchy = true
        errorMessage = nil
        do {
            unions = try await services.hierarchy.fetchUnions()
            if selectedUnion == nil, unions.count == 1 {
                selectedUnion = unions.first
            }
            if unions.isEmpty {
                // RLS + anon often returns [] with no error — user sees an empty picker.
                errorMessage =
                    "No hay uniones visibles. Con RLS activo, el rol «anon» necesita política SELECT en union_org, campo, district, church y small_group. Ejecuta el SQL en Design/supabase_login_hierarchy.sql."
            }
        } catch {
            errorMessage = "No se pudieron cargar las uniones: \(error.localizedDescription)"
        }
        isLoadingHierarchy = false
    }

    @MainActor
    private func loadCampos(unionId: UUID) async {
        do {
            campos = try await services.hierarchy.fetchCampos(unionId: unionId)
            if selectedCampo == nil, campos.count == 1 {
                selectedCampo = campos.first
            }
        } catch {
            errorMessage = "No se pudo cargar el campo."
        }
    }

    @MainActor
    private func loadChurches(campoId: UUID) async {
        do {
            churches = try await services.hierarchy.fetchChurches(campoId: campoId)
        } catch {
            errorMessage = "No se pudo cargar la iglesia."
        }
    }

    @MainActor
    private func loadGroups(churchId: UUID) async {
        do {
            groups = try await services.hierarchy.fetchSmallGroups(churchId: churchId)
        } catch {
            errorMessage = "No se pudo cargar el grupo pequeño."
        }
    }

    private func signIn() async {
        guard let u = selectedUnion, let c = selectedCampo, let ch = selectedChurch, let g = selectedGroup else {
            errorMessage = "Selecciona unión, campo, iglesia y grupo pequeño."
            return
        }
        guard !password.isEmpty else {
            errorMessage = "Ingresa tu contraseña."
            return
        }
        isSigningIn = true
        errorMessage = nil
        do {
            _ = try await services.auth.signInHierarchy(union: u, campo: c, church: ch, group: g, password: password)
            HapticFeedback.success()
            onSignedIn?()
        } catch let loginError as PresenciaLoginError {
            errorMessage = loginError.errorDescription
            HapticFeedback.warning()
        } catch {
            errorMessage = error.localizedDescription
            HapticFeedback.warning()
        }
        isSigningIn = false
    }
}

// MARK: - Header (neumorphic hero — matches Splash / Home tone)

private struct LoginHeader: View {
    var body: some View {
        VStack(spacing: 20) {
            NeuIconBadge(icon: AppIcon.book, size: 64)

            VStack(spacing: 8) {
                Text("Presencia")
                    .font(.displayLarge)
                    .foregroundStyle(Color.textPrimary)

                Text("Gestión de grupos pequeños SDA")
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 8)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 32)
        .padding(.bottom, 8)
    }
}

// MARK: - Body

private struct LoginHierarchyBody: View {
    let unions: [UnionOrg]
    let campos: [Campo]
    let churches: [Church]
    let groups: [SmallGroup]

    @Binding var selectedUnion: UnionOrg?
    @Binding var selectedCampo: Campo?
    @Binding var selectedChurch: Church?
    @Binding var selectedGroup: SmallGroup?
    @Binding var password: String

    let isLoadingHierarchy: Bool
    let isSigningIn: Bool
    let errorMessage: String?

    let onRetryLoadUnions: () -> Void
    let onLogin: () -> Void

    private var campoPlaceholder: String {
        guard selectedUnion != nil else { return "Primero elige la unión" }
        return campos.isEmpty ? "Sin datos — toca para ver" : "Seleccionar campo"
    }

    private var churchPlaceholder: String {
        guard selectedCampo != nil else { return "Primero elige el campo" }
        return churches.isEmpty ? "Sin datos — toca para ver" : "Seleccionar iglesia"
    }

    private var groupPlaceholder: String {
        guard selectedChurch != nil else { return "Primero elige la iglesia" }
        return groups.isEmpty ? "Sin datos — toca para ver" : "Seleccionar GP"
    }

    var body: some View {
        NeuCard(padding: 22, cornerRadius: NeuStyle.cardRadius) {
            VStack(alignment: .leading, spacing: 0) {
                Text("INICIAR SESIÓN")
                    .font(.captionStyle)
                    .foregroundStyle(Color.textMuted)

                Text("Bienvenido")
                    .font(.titleLarge)
                    .foregroundStyle(Color.textPrimary)
                    .padding(.top, 4)

                Text("Selecciona unión, campo, iglesia y grupo; tu usuario se forma solo.")
                    .font(.bodyRegular)
                    .foregroundStyle(Color.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.top, 8)
                    .padding(.bottom, 18)

                if isLoadingHierarchy && unions.isEmpty {
                    HStack {
                        Spacer()
                        ProgressView()
                            .tint(Color.accent)
                        Spacer()
                    }
                    .padding(.vertical, 16)
                }

                VStack(spacing: 14) {
                    SearchableHierarchyPicker(
                        title: "Unión",
                        fieldLabel: "UNIÓN",
                        placeholder: unions.isEmpty ? "Sin datos — toca para ver" : "Seleccionar unión",
                        selection: $selectedUnion,
                        items: unions,
                        displayPrimary: { $0.name },
                        displaySecondary: { $0.code },
                        isEnabled: !isLoadingHierarchy
                    )
                    SearchableHierarchyPicker(
                        title: "Campo",
                        fieldLabel: "CAMPO",
                        placeholder: campoPlaceholder,
                        selection: $selectedCampo,
                        items: campos,
                        displayPrimary: { $0.name },
                        displaySecondary: { $0.code },
                        isEnabled: selectedUnion != nil
                    )
                    SearchableHierarchyPicker(
                        title: "Iglesia",
                        fieldLabel: "IGLESIA",
                        placeholder: churchPlaceholder,
                        selection: $selectedChurch,
                        items: churches,
                        displayPrimary: { $0.name },
                        displaySecondary: { $0.code },
                        isEnabled: selectedCampo != nil
                    )
                    SearchableHierarchyPicker(
                        title: "Grupo pequeño",
                        fieldLabel: "GRUPO PEQUEÑO",
                        placeholder: groupPlaceholder,
                        selection: $selectedGroup,
                        items: groups,
                        displayPrimary: { $0.name },
                        displaySecondary: { $0.gpUsername },
                        isEnabled: selectedChurch != nil
                    )

                    NeuTextField(
                        placeholder: "Contraseña",
                        text: $password,
                        label: "CONTRASEÑA",
                        isSecure: true
                    )
                }

                Spacer(minLength: 14)

                LoginNoticeCard()

                Spacer(minLength: 18)

                if let errorMessage {
                    Text(errorMessage)
                        .font(.bodyRegular)
                        .foregroundStyle(Color.blush)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.bottom, 6)
                }

                if !isLoadingHierarchy, unions.isEmpty {
                    Button(action: onRetryLoadUnions) {
                        Text("Reintentar cargar ubicaciones")
                            .font(.bodyLarge)
                            .foregroundStyle(Color.accent)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                    }
                    .buttonStyle(.plain)
                    .padding(.bottom, 6)
                }

                if isSigningIn {
                    HStack {
                        Spacer()
                        ProgressView()
                            .progressViewStyle(.circular)
                            .tint(Color.accent)
                        Spacer()
                    }
                    .frame(height: 56)
                } else {
                    NeuButton("Entrar", variant: .primary, action: onLogin)
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 28)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Notice

private struct LoginNoticeCard: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: NeuStyle.inputRadius, style: .continuous)
                .fill(Color.neuBackgroundDeep)
                .shadow(color: Color.neuShadowDark.opacity(0.2), radius: 4, x: 2, y: 2)
                .shadow(color: Color.neuShadowLight.opacity(0.7), radius: 4, x: -2, y: -2)

            RoundedRectangle(cornerRadius: NeuStyle.inputRadius, style: .continuous)
                .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [6, 4]))
                .foregroundStyle(Color.accent.opacity(0.28))

            (
                Text("Tu cuenta usa el mismo formato que te dio tu ")
                    .foregroundStyle(Color.textSecondary)
                + Text("pastor o administrador")
                    .font(.bodyLarge)
                    .foregroundStyle(Color.textPrimary)
                + Text(" (unión, campo, iglesia y GP). Si no tienes acceso, contáctalo.")
                    .foregroundStyle(Color.textSecondary)
            )
            .font(.bodyRegular)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 14)
            .padding(.vertical, 14)
        }
        .allowsHitTesting(false)
    }
}

#Preview {
    LoginView(onSignedIn: {})
        .environment(\.services, .preview)
}
