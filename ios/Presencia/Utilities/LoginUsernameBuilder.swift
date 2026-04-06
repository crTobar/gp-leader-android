import Foundation

// MARK: - Login identifier from hierarchy (Unión–Campo–Iglesia–GP)

enum LoginUsernameBuilder {
    /// Supabase Auth’s email provider requires an email-shaped value; the app never shows a “correo” field.
    /// When the identifier has no `@`, this domain is appended only for `signIn(email:password:)`.
    static let authEmailDomain = "login.presencia.app"

    /// Login string from the chosen hierarchy only: `union-campo-church-gp` (codes when present, else name slugs).
    /// Always matches the path the user picked; Supabase Auth must store the same local part + `@login.presencia.app`.
    static func loginIdentifier(
        union: UnionOrg,
        campo: Campo,
        church: Church,
        group: SmallGroup
    ) -> String {
        buildComposite(union: union, campo: campo, church: church, group: group)
    }

    /// Builds `union-campo-church-gp` using `code` when present, otherwise a slug from `name`.
    static func buildComposite(
        union: UnionOrg,
        campo: Campo,
        church: Church,
        group: SmallGroup
    ) -> String {
        [
            segment(from: union.code, name: union.name),
            segment(from: campo.code, name: campo.name),
            segment(from: church.code, name: church.name),
            segment(from: nil, name: group.name),
        ]
        .joined(separator: "-")
    }

    /// Ordered synthetic emails to try at sign-in: composite from selections, then optional `gp_username` if different.
    static func loginEmailCandidates(
        union: UnionOrg,
        campo: Campo,
        church: Church,
        group: SmallGroup
    ) -> [String] {
        let primary = authEmail(fromComposite: loginIdentifier(union: union, campo: campo, church: church, group: group))
        var out = [primary]
        if let handle = group.gpUsername?.trimmingCharacters(in: .whitespacesAndNewlines), !handle.isEmpty {
            let alt = authEmail(fromComposite: handle)
            if alt.lowercased() != primary.lowercased() {
                out.append(alt)
            }
        }
        return out
    }

    /// Maps the hierarchy username to the synthetic address Supabase expects for `signIn(email:password:)`.
    static func authEmail(fromComposite composite: String) -> String {
        let trimmed = composite.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return trimmed }
        if trimmed.contains("@") { return trimmed.lowercased() }
        return "\(trimmed.lowercased())@\(authEmailDomain)"
    }

    private static func segment(from code: String?, name: String) -> String {
        if let c = code?.trimmingCharacters(in: .whitespacesAndNewlines), !c.isEmpty {
            return c.replacingOccurrences(of: " ", with: "")
        }
        return nameSlug(name)
    }

    private static func nameSlug(_ name: String) -> String {
        // Fixed locale so the same hierarchy always maps to the same Auth email on every device.
        let posix = Locale(identifier: "en_US_POSIX")
        let folded = name.folding(options: .diacriticInsensitive, locale: posix)
        let parts = folded.split { !$0.isLetter && !$0.isNumber }
        return parts.map(String.init).filter { !$0.isEmpty }.joined(separator: "-")
    }
}
