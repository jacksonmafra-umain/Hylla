import Foundation

/// Shelf tags are `HYL-` and three digits. People type them loosely; the scanner does not.
enum ShelfTag {
    /// `HYL-002`, `hyl-2`, `HYL2` and `2` all mean `HYL-002`. Anything else is `nil`.
    static func parse(_ text: String) -> Device.ID? {
        let pattern = /^(?:HYL-?)?0*(\d{1,3})$/
        guard let match = text.trimmingCharacters(in: .whitespaces).uppercased().wholeMatch(of: pattern),
              let number = Int(match.output.1) else { return nil }
        return Device.ID(rawValue: String(format: "HYL-%03d", number))
    }
}
