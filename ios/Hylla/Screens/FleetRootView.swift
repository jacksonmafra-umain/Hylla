import SwiftUI

/// Fleet, detail and history: one at a time, two side by side, or all three.
///
/// `selection` is the single source of truth in both. One pane: a `NavigationStack` whose path is
/// derived from the selection. Two or three panes: a `NavigationSplitView` showing it beside the
/// list, with its history as a third column when there is room. Resizing a window in Stage
/// Manager switches between them without losing the selection.
///
/// The panes are laid out in this view's own area, not the window: when the tab view shows its
/// sidebar, the sidebar takes width the panes no longer have.
struct FleetRootView: View {
    let store: FleetStore
    @Binding var selection: Device.ID?
    var me: Person.ID?
    let onScan: () -> Void
    @State private var filter = FleetFilter()
    @State private var filtering = false

    private var fleet: Fleet { store.fleet }

    var body: some View {
        WindowPostureReader { posture in
            let layout = PaneLayout.compute(posture)
            Group {
                switch layout.paneCount {
                case 3: threeColumns(posture, layout)
                case 2: twoColumns(posture, listWidth: layout.panes[0].width)
                default: stack(posture)
                }
            }
            // Beside the list, a new selection changes another pane without moving focus there;
            // say what it now shows, so VoiceOver users know the detail changed.
            .onChange(of: selection) { _, new in
                guard PaneLayout.compute(posture).paneCount > 1, let name = new.flatMap(fleet.device)?.deviceName else { return }
                AccessibilityNotification.Announcement(String(localized: "Showing \(name)")).post()
            }
            // Presented here, where the filter lives, not from inside a split view column: from
            // the sidebar of a NavigationSplitView on iPad the sheet's toggles did not update it.
            .sheet(isPresented: $filtering) { FilterSheet(filter: $filter) }
        }
    }

    private func stack(_ posture: WindowPosture) -> some View {
        NavigationStack(path: Binding(
            get: { selection.map { [$0] } ?? [] },
            set: { selection = $0.last }
        )) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: false, filter: filter, onFilters: { filtering = true }, onScan: onScan)
                .navigationDestination(for: Device.ID.self) { id in
                    DeviceDetailView(store: store, id: id, me: me, widthClass: posture.widthClass)
                }
        }
    }

    private func threeColumns(_ posture: WindowPosture, _ layout: PaneLayout) -> some View {
        NavigationSplitView(columnVisibility: .constant(.all)) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: true, filter: filter, onFilters: { filtering = true }, onScan: onScan)
                // Width last: applied before `.toolbar(removing:)`, it is lost and the column
                // falls back to the system's 320 pt. Fixed, not min/ideal/max: with three columns
                // the system settles on the minimum instead of the ideal (chapter 20).
                .toolbar(removing: .sidebarToggle)
                .navigationSplitViewColumnWidth(layout.panes[0].width)
        } content: {
            Group {
                if let selection {
                    DeviceDetailView(store: store, id: selection, me: me, widthClass: posture.widthClass, showsHistory: false)
                } else {
                    ContentUnavailableView("Select a device to see its details.", systemImage: "iphone.gen3")
                }
            }
            .navigationSplitViewColumnWidth(layout.panes[1].width)
        } detail: {
            HistoryView(fleet: fleet, device: selection)
        }
        .navigationSplitViewStyle(.balanced)
    }

    private func twoColumns(_ posture: WindowPosture, listWidth: CGFloat) -> some View {
        NavigationSplitView(columnVisibility: .constant(.all)) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: true, filter: filter, onFilters: { filtering = true }, onScan: onScan)
                // Width last, as in `threeColumns`.
                .toolbar(removing: .sidebarToggle)
                .navigationSplitViewColumnWidth(min: AdaptiveLayout.minPaneWidth, ideal: listWidth, max: listWidth)
        } detail: {
            if let selection {
                DeviceDetailView(store: store, id: selection, me: me, widthClass: posture.widthClass)
            } else {
                ContentUnavailableView("Select a device to see its details.", systemImage: "iphone.gen3")
            }
        }
        .navigationSplitViewStyle(.balanced)
    }
}

#Preview {
    FleetRootView(store: FleetStore(fleet: try! FleetFixture.load()), selection: .constant(nil), onScan: {})
}
