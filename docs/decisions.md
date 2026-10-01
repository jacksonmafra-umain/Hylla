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

## Open

- **Landing page.** Undecided. Blocks the first screenshots.
