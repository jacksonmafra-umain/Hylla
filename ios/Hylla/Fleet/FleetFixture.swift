import Foundation

/// Parses and validates the shared fixture.
enum FleetFixture {
    static let schemaVersion = 1

    enum Failure: Error, Equatable {
        /// A device has keys the model does not know, or lacks keys it requires.
        case keys(device: Int, unknown: [String], missing: [String])
        /// An assignment has keys the model does not know, or lacks keys it requires.
        case assignmentKeys(assignment: Int, unknown: [String], missing: [String])
        case invalid([String])
    }

    /// Strict on purpose. `Codable` ignores unknown keys and treats a missing optional as `nil`;
    /// both are checked here so a field that must never be modelled (serial number, IMEI, EID,
    /// UDID) cannot slip into the fixture unnoticed.
    static func decode(_ data: Data) throws -> Fleet {
        try checkKeys(data)
        let fleet = try JSONDecoder().decode(Fleet.self, from: data)
        let problems = validate(fleet)
        guard problems.isEmpty else { throw Failure.invalid(problems) }
        return fleet
    }

    static func load(from bundle: Bundle = .main) throws -> Fleet {
        guard let url = bundle.url(forResource: "devices", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        return try decode(Data(contentsOf: url))
    }

    static func validate(_ fleet: Fleet) -> [String] {
        var problems: [String] = []
        if fleet.schemaVersion != schemaVersion {
            problems.append("schemaVersion \(fleet.schemaVersion), expected \(schemaVersion)")
        }
        let counts = Dictionary(grouping: fleet.devices, by: \.id)
        for (id, devices) in counts where devices.count > 1 {
            problems.append("duplicate device id \(id.rawValue)")
        }
        let people = Set(fleet.people.map(\.id))
        for device in fleet.devices {
            let id = device.id.rawValue
            switch (device.assignmentStatus, device.currentUser) {
            case (.inUse, nil):
                problems.append("\(id) is in use without a current user")
            case (let status, .some) where status != .inUse:
                problems.append("\(id) has a current user but is \(status.rawValue)")
            case (_, let user?) where !people.contains(user):
                problems.append("\(id) references unknown person \(user.rawValue)")
            default:
                break
            }
        }
        problems += validateAssignments(fleet, people: people)
        return problems
    }

    private static func validateAssignments(_ fleet: Fleet, people: Set<Person.ID>) -> [String] {
        var problems: [String] = []
        let devices = Set(fleet.devices.map(\.id))
        for assignment in fleet.assignments {
            let id = assignment.deviceId.rawValue
            if !devices.contains(assignment.deviceId) { problems.append("assignment for unknown device \(id)") }
            if !people.contains(assignment.person) {
                problems.append("assignment on \(id) references unknown person \(assignment.person.rawValue)")
            }
            if let to = assignment.to, to < assignment.from { problems.append("assignment on \(id) ends before it starts") }
        }
        for (device, periods) in Dictionary(grouping: fleet.assignments, by: \.deviceId).sorted(by: { $0.key.rawValue < $1.key.rawValue }) {
            // Nobody holds a device twice at once: each period starts after the one before ends.
            let sorted = periods.sorted { $0.from < $1.from }
            let overlaps = zip(sorted, sorted.dropFirst()).contains { earlier, later in
                guard let end = earlier.to else { return true }
                return later.from < end
            }
            if overlaps { problems.append("assignments on \(device.rawValue) overlap") }
        }
        for device in fleet.devices {
            let open = fleet.assignments.filter { $0.deviceId == device.id && $0.to == nil }
            let id = device.id.rawValue
            if device.assignmentStatus == .inUse {
                if open.count != 1 {
                    problems.append("\(id) is in use with \(open.count) open assignments, expected 1")
                } else if open[0].person != device.currentUser {
                    problems.append("\(id) open assignment is not held by its current user")
                } else if open[0].from != device.since {
                    problems.append("\(id) open assignment does not start on since")
                }
            } else if !open.isEmpty {
                problems.append("\(id) is \(device.assignmentStatus.rawValue) but has an open assignment")
            }
        }
        return problems
    }

    private static func checkKeys(_ data: Data) throws {
        let root = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        let deviceKeys = Set(Device.CodingKeys.allCases.map(\.rawValue))
        for (index, device) in (root?["devices"] as? [[String: Any]] ?? []).enumerated() {
            let (unknown, missing) = compare(Set(device.keys), deviceKeys)
            if !unknown.isEmpty || !missing.isEmpty {
                throw Failure.keys(device: index, unknown: unknown, missing: missing)
            }
        }
        let assignmentKeys = Set(Assignment.CodingKeys.allCases.map(\.rawValue))
        for (index, assignment) in (root?["assignments"] as? [[String: Any]] ?? []).enumerated() {
            let (unknown, missing) = compare(Set(assignment.keys), assignmentKeys)
            if !unknown.isEmpty || !missing.isEmpty {
                throw Failure.assignmentKeys(assignment: index, unknown: unknown, missing: missing)
            }
        }
    }

    private static func compare(_ keys: Set<String>, _ expected: Set<String>) -> (unknown: [String], missing: [String]) {
        (keys.subtracting(expected).sorted(), expected.subtracting(keys).sorted())
    }
}
