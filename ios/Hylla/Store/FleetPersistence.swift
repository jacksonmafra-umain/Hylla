import Foundation

/// Where the fleet lives between launches.
protocol FleetPersistence: Sendable {
    /// The last saved fleet, or nil on first run or when what was saved cannot be read.
    func load() -> Fleet?
    func save(_ fleet: Fleet)
    func append(_ operation: Operation)
}

/// A snapshot of the whole fleet plus an append-only journal, as files in Application Support.
/// The snapshot is written with `.atomic`, so a crash mid-write leaves the previous one whole. A
/// snapshot that fails validation is ignored, not trusted.
struct FileFleetPersistence: FleetPersistence {
    let directory: URL

    private var snapshot: URL { directory.appending(path: "fleet.json") }
    private var journal: URL { directory.appending(path: "journal.jsonl") }

    static var standard: FileFleetPersistence {
        let directory = URL.applicationSupportDirectory
        try? FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        return FileFleetPersistence(directory: directory)
    }

    func load() -> Fleet? {
        guard let data = try? Data(contentsOf: snapshot) else { return nil }
        return try? FleetFixture.decode(data)
    }

    func save(_ fleet: Fleet) {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        guard let data = try? encoder.encode(fleet) else { return }
        try? data.write(to: snapshot, options: .atomic)
    }

    func append(_ operation: Operation) {
        guard var line = try? JSONEncoder().encode(operation) else { return }
        line.append(0x0A)
        if let handle = try? FileHandle(forWritingTo: journal) {
            defer { try? handle.close() }
            _ = try? handle.seekToEnd()
            try? handle.write(contentsOf: line)
        } else {
            try? line.write(to: journal, options: .atomic)
        }
    }

    func operations() -> [Operation] {
        guard let text = try? String(contentsOf: journal, encoding: .utf8) else { return [] }
        return text.split(separator: "\n").compactMap { try? JSONDecoder().decode(Operation.self, from: Data($0.utf8)) }
    }
}
