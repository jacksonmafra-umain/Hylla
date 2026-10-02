import SwiftUI

/// The third column: who held the selected device, newest first.
struct HistoryView: View {
    let fleet: Fleet
    let device: Device.ID?

    var body: some View {
        let history = device.map { fleet.history(of: $0) } ?? []
        Group {
            if device == nil {
                ContentUnavailableView("History appears here for the selected device.", systemImage: "clock")
            } else if history.isEmpty {
                ContentUnavailableView("Nobody has borrowed this device yet.", systemImage: "clock")
            } else {
                List(history, id: \.self) { HistoryRow(fleet: fleet, assignment: $0) }
            }
        }
        .navigationTitle("History")
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// History as a section of the detail, used when there is no room for a third column.
struct HistorySection: View {
    let fleet: Fleet
    let device: Device.ID

    var body: some View {
        let history = fleet.history(of: device)
        VStack(alignment: .leading, spacing: 8) {
            Text("History")
                .font(.headline)
                .accessibilityAddTraits(.isHeader)
            if history.isEmpty {
                Text("Nobody has borrowed this device yet.")
                    .foregroundStyle(.secondary)
            }
            ForEach(history, id: \.self) { HistoryRow(fleet: fleet, assignment: $0) }
        }
        .padding(.top, 16)
    }
}

struct HistoryRow: View {
    let fleet: Fleet
    let assignment: Assignment

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(fleet.person(assignment.person)?.name ?? assignment.person.rawValue)
            Text(period)
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }

    private var period: String {
        let from = format(assignment.from)
        guard let to = assignment.to else { return String(localized: "Since \(from)") }
        return "\(from) – \(format(to))"
    }

    private func format(_ date: CalendarDate) -> String {
        date.date().map { $0.formatted(date: .abbreviated, time: .omitted) } ?? date.description
    }
}
