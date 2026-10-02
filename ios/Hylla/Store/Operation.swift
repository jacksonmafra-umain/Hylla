import Foundation

/// One change to the fleet, as it was asked for, in the same JSON shape as Android's journal.
/// Nothing reads them back today; they are the seam a future sync would send from.
enum Operation: Codable, Equatable, Sendable {
    case claim(device: Device.ID, person: Person.ID, on: CalendarDate)
    case `return`(device: Device.ID, on: CalendarDate)
    case edit(device: Device.ID)
    case undo

    private enum CodingKeys: String, CodingKey { case type, device, person, on }

    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        switch self {
        case let .claim(device, person, on):
            try container.encode("claim", forKey: .type)
            try container.encode(device, forKey: .device)
            try container.encode(person, forKey: .person)
            try container.encode(on, forKey: .on)
        case let .return(device, on):
            try container.encode("return", forKey: .type)
            try container.encode(device, forKey: .device)
            try container.encode(on, forKey: .on)
        case let .edit(device):
            try container.encode("edit", forKey: .type)
            try container.encode(device, forKey: .device)
        case .undo:
            try container.encode("undo", forKey: .type)
        }
    }

    init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        switch try container.decode(String.self, forKey: .type) {
        case "claim":
            self = .claim(device: try container.decode(Device.ID.self, forKey: .device),
                          person: try container.decode(Person.ID.self, forKey: .person),
                          on: try container.decode(CalendarDate.self, forKey: .on))
        case "return":
            self = .return(device: try container.decode(Device.ID.self, forKey: .device),
                           on: try container.decode(CalendarDate.self, forKey: .on))
        case "edit":
            self = .edit(device: try container.decode(Device.ID.self, forKey: .device))
        case "undo":
            self = .undo
        case let other:
            throw DecodingError.dataCorruptedError(forKey: .type, in: container, debugDescription: "Unknown operation \(other)")
        }
    }
}
