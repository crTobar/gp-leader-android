import Foundation
import Supabase

// MARK: - Shared Supabase Client

/// Decodes `Date` from Postgres `date` (`yyyy-MM-dd`), `timestamptz`, and numeric epoch values.
private func decodeSupabaseDate(from decoder: Decoder) throws -> Date {
    var c = try decoder.singleValueContainer()
    if let s = try? c.decode(String.self) {
        let trimmed = s.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmed.isEmpty, let d = parseSupabaseDateString(trimmed) {
            return d
        }
        throw DecodingError.dataCorruptedError(
            in: c,
            debugDescription: "Unrecognized date string: \(s)"
        )
    }
    if let t = try? c.decode(Double.self) {
        return Date(timeIntervalSince1970: t)
    }
    throw DecodingError.dataCorruptedError(in: c, debugDescription: "Expected date string or number")
}

private func parseSupabaseDateString(_ raw: String) -> Date? {
    let s = raw.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !s.isEmpty else { return nil }

    // Postgres `date` column (no time) — JSON is often "yyyy-MM-dd" only; .iso8601 decoder rejects this.
    if s.count == 10,
       s[s.index(s.startIndex, offsetBy: 4)] == "-",
       s[s.index(s.startIndex, offsetBy: 7)] == "-" {
        let df = DateFormatter()
        df.calendar = Calendar(identifier: .gregorian)
        df.locale = Locale(identifier: "en_US_POSIX")
        df.timeZone = TimeZone(secondsFromGMT: 0)
        df.dateFormat = "yyyy-MM-dd"
        if let d = df.date(from: s) { return d }
    }

    let isoFrac = ISO8601DateFormatter()
    isoFrac.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
    if let d = isoFrac.date(from: s) { return d }

    let iso = ISO8601DateFormatter()
    iso.formatOptions = [.withInternetDateTime]
    if let d = iso.date(from: s) { return d }

    if s.hasSuffix("Z"), let d = iso.date(from: String(s.dropLast()) + "+00:00") { return d }

    let df = DateFormatter()
    df.locale = Locale(identifier: "en_US_POSIX")
    df.timeZone = TimeZone(secondsFromGMT: 0)
    let patterns = [
        "yyyy-MM-dd HH:mm:ss.SSSSSSXXXXX",
        "yyyy-MM-dd HH:mm:ss.SSSXXXXX",
        "yyyy-MM-dd HH:mm:ssXXXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXXXX",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
    ]
    for p in patterns {
        df.dateFormat = p
        if let d = df.date(from: s) { return d }
    }
    return nil
}

private let _encoder: JSONEncoder = {
    let e = JSONEncoder()
    e.keyEncodingStrategy = .convertToSnakeCase
    e.dateEncodingStrategy = .iso8601
    return e
}()

private let _decoder: JSONDecoder = {
    let d = JSONDecoder()
    d.keyDecodingStrategy = .convertFromSnakeCase
    d.dateDecodingStrategy = .custom(decodeSupabaseDate(from:))
    return d
}()

let supabase = SupabaseClient(
    supabaseURL: URL(string: Config.supabaseURL)!,
    supabaseKey: Config.supabaseAnonKey,
    options: SupabaseClientOptions(
        db: .init(encoder: _encoder, decoder: _decoder)
    )
)
