import Foundation

/// Parses and validates the shared fixture.
enum FleetFixture {
    static let schemaVersion = 1

    enum Failure: Error, Equatable {
        /// A device has keys the model does not know, or lacks keys it requires.
        case keys(device: Int, unknown: [String], missing: [String])
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
        return problems
    }

    private static func checkKeys(_ data: Data) throws {
        let root = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        let devices = root?["devices"] as? [[String: Any]] ?? []
        let expected = Set(Device.CodingKeys.allCases.map(\.rawValue))
        for (index, device) in devices.enumerated() {
            let keys = Set(device.keys)
            let unknown = keys.subtracting(expected).sorted()
            let missing = expected.subtracting(keys).sorted()
            if !unknown.isEmpty || !missing.isEmpty {
                throw Failure.keys(device: index, unknown: unknown, missing: missing)
            }
        }
    }
}
