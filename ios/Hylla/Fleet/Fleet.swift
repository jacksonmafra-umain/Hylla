import Foundation

/// The whole QA fleet as read from `fixtures/devices.json`.
struct Fleet: Codable, Hashable, Sendable {
    var schemaVersion: Int
    var people: [Person]
    var devices: [Device]
    var assignments: [Assignment]

    func person(_ id: Person.ID) -> Person? {
        people.first { $0.id == id }
    }

    /// Who held `device`, newest first.
    func history(of device: Device.ID) -> [Assignment] {
        assignments.filter { $0.deviceId == device }.sorted { $0.from > $1.from }
    }
}

/// One period a person held a device. `to` is `nil` while it is still held.
struct Assignment: Codable, Hashable, Sendable {
    var deviceId: Device.ID
    var person: Person.ID
    var from: CalendarDate
    var to: CalendarDate?

    enum CodingKeys: String, CodingKey, CaseIterable {
        case deviceId, person, from, to
    }

    /// `to` is written as `null` while the device is held, never left out.
    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(deviceId, forKey: .deviceId)
        try container.encode(person, forKey: .person)
        try container.encode(from, forKey: .from)
        try container.encode(to, forKey: .to)
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

    /// Writes `null` for absent values instead of leaving the key out. The decoder requires every
    /// key (chapter 1), so a saved fleet must have them all, the same as the fixture.
    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(deviceName, forKey: .deviceName)
        try container.encode(deviceType, forKey: .deviceType)
        try container.encode(platform, forKey: .platform)
        try container.encode(modelName, forKey: .modelName)
        try container.encode(modelNumber, forKey: .modelNumber)
        try container.encode(osVersion, forKey: .osVersion)
        try container.encode(uiVersion, forKey: .uiVersion)
        try container.encode(assignmentStatus, forKey: .assignmentStatus)
        try container.encode(currentUser, forKey: .currentUser)
        try container.encode(since, forKey: .since)
        try container.encode(homeUse, forKey: .homeUse)
        try container.encode(lifecycle, forKey: .lifecycle)
        try container.encode(notes, forKey: .notes)
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
