import Foundation

/// `hylla://device/HYL-003` opens that device. The path is a shelf tag, read as loosely as a
/// typed one, so a link written by hand still works. Anything else is not a Hylla link.
enum DeepLink {
    static let scheme = "hylla"
    static let deviceHost = "device"

    static func device(_ id: Device.ID) -> URL {
        URL(string: "\(scheme)://\(deviceHost)/\(id.rawValue)")!
    }

    static func parse(_ url: URL) -> Device.ID? {
        guard url.scheme?.lowercased() == scheme, url.host()?.lowercased() == deviceHost else { return nil }
        let segments = url.pathComponents.filter { $0 != "/" }
        guard segments.count == 1 else { return nil }
        return ShelfTag.parse(segments[0])
    }
}
