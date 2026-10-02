import SwiftUI

@main
struct HyllaApp: App {
    /// Seeded from the bundled fixture. A broken fixture is a build-time mistake, so failing
    /// loudly is right.
    @State private var store: FleetStore = {
        do {
            return FleetStore(fleet: try FleetFixture.load())
        } catch {
            fatalError("devices.json failed to load: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView(store: store)
        }
    }
}
