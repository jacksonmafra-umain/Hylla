import SwiftUI

struct ContentView: View {
    let fleet: Fleet

    private var available: Int {
        fleet.devices.count { $0.assignmentStatus == .available && $0.lifecycle == .inUse }
    }

    var body: some View {
        WindowPostureReader { posture in
            // Centred when it fits; scrolls at large Dynamic Type sizes instead of truncating.
            ViewThatFits(in: .vertical) {
                summary(posture)
                ScrollView {
                    summary(posture)
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }

    private func summary(_ posture: WindowPosture) -> some View {
        VStack(spacing: 8) {
            Text("Hylla")
                .font(.largeTitle)
                .accessibilityAddTraits(.isHeader)
            Text("^[\(fleet.devices.count) device](inflect: true)")
            Text("\(available) available")
            PostureReadout(posture: posture)
                .padding(.top, 16)
        }
        .font(.body)
        .multilineTextAlignment(.center)
        .padding(24)
    }
}

#Preview {
    ContentView(fleet: try! FleetFixture.load())
}
