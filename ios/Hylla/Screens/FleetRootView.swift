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
    let onScan: () -> Void

    private var fleet: Fleet { store.fleet }

    var body: some View {
        WindowPostureReader { posture in
            let layout = PaneLayout.compute(posture)
            switch layout.paneCount {
            case 3: threeColumns(posture, layout)
            case 2: twoColumns(posture, listWidth: layout.panes[0].width)
            default: stack(posture)
            }
        }
    }

    private func stack(_ posture: WindowPosture) -> some View {
        NavigationStack(path: Binding(
            get: { selection.map { [$0] } ?? [] },
            set: { selection = $0.last }
        )) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: false, onScan: onScan)
                .navigationDestination(for: Device.ID.self) { id in
                    DeviceDetailView(fleet: fleet, id: id, widthClass: posture.widthClass)
                }
        }
    }

    private func threeColumns(_ posture: WindowPosture, _ layout: PaneLayout) -> some View {
        NavigationSplitView(columnVisibility: .constant(.all)) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: true, onScan: onScan)
                .navigationSplitViewColumnWidth(min: AdaptiveLayout.minPaneWidth, ideal: layout.panes[0].width, max: layout.panes[0].width)
                .toolbar(removing: .sidebarToggle)
        } content: {
            Group {
                if let selection {
                    DeviceDetailView(fleet: fleet, id: selection, widthClass: posture.widthClass, showsHistory: false)
                } else {
                    ContentUnavailableView("Select a device to see its details.", systemImage: "iphone.gen3")
                }
            }
            .navigationSplitViewColumnWidth(ideal: layout.panes[1].width)
        } detail: {
            HistoryView(fleet: fleet, device: selection)
        }
        .navigationSplitViewStyle(.balanced)
    }

    private func twoColumns(_ posture: WindowPosture, listWidth: CGFloat) -> some View {
        NavigationSplitView(columnVisibility: .constant(.all)) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: true, onScan: onScan)
                .navigationSplitViewColumnWidth(min: AdaptiveLayout.minPaneWidth, ideal: listWidth, max: listWidth)
                .toolbar(removing: .sidebarToggle)
        } detail: {
            if let selection {
                DeviceDetailView(fleet: fleet, id: selection, widthClass: posture.widthClass)
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
