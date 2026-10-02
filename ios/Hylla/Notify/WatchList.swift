import Foundation

/// Devices the phone holder is waiting for, kept as a comma-separated `AppStorage` string.
enum WatchList {
    static func decode(_ raw: String) -> Set<Device.ID> {
        Set(raw.split(separator: ",").map { Device.ID(rawValue: String($0)) })
    }

    static func encode(_ ids: Set<Device.ID>) -> String {
        ids.map(\.rawValue).sorted().joined(separator: ",")
    }
}
