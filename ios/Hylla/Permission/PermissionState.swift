import AVFoundation
import UserNotifications

/// Where a runtime permission stands. The same four states as Android; iOS reports them directly,
/// so there is no "asked before" flag to keep.
enum PermissionState: Equatable, Sendable {
    /// Not asked yet: explain, then ask.
    case notAsked
    case granted
    /// Denied, or restricted by the device's management profile. Only Settings can change it.
    case blocked

    init(camera status: AVAuthorizationStatus) {
        self = switch status {
        case .authorized: .granted
        case .notDetermined: .notAsked
        default: .blocked
        }
    }

    init(notifications status: UNAuthorizationStatus) {
        self = switch status {
        case .authorized, .provisional, .ephemeral: .granted
        case .notDetermined: .notAsked
        default: .blocked
        }
    }
}
