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
            if let size = Self.windowOverride ?? size {
                content(WindowPosture(
                    size: size,
                    horizontalSizeClass: horizontalSizeClass,
                    verticalSizeClass: verticalSizeClass
                ))
                .frame(width: Self.windowOverride?.width, height: Self.windowOverride?.height)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .onGeometryChange(for: CGSize.self, of: \.windowSize) { size = $0 }
    }

    /// Debug builds only: `-HyllaWindowOverride 330x350` lays the app out as if the window were
    /// that size, the iOS counterpart of `adb shell wm size`. Simulators cannot make a window as
    /// small as a cover screen from a script, so the UI tests use this to reach the cover surface.
    private static var windowOverride: CGSize? {
        #if DEBUG
        guard let value = UserDefaults.standard.string(forKey: "HyllaWindowOverride") else { return nil }
        let parts = value.split(separator: "x").compactMap { Double($0) }
        guard parts.count == 2 else { return nil }
        return CGSize(width: parts[0], height: parts[1])
        #else
        return nil
        #endif
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
