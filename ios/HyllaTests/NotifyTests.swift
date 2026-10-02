import Foundation
import Testing
import UserNotifications
@testable import Hylla

struct NoticesTests {
    let fleet: Fleet = {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        return try! FleetFixture.decode(Data(contentsOf: url))
    }()
    let alva = Person.ID(rawValue: "p-01")
    let noah = Person.ID(rawValue: "p-02")

    @Test func nothingIsOverdueOnTheDayItWasClaimed() {
        #expect(Notices.overdue(fleet, me: alva, today: CalendarDate(year: 2026, month: 9, day: 22)).isEmpty)
    }

    @Test func aDeviceHeldFourteenDaysIsOverdue() throws {
        #expect(Notices.overdue(fleet, me: alva, today: CalendarDate(year: 2026, month: 10, day: 6)).map(\.id.rawValue) == ["HYL-001"])
        let fold = try #require(fleet.device(Device.ID(rawValue: "HYL-001")))
        #expect(Notices.overdueOn(fold) == CalendarDate(year: 2026, month: 10, day: 6))
    }

    @Test func onlyThePhoneHoldersDevicesLongestHeldFirst() {
        #expect(Notices.overdue(fleet, me: noah, today: CalendarDate(year: 2026, month: 10, day: 2)).map(\.id.rawValue) == ["HYL-017", "HYL-005"])
        #expect(Notices.overdue(fleet, me: nil, today: CalendarDate(year: 2026, month: 10, day: 2)).isEmpty)
    }

    @Test @MainActor func aWatchedDeviceReturnedToTheShelfIsBack() throws {
        let store = FleetStore(fleet: fleet)
        try store.returnDevice(Device.ID(rawValue: "HYL-003"))
        #expect(Notices.backOnTheShelf(before: fleet, after: store.fleet, watched: [Device.ID(rawValue: "HYL-003")]).map(\.id.rawValue) == ["HYL-003"])
        #expect(Notices.backOnTheShelf(before: fleet, after: store.fleet, watched: [Device.ID(rawValue: "HYL-005")]).isEmpty)
    }
}

/// Records what the notifier schedules instead of handing it to the system.
final class RecordingCenter: NotificationScheduling, @unchecked Sendable {
    private let lock = NSLock()
    private var requests: [UNNotificationRequest] = []

    var scheduled: [UNNotificationRequest] { lock.withLock { requests } }

    func add(_ request: UNNotificationRequest) async throws {
        lock.withLock { requests.removeAll { $0.identifier == request.identifier }; requests.append(request) }
    }

    func removePendingNotificationRequests(withIdentifiers identifiers: [String]) {
        lock.withLock { requests.removeAll { identifiers.contains($0.identifier) } }
    }

    func pendingNotificationRequests() async -> [UNNotificationRequest] { scheduled }
}

struct NotifierTests {
    let fleet = NoticesTests().fleet

    @Test func overdueIsScheduledForNineOnTheDueDayWithAReturnAction() async throws {
        let center = RecordingCenter()
        await Notifier(center: center).scheduleOverdue(fleet, me: Person.ID(rawValue: "p-01"), today: CalendarDate(year: 2026, month: 10, day: 2))

        let request = try #require(center.scheduled.first)
        #expect(request.identifier == "overdue-HYL-001")
        #expect(request.content.categoryIdentifier == Notifier.overdueCategory)
        #expect(request.content.userInfo[Notifier.linkKey] as? String == "hylla://device/HYL-001")
        let trigger = try #require(request.trigger as? UNCalendarNotificationTrigger)
        #expect(trigger.dateComponents.day == 6 && trigger.dateComponents.month == 10 && trigger.dateComponents.hour == 9)
    }

    @Test func alreadyOverdueDevicesNotifyAtOnce() async {
        let center = RecordingCenter()
        await Notifier(center: center).scheduleOverdue(fleet, me: Person.ID(rawValue: "p-02"), today: CalendarDate(year: 2026, month: 10, day: 2))

        #expect(Set(center.scheduled.map(\.identifier)) == ["overdue-HYL-017", "overdue-HYL-005"])
        #expect(center.scheduled.allSatisfy { $0.trigger is UNTimeIntervalNotificationTrigger })
    }

    @Test func reschedulingReplacesTheOldSchedule() async {
        let center = RecordingCenter()
        let notifier = Notifier(center: center)
        await notifier.scheduleOverdue(fleet, me: Person.ID(rawValue: "p-02"), today: CalendarDate(year: 2026, month: 10, day: 2))
        await notifier.scheduleOverdue(fleet, me: Person.ID(rawValue: "p-01"), today: CalendarDate(year: 2026, month: 10, day: 2))

        #expect(center.scheduled.map(\.identifier) == ["overdue-HYL-001"])
    }
}
