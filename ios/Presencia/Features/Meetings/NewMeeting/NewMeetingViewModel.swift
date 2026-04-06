import Foundation
import Observation

// MARK: - Extra Activity (custom, added by leader during registration)

struct ExtraActivity: Identifiable {
    let id = UUID()
    var name: String
    var count: Int
    var unitLabel: String
}

// MARK: - ViewModel

@Observable
final class NewMeetingViewModel {
    var currentStep: Int = 0
    var meetingDate: Date = Date()
    var notes: String = ""
    var noMeetingToday: Bool = false
    var attendanceMap: [UUID: AttendanceStatus] = [:]
    var activityCounts: [UUID: Int?] = [:]   // nil = "—" (not entered)
    var activityNotes: [UUID: String] = [:]
    var members: [Member] = []
    var activityTypes: [ActivityType] = []
    var visitorsForThisMeeting: [Member] = []
    var extraActivities: [ExtraActivity] = []
    /// Starts true so the sheet never flashes an empty asistencia step before network calls begin.
    var isLoading: Bool = true
    /// Group display name for summary card (Kotlin `paso3_label_grupo_header`).
    var groupName: String = ""
    var isSubmitting: Bool = false
    var isSubmitted: Bool = false
    var showAllAbsentAlert: Bool = false
    var errorMessage: String? = nil

    /// When set, `load()` hydrates from this draft and `save` updates instead of inserting a new meeting.
    var editingMeeting: Meeting?

    private let services: ServiceContainer
    let groupId: UUID
    let profileId: UUID

    /// Stable attendance row ids (edit mode + upsert). Activity row ids by type for upsert.
    private var attendanceRowIdsByMemberId: [UUID: UUID] = [:]
    private var activityRowIdsByTypeId: [UUID: UUID] = [:]

    init(services: ServiceContainer, groupId: UUID, profileId: UUID, editingMeeting: Meeting? = nil) {
        self.services = services
        self.groupId = groupId
        self.profileId = profileId
        self.editingMeeting = editingMeeting
    }

    // MARK: - Computed

    var regularMembers: [Member]  { members.filter { !$0.isVisitor } }
    var priorVisitors: [Member]   { members.filter { $0.isVisitor } }

    var allMeetingMembers: [Member] {
        regularMembers + visitorsForThisMeeting
    }

    var presentCount: Int   { attendanceMap.values.filter { $0 == .present }.count }
    var absentCount: Int    { attendanceMap.values.filter { $0 == .absent }.count }
    var justifiedCount: Int { attendanceMap.values.filter { $0 == .justified }.count }
    var totalCount: Int     { allMeetingMembers.count }
    var attendanceRate: Double { totalCount > 0 ? Double(presentCount) / Double(totalCount) : 0 }

    var allAbsent: Bool {
        !regularMembers.isEmpty && regularMembers.allSatisfy { attendanceMap[$0.id] == .absent }
            && visitorsForThisMeeting.isEmpty
    }

    // MARK: - Load

    @MainActor
    func load() async {
        isLoading = true
        attendanceRowIdsByMemberId = [:]
        activityRowIdsByTypeId = [:]
        attendanceMap = [:]
        activityNotes = [:]
        visitorsForThisMeeting = []
        do {
            if let group = try? await services.group.getGroup(id: groupId) {
                groupName = group.name
            }
            let allActive = try await services.members.getMembers(groupId: groupId)
            members = allActive
            activityTypes = try await services.activities.getActivityTypes(
                scope: nil, campoId: nil, churchId: nil
            )

            if var draft = editingMeeting {
                if let fresh = try? await services.meetings.getMeeting(id: draft.id) {
                    draft = fresh
                }
                editingMeeting = draft
                meetingDate = draft.meetingDate
                if let n = draft.notes {
                    noMeetingToday = n.contains("Sin reunión.")
                    var rest = n.replacingOccurrences(of: "Sin reunión.", with: "")
                    rest = rest.trimmingCharacters(in: .whitespacesAndNewlines)
                    notes = rest
                } else {
                    noMeetingToday = false
                    notes = ""
                }

                let att = try await services.attendance.getAttendance(meetingId: draft.id)
                attendanceRowIdsByMemberId = Dictionary(uniqueKeysWithValues: att.map { ($0.memberId, $0.id) })
                for row in att {
                    attendanceMap[row.memberId] = row.status
                }
                for member in regularMembers where attendanceMap[member.id] == nil {
                    attendanceMap[member.id] = .absent
                }
                visitorsForThisMeeting = members.filter { m in
                    m.isVisitor && att.contains(where: { $0.memberId == m.id })
                }

                let records = try await services.activities.getActivityRecords(meetingId: draft.id)
                activityRowIdsByTypeId = Dictionary(uniqueKeysWithValues: records.map { ($0.activityTypeId, $0.id) })
                for type in activityTypes {
                    activityCounts[type.id] = nil
                }
                for record in records {
                    activityCounts[record.activityTypeId] = record.count
                    if let n = record.notes { activityNotes[record.activityTypeId] = n }
                }
            } else {
                for member in regularMembers {
                    attendanceMap[member.id] = .absent
                }
                for type in activityTypes {
                    activityCounts[type.id] = nil
                }
            }
        } catch {
            errorMessage = "Error al cargar los datos."
        }
        isLoading = false
    }

    // MARK: - Attendance

    func setAllPresent() {
        setAllStatus(.present)
    }

    func setAllStatus(_ status: AttendanceStatus) {
        for member in regularMembers {
            attendanceMap[member.id] = status
        }
        HapticFeedback.medium()
    }

    func cycleAttendance(memberId: UUID) {
        let current = attendanceMap[memberId] ?? .absent
        attendanceMap[memberId] = current.next
        HapticFeedback.medium()
    }

    // MARK: - Visitors

    func addVisitorToMeeting(_ visitor: Member) {
        guard !visitorsForThisMeeting.contains(where: { $0.id == visitor.id }) else { return }
        visitorsForThisMeeting.append(visitor)
        attendanceMap[visitor.id] = .present
        HapticFeedback.light()
    }

    @MainActor
    func createAndAddVisitor(firstName: String, lastName: String) async {
        let visitor = Member(
            id: UUID(), smallGroupId: groupId,
            firstName: firstName, lastName: lastName,
            isVisitor: true, isActive: true,
            createdAt: Date(), updatedAt: Date()
        )
        do {
            let saved = try await services.members.createMember(visitor)
            visitorsForThisMeeting.append(saved)
            attendanceMap[saved.id] = .present
            HapticFeedback.success()
        } catch {
            errorMessage = "No se pudo agregar el visitante."
        }
    }

    func removeVisitorFromMeeting(_ visitor: Member) {
        visitorsForThisMeeting.removeAll { $0.id == visitor.id }
        attendanceMap.removeValue(forKey: visitor.id)
    }

    // MARK: - Activities

    /// First tap from empty matches Kotlin `ContadorGrande` (+ from nil → 0).
    func increment(typeId: UUID) {
        if activityCounts[typeId].flatMap({ $0 }) == nil {
            activityCounts[typeId] = 0
        } else if let c = activityCounts[typeId].flatMap({ $0 }) {
            activityCounts[typeId] = c + 1
        }
    }

    func decrement(typeId: UUID) {
        guard let current = activityCounts[typeId].flatMap({ $0 }) else {
            activityCounts[typeId] = nil
            return
        }
        if current <= 1 {
            activityCounts[typeId] = nil
        } else {
            activityCounts[typeId] = current - 1
        }
    }

    func addExtraActivity(name: String, unitLabel: String, count: Int) {
        extraActivities.append(ExtraActivity(name: name, count: count, unitLabel: unitLabel))
        HapticFeedback.success()
    }

    func removeExtraActivity(id: UUID) {
        extraActivities.removeAll { $0.id == id }
    }

    // MARK: - Navigation

    func nextStep() {
        if currentStep == 0 && noMeetingToday {
            currentStep = 2
            return
        }
        if currentStep == 0 && allAbsent {
            showAllAbsentAlert = true
            return
        }
        if currentStep < 2 { currentStep += 1 }
    }

    func confirmNextStepDespiteAllAbsent() {
        showAllAbsentAlert = false
        if currentStep < 2 { currentStep += 1 }
    }

    func prevStep() {
        if currentStep == 2 && noMeetingToday {
            currentStep = 0
            return
        }
        if currentStep > 0 { currentStep -= 1 }
    }

    // MARK: - Save / Submit

    @MainActor
    func saveDraft() async {
        await save(submit: false)
    }

    @MainActor
    func submit() async {
        await save(submit: true)
    }

    @MainActor
    private func save(submit: Bool) async {
        isSubmitting = true
        do {
            let meetingNotes: String? = {
                var parts: [String] = []
                if noMeetingToday { parts.append("Sin reunión.") }
                if !notes.isEmpty { parts.append(notes) }
                return parts.isEmpty ? nil : parts.joined(separator: " ")
            }()

            // Guard: block duplicate submissions for the same date (new meetings only)
            if submit && editingMeeting == nil {
                let cal = Calendar.current
                let startOfDay = cal.startOfDay(for: meetingDate)
                let endOfDay   = cal.date(byAdding: .day, value: 1, to: startOfDay)!.addingTimeInterval(-1)
                let existing   = (try? await services.meetings.getMeetings(groupId: groupId, dateRange: startOfDay...endOfDay)) ?? []
                if existing.contains(where: { $0.status == .submitted }) {
                    errorMessage = "Alguien ya envió una reunión para esta fecha. No es posible enviar dos reuniones el mismo día."
                    isSubmitting = false
                    return
                }
            }

            let skipExtraActivities = editingMeeting != nil
            let meetingId: UUID
            if var base = editingMeeting {
                base.meetingDate = meetingDate
                base.notes = meetingNotes
                base.updatedAt = Date()
                if submit {
                    base.status = .submitted
                    base.submittedAt = Date()
                    base.submittedBy = profileId
                } else {
                    base.status = .draft
                    base.submittedAt = nil
                    base.submittedBy = nil
                }
                let saved = try await services.meetings.updateMeeting(base)
                editingMeeting = saved
                meetingId = saved.id
            } else {
                let meeting = Meeting(
                    id: UUID(), smallGroupId: groupId,
                    meetingDate: meetingDate,
                    status: submit ? .submitted : .draft,
                    submittedAt: submit ? Date() : nil,
                    submittedBy: submit ? profileId : nil,
                    notes: meetingNotes,
                    createdAt: Date(), updatedAt: Date()
                )
                let created = try await services.meetings.createMeeting(meeting)
                meetingId = created.id
            }

            let attendanceRecords: [Attendance] = allMeetingMembers.map { member in
                let rowId = attendanceRowIdsByMemberId[member.id] ?? UUID()
                return Attendance(
                    id: rowId,
                    meetingId: meetingId,
                    memberId: member.id,
                    status: attendanceMap[member.id] ?? .absent,
                    note: nil
                )
            }
            try await services.attendance.batchUpdateAttendance(attendanceRecords)

            for (typeId, countOpt) in activityCounts {
                guard let count = countOpt else { continue }
                let recordId = activityRowIdsByTypeId[typeId] ?? UUID()
                let record = ActivityRecord(
                    id: recordId,
                    meetingId: meetingId,
                    activityTypeId: typeId,
                    count: count,
                    notes: activityNotes[typeId]
                )
                try await services.activities.upsertActivityRecord(record)
            }

            if !skipExtraActivities {
                for extra in extraActivities where extra.count > 0 {
                    let customType = ActivityType(
                        id: UUID(), name: extra.name, description: nil,
                        icon: nil, unitLabel: extra.unitLabel,
                        scope: .church, level: .myGroup,
                        campoId: nil, churchId: nil,
                        isActive: true, sortOrder: 99,
                        createdBy: profileId,
                        createdAt: Date(), updatedAt: Date()
                    )
                    let savedType = try await services.activities.createActivityType(customType)
                    let record = ActivityRecord(
                        id: UUID(), meetingId: meetingId,
                        activityTypeId: savedType.id, count: extra.count, notes: nil
                    )
                    try await services.activities.upsertActivityRecord(record)
                }
            }

            if submit {
                try await services.meetings.submitMeeting(id: meetingId, submittedBy: profileId)
                let dateLabel = DateFormatters.formatShortDate(meetingDate)
                try? await services.activityLog.log(
                    profileId: profileId,
                    groupId: groupId,
                    actionType: "meeting_submitted",
                    description: "Reunión del \(dateLabel) enviada"
                )
                HapticFeedback.success()
                isSubmitted = true
            }
        } catch {
            errorMessage = "No se pudo guardar la reunión."
        }
        isSubmitting = false
    }
}
