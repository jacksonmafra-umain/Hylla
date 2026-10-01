import SwiftUI

/// Measures the window and hands its ``WindowPosture`` to `content`.
///
/// The size is the window's, safe areas included, to match Android's window metrics: the
/// container's size plus its safe-area insets. It is read with `onGeometryChange`, so the content
/// keeps its normal safe-area layout and no `GeometryReader` wraps the tree.
struct WindowPostureReader<Content: View>: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @State private var size: CGSize?

    @ViewBuilder var content: (WindowPosture) -> Content

    var body: some View {
        Group {
            if let size {
                content(WindowPosture(
                    size: size,
                    horizontalSizeClass: horizontalSizeClass,
                    verticalSizeClass: verticalSizeClass
                ))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .onGeometryChange(for: CGSize.self, of: \.windowSize) { size = $0 }
    }
}

private extension GeometryProxy {
    var windowSize: CGSize {
        CGSize(
            width: size.width + safeAreaInsets.leading + safeAreaInsets.trailing,
            height: size.height + safeAreaInsets.top + safeAreaInsets.bottom
        )
    }
}
