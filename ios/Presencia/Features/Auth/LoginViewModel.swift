import Foundation
import Observation

@Observable
final class LoginViewModel {
    var email: String = ""
    var password: String = ""
    var isLoading: Bool = false
    var errorMessage: String? = nil

    private let authService: any AuthService

    init(authService: any AuthService) {
        self.authService = authService
    }

    @MainActor
    func signIn() async {
        guard !email.trimmingCharacters(in: .whitespaces).isEmpty,
              !password.isEmpty else {
            errorMessage = "Por favor completa todos los campos."
            HapticFeedback.warning()
            return
        }
        isLoading = true
        errorMessage = nil
        do {
            _ = try await authService.signIn(email: email.trimmingCharacters(in: .whitespacesAndNewlines), password: password)
            HapticFeedback.success()
        } catch let e as PresenciaLoginError {
            switch e {
            case .invalidCredentials:
                errorMessage = "Credenciales incorrectas. Intenta de nuevo."
            case .networkOrServer:
                errorMessage = "Sin conexión. Verifica tu internet e intenta de nuevo."
            case .profileNotAvailable, .authUserTokenColumnsNull:
                errorMessage = "Tu perfil no está disponible. Contacta al administrador."
            }
            HapticFeedback.warning()
        } catch {
            errorMessage = "Ocurrió un error. Intenta de nuevo."
            HapticFeedback.warning()
        }
        isLoading = false
    }
}
