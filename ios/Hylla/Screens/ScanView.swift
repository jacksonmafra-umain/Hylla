import SwiftUI

/// Check a device out or back in at the shelf.
///
/// The viewfinder and the controls take the sizes ``ScanLayout`` computes. The camera itself
/// arrives with the permission flow in chapter 14; typing the tag works everywhere and stays as
/// the accessible fallback.
struct ScanView: View {
    let store: FleetStore
    let posture: WindowPosture
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let layout = ScanLayout.compute(posture)
        if layout.sideBySide {
            HStack(spacing: 0) {
                viewfinder.frame(width: layout.viewfinder.width).ignoresSafeArea(edges: [.leading, .vertical])
                ScanControls(store: store)
            }
        } else {
            VStack(spacing: 0) {
                viewfinder.frame(height: layout.viewfinder.height).ignoresSafeArea(edges: [.top, .horizontal])
                // An occluding crease is left empty.
                Spacer().frame(height: max(0, layout.controls.minY - layout.viewfinder.maxY))
                ScanControls(store: store)
            }
        }
    }

    private var viewfinder: some View {
        ZStack {
            Color(red: 0.06, green: 0.08, blue: 0.09)
            RoundedRectangle(cornerRadius: 24)
                .strokeBorder(.white.opacity(0.8), lineWidth: 3)
                .frame(width: 200, height: 200)
                .accessibilityLabel("Camera viewfinder")
            VStack {
                HStack {
                    Button("Close", systemImage: "xmark") { dismiss() }
                        .labelStyle(.iconOnly)
                        .font(.title2)
                        .foregroundStyle(.white)
                        .frame(minWidth: 44, minHeight: 44)
                    Spacer()
                }
                Spacer()
                Text("Point the camera at a shelf tag, or type it below.")
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)
            }
            .padding()
        }
    }
}

private struct ScanControls: View {
    let store: FleetStore
    @State private var tag = ""
    @State private var found: Device.ID?
    @State private var notATag = false
    @State private var person: Person.ID?
    @State private var message: String?
    @FocusState private var tagFocused: Bool

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Check out or return")
                    .font(.title2.bold())
                    .accessibilityAddTraits(.isHeader)
                TextField("Shelf tag", text: $tag)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.characters)
                    .autocorrectionDisabled()
                    .submitLabel(.search)
                    .focused($tagFocused)
                    .onSubmit(lookUp)
                Button("Look up", action: lookUp)
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)
                if notATag {
                    Text("That is not a shelf tag. Tags look like HYL-002.")
                } else if let found {
                    if let device = store.fleet.device(found) {
                        foundCard(device)
                    } else {
                        Text("No device with shelf tag \(found.rawValue).")
                    }
                }
                if let message {
                    Text(message)
                }
            }
            .padding()
        }
        .onChange(of: message) { _, new in
            if let new { AccessibilityNotification.Announcement(new).post() }
        }
    }

    private func foundCard(_ device: Device) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(device.deviceName).font(.headline)
            Text(device.statusText(in: store.fleet)).font(.subheadline.weight(.semibold))
            if device.assignmentStatus == .inUse {
                Button("Return to the shelf") {
                    try? store.returnDevice(device.id)
                    message = String(localized: "\(device.deviceName) is back on the shelf.")
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
            } else {
                Picker("Claim as", selection: $person) {
                    ForEach(store.fleet.people) { Text($0.name).tag(Optional($0.id)) }
                }
                Button("Claim") {
                    guard let person = person ?? store.fleet.people.first?.id else { return }
                    try? store.claim(device.id, by: person)
                    let name = store.fleet.person(person)?.name ?? ""
                    message = String(localized: "\(device.deviceName) is now with \(name).")
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground), in: .rect(cornerRadius: 12))
        .onAppear { person = person ?? store.fleet.people.first?.id }
    }

    private func lookUp() {
        tagFocused = false
        message = nil
        let id = ShelfTag.parse(tag)
        notATag = id == nil
        found = id
    }
}
