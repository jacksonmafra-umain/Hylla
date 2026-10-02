import Foundation
import Testing
@testable import Hylla

@MainActor
struct RegistrationTests {
    let fleet = NoticesTests().fleet
    let today = CalendarDate(year: 2026, month: 10, day: 2)
    let iPhone = BuildInfo(model: "iPhone", machine: "iPhone18,1", systemVersion: "27.2", isPad: false)

    @Test func theDraftDescribesTheDeviceItRunsOn() {
        let draft = Registration.draft(iPhone, posture: WindowPosture(size: CGSize(width: 402, height: 874)), fleet: fleet, today: today)

        #expect(draft.modelNumber == "iPhone18,1")
        #expect(draft.osVersion == "27.2")
        #expect(draft.platform == .iOS)
        #expect(draft.uiVersion == nil)
        #expect(draft.notes == "Registered from the device. Window 402 × 874 pt, compact × medium, posture flat, no folds.")
    }

    @Test func anIPadIsATabletOnIPadOS() {
        let pad = BuildInfo(model: "iPad", machine: "iPad17,1", systemVersion: "27.2", isPad: true)
        let draft = Registration.draft(pad, posture: WindowPosture(size: CGSize(width: 1032, height: 1376)), fleet: fleet, today: today)

        #expect(draft.deviceType == .tablet)
        #expect(draft.platform == .iPadOS)
    }

    @Test func itTakesTheNextFreeShelfTag() {
        #expect(Registration.nextTag(fleet) == Device.ID(rawValue: "HYL-020"))
    }

    @Test func aRegisteredDeviceJoinsTheFleetAndKeepsItsRules() throws {
        let store = FleetStore(fleet: fleet)
        let draft = Registration.draft(iPhone, posture: WindowPosture(size: CGSize(width: 402, height: 874)), fleet: fleet, today: today)

        try store.register(draft)
        #expect(store.fleet.devices.count == 20)
        #expect(throws: FleetStore.Failure.self) { try store.register(draft) }
    }
}
