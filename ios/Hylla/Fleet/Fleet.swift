import Foundation

/// The whole QA fleet as read from `fixtures/devices.json`.
struct Fleet: Codable, Hashable, Sendable {
    var schemaVersion: Int
    var people: [Person]
    var devices: [Device]

    func person(_ id: Person.ID) -> Person? {
        people.first { $0.id == id }
    }
}

struct Person: Codable, Hashable, Identifiable, Sendable {
    struct ID: RawRepresentable, Codable, Hashable, Sendable {
        var rawValue: String
    }

    var id: ID
    var name: String
}

struct Device: Codable, Hashable, Identifiable, Sendable {
    /// Shelf tag, `HYL-NNN`. The value the scanner reads.
    struct ID: RawRepresentable, Codable, Hashable, Sendable {
        var rawValue: String
    }

    var id: ID
    var deviceName: String
    var deviceType: DeviceType
    var platform: Platform
    var modelName: String
    var modelNumber: String
    var osVersion: String
    /// Manufacturer skin such as One UI or HyperOS. `nil` when the platform has none.
    var uiVersion: String?
    var assignmentStatus: AssignmentStatus
    var currentUser: Person.ID?
    /// In use or available since. A calendar date with no time or zone.
    var since: CalendarDate
    var homeUse: HomeUse
    var lifecycle: Lifecycle
    var notes: String?

    enum CodingKeys: String, CodingKey, CaseIterable {
        case id, deviceName, deviceType, platform, modelName, modelNumber, osVersion, uiVersion
        case assignmentStatus, currentUser, since, homeUse, lifecycle, notes
    }
}

enum DeviceType: String, Codable, CaseIterable, Sendable {
    case phone, tablet, headset, desktop
}

enum Platform: String, Codable, CaseIterable, Sendable {
    case iOS = "ios"
    case iPadOS = "ipados"
    case android
    case visionOS = "visionos"
    case macOS = "macos"
    case carPlay = "carplay"
}

enum AssignmentStatus: String, Codable, CaseIterable, Sendable {
    case inUse, available, missing, needsSorting
}

enum HomeUse: String, Codable, CaseIterable, Sendable {
    case approved, officeOnly, notApproved
}

enum Lifecycle: String, Codable, CaseIterable, Sendable {
    case inUse, decommission
}
