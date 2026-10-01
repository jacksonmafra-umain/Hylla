import SwiftUI

struct DeviceDetailView: View {
    let fleet: Fleet
    let id: Device.ID

    var body: some View {
        if let device = fleet.devices.first(where: { $0.id == id }) {
            List(fields(for: device), id: \.label) { field in
                // Label above value, so large Dynamic Type sizes wrap instead of colliding.
                VStack(alignment: .leading, spacing: 2) {
                    Text(field.label)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(field.value)
                }
                .accessibilityElement(children: .combine)
            }
            .navigationTitle(device.deviceName)
            .navigationBarTitleDisplayMode(.inline)
        } else {
            ContentUnavailableView(
                "Not found",
                systemImage: "questionmark.square.dashed",
                description: Text("No device with shelf tag \(id.rawValue).")
            )
        }
    }

    private struct Field {
        var label: String
        var value: String
    }

    private func fields(for device: Device) -> [Field] {
        let since = device.since.date().map { $0.formatted(date: .abbreviated, time: .omitted) }
            ?? device.since.description
        let os = ["\(device.platform.label) \(device.osVersion)", device.uiVersion]
            .compactMap(\.self)
            .joined(separator: " · ")
        return [
            Field(label: String(localized: "Status"), value: device.statusText(in: fleet)),
            Field(label: String(localized: "Since"), value: since),
            Field(label: String(localized: "Model"), value: device.modelName),
            Field(label: String(localized: "Model number"), value: device.modelNumber),
            Field(label: String(localized: "Type"), value: device.deviceType.label),
            Field(label: String(localized: "Operating system"), value: os),
            Field(label: String(localized: "Home use"), value: device.homeUse.label),
            Field(label: String(localized: "Lifecycle"), value: device.lifecycle.label),
            Field(label: String(localized: "Shelf tag"), value: device.id.rawValue),
        ] + (device.notes.map { [Field(label: String(localized: "Notes"), value: $0)] } ?? [])
    }
}

#Preview {
    NavigationStack {
        DeviceDetailView(fleet: try! FleetFixture.load(), id: Device.ID(rawValue: "HYL-001"))
    }
}
