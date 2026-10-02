import Foundation

/// What deserves a notification. Pure, so the rules are tested apart from how they are posted.
enum Notices {
    /// A device held this many days or more is overdue for return.
    static let overdueAfterDays = 14

    /// Devices `me` has held for ``overdueAfterDays`` or more, longest first.
    static func overdue(_ fleet: Fleet, me: Person.ID?, today: CalendarDate) -> [Device] {
        guard let me else { return [] }
        return fleet.devices
            .filter { $0.assignmentStatus == .inUse && $0.currentUser == me && overdueOn($0) <= today }
            .sorted { $0.since < $1.since }
    }

    /// The day `device` becomes overdue, for scheduling.
    static func overdueOn(_ device: Device) -> CalendarDate {
        let calendar = Calendar(identifier: .gregorian)
        let start = calendar.date(from: DateComponents(year: device.since.year, month: device.since.month, day: device.since.day))!
        let due = calendar.date(byAdding: .day, value: overdueAfterDays, to: start)!
        let parts = calendar.dateComponents([.year, .month, .day], from: due)
        return CalendarDate(year: parts.year!, month: parts.month!, day: parts.day!)
    }

    /// Watched devices that could not be taken `before` and can be `after`.
    static func backOnTheShelf(before: Fleet, after: Fleet, watched: Set<Device.ID>) -> [Device] {
        after.devices.filter { device in
            watched.contains(device.id) && device.isClaimable && before.device(device.id)?.isClaimable == false
        }
    }
}
