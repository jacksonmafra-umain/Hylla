import SwiftUI

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
