import CoreGraphics

/// Spacing and column counts that adapt to the space available.
///
/// Margins and gutters follow the window's width class. Column counts follow the width of the
/// container they fill, never the window, because a pane in a two-pane layout is narrower than the
/// window it sits in.
enum AdaptiveLayout {
    /// Below this, a pane is not legible. A layout collapses a pane rather than go narrower.
    static let minPaneWidth: CGFloat = 360

    /// Minimum width of one fleet tile at the default text size.
    static let fleetTileMinWidth: CGFloat = 280

    /// Minimum width of one detail field at the default text size.
    static let detailFieldMinWidth: CGFloat = 220

    /// Column minimums grow with text size up to this factor, then stop.
    static let maxTextScale: CGFloat = 2

    static func margin(_ widthClass: WidthClass) -> CGFloat {
        widthClass == .compact ? 16 : 24
    }

    static func gutter(_ widthClass: WidthClass) -> CGFloat {
        widthClass == .compact ? 12 : 16
    }

    /// How many columns of at least `minColumnWidth` fit in `availableWidth`, with `gutter` between
    /// them. Always at least one. A wider container gains columns; each column never grows past
    /// roughly twice its minimum, so nothing stretches.
    ///
    /// `textScale` is the user's text size relative to default. Larger text needs wider columns to
    /// stay legible, so the minimum scales with it, capped at ``maxTextScale``.
    static func columns(
        availableWidth: CGFloat,
        minColumnWidth: CGFloat,
        gutter: CGFloat,
        textScale: CGFloat = 1
    ) -> Int {
        let minimum = minColumnWidth * min(max(textScale, 1), maxTextScale)
        return max(1, Int(((availableWidth + gutter) / (minimum + gutter)).rounded(.down)))
    }
}
