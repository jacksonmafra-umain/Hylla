import Foundation
import Testing
@testable import Hylla

struct FleetFilterTests {
    let fleet: Fleet = {
        let url = URL(filePath: #filePath).deletingLastPathComponent().appending(path: "../../fixtures/devices.json").standardized
        return try! FleetFixture.decode(Data(contentsOf: url))
    }()

    func names(_ filter: FleetFilter) -> [String] {
        fleet.devices.filter(filter.matches).map(\.deviceName)
    }

    @Test func noConditionsMatchEverything() {
        #expect(names(FleetFilter()).count == fleet.devices.count)
        #expect(FleetFilter().activeCount == 0)
    }

    @Test func platformsAreAnyOf() {
        #expect(names(FleetFilter(platforms: [.iPadOS, .visionOS])) == ["iPad Pro 13", "iPad mini", "Vision Pro"])
    }

    @Test func conditionsCombineWithAnd() {
        let filter = FleetFilter(platforms: [.android], type: .tablet)
        #expect(names(filter) == ["Tab S10 FE"])
        #expect(filter.activeCount == 2)
    }

    @Test func claimableExcludesHeldMissingAndDecommissioned() {
        let claimable = fleet.devices.filter(FleetFilter(claimableOnly: true).matches)
        #expect(claimable.allSatisfy { $0.assignmentStatus == .available && $0.lifecycle == .inUse })
        #expect(!claimable.map(\.deviceName).contains("Galaxy Fold4"))
        #expect(claimable.count == 9)
    }
}
