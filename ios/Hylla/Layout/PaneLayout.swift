import CoreGraphics

/// How many panes the window shows side by side, and where each one sits, in window
/// coordinates. One entry means one pane at a time, reached by navigation.
struct PaneLayout: Equatable, Sendable {
    /// The list pane in a flat split: this share of the width, clamped to the range below.
    static let listShare: CGFloat = 0.4
    static let listMaxWidth: CGFloat = 480

    /// Three panes in a flat window: list share and cap, and the history pane's fixed width.
    static let threePaneListShare: CGFloat = 0.28
    static let threePaneListMaxWidth: CGFloat = 420
    static let historyWidth: CGFloat = 360

    var panes: [CGRect]
    var paneCount: Int { panes.count }

    static func single(_ posture: WindowPosture) -> PaneLayout {
        PaneLayout(panes: [posture.window])
    }

    /// As many panes, up to three, as the window has room for at a legible width.
    ///
    /// Separating vertical hinges decide the split by themselves: the panes are the segments they
    /// leave. iOS reports no hinges today, so in practice only the flat rules apply: a large window
    /// gets list, detail and history; a smaller one list and detail; anything narrower one pane.
    static func compute(_ posture: WindowPosture) -> PaneLayout {
        switch posture.posture {
        // The cover surface and tabletop have layouts of their own.
        case .cover, .tabletop:
            return single(posture)
        case .book, .triFold:
            return fromSegments(posture.segments) ?? single(posture)
        case .flat:
            if posture.heightClass == .compact { return single(posture) }
            return flatThree(posture) ?? flatSplit(posture) ?? single(posture)
        }
    }

    private static func fromSegments(_ segments: [CGRect]) -> PaneLayout? {
        guard (2...3).contains(segments.count),
              segments.allSatisfy({ $0.width >= AdaptiveLayout.minPaneWidth }) else { return nil }
        return PaneLayout(panes: segments)
    }

    private static func flatThree(_ posture: WindowPosture) -> PaneLayout? {
        guard posture.widthClass == .large else { return nil }
        let width = posture.size.width
        let height = posture.size.height
        let list = min(max(width * threePaneListShare, AdaptiveLayout.minPaneWidth), threePaneListMaxWidth)
        let historyStart = width - historyWidth
        guard historyStart - list >= AdaptiveLayout.minPaneWidth else { return nil }
        return PaneLayout(panes: [
            CGRect(x: 0, y: 0, width: list, height: height),
            CGRect(x: list, y: 0, width: historyStart - list, height: height),
            CGRect(x: historyStart, y: 0, width: historyWidth, height: height),
        ])
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
