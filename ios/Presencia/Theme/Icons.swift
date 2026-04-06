import SwiftUI
import UIKit
import LucideIcons

// MARK: - App Icon Namespace
// All app icons sourced from LucideIcons. Prefer `LucideIcon` or `Image(uiImage:).renderingMode(.template)`.

enum AppIcon {
    // Navigation / Tabs
    static let home           = Lucide.house
    static let meetings       = Lucide.clipboardList
    static let members        = Lucide.users
    static let more           = Lucide.circleEllipsis
    static let chevronLeft    = Lucide.chevronLeft

    // Auth
    static let email          = Lucide.mail
    static let password       = Lucide.lock
    static let login          = Lucide.circleArrowRight
    static let signup         = Lucide.userPlus
    static let eye            = Lucide.eye
    static let eyeOff         = Lucide.eyeOff

    // Actions
    static let check          = Lucide.check
    static let checkCircle    = Lucide.circleCheckBig
    static let plus           = Lucide.plus
    static let plusCircle     = Lucide.circlePlus
    static let minus          = Lucide.minus
    static let send           = Lucide.sendHorizontal
    static let save           = Lucide.save
    static let arrowRight     = Lucide.arrowRight
    static let arrowLeft      = Lucide.arrowLeft
    static let chevronRight   = Lucide.chevronRight
    static let chevronDown    = Lucide.chevronDown
    static let chevronUp      = Lucide.chevronUp
    static let archive        = Lucide.archive
    static let archiveRestore = Lucide.archiveRestore
    static let share          = Lucide.share2
    static let clock          = Lucide.clock
    static let edit           = Lucide.pencil
    static let delete         = Lucide.trash
    static let close          = Lucide.x
    static let search         = Lucide.search
    static let logout         = Lucide.logOut

    // Features
    static let calendar       = Lucide.calendar
    static let calendarClock  = Lucide.calendarClock
    static let reports        = Lucide.clipboardList
    static let settings       = Lucide.settings
    static let activities     = Lucide.listChecks
    static let admin          = Lucide.shieldCheck
    static let book           = Lucide.bookOpen
    static let inbox          = Lucide.inbox

    // People
    static let user           = Lucide.user
    static let userCircle     = Lucide.circleUserRound
    static let userPlus       = Lucide.userPlus
    static let userKey        = Lucide.userKey
    static let userClock      = Lucide.userCheck
    static let usersGroup     = Lucide.users

    // Content
    static let file           = Lucide.fileText
    static let note           = Lucide.notepadText
    static let newspaper      = Lucide.newspaper
    static let star           = Lucide.star
    static let tag            = Lucide.tag
    static let info           = Lucide.info

    // Places / Contact
    static let building       = Lucide.building
    static let map            = Lucide.map
    static let mapPin         = Lucide.mapPin
    static let phone          = Lucide.phone
    static let hash           = Lucide.hash
    static let person         = Lucide.personStanding
    static let wifi           = Lucide.wifi
    static let wifiOff        = Lucide.wifiOff
    static let locked         = Lucide.lock
    static let unlock         = Lucide.lockOpen
    static let personWalking  = Lucide.personStanding
    static let sparkles       = Lucide.sparkles
    static let refresh        = Lucide.refreshCw
    static let xCircle        = Lucide.circleX
    static let key            = Lucide.keyRound
}

// MARK: - SwiftUI template wrapper

/// Lucide `UIImage` as a template icon at a fixed point size.
struct LucideIcon: View {
    let uiImage: UIImage
    var size: CGFloat = 20

    var body: some View {
        Image(uiImage: uiImage)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: size, height: size)
    }
}
