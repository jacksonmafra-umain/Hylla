import SwiftUI

@main
struct HyllaApp: App {
    /// Bundled fixture. A broken fixture is a build-time mistake, so failing loudly is right.
    private let fleet: Fleet = {
        do {
            return try FleetFixture.load()
        } catch {
            fatalError("devices.json failed to load: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView(fleet: fleet)
        }
    }
}
