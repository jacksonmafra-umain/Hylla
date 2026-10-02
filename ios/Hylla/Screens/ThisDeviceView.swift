import SwiftUI
import UIKit

/// What the app knows about the device it runs on, and registering it in the fleet.
struct ThisDeviceView: View {
    let posture: WindowPosture
    let store: FleetStore
    @State private var registering = false
    @Environment(Feedback.self) private var feedback: Feedback?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    PostureReadout(posture: posture)
                    Text("Adds this device to the fleet with its model, OS and the window it reports right now.")
                        .font(.subheadline)
                        .multilineTextAlignment(.center)
                    Button("Register this device") { registering = true }
                        .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity)
                .padding()
            }
            .navigationTitle("This device")
            .sheet(isPresented: $registering) {
                EditSheet(
                    device: Registration.draft(.current, posture: posture, fleet: store.fleet, today: .today()),
                    title: String(localized: "Register this device"),
                    saveLabel: String(localized: "Register")
                ) { (device: Device) throws(FleetStore.Failure) in
                    try store.register(device)
                    feedback?.show(String(localized: "\(device.deviceName) is registered as \(device.id.rawValue)."))
                }
            }
        }
    }
}

extension BuildInfo {
    /// The device the app is running on. On the simulator, the simulated model's identifier.
    @MainActor static var current: BuildInfo {
        var system = utsname()
        uname(&system)
        let hardware = withUnsafeBytes(of: system.machine) { String(decoding: $0.prefix { $0 != 0 }, as: UTF8.self) }
        let machine = ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] ?? hardware
        let device = UIDevice.current
        return BuildInfo(model: device.model, machine: machine, systemVersion: device.systemVersion, isPad: device.userInterfaceIdiom == .pad)
    }
}
