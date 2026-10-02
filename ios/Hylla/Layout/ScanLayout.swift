import CoreGraphics

/// Where the scanner's viewfinder and its controls go, in window coordinates.
///
/// In tabletop the device stands on its own folded half: the viewfinder takes the top segment,
/// facing the shelf, and the claim form the bottom one, in the thumb zone. iOS reports no hinges,
/// so on iOS this is the stacked or side-by-side layout; tabletop is unit tested with synthetic
/// folds so the two platforms cannot drift.
struct ScanLayout: Equatable, Sendable {
    /// In a flat portrait window, the viewfinder's share of the height.
    static let viewfinderShare: CGFloat = 0.45

    var viewfinder: CGRect
    var controls: CGRect
    var sideBySide: Bool

    static func compute(_ posture: WindowPosture) -> ScanLayout {
        let window = posture.window
        if posture.posture == .tabletop, let top = posture.segments.first, let bottom = posture.segments.last {
            return ScanLayout(viewfinder: top, controls: bottom, sideBySide: false)
        }
        if posture.heightClass == .compact && posture.posture != .cover {
            let (left, right) = window.divided(atDistance: window.width / 2, from: .minXEdge)
            return ScanLayout(viewfinder: left, controls: right, sideBySide: true)
        }
        let (top, bottom) = window.divided(atDistance: window.height * viewfinderShare, from: .minYEdge)
        return ScanLayout(viewfinder: top, controls: bottom, sideBySide: false)
    }
}
