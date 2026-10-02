import AVFoundation
import SwiftUI

/// Check a device out or back in at the shelf.
///
/// The viewfinder and the controls take the sizes ``ScanLayout`` computes. The camera itself
/// arrives with the permission flow in chapter 14; typing the tag works everywhere and stays as
/// the accessible fallback.
struct ScanView: View {
    let store: FleetStore
    let posture: WindowPosture
    var me: Person.ID?
    @Environment(\.dismiss) private var dismiss
    @State private var viewfinderSize: CGSize = .zero
    @State private var insets = EdgeInsets()
    /// The tag being looked up, shared by the camera and the typed field.
    @State private var found: Device.ID?
    @State private var camera = PermissionState(camera: AVCaptureDevice.authorizationStatus(for: .video))
    @Environment(\.openURL) private var openURL
    @Environment(\.scenePhase) private var scenePhase

    /// ``ScanLayout`` works in window coordinates; the stacks lay out inside the safe area. The
    /// viewfinder extends under the safe area, so the inset is taken off its layout size to make
    /// its visible size the one ``ScanLayout`` asked for.
    var body: some View {
        let layout = ScanLayout.compute(posture)
        Group {
            if layout.sideBySide {
                HStack(spacing: 0) {
                    viewfinder
                        .frame(width: max(0, layout.viewfinder.width - insets.leading))
                        .ignoresSafeArea(edges: [.leading, .vertical])
                    ScanControls(store: store, me: me, found: $found)
                }
            } else {
                VStack(spacing: 0) {
                    viewfinder
                        .frame(height: max(0, layout.viewfinder.height - insets.top))
                        .ignoresSafeArea(edges: [.top, .horizontal])
                    // An occluding crease is left empty.
                    Spacer().frame(height: max(0, layout.controls.minY - layout.viewfinder.maxY))
                    ScanControls(store: store, me: me, found: $found)
                }
            }
        }
        .onGeometryChange(for: EdgeInsets.self) { $0.safeAreaInsets } action: { insets = $0 }
        // Settings can change the permission while Hylla is in the background.
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { camera = PermissionState(camera: AVCaptureDevice.authorizationStatus(for: .video)) }
        }
        .sensoryFeedback(.success, trigger: found)
    }

    private var viewfinder: some View {
        ZStack {
            Color(red: 0.06, green: 0.08, blue: 0.09)
            if camera == .granted && CameraScanner.isAvailable {
                CameraScanner { raw in
                    if let id = ScannedCode.parse(raw), id != found { found = id }
                }
            } else {
                cameraPrompt
            }
            // The aiming guide scales with the viewfinder, which is ~150 pt tall on a cover-sized
            // window. `containerRelativeFrame` would measure the screen, not the viewfinder, so
            // the viewfinder measures itself.
            let guide = min(min(viewfinderSize.width, viewfinderSize.height) * 0.55, 200)
            if camera == .granted && CameraScanner.isAvailable {
                RoundedRectangle(cornerRadius: guide / 8)
                    .strokeBorder(.white.opacity(0.8), lineWidth: 3)
                    .frame(width: guide, height: guide)
                    .accessibilityLabel("Camera viewfinder")
            }
            VStack {
                HStack {
                    // A full-screen cover has no back gesture; Close is the way out, and Escape on
                    // a hardware keyboard presses it.
                    Button("Close", systemImage: "xmark") { dismiss() }
                        .keyboardShortcut(.cancelAction)
                        .labelStyle(.iconOnly)
                        .font(.title2)
                        .foregroundStyle(.white)
                        .frame(minWidth: 44, minHeight: 44)
                    Spacer()
                }
                Spacer()
                if viewfinderSize.height >= 280 && camera == .granted && CameraScanner.isAvailable {
                    Text("Point the camera at a shelf tag, or type it below.")
                        .foregroundStyle(.white)
                        .multilineTextAlignment(.center)
                }
            }
            .padding()
        }
        .onGeometryChange(for: CGSize.self) { $0.size } action: { viewfinderSize = $0 }
    }
}

extension ScanView {
    /// Asks for the camera, or says where to turn it on. Never at launch: only when scanning.
    @ViewBuilder
    private var cameraPrompt: some View {
        VStack(spacing: 12) {
            switch camera {
            case .notAsked:
                Text("Hylla uses the camera only to read shelf tags. Nothing is recorded or stored.")
                Button("Use the camera") {
                    Task {
                        _ = await AVCaptureDevice.requestAccess(for: .video)
                        camera = PermissionState(camera: AVCaptureDevice.authorizationStatus(for: .video))
                    }
                }
                .buttonStyle(.borderedProminent)
            case .blocked:
                Text("Camera access is off for Hylla. Turn it on in Settings, or type the tag.")
                Button("Open Settings") {
                    if let url = URL(string: UIApplication.openSettingsURLString) { openURL(url) }
                }
                .buttonStyle(.borderedProminent)
            case .granted:
                Text("This device cannot scan codes. Type the tag below.")
            }
        }
        .foregroundStyle(.white)
        .multilineTextAlignment(.center)
        .padding(24)
    }
}

private struct ScanControls: View {
    let store: FleetStore
    let me: Person.ID?
    @State private var tag = ""
    @Binding var found: Device.ID?
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
        .onAppear { person = person ?? me ?? store.fleet.people.first?.id }
    }

    private func lookUp() {
        tagFocused = false
        message = nil
        let id = ShelfTag.parse(tag)
        notATag = id == nil
        found = id
    }
}
