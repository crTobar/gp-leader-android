import Foundation

// MARK: - Union

struct UnionOrg: Identifiable, Codable, Hashable {
    let id: UUID
    var name: String
    var code: String?
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - Campo

struct Campo: Identifiable, Codable, Hashable {
    let id: UUID
    let unionId: UUID
    var name: String
    var code: String?
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - District

struct District: Identifiable, Codable, Hashable {
    let id: UUID
    let campoId: UUID
    var name: String
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - Church

struct Church: Identifiable, Codable, Hashable {
    let id: UUID
    let districtId: UUID
    var name: String
    var code: String?
    var address: String?
    let createdAt: Date
    var updatedAt: Date
}

// MARK: - Small Group

struct SmallGroup: Identifiable, Codable, Hashable {
    let id: UUID
    let churchId: UUID
    var name: String
    var meetingDay: String?
    var meetingTime: Date?
    var isActive: Bool
    let createdAt: Date
    var updatedAt: Date
    /// Optional display / admin handle in `small_group`; login uses the hierarchy composite (see `LoginUsernameBuilder`), not this field alone.
    var gpUsername: String?
    var gpTempPassword: String?
    var gpPasswordSet: Bool?
    var hymn: String?
    var favoriteVerse: String?
    var bibleChapter: String?
    var meetingPlace: String?

    enum CodingKeys: String, CodingKey {
        case id
        case churchId
        case name
        case meetingDay
        case meetingTime
        case isActive
        case createdAt
        case updatedAt
        case gpUsername
        case gpTempPassword
        case gpPasswordSet
        case hymn
        case favoriteVerse
        case bibleChapter
        case meetingPlace
    }

    init(
        id: UUID,
        churchId: UUID,
        name: String,
        meetingDay: String? = nil,
        meetingTime: Date? = nil,
        isActive: Bool,
        createdAt: Date,
        updatedAt: Date,
        gpUsername: String? = nil,
        gpTempPassword: String? = nil,
        gpPasswordSet: Bool? = nil,
        hymn: String? = nil,
        favoriteVerse: String? = nil,
        bibleChapter: String? = nil,
        meetingPlace: String? = nil
    ) {
        self.id = id
        self.churchId = churchId
        self.name = name
        self.meetingDay = meetingDay
        self.meetingTime = meetingTime
        self.isActive = isActive
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.gpUsername = gpUsername
        self.gpTempPassword = gpTempPassword
        self.gpPasswordSet = gpPasswordSet
        self.hymn = hymn
        self.favoriteVerse = favoriteVerse
        self.bibleChapter = bibleChapter
        self.meetingPlace = meetingPlace
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(UUID.self, forKey: .id)
        churchId = try c.decode(UUID.self, forKey: .churchId)
        name = try c.decode(String.self, forKey: .name)
        meetingDay = try c.decodeIfPresent(String.self, forKey: .meetingDay)
        meetingTime = try Self.decodeMeetingTime(from: c)
        isActive = try c.decode(Bool.self, forKey: .isActive)
        createdAt = try c.decode(Date.self, forKey: .createdAt)
        updatedAt = try c.decode(Date.self, forKey: .updatedAt)
        gpUsername = try c.decodeIfPresent(String.self, forKey: .gpUsername)
        gpTempPassword = try c.decodeIfPresent(String.self, forKey: .gpTempPassword)
        gpPasswordSet = try c.decodeIfPresent(Bool.self, forKey: .gpPasswordSet)
        hymn = try c.decodeIfPresent(String.self, forKey: .hymn)
        favoriteVerse = try c.decodeIfPresent(String.self, forKey: .favoriteVerse)
        bibleChapter = try c.decodeIfPresent(String.self, forKey: .bibleChapter)
        meetingPlace = try c.decodeIfPresent(String.self, forKey: .meetingPlace)
    }

    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(churchId, forKey: .churchId)
        try c.encode(name, forKey: .name)
        try c.encodeIfPresent(meetingDay, forKey: .meetingDay)
        if let meetingTime {
            try c.encode(meetingTime, forKey: .meetingTime)
        } else {
            try c.encodeNil(forKey: .meetingTime)
        }
        try c.encode(isActive, forKey: .isActive)
        try c.encode(createdAt, forKey: .createdAt)
        try c.encode(updatedAt, forKey: .updatedAt)
        try c.encodeIfPresent(gpUsername, forKey: .gpUsername)
        try c.encodeIfPresent(gpTempPassword, forKey: .gpTempPassword)
        try c.encodeIfPresent(gpPasswordSet, forKey: .gpPasswordSet)
        try c.encodeIfPresent(hymn, forKey: .hymn)
        try c.encodeIfPresent(favoriteVerse, forKey: .favoriteVerse)
        try c.encodeIfPresent(bibleChapter, forKey: .bibleChapter)
        try c.encodeIfPresent(meetingPlace, forKey: .meetingPlace)
    }

    /// PostgreSQL `time without time zone` is JSON-encoded as `"HH:mm:ss"` (or with fractional seconds), not ISO8601 — the shared decoder would fail the whole row.
    private static func decodeMeetingTime(from c: KeyedDecodingContainer<CodingKeys>) throws -> Date? {
        guard c.contains(.meetingTime) else { return nil }
        if try c.decodeNil(forKey: .meetingTime) { return nil }
        if let string = try? c.decode(String.self, forKey: .meetingTime) {
            return dateFromPostgresTimeString(string)
        }
        return try? c.decode(Date.self, forKey: .meetingTime)
    }

    /// Maps a wall-clock time to a fixed calendar day so `Date` formatters in the UI stay stable.
    private static func dateFromPostgresTimeString(_ raw: String) -> Date? {
        let s = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !s.isEmpty else { return nil }
        if let iso = ISO8601DateFormatter().date(from: s) { return iso }

        let head = String(s.split(separator: ".").first ?? Substring(s))
        let parts = head.split(separator: ":")
        guard let hour = parts.first.flatMap({ Int($0) }) else { return nil }
        let minute = parts.count > 1 ? Int(parts[1]) ?? 0 : 0
        let second = parts.count > 2 ? Int(parts[2]) ?? 0 : 0

        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(secondsFromGMT: 0) ?? .gmt
        var dc = DateComponents()
        dc.year = 2000
        dc.month = 1
        dc.day = 1
        dc.hour = hour
        dc.minute = minute
        dc.second = second
        return cal.date(from: dc)
    }
}
