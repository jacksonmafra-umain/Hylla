import SwiftUI
import UserNotifications

/// The app shell: three top-level destinations in a tab view that adapts its own shape.
///
/// `.sidebarAdaptable` gives a tab bar on iPhone and, on iPad, tabs at the top that can become a
/// sidebar. Unlike Android's chrome, the system decides the shape from the horizontal size class;
/// what the app decides is everything inside it. The cover surface and the scanner take the whole
/// window, so they are drawn instead of the shell rather than inside it.
/// What the overdue schedule depends on; a change to any of them reschedules it.
private struct OverdueInputs: Equatable {
    let fleet: Fleet
    let me: Person.ID?
    let wanted: Bool
}

struct RootView: View {
    enum Destination: Hashable { case fleet, thisDevice, you }

    let store: FleetStore
    @State private var destination = Destination.fleet
    @State private var selection: Device.ID?
    /// The selected device, restored when the system brings the scene back after killing it.
    @SceneStorage("selection") private var storedSelection: String?
    @State private var scanning = false
    @State private var feedback = Feedback()
    /// Who holds this phone. Local only: not an account, never synced.
    @AppStorage("me") private var meRaw = ""

    private var me: Person.ID? { meRaw.isEmpty ? nil : Person.ID(rawValue: meRaw) }
    @AppStorage("notifications.enabled") private var notificationsWanted = false
    @AppStorage("watched") private var watchedRaw = ""
    private let notifier = Notifier(center: UNUserNotificationCenter.current())

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
                            FleetRootView(store: store, selection: $selection, me: me, onScan: { scanning = true })
                        }
                        Tab("This device", systemImage: "info.circle", value: .thisDevice) {
                            ThisDeviceView(posture: window)
                        }
                        Tab("You", systemImage: "person", value: .you) {
                            YouView(fleet: store.fleet, me: $meRaw)
                        }
                    }
                    .tabViewStyle(.sidebarAdaptable)
                    // One banner for the whole shell, above the tab bar, so it stays put when a
                    // pane changes underneath it.
                    .overlay(alignment: .bottom) {
                        FeedbackBanner(feedback: feedback)
                            .padding(.bottom, 56)
                            .animation(.snappy, value: feedback.current)
                    }
                    .environment(feedback)
                }
            }
            .fullScreenCover(isPresented: $scanning) {
                WindowPostureReader { ScanView(store: store, posture: $0, me: me) }
            }
        }
        // A device link shows that device on the fleet, replacing whatever was selected, and
        // closes the scanner if it was open.
        .onOpenURL { url in
            guard let id = DeepLink.parse(url) else { return }
            scanning = false
            destination = .fleet
            selection = id
        }
        .onAppear { if selection == nil { selection = storedSelection.map(Device.ID.init(rawValue:)) } }
        // A watched device that becomes claimable is announced once and leaves the watch list.
        .onChange(of: store.fleet) { before, after in
            let watched = WatchList.decode(watchedRaw)
            for device in Notices.backOnTheShelf(before: before, after: after, watched: watched) {
                if notificationsWanted { Task { await notifier.backOnTheShelf(device) } }
                watchedRaw = WatchList.encode(watched.subtracting([device.id]))
            }
        }
        // Overdue notifications follow the fleet, who holds the phone and the switch.
        .task(id: OverdueInputs(fleet: store.fleet, me: me, wanted: notificationsWanted)) {
            await notifier.scheduleOverdue(store.fleet, me: notificationsWanted ? me : nil, today: .today())
        }
        .onChange(of: selection) { _, new in storedSelection = new?.rawValue }
    }
}
