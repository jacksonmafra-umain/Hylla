import Foundation
import Testing
@testable import Hylla

@MainActor
struct PersistenceTests {
    let fixture: Fleet = {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        return try! FleetFixture.decode(Data(contentsOf: url))
    }()
    let directory = FileManager.default.temporaryDirectory.appending(path: UUID().uuidString)
    let day = CalendarDate(year: 2026, month: 10, day: 2)
    let flip = Device.ID(rawValue: "HYL-002")
    let noah = Person.ID(rawValue: "p-02")

    init() throws {
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
    }

    var persistence: FileFleetPersistence { FileFleetPersistence(directory: directory) }
    func store() -> FleetStore { FleetStore(fleet: fixture, today: { [day] in day }, persistence: persistence) }

    @Test func theFirstRunStartsFromTheFixture() {
        #expect(persistence.load() == nil)
        #expect(store().fleet == fixture)
    }

    @Test func aClaimSurvivesANewLaunch() throws {
        try store().claim(flip, by: noah)

        let reopened = store()
        #expect(reopened.fleet.device(flip)?.assignmentStatus == .inUse)
        #expect(reopened.fleet.device(flip)?.currentUser == noah)
    }

    @Test func everyChangeIsJournalledInOrderFailuresAreNot() throws {
        let store = store()
        try store.claim(flip, by: noah)
        #expect(throws: FleetStore.Failure.self) { try store.claim(flip, by: noah) }
        try store.returnDevice(flip)
        store.undo()

        #expect(persistence.operations() == [.claim(device: flip, person: noah, on: day), .return(device: flip, on: day), .undo])
    }

    @Test func undoIsSavedToo() throws {
        let store = store()
        try store.claim(flip, by: noah)
        store.undo()

        #expect(self.store().fleet.device(flip)?.assignmentStatus == .available)
    }

    @Test func anUnreadableSnapshotFallsBackToTheFixture() throws {
        try Data("{ not json".utf8).write(to: directory.appending(path: "fleet.json"))

        #expect(store().fleet == fixture)
    }

    @Test func aSnapshotThatBreaksTheRulesIsNotTrusted() throws {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        let broken = try String(contentsOf: url, encoding: .utf8)
            .replacing(#""currentUser": "p-01""#, with: #""currentUser": null"#, maxReplacements: 1)
        try Data(broken.utf8).write(to: directory.appending(path: "fleet.json"))

        #expect(store().fleet == fixture)
    }

    @Test func aSavedFleetReadsBackExactly() {
        persistence.save(fixture)
        #expect(persistence.load() == fixture)
    }

    @Test func theJournalMatchesAndroidsShape() throws {
        persistence.append(.claim(device: flip, person: noah, on: day))
        let line = try String(contentsOf: directory.appending(path: "journal.jsonl"), encoding: .utf8)
        let object = try #require(JSONSerialization.jsonObject(with: Data(line.utf8)) as? [String: String])

        #expect(object == ["type": "claim", "device": "HYL-002", "person": "p-02", "on": "2026-10-02"])
    }
}
