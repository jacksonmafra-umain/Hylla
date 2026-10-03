# Hylla

*Hylla* is Swedish for *shelf*. It is Umain's internal app for borrowing devices from the QA
fleet: find a phone, claim it, return it, and see who has the one you need.

It is also a **codelab on adaptive layout for foldables**, built as two purely native apps that
share nothing but a fixture file:

- **Android:** Kotlin, Jetpack Compose, Navigation 3, `androidx.window`.
- **iOS:** Swift 6, SwiftUI.

![List and detail on a foldable emulator, as panes appear and the window folds](docs/chapters/img/05-fold-two-panes.png)

## Start here

The codelab is in [`docs/chapters`](docs/chapters/README.md): twenty chapters, from the window
posture model to testing. Each one is tagged (`chapter-01` to `chapter-20`) and implemented on
both platforms.

## Build

There is no distribution channel: build from a clean checkout. No network beyond dependency
resolution and no credentials are needed; the apps run from `fixtures/devices.json`.

**Android:** Android 14 (API 34) or later.

```sh
cd android
./gradlew :app:installDebug
./gradlew :app:testDebugUnitTest
ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest   # a foldable emulator
```

**iOS:** iOS 18 or later. The Xcode project is generated from `ios/project.yml` with
[XcodeGen](https://github.com/yonaskolb/XcodeGen).

```sh
cd ios
xcodegen generate
open Hylla.xcodeproj    # set your own team under Signing, then run
xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPhone 18 Pro' test
```

## Repository

| Path | What |
| --- | --- |
| `android/` | The Android app |
| `ios/` | The iOS app |
| `fixtures/devices.json` | The fleet, the only file both apps share ([schema](fixtures/README.md)) |
| `docs/chapters/` | The codelab |
| `docs/assets/` | The icon masters and printable shelf tags |
| `scripts/` | Screenshot matrices for foldable postures and text sizes |

## What Hylla never stores

No serial number, IMEI, EID, UDID or payment account, not even as fake values in fixtures.
A device is identified by its shelf tag (`HYL-001`) and nothing else.
