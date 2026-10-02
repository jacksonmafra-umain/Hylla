import Foundation
import Observation

/// The fleet as the app currently knows it, and the changes made to it.
///
/// In memory for now: it lives as long as the process. Chapter 16 makes it persistent and queues
/// every change for sync. Every operation keeps the fixture invariants, so
/// ``FleetFixture/validate(_:)`` passes after each one.
@MainActor
@Observable
final class FleetStore {
    enum Failure: Error, Equatable {
        case noDevice(Device.ID)
        case noPerson(Person.ID)
        case inUse(String)
        case notInUse(String)
        case holderChanged(String)
        case sinceInFuture
        case invalid([String])
    }

    private(set) var fleet: Fleet
    fileprivate let today: () -> CalendarDate

    init(fleet: Fleet, today: @escaping () -> CalendarDate = { CalendarDate.today() }) {
        self.fleet = fleet
        self.today = today
    }

    /// `person` takes `device` from the shelf. Throws if someone already holds it.
    func claim(_ device: Device.ID, by person: Person.ID) throws(Failure) {
        guard let index = fleet.devices.firstIndex(where: { $0.id == device }) else { throw .noDevice(device) }
        let current = fleet.devices[index]
        guard current.assignmentStatus != .inUse else { throw .inUse(current.deviceName) }
        guard fleet.person(person) != nil else { throw .noPerson(person) }
        let day = today()
        fleet.devices[index].assignmentStatus = .inUse
        fleet.devices[index].currentUser = person
        fleet.devices[index].since = day
        fleet.assignments.append(Assignment(deviceId: device, person: person, from: day, to: nil))
    }

    fileprivate func replace(_ next: Fleet) {
        fleet = next
    }

    /// `device` goes back on the shelf. Throws if nobody holds it.
    func returnDevice(_ device: Device.ID) throws(Failure) {
        guard let index = fleet.devices.firstIndex(where: { $0.id == device }) else { throw .noDevice(device) }
        let current = fleet.devices[index]
        guard current.assignmentStatus == .inUse else { throw .notInUse(current.deviceName) }
        let day = today()
        fleet.devices[index].assignmentStatus = .available
        fleet.devices[index].currentUser = nil
        fleet.devices[index].since = day
        for i in fleet.assignments.indices where fleet.assignments[i].deviceId == device && fleet.assignments[i].to == nil {
            fleet.assignments[i].to = day
        }
    }
}

extension FleetStore {
    /// Replaces the editable fields of a device: everything except who holds it, which only
    /// ``claim(_:by:)`` and ``returnDevice(_:)`` change. Moving `since` on a held device moves the
    /// start of its open assignment with it. An edit that would break the fleet's invariants is
    /// rejected and changes nothing.
    func update(_ edited: Device) throws(Failure) {
        guard let current = fleet.device(edited.id) else { throw .noDevice(edited.id) }
        guard edited.assignmentStatus == current.assignmentStatus, edited.currentUser == current.currentUser else {
            throw .holderChanged(current.deviceName)
        }
        guard edited.since <= today() else { throw .sinceInFuture }
        var next = fleet
        next.devices = next.devices.map { $0.id == edited.id ? edited : $0 }
        next.assignments = next.assignments.map { assignment in
            var assignment = assignment
            if assignment.deviceId == edited.id && assignment.to == nil { assignment.from = edited.since }
            return assignment
        }
        let problems = FleetFixture.validate(next)
        guard problems.isEmpty else { throw .invalid(problems) }
        replace(next)
    }
}

extension Fleet {
    func device(_ id: Device.ID) -> Device? {
        devices.first { $0.id == id }
    }
}

extension CalendarDate {
    /// Today in `calendar`'s time zone.
    static func today(in calendar: Calendar = .current, now: Date = .now) -> CalendarDate {
        let parts = calendar.dateComponents([.year, .month, .day], from: now)
        return CalendarDate(year: parts.year!, month: parts.month!, day: parts.day!)
    }
}
