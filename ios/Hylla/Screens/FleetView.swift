import SwiftUI

struct FleetView: View {
    let fleet: Fleet
    let posture: WindowPosture

    var body: some View {
        List {
            Section {
                ForEach(fleet.devices) { device in
                    NavigationLink(value: device.id) {
                        DeviceRow(device: device, fleet: fleet)
                    }
                }
            }
            Section("This window") {
                PostureReadout(posture: posture)
                    .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle("Fleet")
    }
}

private struct DeviceRow: View {
    let device: Device
    let fleet: Fleet

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(device.deviceName)
                .font(.headline)
            Text("\(device.modelName) · \(device.platform.label) \(device.osVersion)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Text(device.statusText(in: fleet))
                .font(.subheadline.weight(.semibold))
        }
        .padding(.vertical, 2)
    }
}

#Preview {
    NavigationStack {
        FleetView(fleet: try! FleetFixture.load(), posture: WindowPosture(size: CGSize(width: 402, height: 874)))
    }
}
