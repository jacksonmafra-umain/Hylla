import SwiftUI

@main
struct HyllaApp: App {
    @UIApplicationDelegateAdaptor(NotificationDelegate.self) private var notificationDelegate
    /// Seeded from the bundled fixture. A broken fixture is a build-time mistake, so failing
    /// loudly is right.
    @State private var store: FleetStore = {
        do {
            return FleetStore(fleet: try FleetFixture.load())
        } catch {
            fatalError("devices.json failed to load: \(error)")
        }
    }()

    init() {
        #if DEBUG
        // UI tests start from no saved preferences with `-HyllaResetDefaults YES`.
        if UserDefaults.standard.bool(forKey: "HyllaResetDefaults"), let domain = Bundle.main.bundleIdentifier {
            UserDefaults.standard.removePersistentDomain(forName: domain)
        }
        #endif
    }

    var body: some Scene {
        WindowGroup {
            RootView(store: store)
                .task { notificationDelegate.store = store }
        }
    }
}
