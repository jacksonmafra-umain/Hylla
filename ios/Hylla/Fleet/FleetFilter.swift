import Foundation

/// Which devices the fleet shows. Empty sets mean "any".
struct FleetFilter: Codable, Hashable, Sendable {
    var platforms: Set<Platform> = []
    var type: DeviceType?
    var statuses: Set<AssignmentStatus> = []
    /// Only devices someone could take right now: available and still in service.
    var claimableOnly = false

    /// How many separate conditions are set, for the "Filters (n)" label.
    var activeCount: Int {
        [!platforms.isEmpty, type != nil, !statuses.isEmpty, claimableOnly].filter { $0 }.count
    }

    func matches(_ device: Device) -> Bool {
        (platforms.isEmpty || platforms.contains(device.platform))
            && (type == nil || device.deviceType == type)
            && (statuses.isEmpty || statuses.contains(device.assignmentStatus))
            && (!claimableOnly || device.isClaimable)
    }
}

extension Device {
    var isClaimable: Bool { assignmentStatus == .available && lifecycle == .inUse }
}
