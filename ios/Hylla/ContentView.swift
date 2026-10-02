import SwiftUI

/// Fleet and detail, one at a time or side by side.
///
/// `selection` is the single source of truth in both. One pane: a `NavigationStack` whose path is
/// derived from the selection. Two panes: a `NavigationSplitView` showing it beside the list.
/// Resizing a window in Stage Manager switches between the two without losing the selection.
struct ContentView: View {
    let fleet: Fleet
    @State private var selection: Device.ID?

    var body: some View {
        WindowPostureReader { posture in
            let layout = PaneLayout.compute(posture)
            if layout.paneCount > 1 {
                split(posture, listWidth: layout.panes[0].width)
            } else {
                stack(posture)
            }
        }
    }

    private func stack(_ posture: WindowPosture) -> some View {
        NavigationStack(path: Binding(
            get: { selection.map { [$0] } ?? [] },
            set: { selection = $0.last }
        )) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: false)
                .navigationDestination(for: Device.ID.self) { id in
                    DeviceDetailView(fleet: fleet, id: id, widthClass: posture.widthClass)
                }
        }
    }

    private func split(_ posture: WindowPosture, listWidth: CGFloat) -> some View {
        NavigationSplitView(columnVisibility: .constant(.all)) {
            FleetView(fleet: fleet, posture: posture, selection: $selection, highlightsSelection: true)
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
    ContentView(fleet: try! FleetFixture.load())
}
