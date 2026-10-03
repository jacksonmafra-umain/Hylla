# Decisions

Architecture and process decisions, newest last. Each entry records what was decided, why, and
what it blocks or unblocks.

## 2026-10-01 — Identity

- Android application ID and namespace: `com.umain.hylla`. iOS bundle identifier: `com.umain.hylla`.
- App label on both platforms: **Hylla**.

## 2026-10-01 — Distribution: developers build locally

There is no distribution channel for v1. Each developer builds and installs from a clean checkout
onto the fleet devices with Android Studio or Xcode. No Firebase App Distribution, no TestFlight,
no Enterprise Program.

Consequence: a clean checkout must build with no credentials, no signing secrets beyond a
developer's own Apple ID team, and no network beyond dependency resolution. Unblocks chapter 1.

## 2026-10-01 — Minimum OS: Android 14, iOS 18

- Android `minSdk = 34`.
- iOS deployment target `18.0`.

High enough that the posture APIs taught in the codelab (`WindowInfoTracker`, `FoldingFeature`,
`NavigationSplitView`, `containerRelativeFrame`) need no compatibility paths, low enough to keep
older fleet devices in rotation. Unblocks chapter 2.

## 2026-10-01 — Notion: read-only in v1

Hylla reads the `QA Devices` database and never writes back. Claims and returns are stored
locally and synced between devices only through what Hylla itself owns. Chapter 16 covers the
local store, the queue and conflicts between Hylla clients; conflicts with browser edits in
Notion are out of scope for v1.

## 2026-10-01 — Notion credentials: local config, development only

The integration token lives in a gitignored file on the developer's machine:

- Android: `android/local.properties`, key `notion.token`.
- iOS: `ios/Config/Secrets.xcconfig`, key `NOTION_TOKEN`.

Neither file is committed; both are listed in `.gitignore`. Without a token the apps run from
`fixtures/devices.json` only. A token is never written to source, fixtures, logs or docs.

## 2026-10-02 — Offline-first is local only: no sync in v1

Chapter 16 makes the store durable on the device: the fleet survives process death, and every
change is appended to an operation journal. Nothing leaves the device. There is no Hylla backend
and Notion stays read-only (2026-10-01), so claims and returns made on one phone are not seen on
another, and there are no cross-device conflicts to resolve.

Considered and not chosen for v1: a simulated remote behind a sync interface, Firebase /
Firestore, and making Notion read-write. The journal is the seam a future sync would read from;
the decision to add one can be made without changing how the store records changes.

Consequence: the feature inventory's "queue, sync, resolve conflicts" is out of scope for v1, and
chapter 16 says so.

## 2026-10-03 — iOS folds from the 27.1 SDK, behind an SDK check

iOS 27.1 reports a dividing hinge through `GeometryProxy.reservedRegions(kind: .division)`. The
pinned toolchain stays Xcode 27.0, which does not have it: no beta is adopted. The call is
compiled only when the SDK's SwiftUICore is 8.0.85 or later
(`#if canImport(SwiftUICore, _version: 8.0.85)`), so a clean checkout builds on Xcode 27.0 and
reports no folds, as before. The Xcode 27.2 beta is used only to check that the path compiles and
passes the same tests. When a release Xcode with the 27.1 SDK is pinned, the check stays: it costs
nothing and documents why the path exists.

## Open

- **Landing page.** Undecided. Blocks the first screenshots.
