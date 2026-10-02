import UIKit
import UserNotifications

/// Receives notification taps and actions. A tap opens the device's link, the same path as any
/// other link; Return returns the device without opening the app.
final class NotificationDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    /// Set by the app once its store exists.
    @MainActor var store: FleetStore?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions options: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        let center = UNUserNotificationCenter.current()
        center.delegate = self
        center.setNotificationCategories(Notifier.categories)
        return true
    }

    /// Shown while Hylla is in front too: a returned device is worth seeing then as well.
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }

    // Nonisolated: the notification objects are not Sendable, so read what is needed here and
    // hop to the main actor with plain values.
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        guard let link = response.notification.request.content.userInfo[Notifier.linkKey] as? String,
              let url = URL(string: link), let id = DeepLink.parse(url) else { return }
        let returning = response.actionIdentifier == Notifier.returnAction
        await MainActor.run {
            if returning {
                _ = try? store?.returnDevice(id)
            } else {
                UIApplication.shared.open(url)
            }
        }
    }
}
