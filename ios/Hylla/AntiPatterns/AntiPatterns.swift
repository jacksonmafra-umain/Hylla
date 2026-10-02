#if DEBUG
import CoreGraphics

/// The mistakes chapter 19 is about, written on purpose, in debug builds only. The tests next to
/// them show each one giving the wrong layout where the correct rule does not.
enum AntiPatterns {
    /// Wrong: landscape means two panes. Orientation is not size.
    static func panesByOrientation(_ size: CGSize) -> Int {
        size.width > size.height ? 2 : 1
    }

    /// Wrong: lay out for `UIScreen.main.bounds` instead of the window. In Split View, Slide Over
    /// or Stage Manager the screen is wider than the window.
    static func panesByScreen(screenWidth: CGFloat, windowHeight: CGFloat) -> Int {
        PaneLayout.compute(WindowPosture(size: CGSize(width: screenWidth, height: windowHeight))).paneCount
    }

    /// Wrong: take the first fold and ignore the rest.
    static func segmentsFromFirstHinge(_ window: CGRect, folds: [Fold]) -> [CGRect] {
        guard let first = folds.first(where: \.isSeparating) else { return [window] }
        return window.split(at: [first])
    }
}
#endif
