import SwiftUI

/// Compact navigation: one screen at a time, fleet then detail.
struct ContentView: View {
    let fleet: Fleet
    @State private var path: [Device.ID] = []

    var body: some View {
        WindowPostureReader { posture in
            NavigationStack(path: $path) {
                FleetView(fleet: fleet, posture: posture)
                    .navigationDestination(for: Device.ID.self) { id in
                        DeviceDetailView(fleet: fleet, id: id, widthClass: posture.widthClass)
                    }
            }
        }
    }
}

#Preview {
    ContentView(fleet: try! FleetFixture.load())
}
