# 09 — Navigation chrome by width

**Tag:** `chapter-09`

## The concept

The app now has three top-level destinations:

| Destination | Content |
| --- | --- |
| **Fleet** | List, detail and history, as in chapters 3–6. |
| **This device** | The posture readout, moved out of the fleet list. Chapter 17 adds registering the device. |
| **You** | Who holds this phone, and what they hold. |

The chrome that switches between them changes shape with the window: **bottom bar → navigation
rail → drawer**.

## Android: the rule is the app's

`ChromeLayout.compute(posture)`:

| Window | Chrome | Why |
| --- | --- | --- |
| Cover | None | No room, and nothing to switch to from a shut phone. |
| Separating hinge (book, tri-fold) or tabletop | Bottom bar | A rail on the start edge would take 80 dp from the first segment and move the panes off the hinge. A bar takes height instead, which the split does not use. |
| Compact height, any width | Rail | A phone on its side has ~400 dp of height; a bar would take a fifth of it. *Landscape is not width*, a third time. |
| Flat, compact width | Bottom bar | |
| Flat, medium to large | Rail | |
| Flat, 1600 dp and up | Drawer | Only where its 240 dp do not cost a pane: 1600 − 240 is still `Large`, so three panes remain. |

`NavigationSuiteScaffold` would choose a layout from the size class by itself. It is given
`ChromeLayout`'s choice instead, because its default knows nothing about hinges or a compact
height.

**Panes are laid out in what the chrome leaves.** `WindowPosture.contentArea(chrome)` subtracts
the rail or drawer before `PaneLayout` runs:

- On an unfolded `fold_api36` (852 dp) the panes are 360 \| 412 next to the rail, not 360 \| 492.
- In book posture the chrome is a bar, so nothing is subtracted and the split stays at the
  hinge: 426 \| 426.

## iOS: the shape is the system's

`TabView` with `.tabViewStyle(.sidebarAdaptable)`:

- **iPhone:** a tab bar.
- **iPad:** tabs at the top, which the user can turn into a sidebar.

There is no `ChromeLayout` on iOS, and that is deliberate. The system decides the shape from the
horizontal size class, and the iPad tab bar does not take width at all. What carries over is the
principle that panes are laid out in the space the chrome leaves:

- `FleetRootView` measures its *own* area with `WindowPostureReader`.
- `RootView` measures the window, for the cover decision.

When the sidebar is open it takes width, the fleet's area gets narrower, and `PaneLayout` sees
that.

This is the clearest platform difference so far:

- Android exposes the chrome as a layout the app chooses.
- iOS exposes it as a style the system resolves.

Writing the logic twice would have meant writing Android's logic into iOS.

## The cover and the scanner sit outside the shell

Both take the whole window, so they are drawn *instead of* the shell rather than inside it:

- **Android:** `HyllaNavigation` returns early for them.
- **iOS:** `RootView` swaps in `CoverView` and presents `ScanView` with `fullScreenCover`.

On Android the scanner is drawn outside `NavDisplay`, so it registers its own `BackHandler`.

Back from *This device* or *You* returns to *Fleet* before it leaves the app (`BackHandler` on
Android; iOS tabs have no back).

## Splash screen and brand colours

| | Android | iOS |
| --- | --- | --- |
| Launch | Platform splash screen through theme attributes, `windowSplashScreenBackground` and `windowSplashScreenAnimatedIcon` | `UILaunchScreen` with `UIColorName` and `UIImageName`, in a small `Info.plist` merged into the generated one |
| Colours | `HyllaTheme`, light and dark schemes from the icon's palette | `AccentColor` with light and dark variants from the same palette |

At minSdk 34 the platform has had a splash screen API since Android 12, so the
`core-splashscreen` compatibility library is not needed. This is exactly the kind of
compatibility path a high OS floor removes.

The colours are the brand's, not Android's dynamic colour. The codelab compares the two apps side
by side, and they should look like the same product.

## What went wrong on the way

**A launch argument shadowed the app's own writes.** The first iOS shell test passed `-me ""`
to start with nobody chosen, then picked Alva on *You* and expected her devices. They never
appeared. Launch arguments go into `UserDefaults`' argument domain, which takes precedence over
the app's own domain. So `@AppStorage("me")` wrote Alva, and reading it back still returned `""`.
Debug builds now accept `-HyllaResetDefaults YES`, which clears the saved preferences at launch
instead of shadowing them.

## Observed

| `fold_api36` | Chrome | Panes |
| --- | --- | --- |
| Closed, portrait | Bottom bar | 1 |
| Closed, landscape | Rail | 1 |
| Opened, flat | Rail | 360 \| 412 |
| Half-opened, book | Bottom bar | 426 \| 426 |
| `wm size 2600x1500`, `wm density 240` (1733 dp) | Drawer | 3 |

## Verify

```sh
cd android && ./gradlew :app:testDebugUnitTest      # ChromeLayoutTest: 9 tests
ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest
cd ios && xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPad Pro 13-inch (M5),OS=27.2' test
```
