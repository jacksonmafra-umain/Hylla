import SwiftUI

/// The app shell: three top-level destinations in a tab view that adapts its own shape.
///
/// `.sidebarAdaptable` gives a tab bar on iPhone and, on iPad, tabs at the top that can become a
/// sidebar. Unlike Android's chrome, the system decides the shape from the horizontal size class;
/// what the app decides is everything inside it. The cover surface and the scanner take the whole
/// window, so they are drawn instead of the shell rather than inside it.
struct RootView: View {
    enum Destination: Hashable { case fleet, thisDevice, you }

    let store: FleetStore
    @State private var destination = Destination.fleet
    @State private var selection: Device.ID?
    @State private var scanning = false
    /// Who holds this phone. Local only: not an account, never synced.
    @AppStorage("me") private var meRaw = ""

    private var me: Person.ID? { meRaw.isEmpty ? nil : Person.ID(rawValue: meRaw) }

    var body: some View {
        WindowPostureReader { window in
            Group {
                // The cover replaces the shell without touching the selection, so a larger window
                // returns to where it was.
                if window.posture == .cover {
                    CoverView(fleet: store.fleet, me: me, onScan: { scanning = true })
                } else {
                    TabView(selection: $destination) {
                        Tab("Fleet", systemImage: "list.bullet", value: .fleet) {
                            FleetRootView(store: store, selection: $selection, onScan: { scanning = true })
                        }
                        Tab("This device", systemImage: "info.circle", value: .thisDevice) {
                            ThisDeviceView(posture: window)
                        }
                        Tab("You", systemImage: "person", value: .you) {
                            YouView(fleet: store.fleet, me: $meRaw)
                        }
                    }
                    .tabViewStyle(.sidebarAdaptable)
                }
            }
            .fullScreenCover(isPresented: $scanning) {
                WindowPostureReader { ScanView(store: store, posture: $0, me: me) }
            }
        }
    }
}
