import Foundation
import UserNotifications

/// The part of `UNUserNotificationCenter` the notifier uses, so tests can see what it schedules.
protocol NotificationScheduling: Sendable {
    func add(_ request: UNNotificationRequest) async throws
    func removePendingNotificationRequests(withIdentifiers identifiers: [String])
    func pendingNotificationRequests() async -> [UNNotificationRequest]
}

extension UNUserNotificationCenter: NotificationScheduling {}

/// Schedules Hylla's two notifications. Each carries its device's link, so a tap opens the device
/// through the same path as any other link.
struct Notifier: Sendable {
    static let overdueCategory = "overdue"
    static let returnAction = "return"
    static let linkKey = "link"

    let center: any NotificationScheduling

    /// One overdue notification per device the holder has, at 09:00 on the day it becomes
    /// overdue, or at once if that day has passed. Replaces whatever was scheduled before.
    func scheduleOverdue(_ fleet: Fleet, me: Person.ID?, today: CalendarDate) async {
        let stale = await center.pendingNotificationRequests().map(\.identifier).filter { $0.hasPrefix("overdue-") }
        center.removePendingNotificationRequests(withIdentifiers: stale)
        guard let me else { return }
        for device in fleet.devices where device.assignmentStatus == .inUse && device.currentUser == me {
            let due = Notices.overdueOn(device)
            let trigger: UNNotificationTrigger = due <= today
                ? UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
                : UNCalendarNotificationTrigger(
                    dateMatching: DateComponents(year: due.year, month: due.month, day: due.day, hour: 9),
                    repeats: false
                )
            let content = UNMutableNotificationContent()
            content.title = String(localized: "\(device.deviceName) is overdue")
            content.body = String(localized: "You have had it for two weeks. Return it to the shelf, or keep it and ignore this.")
            content.categoryIdentifier = Self.overdueCategory
            content.userInfo = [Self.linkKey: DeepLink.device(device.id).absoluteString]
            try? await center.add(UNNotificationRequest(identifier: "overdue-\(device.id.rawValue)", content: content, trigger: trigger))
        }
    }

    func backOnTheShelf(_ device: Device) async {
        let content = UNMutableNotificationContent()
        content.title = String(localized: "\(device.deviceName) is back on the shelf")
        content.body = String(localized: "The device you were waiting for can be claimed.")
        content.userInfo = [Self.linkKey: DeepLink.device(device.id).absoluteString]
        try? await center.add(UNNotificationRequest(identifier: "back-\(device.id.rawValue)", content: content, trigger: nil))
    }

    static var categories: Set<UNNotificationCategory> {
        [UNNotificationCategory(
            identifier: overdueCategory,
            actions: [UNNotificationAction(identifier: returnAction, title: String(localized: "Return to the shelf"))],
            intentIdentifiers: []
        )]
    }
}
