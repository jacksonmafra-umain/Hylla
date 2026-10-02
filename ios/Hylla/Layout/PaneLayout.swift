import CoreGraphics

/// How many panes the window shows side by side, and where each one sits, in window
/// coordinates. One entry means one pane at a time, reached by navigation.
struct PaneLayout: Equatable, Sendable {
    /// The list pane in a flat split: this share of the width, clamped to the range below.
    static let listShare: CGFloat = 0.4
    static let listMaxWidth: CGFloat = 480

    var panes: [CGRect]
    var paneCount: Int { panes.count }

    static func single(_ posture: WindowPosture) -> PaneLayout {
        PaneLayout(panes: [posture.window])
    }

    /// Two panes when the window has room for two legible ones, else one.
    ///
    /// A separating vertical hinge decides the split by itself: the panes are the segments it
    /// leaves. iOS reports no hinges today, so in practice only the flat rule applies: the list
    /// takes ``listShare`` of the width and the detail the rest.
    static func compute(_ posture: WindowPosture) -> PaneLayout {
        switch posture.posture {
        // The cover surface and tabletop have layouts of their own.
        case .cover, .tabletop, .triFold:
            return single(posture)
        case .book:
            return fromSegments(posture.segments) ?? single(posture)
        case .flat:
            if posture.heightClass == .compact { return single(posture) }
            return flatSplit(posture) ?? single(posture)
        }
    }

    private static func fromSegments(_ segments: [CGRect]) -> PaneLayout? {
        guard segments.count == 2, segments.allSatisfy({ $0.width >= AdaptiveLayout.minPaneWidth }) else { return nil }
        return PaneLayout(panes: segments)
    }

    private static func flatSplit(_ posture: WindowPosture) -> PaneLayout? {
        let width = posture.size.width
        let list = min(max(width * listShare, AdaptiveLayout.minPaneWidth), listMaxWidth)
        guard width - list >= AdaptiveLayout.minPaneWidth else { return nil }
        let window = posture.window
        return PaneLayout(panes: [
            CGRect(x: 0, y: 0, width: list, height: window.height),
            CGRect(x: list, y: 0, width: width - list, height: window.height),
        ])
    }
}

extension WindowPosture {
    var window: CGRect { CGRect(origin: .zero, size: size) }
}
