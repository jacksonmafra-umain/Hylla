import CoreGraphics
import SwiftUI

/// Everything layout is allowed to know about the window: its size, its size classes and the
/// folds that cross it. Built from the container size, never from the screen or the orientation.
///
/// A plain value so every posture can be unit tested without a simulator.
struct WindowPosture: Equatable, Sendable {
    /// Below this width, with a compact height, the window is a cover surface.
    static let coverMaxWidth: CGFloat = 480

    var size: CGSize
    /// The system's coarse classes: `.compact` or `.regular`, nothing in between. They describe
    /// how the system treats the window, not how wide it is, so measurement-driven layout uses
    /// ``widthClass`` instead.
    var horizontalSizeClass: UserInterfaceSizeClass?
    var verticalSizeClass: UserInterfaceSizeClass?
    var widthClass: WidthClass
    var heightClass: HeightClass
    var posture: Posture
    /// Every fold reported, separating or not.
    var folds: [Fold]
    /// The folds that split the layout. Always a list: a tri-fold reports two.
    var hinges: [Fold]
    /// The window divided at each hinge, in reading order. One entry when nothing separates.
    var segments: [CGRect]

    init(
        size: CGSize,
        horizontalSizeClass: UserInterfaceSizeClass? = nil,
        verticalSizeClass: UserInterfaceSizeClass? = nil,
        folds: [Fold] = []
    ) {
        self.size = size
        self.horizontalSizeClass = horizontalSizeClass
        self.verticalSizeClass = verticalSizeClass
        widthClass = WidthClass(width: size.width)
        heightClass = HeightClass(height: size.height)
        self.folds = folds

        // isSeparating, not the fold state, decides whether a fold splits the layout.
        let separating = folds.filter(\.isSeparating)
        let orientations = Set(separating.map(\.orientation))
        let cover = size.width < Self.coverMaxWidth && heightClass == .compact

        posture = if cover {
            .cover
        } else if separating.isEmpty {
            .flat
        } else if orientations.count > 1 {
            // No hardware reports crossing folds. Refuse to guess a split.
            .flat
        } else if orientations.first == .horizontal {
            .tabletop
        } else if separating.count == 1 {
            .book
        } else {
            .triFold
        }
        hinges = posture == .flat || posture == .cover ? [] : separating
        segments = CGRect(origin: .zero, size: size).split(at: hinges)
    }
}

enum Posture: Sendable {
    /// No separating fold: a phone, a tablet, an iPad window in Stage Manager.
    case flat
    /// A window too small for navigation: a cover screen, a narrow Slide Over or Stage Manager window.
    case cover
    /// One vertical separating fold: two panes side by side.
    case book
    /// Horizontal separating folds: content above, controls below.
    case tabletop
    /// Two or more vertical separating folds.
    case triFold
}

/// Width breakpoints shared with Android, in points: 600, 840, 1200.
enum WidthClass: Comparable, Sendable {
    case compact, medium, expanded, large

    init(width: CGFloat) {
        self = switch width {
        case 1200...: .large
        case 840...: .expanded
        case 600...: .medium
        default: .compact
        }
    }
}

/// Height breakpoints shared with Android, in points: 480, 900.
enum HeightClass: Comparable, Sendable {
    case compact, medium, expanded

    init(height: CGFloat) {
        self = switch height {
        case 900...: .expanded
        case 480...: .medium
        default: .compact
        }
    }
}

/// A fold in window coordinates.
///
/// iOS has no public API that reports folds, so on today's hardware the list is always empty and
/// every window is flat or cover. The model still takes a list so it matches Android, and so a
/// posture can be previewed and tested before any hardware reports one.
struct Fold: Equatable, Sendable {
    enum Orientation: Sendable { case vertical, horizontal }

    var frame: CGRect
    var orientation: Orientation
    var isSeparating: Bool
    var occludes: Bool
}

extension CGRect {
    /// Splits at each hinge's frame, so an occluding hinge is excluded from both sides and a hinge
    /// off-centre gives unequal segments. All hinges must share one orientation.
    func split(at hinges: [Fold]) -> [CGRect] {
        guard let first = hinges.first else { return [self] }
        let vertical = first.orientation == .vertical
        let cuts = hinges
            .map { vertical ? ($0.frame.minX, $0.frame.maxX) : ($0.frame.minY, $0.frame.maxY) }
            .sorted { $0.0 < $1.0 }
        let starts = [vertical ? minX : minY] + cuts.map(\.1)
        let ends = cuts.map(\.0) + [vertical ? maxX : maxY]
        return zip(starts, ends)
            .filter { $0.1 > $0.0 }
            .map { start, end in
                vertical
                    ? CGRect(x: start, y: minY, width: end - start, height: height)
                    : CGRect(x: minX, y: start, width: width, height: end - start)
            }
    }
}
