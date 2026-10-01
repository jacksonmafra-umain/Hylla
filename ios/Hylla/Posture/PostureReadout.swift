import SwiftUI

/// What the posture model sees. A change of posture is announced to VoiceOver.
struct PostureReadout: View {
    let posture: WindowPosture

    var body: some View {
        VStack(spacing: 4) {
            Text("Window \(Int(posture.size.width.rounded())) × \(Int(posture.size.height.rounded())) pt")
            Text("Width \(posture.widthClass.label) · height \(posture.heightClass.label)")
            Text("Size classes \(posture.horizontalSizeClass.label) · \(posture.verticalSizeClass.label)")
            Text("Posture: \(posture.posture.label)")
            Text("Segments: \(posture.segments.map(\.label).joined(separator: " | ")) pt")
        }
        .font(.subheadline)
        .multilineTextAlignment(.center)
        .onChange(of: posture.posture) { _, new in
            AccessibilityNotification.Announcement("Posture: \(new.label)").post()
        }
    }
}

private extension CGRect {
    var label: String {
        "\(Int(width.rounded())) × \(Int(height.rounded()))"
    }
}

private extension WidthClass {
    var label: String {
        switch self {
        case .compact: String(localized: "compact")
        case .medium: String(localized: "medium")
        case .expanded: String(localized: "expanded")
        case .large: String(localized: "large")
        }
    }
}

private extension HeightClass {
    var label: String {
        switch self {
        case .compact: String(localized: "compact")
        case .medium: String(localized: "medium")
        case .expanded: String(localized: "expanded")
        }
    }
}

private extension Optional<UserInterfaceSizeClass> {
    var label: String {
        switch self {
        case .compact: String(localized: "compact")
        case .regular: String(localized: "regular")
        default: String(localized: "unspecified")
        }
    }
}

private extension Posture {
    var label: String {
        switch self {
        case .flat: String(localized: "flat")
        case .cover: String(localized: "cover")
        case .book: String(localized: "book")
        case .tabletop: String(localized: "tabletop")
        case .triFold: String(localized: "tri-fold")
        }
    }
}
