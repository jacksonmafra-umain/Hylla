import SwiftUI
import UIKit

/// A vertical grid whose column count comes from its own width.
///
/// `GridItem(.adaptive(minimum:))` uses the same formula, but computing the count with
/// ``AdaptiveLayout/columns(availableWidth:minColumnWidth:gutter:textScale:)`` keeps it unit tested
/// and identical to Android. The width is read with `onGeometryChange`, not a `GeometryReader`.
struct AdaptiveGrid<Content: View>: View {
    let widthClass: WidthClass
    let minColumnWidth: CGFloat
    @ViewBuilder var content: Content

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var width: CGFloat = 0

    var body: some View {
        let gutter = AdaptiveLayout.gutter(widthClass)
        let count = AdaptiveLayout.columns(
            availableWidth: width,
            minColumnWidth: minColumnWidth,
            gutter: gutter,
            textScale: textScale
        )
        LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: gutter, alignment: .top), count: count),
            alignment: .leading,
            spacing: gutter
        ) {
            content
        }
        .frame(maxWidth: .infinity)
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { width = $0 }
    }

    /// Body text size relative to the default, the counterpart of Android's `fontScale`.
    private var textScale: CGFloat {
        let traits = UITraitCollection(preferredContentSizeCategory: UIContentSizeCategory(dynamicTypeSize))
        return UIFontMetrics(forTextStyle: .body).scaledValue(for: 1, compatibleWith: traits)
    }
}
