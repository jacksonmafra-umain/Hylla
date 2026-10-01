import SwiftUI

struct ContentView: View {
    let fleet: Fleet

    private var available: Int {
        fleet.devices.count { $0.assignmentStatus == .available && $0.lifecycle == .inUse }
    }

    var body: some View {
        VStack(spacing: 8) {
            Text("Hylla")
                .font(.largeTitle)
                .accessibilityAddTraits(.isHeader)
            Text("^[\(fleet.devices.count) device](inflect: true)")
            Text("\(available) available")
        }
        .font(.body)
        .multilineTextAlignment(.center)
        .padding(24)
    }
}

#Preview {
    ContentView(fleet: try! FleetFixture.load())
}
