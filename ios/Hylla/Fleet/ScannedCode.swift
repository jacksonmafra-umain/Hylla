import Foundation

/// A shelf tag's QR code holds its device link, `hylla://device/HYL-003`, so the camera, a link and
/// a typed tag all arrive at the same id. Older tags printed with the bare tag still scan.
enum ScannedCode {
    static func parse(_ raw: String) -> Device.ID? {
        if let url = URL(string: raw), let id = DeepLink.parse(url) { return id }
        return ShelfTag.parse(raw)
    }
}
