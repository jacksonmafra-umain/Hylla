import SwiftUI

/// Transient feedback inside the app, the iOS counterpart of Android's snackbar.
///
/// iOS has no snackbar or toast, and Apple's guidance avoids pop-up toasts. The platform-correct
/// shape is a short banner tied to what you just did, with an Undo when the change can be undone,
/// a haptic, and a VoiceOver announcement, because a banner nobody can see is not feedback. It stays
/// longer while VoiceOver is running, so it can be reached before it goes.
@MainActor
@Observable
final class Feedback {
    struct Message: Identifiable, Equatable {
        let id = UUID()
        let text: String
        let undo: Bool
    }

    private(set) var current: Message?
    private var onUndo: (() -> Void)?
    private var dismissal: Task<Void, Never>?

    func show(_ text: String, onUndo: (() -> Void)? = nil) {
        let message = Message(text: text, undo: onUndo != nil)
        current = message
        self.onUndo = onUndo
        AccessibilityNotification.Announcement(text).post()
        dismissal?.cancel()
        let seconds: Double = UIAccessibility.isVoiceOverRunning ? 10 : 4
        dismissal = Task { [weak self] in
            try? await Task.sleep(for: .seconds(seconds))
            guard !Task.isCancelled, self?.current == message else { return }
            self?.current = nil
        }
    }

    func undo() {
        onUndo?()
        current = nil
        onUndo = nil
    }
}

struct FeedbackBanner: View {
    let feedback: Feedback

    var body: some View {
        if let message = feedback.current {
            HStack(spacing: 12) {
                Text(message.text)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if message.undo {
                    Button("Undo") { feedback.undo() }
                        .bold()
                }
            }
            .padding()
            .background(.regularMaterial, in: .rect(cornerRadius: 16))
            .padding()
            .frame(maxWidth: 560)
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .sensoryFeedback(.success, trigger: message.id)
            .accessibilityElement(children: .contain)
        }
    }
}
