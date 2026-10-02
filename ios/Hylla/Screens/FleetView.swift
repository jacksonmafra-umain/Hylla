import SwiftUI

struct FleetView: View {
    let fleet: Fleet
    let posture: WindowPosture

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                AdaptiveGrid(widthClass: posture.widthClass, minColumnWidth: AdaptiveLayout.fleetTileMinWidth) {
                    ForEach(fleet.devices) { device in
                        NavigationLink(value: device.id) {
                            DeviceTile(device: device, fleet: fleet)
                        }
                        .buttonStyle(.plain)
                    }
                }
                VStack(alignment: .leading, spacing: 8) {
                    Text("This window")
                        .font(.headline)
                        .accessibilityAddTraits(.isHeader)
                    PostureReadout(posture: posture)
                }
            }
            .padding(.horizontal, AdaptiveLayout.margin(posture.widthClass))
            .padding(.vertical, AdaptiveLayout.gutter(posture.widthClass))
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle("Fleet")
    }
}

/// Status sits on its own line rather than beside the name: at large Dynamic Type sizes a
/// trailing label squeezes the model name into a column one word wide.
private struct DeviceTile: View {
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
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: 12))
        .contentShape(.rect(cornerRadius: 12))
    }
}

#Preview {
    NavigationStack {
        FleetView(fleet: try! FleetFixture.load(), posture: WindowPosture(size: CGSize(width: 402, height: 874)))
    }
}
