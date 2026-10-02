import Foundation

/// What the running device says about itself. A plain value, so drafts are unit tested.
struct BuildInfo: Sendable {
    /// `UIDevice.model`, such as "iPhone" or "iPad".
    var model: String
    /// The hardware identifier, such as "iPhone18,1".
    var machine: String
    var systemVersion: String
    var isPad: Bool
}

/// A new record drafted from the device the app is running on. The posture model the codelab
/// teaches describes the very device being added.
enum Registration {
    static func draft(_ build: BuildInfo, posture: WindowPosture, fleet: Fleet, today: CalendarDate) -> Device {
        Device(
            id: nextTag(fleet),
            deviceName: "\(build.model) \(build.machine)",
            deviceType: build.isPad ? .tablet : .phone,
            platform: build.isPad ? .iPadOS : .iOS,
            modelName: build.model,
            modelNumber: build.machine,
            osVersion: build.systemVersion,
            uiVersion: nil,
            assignmentStatus: .available,
            currentUser: nil,
            since: today,
            homeUse: .approved,
            lifecycle: .inUse,
            notes: windowNote(posture)
        )
    }

    /// The first shelf tag not taken, `HYL-020` after `HYL-019`.
    static func nextTag(_ fleet: Fleet) -> Device.ID {
        let taken = Set(fleet.devices.compactMap { Int($0.id.rawValue.replacing("HYL-", with: "")) })
        let next = (1...).first { !taken.contains($0) }!
        return Device.ID(rawValue: String(format: "HYL-%03d", next))
    }

    /// The window as the posture model saw it at registration, in words.
    static func windowNote(_ posture: WindowPosture) -> String {
        let width = Int(posture.size.width.rounded()), height = Int(posture.size.height.rounded())
        var note = "Registered from the device. Window \(width) × \(height) pt, \(posture.widthClass) × \(posture.heightClass), posture \(posture.posture)"
        note += posture.folds.isEmpty ? ", no folds." : ", \(posture.folds.count) fold(s)."
        return note
    }
}

extension FleetStore {
    /// Adds a new device to the fleet. Throws if its shelf tag is taken or it breaks the rules.
    func register(_ device: Device) throws(Failure) {
        var next = fleet
        next.devices.append(device)
        let problems = FleetFixture.validate(next)
        guard problems.isEmpty else { throw .invalid(problems) }
        commit(next, .register(device: device.id))
    }
}
