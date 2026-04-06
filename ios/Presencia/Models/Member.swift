import Foundation

// MARK: - Member (GP attendees — no login, no app access)

struct Member: Identifiable, Codable, Hashable {
    let id: UUID
    let smallGroupId: UUID
    var firstName: String
    var secondName: String? = nil
    var lastName: String
    var secondLastName: String? = nil
    var phone: String?
    var email: String?
    var isVisitor: Bool
    var isActive: Bool
    let createdAt: Date
    var updatedAt: Date

    enum CodingKeys: String, CodingKey {
        case id
        case smallGroupId = "small_group_id"
        case legacyGroupId = "group_id"
        case firstName = "first_name"
        case secondName = "middle_name"
        case lastName = "last_name"
        case secondLastName = "second_last_name"
        case phone, email
        case isVisitor = "is_visitor"
        case isActive = "is_active"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }

    init(
        id: UUID,
        smallGroupId: UUID,
        firstName: String,
        secondName: String? = nil,
        lastName: String,
        secondLastName: String? = nil,
        phone: String? = nil,
        email: String? = nil,
        isVisitor: Bool,
        isActive: Bool,
        createdAt: Date,
        updatedAt: Date
    ) {
        self.id = id
        self.smallGroupId = smallGroupId
        self.firstName = firstName
        self.secondName = secondName
        self.lastName = lastName
        self.secondLastName = secondLastName
        self.phone = phone
        self.email = email
        self.isVisitor = isVisitor
        self.isActive = isActive
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try Self.decodeUUID(in: c, forKey: .id)

        if let sg = try Self.decodeUUIDIfPresent(in: c, forKey: .smallGroupId) {
            smallGroupId = sg
        } else if let lg = try Self.decodeUUIDIfPresent(in: c, forKey: .legacyGroupId) {
            smallGroupId = lg
        } else {
            throw DecodingError.keyNotFound(
                CodingKeys.smallGroupId,
                .init(codingPath: c.codingPath, debugDescription: "Expected small_group_id or group_id")
            )
        }

        firstName = try Self.decodeString(in: c, forKey: .firstName) ?? ""
        secondName = try Self.decodeString(in: c, forKey: .secondName)
        lastName = try Self.decodeString(in: c, forKey: .lastName) ?? ""
        secondLastName = try Self.decodeString(in: c, forKey: .secondLastName)
        phone = try Self.decodeString(in: c, forKey: .phone)
        email = try Self.decodeString(in: c, forKey: .email)
        isVisitor = Self.decodeBool(in: c, forKey: .isVisitor) ?? false
        isActive = Self.decodeBool(in: c, forKey: .isActive) ?? true
        let fallbackDate = Date(timeIntervalSince1970: 0)
        createdAt = Self.decodeDate(in: c, forKey: .createdAt) ?? fallbackDate
        updatedAt = Self.decodeDate(in: c, forKey: .updatedAt) ?? fallbackDate
    }

    // MARK: - Flexible JSON (PostgREST often returns timestamps the default .iso8601 decoder rejects)

    private static func decodeUUID(in c: KeyedDecodingContainer<CodingKeys>, forKey key: CodingKeys) throws -> UUID {
        if let u = try? c.decode(UUID.self, forKey: key) { return u }
        if let s = try? c.decode(String.self, forKey: key), let u = UUID(uuidString: s) { return u }
        throw DecodingError.typeMismatch(
            UUID.self,
            .init(codingPath: c.codingPath + [key], debugDescription: "Expected UUID string or object")
        )
    }

    private static func decodeUUIDIfPresent(in c: KeyedDecodingContainer<CodingKeys>, forKey key: CodingKeys) throws -> UUID? {
        guard c.contains(key), try c.decodeNil(forKey: key) == false else { return nil }
        return try? decodeUUID(in: c, forKey: key)
    }

    private static func decodeString(in c: KeyedDecodingContainer<CodingKeys>, forKey key: CodingKeys) throws -> String? {
        if let s = try? c.decode(String.self, forKey: key) { return s }
        if let i = try? c.decode(Int.self, forKey: key) { return String(i) }
        if let d = try? c.decode(Double.self, forKey: key) {
            if d.rounded() == d { return String(Int(d)) }
            return String(d)
        }
        return nil
    }

    private static func decodeBool(in c: KeyedDecodingContainer<CodingKeys>, forKey key: CodingKeys) -> Bool? {
        if let b = try? c.decode(Bool.self, forKey: key) { return b }
        if let i = try? c.decode(Int.self, forKey: key) { return i != 0 }
        return nil
    }

    private static func decodeDate(in c: KeyedDecodingContainer<CodingKeys>, forKey key: CodingKeys) -> Date? {
        if let d = try? c.decode(Date.self, forKey: key) { return d }
        guard let s = try? c.decode(String.self, forKey: key) else { return nil }
        return parseSupabaseTimestamp(s)
    }

    /// Accepts ISO-8601 variants and common Postgres `timestamptz` strings.
    private static func parseSupabaseTimestamp(_ raw: String) -> Date? {
        let s = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !s.isEmpty else { return nil }

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

    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(smallGroupId, forKey: .smallGroupId)
        try c.encode(firstName, forKey: .firstName)
        try c.encodeIfPresent(secondName, forKey: .secondName)
        try c.encode(lastName, forKey: .lastName)
        try c.encodeIfPresent(secondLastName, forKey: .secondLastName)
        try c.encodeIfPresent(phone, forKey: .phone)
        try c.encodeIfPresent(email, forKey: .email)
        try c.encode(isVisitor, forKey: .isVisitor)
        try c.encode(isActive, forKey: .isActive)
        try c.encode(createdAt, forKey: .createdAt)
        try c.encode(updatedAt, forKey: .updatedAt)
    }

    var fullName: String {
        [firstName, secondName, lastName, secondLastName]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
    }

    var displayName: String {
        "\(firstName) \(lastName)"
    }

    var initials: String {
        "\(firstName.prefix(1))\(lastName.prefix(1))".uppercased()
    }
}
