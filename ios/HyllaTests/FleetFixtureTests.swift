import Foundation
import Testing
@testable import Hylla

struct FleetFixtureTests {
    /// Read from the repository, not the app bundle, so the test exercises the same file the
    /// Android tests read.
    let sharedFixture = try! Data(contentsOf: URL(filePath: #filePath)
        .deletingLastPathComponent()
        .appending(path: "../../fixtures/devices.json")
        .standardized)

    var text: String { String(decoding: sharedFixture, as: UTF8.self) }

    func decode(_ text: String) throws -> Fleet {
        try FleetFixture.decode(Data(text.utf8))
    }

    @Test func sharedFixtureDecodesAndValidates() throws {
        let fleet = try FleetFixture.decode(sharedFixture)

        #expect(fleet.devices.count == 19)
        #expect(fleet.people.count == 6)
        #expect(fleet.assignments.count == 41)
    }

    @Test func sharedFixtureCoversEveryEnumValue() throws {
        let fleet = try FleetFixture.decode(sharedFixture)

        #expect(Set(fleet.devices.map(\.deviceType)) == Set(DeviceType.allCases))
        #expect(Set(fleet.devices.map(\.platform)) == Set(Platform.allCases))
        #expect(Set(fleet.devices.map(\.assignmentStatus)) == Set(AssignmentStatus.allCases))
        #expect(Set(fleet.devices.map(\.homeUse)) == Set(HomeUse.allCases))
        #expect(Set(fleet.devices.map(\.lifecycle)) == Set(Lifecycle.allCases))
    }

    @Test func fieldsDecodeToNativeTypes() throws {
        let fold = try #require(try FleetFixture.decode(sharedFixture).devices.first { $0.id.rawValue == "HYL-001" })

        #expect(fold.platform == .android)
        #expect(fold.uiVersion == "One UI 8")
        #expect(fold.since == CalendarDate(year: 2026, month: 9, day: 22))
        #expect(fold.currentUser == Person.ID(rawValue: "p-01"))
    }

    @Test func nullOptionalFieldsStayNil() throws {
        let iphone = try #require(try FleetFixture.decode(sharedFixture).devices.first { $0.id.rawValue == "HYL-012" })

        #expect(iphone.uiVersion == nil)
        #expect(iphone.currentUser == nil)
        #expect(iphone.notes == nil)
    }

    @Test func fixtureNeverCarriesHardwareIdentifiers() throws {
        let forbidden = ["serial", "imei", "eid", "udid", "applepay"]
        let pattern = /"([A-Za-z0-9 _]+)"\s*:/
        let keys = Set(text.matches(of: pattern).map {
            $0.output.1.lowercased().replacing(" ", with: "").replacing("_", with: "")
        })

        for key in keys {
            #expect(!forbidden.contains { key.hasPrefix($0) }, "forbidden key \(key)")
        }
    }

    @Test func unknownKeysFailTheDecode() {
        let withSerial = text.replacing(
            #""deviceName": "Fold7 Blue""#,
            with: #""deviceName": "Fold7 Blue", "serialNumber": "X""#,
            maxReplacements: 1
        )

        #expect(throws: FleetFixture.Failure.keys(device: 0, unknown: ["serialNumber"], missing: [])) {
            try decode(withSerial)
        }
    }

    @Test func missingNullableKeyFailsTheDecode() {
        let withoutNotes = text.replacing(
            #"  "notes": "Primary book-posture test device.""#,
            with: #"  "_": 0"#,
            maxReplacements: 1
        )

        #expect(throws: FleetFixture.Failure.keys(device: 0, unknown: ["_"], missing: ["notes"])) {
            try decode(withoutNotes)
        }
    }

    @Test func inUseWithoutUserIsRejected() {
        let broken = text.replacing(#""currentUser": "p-01""#, with: #""currentUser": null"#, maxReplacements: 1)

        #expect(problems(broken).contains("HYL-001 is in use without a current user"))
    }

    @Test func unknownPersonIsRejected() {
        let broken = text.replacing(#""currentUser": "p-01""#, with: #""currentUser": "p-99""#, maxReplacements: 1)

        #expect(problems(broken).contains("HYL-001 references unknown person p-99"))
    }

    func problems(_ text: String) -> [String] {
        do {
            _ = try decode(text)
            return []
        } catch FleetFixture.Failure.invalid(let problems) {
            return problems
        } catch {
            return ["unexpected \(error)"]
        }
    }

    @Test func historyIsNewestFirstAndTheOpenAssignmentMatchesTheHolder() throws {
        let fleet = try FleetFixture.decode(sharedFixture)
        let history = fleet.history(of: Device.ID(rawValue: "HYL-001"))

        #expect(history.count == 5)
        #expect(history == history.sorted { $0.from > $1.from })
        #expect(history.first?.to == nil)
        #expect(history.first?.person == Person.ID(rawValue: "p-01"))
    }

    @Test func returnedDeviceWithAnOpenAssignmentIsRejected() {
        let broken = text.replacing(
            "\"assignmentStatus\": \"inUse\",\n      \"currentUser\": \"p-01\"",
            with: "\"assignmentStatus\": \"available\",\n      \"currentUser\": null",
            maxReplacements: 1
        )

        #expect(problems(broken) == ["HYL-001 is available but has an open assignment"])
    }

    @Test func unknownAssignmentKeysFailTheDecode() {
        let broken = text.replacing(#""deviceId": "HYL-001","#, with: #""deviceId": "HYL-001", "udid": "X","#, maxReplacements: 1)

        #expect(throws: FleetFixture.Failure.self) { try decode(broken) }
    }

    @Test func calendarDateRejectsInvalidDays() {
        #expect(CalendarDate("2026-02-30") == nil)
        #expect(CalendarDate("2026-9-22") == nil)
        #expect(CalendarDate("2026-09-22")?.description == "2026-09-22")
    }
}
