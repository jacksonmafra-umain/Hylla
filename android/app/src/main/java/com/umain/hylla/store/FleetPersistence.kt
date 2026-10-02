package com.umain.hylla.store

import android.util.AtomicFile
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetFixture
import java.io.File
import kotlinx.serialization.json.Json

/** Where the fleet lives between processes. */
interface FleetPersistence {
    /** The last saved fleet, or null on first run or when what was saved cannot be read. */
    fun load(): Fleet?

    fun save(fleet: Fleet)

    fun append(operation: Operation)
}

/**
 * A snapshot of the whole fleet plus an append-only journal, as files in the app's private
 * storage. For nineteen devices a JSON snapshot is simpler than a database and just as durable:
 * it is written to a temporary file and moved into place, so a crash mid-write leaves the
 * previous snapshot whole. A snapshot that fails validation is ignored, not trusted.
 */
class FileFleetPersistence(directory: File, private val atomic: (File) -> Snapshot = ::AtomicSnapshot) : FleetPersistence {
    private val snapshot = atomic(File(directory, "fleet.json"))
    private val journal = File(directory, "journal.jsonl")
    private val json = Json { encodeDefaults = true }

    override fun load(): Fleet? = runCatching { FleetFixture.parse(snapshot.read() ?: return null) }.getOrNull()

    override fun save(fleet: Fleet) = snapshot.write(json.encodeToString(Fleet.serializer(), fleet))

    override fun append(operation: Operation) =
        journal.appendText(json.encodeToString(Operation.serializer(), operation) + "\n")

    fun journal(): List<Operation> =
        if (!journal.exists()) emptyList() else journal.readLines().filter { it.isNotBlank() }
            .map { json.decodeFromString(Operation.serializer(), it) }

    /** A file written whole or not at all. */
    interface Snapshot {
        fun read(): String?
        fun write(text: String)
    }

    /** `android.util.AtomicFile` on a device. */
    private class AtomicSnapshot(file: File) : Snapshot {
        private val atomic = AtomicFile(file)
        override fun read(): String? = runCatching { atomic.readFully().decodeToString() }.getOrNull()
        override fun write(text: String) {
            val stream = atomic.startWrite()
            try {
                stream.write(text.encodeToByteArray())
                atomic.finishWrite(stream)
            } catch (error: Exception) {
                atomic.failWrite(stream)
                throw error
            }
        }
    }
}

/** For unit tests on the JVM, where `AtomicFile` is not available: write, then rename. */
class RenamingSnapshot(private val file: File) : FileFleetPersistence.Snapshot {
    override fun read(): String? = file.takeIf { it.exists() }?.readText()
    override fun write(text: String) {
        val temporary = File(file.parentFile, file.name + ".new")
        temporary.writeText(text)
        check(temporary.renameTo(file)) { "Could not replace ${file.name}" }
    }
}
