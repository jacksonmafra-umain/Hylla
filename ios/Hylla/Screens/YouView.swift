import SwiftUI
import UserNotifications

/// Who holds this phone, and what they hold.
struct YouView: View {
    let fleet: Fleet
    /// The raw id of who holds this phone, empty when not chosen.
    @Binding var me: String

    var body: some View {
        NavigationStack {
            Form {
                Picker("Who is holding this phone?", selection: $me) {
                    ForEach(fleet.people) { Text($0.name).tag($0.id.rawValue) }
                }
                .pickerStyle(.inline)
                NotificationsSection()
                Section("You hold") {
                    let held = fleet.devices.filter { !me.isEmpty && $0.currentUser?.rawValue == me }
                    if held.isEmpty {
                        Text("Nothing right now.")
                    }
                    ForEach(held) { Text($0.deviceName) }
                }
            }
            .navigationTitle("You")
        }
    }
}

/// The notification permission is asked for here, when the switch is turned on, never at launch.
private struct NotificationsSection: View {
    @AppStorage("notifications.enabled") private var wanted = false
    @State private var permission = PermissionState.notAsked
    @Environment(\.openURL) private var openURL
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        Section {
            Toggle("Notify me about devices", isOn: Binding(
                get: { wanted && permission == .granted },
                set: { on in
                    wanted = on
                    guard on, permission != .granted else { return }
                    if permission == .blocked {
                        if let url = URL(string: UIApplication.openNotificationSettingsURLString) { openURL(url) }
                    } else {
                        Task {
                            _ = try? await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge])
                            await refresh()
                        }
                    }
                }
            ))
        } header: {
            Text("Notifications")
        } footer: {
            Text(permission == .blocked && wanted
                 ? "Notifications are off for Hylla in Settings."
                 : "When a device you hold is overdue, and when one you are waiting for is back on the shelf.")
        }
        .task { await refresh() }
        .onChange(of: scenePhase) { _, phase in if phase == .active { Task { await refresh() } } }
    }

    private func refresh() async {
        permission = PermissionState(notifications: await UNUserNotificationCenter.current().notificationSettings().authorizationStatus)
    }
}
