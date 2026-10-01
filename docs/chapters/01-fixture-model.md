# 01 — Fixture model and the shared `devices.json`

**Tag:** `chapter-01`

## The concept

Every later chapter compares a Compose screen with a SwiftUI screen and asks whether they look
the same. That comparison only means something if both apps start from identical input. This
chapter creates that input, `fixtures/devices.json`, and a native model on each platform that
reads it.

The file is the only artifact the two apps share. There is no shared code, no generated model
and no schema compiler. Each platform parses the same bytes in its own idiom, and both test
suites read the file straight from the repository rather than from a copy.

## Before

The fleet lives in the `QA Devices` Notion database, edited by hand. It has more columns than
Hylla needs, and some of them (serial number, IMEI, EID, UDID, Apple Pay account) identify
hardware or people and must never leave Notion. There was no machine-readable fleet at all.

## Decisions

**Stable codes, not display labels.** Notion stores `In use`, `Needs sorting`, `Office only`.
The fixture stores `inUse`, `needsSorting`, `officeOnly`. Display labels change with copy edits and
translation; codes do not. Mapping Notion labels to codes is the Notion adapter's job, and turning
codes into text is each app's string resources.

**People are referenced, not embedded.** `currentUser` holds a person id that points into a
top-level `people` list. The person picker in chapter 10 needs that list anyway, and a name typed
twice is a name that drifts.

**The shelf tag is the id.** `HYL-001` is what is printed on the shelf and what the scanner in
chapter 7 reads. One identifier, no lookup table.

**`since` is a calendar date, not an instant.** `2026-09-22` is a day on the shelf, not a moment
in time.

- Android uses `java.time.LocalDate`, available on every supported API level.
- iOS has no zoneless date in Foundation. Decoding the string into a `Date` picks a time zone, and
  west of UTC the app shows September 21. The model uses a small `CalendarDate` value type instead.

**Strict decoding, on both platforms.** An unknown key fails the parse. This is what keeps the
forbidden fields out: nobody can paste `"imei": "…"` into the fixture and have it silently
ignored. A missing key also fails, even for nullable fields, so `null` always means *known to be
empty*, never *forgot to fill in*. The two platforms get there differently:

| | Android | iOS |
| --- | --- | --- |
| Unknown key | `Json { ignoreUnknownKeys = false }` — the default, stated explicitly | `Codable` ignores unknown keys, so `FleetFixture.checkKeys` compares each device's keys with `Device.CodingKeys.allCases` |
| Missing nullable key | A nullable property without a default is required | `Codable` decodes a missing optional as `nil`, so the same key check catches it |
| Enum raw values | `@SerialName("inUse")` | `enum AssignmentStatus: String` |
| Typed ids | `@JvmInline value class DeviceId` | `struct ID: RawRepresentable` nested in `Device` |

This is the pattern for the whole codelab: the same rule, reached through what each platform
gives you for free and what it makes you write yourself.

**Validation beyond the shape.** Both platforms run the same checks after decoding and report
the same messages:

- device ids are unique;
- `inUse` exactly when `currentUser` is set;
- every `currentUser` exists in `people`.

## What the app shows

Both apps load the bundled fixture and show the fleet size and the number of devices available
to claim. Decommissioned devices are excluded from the available count, so both show
*19 devices, 9 available*.

- Android adds `fixtures/` as an asset directory in `app/build.gradle.kts`, excluding the README.
- iOS references `../fixtures/devices.json` as a bundle resource in `project.yml`.

Neither copies the file; edit `fixtures/devices.json` and both apps pick up the change.

## Verify

```sh
# Android: 10 tests
cd android && ./gradlew :app:testDebugUnitTest

# iOS: 10 tests
cd ios && xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPhone 18 Pro,OS=27.2' test
```

Checked for this chapter:

- Android emulator, API 35, at font scale 1.0 and 2.0.
- iOS 27.2 simulator, iPhone 18 Pro, at the default text size and the largest accessibility size.
- The title is exposed as a heading to TalkBack and VoiceOver.

There are no posture-dependent layouts yet; chapter 2 introduces the posture model.
