import Foundation

// MARK: - Spanish Locale Date Formatters

enum DateFormatters {

    static let spanishLocale = Locale(identifier: "es_CO")

    /// "Sábado 8 de Marzo"
    static let meetingDate: DateFormatter = {
        let f = DateFormatter()
        f.locale = spanishLocale
        f.dateFormat = "EEEE d 'de' MMMM"
        return f
    }()

    /// "Mié 5 de Marzo"
    static let meetingDateShort: DateFormatter = {
        let f = DateFormatter()
        f.locale = spanishLocale
        f.dateFormat = "EEE d 'de' MMMM"
        return f
    }()

    /// "8 Mar"
    static let dayMonth: DateFormatter = {
        let f = DateFormatter()
        f.locale = spanishLocale
        f.dateFormat = "d MMM"
        return f
    }()

    /// "Mar 2024"
    static let monthYear: DateFormatter = {
        let f = DateFormatter()
        f.locale = spanishLocale
        f.dateFormat = "MMM yyyy"
        return f
    }()

    /// "Buenos días" / "Buenas tardes" / "Buenas noches"
    static func greeting() -> String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<12: return "Buenos días"
        case 12..<19: return "Buenas tardes"
        default: return "Buenas noches"
        }
    }

    /// Formatted meeting date: "Sábado 8 de Marzo, 2025" (capitalized)
    static func formatMeetingDate(_ date: Date) -> String {
        meetingDate.string(from: date).capitalized
    }

    /// Short date for cards: "Sáb 8 Mar"
    static func formatShortDate(_ date: Date) -> String {
        meetingDateShort.string(from: date).capitalized
    }
}
