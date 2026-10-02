import SwiftUI

/// One of the fleet's people, as rows with a checkmark. Outside a `Form`, an inline `Picker` is a
/// wheel, which is hard to read at large Dynamic Type sizes; rows grow with the text instead.
struct PersonPicker: View {
    let people: [Person]
    @Binding var selection: Person.ID?

    var body: some View {
        VStack(spacing: 0) {
            ForEach(people) { person in
                let selected = person.id == selection
                Button {
                    selection = person.id
                } label: {
                    HStack {
                        Text(person.name)
                        Spacer()
                        if selected {
                            Image(systemName: "checkmark").accessibilityHidden(true)
                        }
                    }
                    .frame(minHeight: 44)
                    .contentShape(.rect)
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(selected ? .isSelected : [])
                Divider()
            }
        }
    }
}
