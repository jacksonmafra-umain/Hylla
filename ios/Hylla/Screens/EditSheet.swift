import SwiftUI

/// Edit a record, in a full-height sheet: a form this long needs the whole window. Who holds the
/// device is not editable here; claiming and returning change that. With unsaved changes the
/// sheet cannot be swiped away, and Cancel asks first.
struct EditSheet: View {
    let original: Device
    let onSave: (Device) throws(FleetStore.Failure) -> Void
    /// Set when the sheet registers a draft rather than edits a record.
    var title: String?
    var saveLabel: String?
    @State private var device: Device
    @State private var confirmingDiscard = false
    @State private var error: String?
    @Environment(\.dismiss) private var dismiss

    init(device: Device, title: String? = nil, saveLabel: String? = nil, onSave: @escaping (Device) throws(FleetStore.Failure) -> Void) {
        original = device
        self.onSave = onSave
        self.title = title
        self.saveLabel = saveLabel
        _device = State(initialValue: device)
    }

    private var dirty: Bool { device != original }

    var body: some View {
        NavigationStack {
            Form {
                TextField("Name", text: $device.deviceName)
                Picker("Type", selection: $device.deviceType) {
                    ForEach(DeviceType.allCases, id: \.self) { Text($0.label).tag($0) }
                }
                TextField("OS version", text: $device.osVersion)
                TextField("UI version", text: Binding(
                    get: { device.uiVersion ?? "" },
                    set: { device.uiVersion = $0.isEmpty ? nil : $0 }
                ))
                Picker("Home use", selection: $device.homeUse) {
                    ForEach(HomeUse.allCases, id: \.self) { Text($0.label).tag($0) }
                }
                Picker("Lifecycle", selection: $device.lifecycle) {
                    ForEach(Lifecycle.allCases, id: \.self) { Text($0.label).tag($0) }
                }
                .pickerStyle(.segmented)
                DatePicker("Since", selection: sinceBinding, in: ...Date.now, displayedComponents: .date)
                TextField("Notes", text: Binding(
                    get: { device.notes ?? "" },
                    set: { device.notes = $0.isEmpty ? nil : $0 }
                ), axis: .vertical)
                .lineLimit(3...)
                if let error {
                    Text(error).foregroundStyle(.red)
                }
            }
            .navigationTitle(title ?? String(localized: "Edit \(original.deviceName)"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { if dirty { confirmingDiscard = true } else { dismiss() } }
                        .confirmationDialog("Discard changes?", isPresented: $confirmingDiscard, titleVisibility: .visible) {
                            Button("Discard", role: .destructive) { dismiss() }
                            // Not role .cancel: a cancel button can be left out of the dialog,
                            // and keeping the edits should be a choice you can see.
                            Button("Keep editing") {}
                        } message: {
                            Text("Your edits to this record have not been saved.")
                        }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(saveLabel ?? String(localized: "Save")) {
                        do {
                            try onSave(device)
                            dismiss()
                        } catch {
                            self.error = String(describing: error)
                        }
                    }
                    .disabled((!dirty && saveLabel == nil) || device.deviceName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .presentationDetents([.large])
        .interactiveDismissDisabled(dirty)
    }

    /// A calendar date shown as local midnight on that day.
    private var sinceBinding: Binding<Date> {
        Binding(
            get: { device.since.date() ?? .now },
            set: { device.since = CalendarDate.today(now: $0) }
        )
    }
}
