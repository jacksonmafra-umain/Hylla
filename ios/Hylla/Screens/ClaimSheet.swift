import SwiftUI

/// Quick claim, in a small sheet sized to its content: who takes it, and one button. The detent
/// is the measured height of the content, so it fits at every Dynamic Type size.
struct ClaimSheet: View {
    let device: Device
    let fleet: Fleet
    let me: Person.ID?
    let onClaim: (Person.ID) -> Void
    @State private var person: Person.ID?
    @State private var height: CGFloat = 320

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Claim \(device.deviceName)")
                .font(.title2.bold())
                .accessibilityAddTraits(.isHeader)
            Text("Claim as")
                .font(.headline)
            PersonPicker(people: fleet.people, selection: $person)
            Button {
                if let person { onClaim(person) }
            } label: {
                Text("Claim").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .accessibilityIdentifier("confirm-claim")
        }
        .padding()
        .fixedSize(horizontal: false, vertical: true)
        .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height = $0 }
        .onAppear { person = person ?? me ?? fleet.people.first?.id }
        .presentationDetents([.height(height)])
        .presentationDragIndicator(.visible)
    }
}
