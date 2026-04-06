import Foundation

extension DecodingError {
    /// Developer-facing path for logs / support (Spanish labels).
    var presenciaDebugDescription: String {
        switch self {
        case .keyNotFound(let key, let ctx):
            let path = (ctx.codingPath + [key]).map(\.stringValue).joined(separator: ".")
            return "Falta el campo «\(key.stringValue)» (ruta: \(path))."
        case .typeMismatch(let type, let ctx):
            let path = ctx.codingPath.map(\.stringValue).joined(separator: ".")
            return "Tipo incorrecto, se esperaba \(String(describing: type)) (ruta: \(path))."
        case .valueNotFound(let type, let ctx):
            let path = ctx.codingPath.map(\.stringValue).joined(separator: ".")
            return "Valor nulo o ausente para \(String(describing: type)) (ruta: \(path))."
        case .dataCorrupted(let ctx):
            return "JSON dañado o inesperado: \(ctx.debugDescription)"
        @unknown default:
            return localizedDescription
        }
    }
}
