import SwiftUI

struct FleetView: View {
    let fleet: Fleet
    let posture: WindowPosture
    @Binding var selection: Device.ID?
    /// Beside the detail, the selected tile is marked; alone, it is about to be covered anyway.
    let highlightsSelection: Bool
    var onScan: () -> Void = {}

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                AdaptiveGrid(widthClass: posture.widthClass, minColumnWidth: AdaptiveLayout.fleetTileMinWidth) {
                    ForEach(fleet.devices) { device in
                        let selected = highlightsSelection && selection == device.id
                        Button {
                            selection = device.id
                        } label: {
                            DeviceTile(device: device, fleet: fleet, selected: selected)
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(selected ? .isSelected : [])
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
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Scan", action: onScan)
            }
        }
    }
}

/// Status sits on its own line rather than beside the name: at large Dynamic Type sizes a
/// trailing label squeezes the model name into a column one word wide.
private struct DeviceTile: View {
    let device: Device
    let fleet: Fleet
    let selected: Bool

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
        .background(
            selected ? AnyShapeStyle(.tint.opacity(0.15)) : AnyShapeStyle(Color(.secondarySystemGroupedBackground)),
            in: .rect(cornerRadius: 12)
        )
        .overlay {
            if selected {
                RoundedRectangle(cornerRadius: 12).strokeBorder(.tint, lineWidth: 2)
            }
        }
        .contentShape(.rect(cornerRadius: 12))
    }
}

#Preview {
    NavigationStack {
        FleetView(
            fleet: try! FleetFixture.load(),
            posture: WindowPosture(size: CGSize(width: 402, height: 874)),
            selection: .constant(nil),
            highlightsSelection: false
        )
    }
}
