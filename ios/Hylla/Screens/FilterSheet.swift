import SwiftUI

/// Filters, in a sheet that opens at half height: enough to see the list change behind it, and
/// draggable to full height when the text is large. Changes apply as they are made.
struct FilterSheet: View {
    @Binding var filter: FleetFilter
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Platform") {
                    ForEach(Platform.allCases, id: \.self) { platform in
                        Toggle(platform.label, isOn: Binding(
                            get: { filter.platforms.contains(platform) },
                            set: { if $0 { filter.platforms.insert(platform) } else { filter.platforms.remove(platform) } }
                        ))
                    }
                }
                Section {
                    Picker("Type", selection: $filter.type) {
                        Text("Any").tag(DeviceType?.none)
                        ForEach(DeviceType.allCases, id: \.self) { Text($0.label).tag(Optional($0)) }
                    }
                }
                Section("Status") {
                    ForEach(AssignmentStatus.allCases, id: \.self) { status in
                        Toggle(status.label, isOn: Binding(
                            get: { filter.statuses.contains(status) },
                            set: { if $0 { filter.statuses.insert(status) } else { filter.statuses.remove(status) } }
                        ))
                    }
                }
                Section {
                    Toggle("Only devices I can take", isOn: $filter.claimableOnly)
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Clear") { filter = FleetFilter() }.disabled(filter.activeCount == 0)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
