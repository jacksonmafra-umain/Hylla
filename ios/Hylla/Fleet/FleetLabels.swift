import Foundation

// Fixture codes become display text here and nowhere else.

extension Platform {
    var label: String {
        switch self {
        case .iOS: String(localized: "iOS")
        case .iPadOS: String(localized: "iPadOS")
        case .android: String(localized: "Android")
        case .visionOS: String(localized: "visionOS")
        case .macOS: String(localized: "macOS")
        case .carPlay: String(localized: "CarPlay")
        }
    }
}

extension DeviceType {
    var label: String {
        switch self {
        case .phone: String(localized: "Phone")
        case .tablet: String(localized: "Tablet")
        case .headset: String(localized: "Headset")
        case .desktop: String(localized: "Desktop")
        }
    }
}

extension AssignmentStatus {
    var label: String {
        switch self {
        case .inUse: String(localized: "In use")
        case .available: String(localized: "Available")
        case .missing: String(localized: "Missing")
        case .needsSorting: String(localized: "Needs sorting")
        }
    }
}

extension HomeUse {
    var label: String {
        switch self {
        case .approved: String(localized: "Approved")
        case .officeOnly: String(localized: "Office only")
        case .notApproved: String(localized: "Not approved")
        }
    }
}

extension Lifecycle {
    var label: String {
        switch self {
        case .inUse: String(localized: "In service")
        case .decommission: String(localized: "Decommission")
        }
    }
}

extension Device {
    /// "Available", or "In use · Alva Berg" when someone holds it.
    func statusText(in fleet: Fleet) -> String {
        guard let holder = currentUser.flatMap(fleet.person) else { return assignmentStatus.label }
        return "\(assignmentStatus.label) · \(holder.name)"
    }
}

extension CalendarDate {
    /// Midnight of this day in `calendar`'s time zone, for formatting only.
    func date(in calendar: Calendar = .current) -> Date? {
        calendar.date(from: DateComponents(year: year, month: month, day: day))
    }
}
