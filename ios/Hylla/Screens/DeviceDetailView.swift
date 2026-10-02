import SwiftUI

struct DeviceDetailView: View {
    let store: FleetStore
    let id: Device.ID
    var me: Person.ID?
    let widthClass: WidthClass
    /// False when a history column sits beside the detail.
    var showsHistory = true

    private enum Modal: Identifiable {
        case claim, edit
        var id: Self { self }
    }

    @Environment(Feedback.self) private var feedback: Feedback?
    @State private var modal: Modal?
    @State private var confirmingReturn = false
    @AppStorage("watched") private var watchedRaw = ""

    private var fleet: Fleet { store.fleet }

    var body: some View {
        if let device = fleet.devices.first(where: { $0.id == id }) {
            ScrollView {
                action(for: device)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, AdaptiveLayout.margin(widthClass))
                    .padding(.top, AdaptiveLayout.gutter(widthClass))
                AdaptiveGrid(widthClass: widthClass, minColumnWidth: AdaptiveLayout.detailFieldMinWidth) {
                    ForEach(fields(for: device), id: \.label) { field in
                        // Label above value, so large Dynamic Type sizes wrap instead of colliding.
                        VStack(alignment: .leading, spacing: 2) {
                            Text(field.label)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text(field.value)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityElement(children: .combine)
                    }
                }
                .padding(.horizontal, AdaptiveLayout.margin(widthClass))
                .padding(.top, AdaptiveLayout.gutter(widthClass))
                if showsHistory {
                    HistorySection(fleet: fleet, device: device.id)
                        .padding(.horizontal, AdaptiveLayout.margin(widthClass))
                }
                Spacer(minLength: AdaptiveLayout.margin(widthClass))
            }
            .navigationTitle(device.deviceName)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button("Edit") { modal = .edit }
                }
            }
            .sheet(item: $modal) { modal in
                switch modal {
                case .claim:
                    ClaimSheet(device: device, fleet: fleet, me: me) { person in
                        if (try? store.claim(device.id, by: person)) != nil {
                            let name = fleet.person(person)?.name ?? ""
                            feedback?.show(String(localized: "\(device.deviceName) is now with \(name)."), onUndo: { store.undo() })
                        }
                        self.modal = nil
                    }
                case .edit:
                    EditSheet(device: device) { (edited: Device) throws(FleetStore.Failure) in
                        try store.update(edited)
                        feedback?.show(String(localized: "\(edited.deviceName) is saved."), onUndo: { store.undo() })
                    }
                }
            }
            .alert("Return \(device.deviceName)?", isPresented: $confirmingReturn) {
                Button("Return to the shelf", role: .destructive) {
                    if (try? store.returnDevice(device.id)) != nil {
                        feedback?.show(String(localized: "\(device.deviceName) is back on the shelf."), onUndo: { store.undo() })
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("It goes back on the shelf and \(device.currentUser.flatMap(fleet.person)?.name ?? "") no longer holds it.")
            }
        } else {
            ContentUnavailableView(
                "Not found",
                systemImage: "questionmark.square.dashed",
                description: Text("No device with shelf tag \(id.rawValue).")
            )
        }
    }

    @ViewBuilder
    private func action(for device: Device) -> some View {
        HStack {
            if device.assignmentStatus == .inUse {
                Button("Return to the shelf") { confirmingReturn = true }
                    .buttonStyle(.bordered)
            } else if device.lifecycle == .inUse {
                Button("Claim") { modal = .claim }
                    .buttonStyle(.borderedProminent)
            }
            // Waiting makes sense only for a device someone cannot take right now.
            if !device.isClaimable && device.lifecycle == .inUse {
                let watched = WatchList.decode(watchedRaw)
                Button(watched.contains(device.id) ? "Stop waiting for it" : "Notify me when it is back") {
                    let next = watched.contains(device.id) ? watched.subtracting([device.id]) : watched.union([device.id])
                    watchedRaw = WatchList.encode(next)
                }
                .buttonStyle(.bordered)
            }
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
        DeviceDetailView(store: FleetStore(fleet: try! FleetFixture.load()), id: Device.ID(rawValue: "HYL-001"), widthClass: .compact)
    }
}
