import SwiftUI

@main
struct HyllaApp: App {
    @UIApplicationDelegateAdaptor(NotificationDelegate.self) private var notificationDelegate
    /// Seeded from the bundled fixture. A broken fixture is a build-time mistake, so failing
    /// loudly is right.
    @State private var store: FleetStore

    init() {
        #if DEBUG
        // UI tests start from no saved preferences and the bundled fleet with
        // `-HyllaResetDefaults YES`.
        if UserDefaults.standard.bool(forKey: "HyllaResetDefaults"), let domain = Bundle.main.bundleIdentifier {
            UserDefaults.standard.removePersistentDomain(forName: domain)
            for name in ["fleet.json", "journal.jsonl"] {
                try? FileManager.default.removeItem(at: URL.applicationSupportDirectory.appending(path: name))
            }
        }
        #endif
        do {
            _store = State(initialValue: FleetStore(fleet: try FleetFixture.load(), persistence: FileFleetPersistence.standard))
        } catch {
            fatalError("devices.json failed to load: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView(store: store)
                .task { notificationDelegate.store = store }
        }
    }
}
