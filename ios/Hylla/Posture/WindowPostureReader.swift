import SwiftUI

/// Measures the window and hands its ``WindowPosture`` to `content`.
///
/// The size is the window's, safe areas included, to match Android's window metrics: the
/// container's size plus its safe-area insets. It is read with `onGeometryChange`, so the content
/// keeps its normal safe-area layout and no `GeometryReader` wraps the tree.
///
/// Folds come from the same measurement: from iOS 27.1, a hinge that divides the window is a
/// reserved region of kind `.division`. Size classes alone cannot describe that window: an
/// unfolded iPhone Duo is regular by regular, like an iPad, with a hinge down the middle.
struct WindowPostureReader<Content: View>: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @State private var measured: WindowMeasurement?

    @ViewBuilder var content: (WindowPosture) -> Content

    var body: some View {
        Group {
            if let measured = Self.windowOverride.map({ WindowMeasurement(size: $0, folds: []) }) ?? measured {
                content(WindowPosture(
                    size: measured.size,
                    horizontalSizeClass: horizontalSizeClass,
                    verticalSizeClass: verticalSizeClass,
                    folds: measured.folds
                ))
                .frame(width: Self.windowOverride?.width, height: Self.windowOverride?.height)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .onGeometryChange(for: WindowMeasurement.self, of: \.windowMeasurement) { measured = $0 }
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

private struct WindowMeasurement: Equatable {
    var size: CGSize
    var folds: [Fold]
}

private extension GeometryProxy {
    var windowMeasurement: WindowMeasurement {
        let origin = CGPoint(x: safeAreaInsets.leading, y: safeAreaInsets.top)
        return WindowMeasurement(
            size: CGSize(
                width: size.width + safeAreaInsets.leading + safeAreaInsets.trailing,
                height: size.height + safeAreaInsets.top + safeAreaInsets.bottom
            ),
            folds: divisions.map { Fold(division: $0, origin: origin) }
        )
    }

    /// The regions the system reserves to divide this view: a hinge. `.fixed` keeps them in
    /// left-to-right coordinates, like the segments they cut (Hylla is not right-to-left).
    ///
    /// `reservedRegions` exists from the iOS 27.1 SDK. The pinned toolchain is Xcode 27.0, so the
    /// call is compiled only when the SDK's SwiftUICore has it (8.0.85 or later; Xcode 27.0 ships
    /// 8.0.84). An OS or compiler check cannot tell the two SDKs apart. Built with Xcode 27.0, the
    /// app reports no folds, as it did before.
    var divisions: [CGRect] {
        #if canImport(SwiftUICore, _version: 8.0.85)
        if #available(iOS 27.1, *) {
            return reservedRegions(kind: .division, layoutDirectionBehavior: .fixed).map(\.frame)
        }
        #endif
        return []
    }
}
