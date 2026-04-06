import Foundation

// MARK: - Login failures (user-facing copy in Spanish)

enum PresenciaLoginError: LocalizedError, Equatable {
    case invalidCredentials
    case profileNotAvailable
    /// GoTrue fails when `auth.users` rows have NULL in token columns after manual SQL inserts.
    case authUserTokenColumnsNull
    case networkOrServer(String)

    var errorDescription: String? {
        switch self {
        case .invalidCredentials:
            return "Usuario o contraseña incorrectos."
        case .profileNotAvailable:
            return "Tu perfil no está disponible. Contacta al administrador de tu iglesia."
        case .authUserTokenColumnsNull:
            return "No se pudo iniciar sesión. Contacta al administrador de tu iglesia."
        case .networkOrServer(let detail):
            return "No se pudo completar el inicio de sesión. \(detail)"
        }
    }

    /// Maps Supabase / GoTrue opaque errors to clearer copy.
    static func mapServerMessage(_ message: String) -> PresenciaLoginError? {
        let m = message
        if m.localizedCaseInsensitiveContains("Database error querying schema")
            || m.localizedCaseInsensitiveContains("confirmation_token")
            || m.localizedCaseInsensitiveContains("converting NULL to string") {
            return .authUserTokenColumnsNull
        }
        return nil
    }
}
