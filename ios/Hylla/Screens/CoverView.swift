import SwiftUI

/// The cover surface: a window too small for navigation, such as a narrow Slide Over or Stage
/// Manager window, and on foldables the outer screen.
///
/// One action, scan to check out, and the devices you hold right now. No navigation chrome.
struct CoverView: View {
    let fleet: Fleet
    let me: Person.ID?
    let onScan: () -> Void

    var body: some View {
        let held = fleet.devices.filter { me != nil && $0.currentUser == me }
        ScrollView {
            VStack(spacing: 12) {
                Button(action: onScan) {
                    Text("Scan to check out")
                        .font(.title3.bold())
                        .frame(maxWidth: .infinity, minHeight: 56)
                }
                .buttonStyle(.borderedProminent)
                Text("You hold")
                    .font(.headline)
                    .accessibilityAddTraits(.isHeader)
                if me == nil {
                    Text("Open the full app and go to You to choose who you are.")
                } else if held.isEmpty {
                    Text("Nothing right now.")
                } else {
                    ForEach(held) { device in
                        let since = device.since.date().map { $0.formatted(date: .abbreviated, time: .omitted) } ?? device.since.description
                        Text("\(device.deviceName), since \(since)")
                    }
                }
            }
            .multilineTextAlignment(.center)
            .padding()
            .frame(maxWidth: .infinity)
        }
        .scrollBounceBehavior(.basedOnSize)
    }
}

#Preview("Cover", traits: .fixedLayout(width: 330, height: 350)) {
    CoverView(fleet: try! FleetFixture.load(), me: Person.ID(rawValue: "p-01"), onScan: {})
}
