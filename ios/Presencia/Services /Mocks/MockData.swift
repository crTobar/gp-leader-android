import Foundation

// MARK: - Mock Data
// Realistic Spanish-language test data for SwiftUI Previews.

enum MockData {

    // MARK: - Fixed IDs

    static let unionId = UUID(uuidString: "00000000-0000-0000-0000-000000000001")!
    static let campo1Id = UUID(uuidString: "00000000-0000-0000-0001-000000000001")!
    static let campo2Id = UUID(uuidString: "00000000-0000-0000-0001-000000000002")!
    static let district1Id = UUID(uuidString: "00000000-0000-0000-0002-000000000001")!
    static let district2Id = UUID(uuidString: "00000000-0000-0000-0002-000000000002")!
    static let church1Id = UUID(uuidString: "00000000-0000-0000-0003-000000000001")!
    static let church2Id = UUID(uuidString: "00000000-0000-0000-0003-000000000002")!
    static let group1Id = UUID(uuidString: "00000000-0000-0000-0004-000000000001")!
    static let profileId = UUID(uuidString: "00000000-0000-0000-0005-000000000001")!

    // MARK: - Organization Hierarchy

    static let union = UnionOrg(
        id: unionId, name: "Unión Colombiana del Norte",
        code: "UCN", createdAt: .distantPast, updatedAt: .distantPast
    )

    static let campos: [Campo] = [
        Campo(id: campo1Id, unionId: unionId, name: "Campo Atlántico", code: "CAT",
              createdAt: .distantPast, updatedAt: .distantPast),
        Campo(id: campo2Id, unionId: unionId, name: "Campo Bolívar", code: "CBL",
              createdAt: .distantPast, updatedAt: .distantPast)
    ]

    static let districts: [District] = [
        District(id: district1Id, campoId: campo1Id, name: "Distrito Norte",
                 createdAt: .distantPast, updatedAt: .distantPast),
        District(id: district2Id, campoId: campo1Id, name: "Distrito Sur",
                 createdAt: .distantPast, updatedAt: .distantPast)
    ]

    static let churches: [Church] = [
        Church(id: church1Id, districtId: district1Id, name: "Iglesia Central Barranquilla",
               code: "ICB", address: "Calle 50 #45-23, Barranquilla",
               createdAt: .distantPast, updatedAt: .distantPast),
        Church(id: church2Id, districtId: district2Id, name: "Iglesia El Prado",
               code: "IEP", address: "Carrera 54 #72-15, Barranquilla",
               createdAt: .distantPast, updatedAt: .distantPast)
    ]

    // 7:00 PM as a reference Date (only time component matters)
    private static let meetingTime7pm: Date = {
        var comps = DateComponents()
        comps.hour = 19; comps.minute = 0
        return Calendar.current.date(from: comps) ?? Date()
    }()

    static let groups: [SmallGroup] = [
        SmallGroup(
            id: group1Id, churchId: church1Id, name: "GP Los Olivos",
            meetingDay: "Miércoles", meetingTime: meetingTime7pm, isActive: true,
            createdAt: .distantPast, updatedAt: .distantPast,
            gpUsername: "ucn-cat-icb-gp-los-olivos",
            gpTempPassword: nil,
            gpPasswordSet: true,
            hymn: nil, favoriteVerse: nil, bibleChapter: nil, meetingPlace: nil
        ),
        SmallGroup(
            id: UUID(), churchId: church1Id, name: "GP Jóvenes en Acción",
            meetingDay: "Viernes", meetingTime: nil, isActive: true,
            createdAt: .distantPast, updatedAt: .distantPast,
            gpUsername: nil,
            gpTempPassword: nil,
            gpPasswordSet: false,
            hymn: nil, favoriteVerse: nil, bibleChapter: nil, meetingPlace: nil
        ),
        SmallGroup(
            id: UUID(), churchId: church1Id, name: "GP Luz y Verdad",
            meetingDay: "Sábado", meetingTime: nil, isActive: true,
            createdAt: .distantPast, updatedAt: .distantPast,
            gpUsername: nil,
            gpTempPassword: nil,
            gpPasswordSet: false,
            hymn: nil, favoriteVerse: nil, bibleChapter: nil, meetingPlace: nil
        )
    ]

    // MARK: - Current User

    static let currentProfile = Profile(
        id: profileId, firstName: "María", lastName: "González",
        email: "maria.gonzalez@example.com", phone: "+57 300 123 4567",
        avatarUrl: nil, isActive: true,
        createdAt: .distantPast, updatedAt: .distantPast
    )

    static let leaderRoleAssignment = RoleAssignment(
        id: UUID(), profileId: profileId, role: .leader,
        smallGroupId: group1Id, churchId: church1Id, districtId: district1Id,
        campoId: campo1Id, unionId: unionId, title: "Líder de GP",
        createdAt: .distantPast
    )

    // MARK: - Members (10 realistic Spanish names)

    static let members: [Member] = [
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Carlos", lastName: "Mendoza",
               phone: "+57 301 111 1111", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Ana", lastName: "Rodríguez",
               phone: "+57 302 222 2222", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Luis", lastName: "Herrera",
               phone: "+57 303 333 3333", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Carmen", lastName: "Torres",
               phone: nil, email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Javier", lastName: "Morales",
               phone: "+57 305 555 5555", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Rosa", lastName: "Vega",
               phone: nil, email: "rosa.vega@example.com", isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Diego", lastName: "Castro",
               phone: "+57 307 777 7777", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Patricia", lastName: "Ríos",
               phone: nil, email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Fernando", lastName: "Salcedo",
               phone: "+57 309 999 9999", email: nil, isVisitor: false, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Lucía", lastName: "Vargas",
               phone: nil, email: nil, isVisitor: true, isActive: true,
               createdAt: .distantPast, updatedAt: .distantPast),
        // Archived member
        Member(id: UUID(), smallGroupId: group1Id, firstName: "Miguel", lastName: "Peña",
               phone: "+57 311 000 0000", email: nil, isVisitor: false, isActive: false,
               createdAt: .distantPast, updatedAt: .distantPast)
    ]

    // MARK: - Activity Types

    static let activityTypes: [ActivityType] = [
        ActivityType(id: UUID(), name: "Estudios Bíblicos",
                     description: "Estudios bíblicos realizados en la semana",
                     icon: "book.fill", unitLabel: "estudios", scope: .global, level: .union,
                     campoId: nil, churchId: nil, isActive: true, sortOrder: 1,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast),
        ActivityType(id: UUID(), name: "Visitas Misioneras",
                     description: "Visitas realizadas a hogares o enfermos",
                     icon: "figure.walk", unitLabel: "visitas", scope: .global, level: .union,
                     campoId: nil, churchId: nil, isActive: true, sortOrder: 2,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast),
        ActivityType(id: UUID(), name: "Publicaciones",
                     description: "Publicaciones distribuidas",
                     icon: "newspaper.fill", unitLabel: "publicaciones", scope: .global, level: .pastor,
                     campoId: nil, churchId: nil, isActive: true, sortOrder: 3,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast),
        ActivityType(id: UUID(), name: "Ofrendas Misioneras",
                     description: "Ofrendas misioneras recolectadas",
                     icon: "dollarsign.circle.fill", unitLabel: "ofrendas", scope: .global, level: .pastor,
                     campoId: nil, churchId: nil, isActive: true, sortOrder: 4,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast),
        ActivityType(id: UUID(), name: "Parejas de Oración",
                     description: "Parejas de oración activas",
                     icon: "hands.sparkles.fill", unitLabel: "parejas", scope: .global, level: .myGroup,
                     campoId: nil, churchId: nil, isActive: true, sortOrder: 5,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast),
        ActivityType(id: UUID(), name: "Bautismos Planificados",
                     description: "Candidatos al bautismo en preparación",
                     icon: "drop.fill", unitLabel: "candidatos", scope: .campo, level: .myGroup,
                     campoId: campo1Id, churchId: nil, isActive: true, sortOrder: 6,
                     createdBy: nil, createdAt: .distantPast, updatedAt: .distantPast)
    ]

    // MARK: - Fixed meeting IDs for attendance lookup
    static let meetingIds: [UUID] = (0..<12).map { _ in UUID() }

    // MARK: - Meetings (12 weeks across ~3 months)

    static var meetings: [Meeting] {
        let calendar = Calendar.current
        let statusPattern: [MeetingStatus] = [
            .draft, .submitted, .submitted, .submitted,
            .submitted, .submitted, .submitted, .submitted,
            .submitted, .submitted, .submitted, .submitted
        ]
        let notePattern: [String?] = [
            nil, "Oración especial por las familias", nil, nil,
            "3 visitantes nuevos", nil, nil, "Estudio sobre Filipenses 4",
            nil, nil, "Reunión de planificación trimestral", nil
        ]
        return (0..<12).map { weekOffset in
            let date = calendar.date(byAdding: .weekOfYear, value: -weekOffset, to: Date()) ?? Date()
            let status = statusPattern[weekOffset]
            return Meeting(
                id: meetingIds[weekOffset],
                smallGroupId: group1Id,
                meetingDate: date,
                status: status,
                submittedAt: status == .submitted ? date : nil,
                submittedBy: status == .submitted ? profileId : nil,
                notes: notePattern[weekOffset],
                createdAt: date,
                updatedAt: date
            )
        }
    }

    // MARK: - Attendance per meeting (varied per week)

    // Each array: attendance statuses for the 10 active members
    private static let attendancePatterns: [[AttendanceStatus]] = [
        [.absent, .present, .present, .present, .present, .present, .present, .present, .absent, .present],    // week 0 draft
        [.present, .present, .present, .present, .present, .present, .absent, .justified, .present, .present], // week 1
        [.present, .absent, .present, .present, .present, .present, .present, .present, .present, .absent],    // week 2
        [.present, .present, .present, .absent, .present, .present, .present, .present, .justified, .present], // week 3
        [.present, .present, .absent, .present, .present, .absent, .present, .present, .present, .present],    // week 4
        [.present, .present, .present, .present, .present, .present, .present, .absent, .present, .present],   // week 5
        [.absent, .present, .present, .present, .justified, .present, .present, .present, .present, .present], // week 6
        [.present, .present, .present, .present, .present, .absent, .present, .present, .present, .present],   // week 7
        [.present, .absent, .present, .present, .present, .present, .present, .present, .absent, .present],    // week 8
        [.present, .present, .present, .present, .present, .present, .present, .justified, .present, .present],// week 9
        [.present, .present, .absent, .present, .present, .present, .present, .present, .present, .present],   // week 10
        [.present, .present, .present, .present, .absent, .present, .present, .present, .present, .present],   // week 11
    ]

    static func attendance(for meeting: Meeting) -> [Attendance] {
        let pattern: [AttendanceStatus]
        if let idx = meetingIds.firstIndex(of: meeting.id) {
            pattern = attendancePatterns[idx % attendancePatterns.count]
        } else {
            pattern = [.present, .present, .present, .present, .present,
                       .present, .present, .justified, .absent, .absent]
        }
        let activeMembers = members.filter { $0.isActive }
        return activeMembers.enumerated().map { index, member in
            Attendance(
                id: UUID(), meetingId: meeting.id, memberId: member.id,
                status: pattern[index % pattern.count],
                note: nil
            )
        }
    }

    // MARK: - Attendance Trend (8 weeks)

    static let attendanceTrend: [WeeklyAttendance] = {
        let calendar = Calendar.current
        let presentCounts = [7, 8, 6, 9, 8, 7, 9, 8]
        return (0..<8).reversed().enumerated().map { index, weekOffset in
            let weekStart = calendar.date(byAdding: .weekOfYear, value: -weekOffset, to: Date()) ?? Date()
            return WeeklyAttendance(
                weekStart: weekStart,
                presentCount: presentCounts[index],
                totalCount: 10
            )
        }
    }()

    // MARK: - Activity Records

    static func activityRecords(for meeting: Meeting) -> [ActivityRecord] {
        let counts = [4, 6, 3, 2, 5]
        return activityTypes.prefix(5).enumerated().map { index, type in
            ActivityRecord(
                id: UUID(), meetingId: meeting.id,
                activityTypeId: type.id,
                count: counts[index],
                notes: nil
            )
        }
    }
}
