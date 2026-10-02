import Foundation
import Testing
@testable import Hylla

@MainActor
struct FleetStoreTests {
    let day = CalendarDate(year: 2026, month: 10, day: 2)
    let flip = Device.ID(rawValue: "HYL-002")
    let fold = Device.ID(rawValue: "HYL-001")
    let noah = Person.ID(rawValue: "p-02")

    func store() throws -> FleetStore {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        let fixture = try FleetFixture.decode(Data(contentsOf: url))
        return FleetStore(fleet: fixture, today: { [day] in day })
    }

    @Test func claimingAnAvailableDevicePutsItInUseFromToday() throws {
        let store = try store()
        try store.claim(flip, by: noah)

        let device = try #require(store.fleet.device(flip))
        #expect(device.assignmentStatus == .inUse)
        #expect(device.currentUser == noah)
        #expect(device.since == day)
        #expect(store.fleet.history(of: flip).first == Assignment(deviceId: flip, person: noah, from: day, to: nil))
    }

    @Test func returningClosesTheOpenAssignment() throws {
        let store = try store()
        try store.returnDevice(fold)

        #expect(store.fleet.device(fold)?.assignmentStatus == .available)
        #expect(store.fleet.device(fold)?.currentUser == nil)
        #expect(store.fleet.history(of: fold).first?.to == day)
    }

    @Test func everyChangeKeepsTheFixtureInvariants() throws {
        let store = try store()
        try store.claim(flip, by: noah)
        try store.returnDevice(fold)
        try store.claim(fold, by: noah)

        #expect(FleetFixture.validate(store.fleet).isEmpty)
    }

    @Test func claimingAHeldDeviceFailsAndChangesNothing() throws {
        let store = try store()
        let before = store.fleet

        #expect(throws: FleetStore.Failure.inUse("Fold7 Blue")) { try store.claim(fold, by: noah) }
        #expect(store.fleet == before)
    }

    @Test func returningADeviceNobodyHoldsFailsAndChangesNothing() throws {
        let store = try store()
        let before = store.fleet

        #expect(throws: FleetStore.Failure.notInUse("Flip7 Black")) { try store.returnDevice(flip) }
        #expect(store.fleet == before)
    }
}

@MainActor
struct FleetStoreEditTests {
    let day = CalendarDate(year: 2026, month: 10, day: 2)
    let fold = Device.ID(rawValue: "HYL-001")

    func store() throws -> FleetStore {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        return FleetStore(fleet: try FleetFixture.decode(Data(contentsOf: url)), today: { [day] in day })
    }

    @Test func editingARecordChangesItsFieldsAndKeepsTheInvariants() throws {
        let store = try store()
        var fold4 = try #require(store.fleet.device(Device.ID(rawValue: "HYL-019")))
        fold4.notes = "With IT"

        try store.update(fold4)

        #expect(store.fleet.device(fold4.id)?.notes == "With IT")
        #expect(FleetFixture.validate(store.fleet).isEmpty)
    }

    @Test func movingSinceOnAHeldDeviceMovesItsOpenAssignment() throws {
        let store = try store()
        var device = try #require(store.fleet.device(fold))
        device.since = CalendarDate(year: 2026, month: 9, day: 21)

        try store.update(device)

        #expect(store.fleet.history(of: fold).first?.from == CalendarDate(year: 2026, month: 9, day: 21))
    }

    @Test func anEditCannotChangeWhoHoldsTheDevice() throws {
        let store = try store()
        var device = try #require(store.fleet.device(fold))
        device.currentUser = Person.ID(rawValue: "p-02")

        #expect(throws: FleetStore.Failure.holderChanged("Fold7 Blue")) { try store.update(device) }
    }

    @Test func sinceCannotMoveIntoTheFuture() throws {
        let store = try store()
        var device = try #require(store.fleet.device(Device.ID(rawValue: "HYL-002")))
        device.since = CalendarDate(year: 2026, month: 10, day: 3)

        #expect(throws: FleetStore.Failure.sinceInFuture) { try store.update(device) }
    }

    @Test func anEditThatOverlapsThePreviousHolderIsRejected() throws {
        let store = try store()
        let before = store.fleet
        var device = try #require(store.fleet.device(fold))
        device.since = CalendarDate(year: 2026, month: 8, day: 1)

        #expect(throws: FleetStore.Failure.invalid(["assignments on HYL-001 overlap"])) { try store.update(device) }
        #expect(store.fleet == before)
    }
}

struct ShelfTagTests {
    @Test(arguments: ["HYL-002", "hyl-2", "HYL2", "2", " 002 "])
    func looseSpellingsResolveToTheCanonicalTag(text: String) {
        #expect(ShelfTag.parse(text) == Device.ID(rawValue: "HYL-002"))
    }

    @Test(arguments: ["", "HYL-", "ABC-002", "HYL-1234", "Fold7"])
    func anythingElseIsRejected(text: String) {
        #expect(ShelfTag.parse(text) == nil)
    }
}

struct ScanLayoutTests {
    func crease(at y: CGFloat, width: CGFloat, height: CGFloat = 0) -> Fold {
        Fold(frame: CGRect(x: 0, y: y, width: width, height: height), orientation: .horizontal,
             isSeparating: true, occludes: height > 0)
    }

    func scan(_ width: CGFloat, _ height: CGFloat, _ folds: Fold...) -> ScanLayout {
        ScanLayout.compute(WindowPosture(size: CGSize(width: width, height: height), folds: folds))
    }

    @Test func tabletopPutsTheViewfinderAboveTheCreaseAndControlsBelow() {
        let layout = scan(883, 852, crease(at: 426, width: 883))
        #expect(layout.viewfinder == CGRect(x: 0, y: 0, width: 883, height: 426))
        #expect(layout.controls == CGRect(x: 0, y: 426, width: 883, height: 426))
    }

    @Test func tabletopSplitsAtTheCreaseNotAtTheDefaultShare() {
        #expect(scan(883, 852, crease(at: 500, width: 883)).viewfinder.maxY == 500)
    }

    @Test func anOccludingCreaseIsLeftEmpty() {
        let layout = scan(720, 826, crease(at: 400, width: 720, height: 26))
        #expect(layout.viewfinder.maxY == 400)
        #expect(layout.controls.minY == 426)
    }

    @Test func flatPortraitStacksTheViewfinderOverTheControls() {
        let layout = scan(402, 874)
        #expect(!layout.sideBySide)
        #expect(layout.viewfinder.maxY == 874 * ScanLayout.viewfinderShare)
    }

    @Test func phoneLandscapePutsThemSideBySide() {
        #expect(scan(874, 402).sideBySide)
    }
}
