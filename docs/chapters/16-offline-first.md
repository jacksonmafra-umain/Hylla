# 16 — Offline-first: the local store

**Tag:** `chapter-16`

## The concept

Hylla works with no network at all, and has since chapter 1: the fleet is bundled, and every
claim, return and edit happens on the device. What it lacked was **durability**. Until now the
store lived in memory, so a killed process forgot everything since launch, and chapter 15's daily
overdue check in a fresh process saw the fixture instead of the real holders.

This chapter makes the store durable:

- **A snapshot of the whole fleet** is written after every successful change.
- **An append-only journal** records each change as it was asked for: claim, return, edit, undo.

Both are written *before* the UI confirms the change, so a snackbar that says *Flip7 Black is now
with Noah* is never ahead of the disk.

## Scope: local only

The decision log (2026-10-02) keeps v1 local. There is no Hylla backend and Notion is read-only, so
nothing leaves the device:

- claims made on one phone are not seen on another;
- there is no queue to send and no cross-device conflict to resolve.

The feature inventory's *queue, sync, resolve conflicts* is out of scope by that decision. The
journal is where a sync would start: it already holds every change in order, in the same JSON shape
on both platforms.

## Files, not a database

| | Android | iOS |
| --- | --- | --- |
| Where | `filesDir` (app-private) | Application Support |
| Snapshot | `fleet.json`, written through `android.util.AtomicFile` | `fleet.json`, written with `Data.write(options: .atomic)` |
| Journal | `journal.jsonl`, one operation per line | the same |

For nineteen devices, a JSON snapshot is simpler than Room or SwiftData and just as durable. An
atomic write goes to a temporary file and is moved into place, so a crash mid-write leaves the
previous snapshot whole. The cost of rewriting the whole fleet on each change is a few kilobytes.
Should the fleet grow to thousands of devices, a database becomes worth its setup.

## Never trust what you saved

On launch the store loads the snapshot **through the same strict decoder and validation as the
fixture**. If the snapshot cannot be read, or it breaks the fleet's rules (an in-use device with
nobody holding it, overlapping assignments), it is ignored and the fixture is used instead. A bad
save can cost the changes since the last good one. It can never crash the app at launch or show an
impossible fleet.

## Observed

On `fold_api36`: claimed the Find N5 for Elias Holm, sent the app home, ran `adb shell am kill
com.umain.hylla`, and relaunched (`LaunchState: COLD`). The detail still shows *In use · Elias
Holm*, and `files/journal.jsonl` holds:

```json
{"type":"claim","device":"HYL-007","person":"p-04","on":"2026-10-02"}
```

## Tests

Both platforms run the same six cases against a temporary directory:

1. The first run starts from the fixture.
2. A claim survives a new store over the same files, which is what the next process sees.
3. Every change is journalled in order, and failed changes are not.
4. Undo is saved too.
5. An unreadable snapshot falls back to the fixture.
6. A snapshot that breaks the rules is not trusted.

iOS adds a seventh: the journal line has exactly Android's shape.

**UI tests start clean.** Debug builds launched with `-HyllaResetDefaults YES` now also delete the
saved fleet and journal. Every UI test passes it, so each test starts from the fixture whatever the
one before it claimed.
