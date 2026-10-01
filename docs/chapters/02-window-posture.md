# 02 — Window posture model

**Tag:** `chapter-02`

## The concept

Every adaptive decision in the rest of the codelab reads one value: `WindowPosture`. It answers
three questions about the window the app is in right now:

1. **How big is it?** Width and height in dp (Android) or points (iOS), and the size class each
   falls into.
2. **What crosses it?** Every fold the system reports, and which of them actually split the layout.
3. **What shape is it in?** Flat, cover, book, tabletop or tri-fold.

It also hands back **segments**: the window divided at each hinge. Chapter 5 lays panes into
those segments, so the panes split at the hinge rather than at 50%.

The model is a plain value with no platform dependencies beyond the size-class breakpoints. That
makes every posture testable in milliseconds, including the ones there is no emulator for.

## Before

There was no model. The easy versions of this code, the ones every chapter 19 anti-pattern
starts from, look like this:

- `if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)` used as a size check;
- `Resources.displayMetrics.widthPixels`, which is the display, not the window;
- `layoutInfo.displayFeatures.firstOrNull()`, which drops the second hinge of a tri-fold;
- `if (fold.state == HALF_OPENED)` used as the only split test.

Each one works on the device it was written on and breaks on another.

## The shape

| | Android | iOS |
| --- | --- | --- |
| Type | `data class WindowPosture` | `struct WindowPosture` |
| Built by | `WindowPosture.compute(widthDp, heightDp, folds)` | `WindowPosture(size:horizontalSizeClass:verticalSizeClass:folds:)` |
| Read in UI | `rememberWindowPosture()` | `WindowPostureReader { posture in … }` |
| Size source | `WindowMetricsCalculator.computeCurrentWindowMetrics(activity)` | `onGeometryChange` on the root container, plus its safe-area insets |
| Fold source | `WindowInfoTracker.windowLayoutInfo(activity)`, every `FoldingFeature` | None. iOS has no public fold API |
| Tests | 16 JUnit tests | 16 Swift Testing tests |

### Size classes

The same breakpoints on both platforms: width 600 / 840 / 1200, height 480 / 900.

- **Android** takes them from `androidx.window`: `WindowSizeClass.BREAKPOINTS_V2` and its
  `WIDTH_DP_*_LOWER_BOUND` and `HEIGHT_DP_*_LOWER_BOUND` constants. The app does not define its own numbers.
- **iOS** has no numeric size classes. `horizontalSizeClass` and `verticalSizeClass` are
  `.compact` or `.regular` and describe how the system treats the window, not how wide it is. An
  iPad window 700 pt wide can be `.compact` in one multitasking arrangement and `.regular` in
  another. So the model carries the system classes for the decisions that are about behaviour
  (chapter 9's navigation chrome), and computes `widthClass` from the measured container for
  every decision that needs a measurement.

**A phone in landscape is `Expanded` width.** 891 × 411 dp crosses 840. Width alone would put a
two-pane layout on a phone turned sideways, which the layout table forbids. The height class is
`Compact` though, and chapter 5 uses that to keep one column. Landscape is not width; it is not
height either; it is both, read separately.

### Folds are a list

`folds` holds every fold the window reports. `hinges` holds the ones that split the layout. Both
are lists all the way through. Nothing in the model, the mapper or the tests ever takes the first
element and assumes it is the only one. A tri-fold reports two hinges and gets three segments.

**`isSeparating`, not `state`, decides a split.**

- A half-open fold separates.
- A flat fold separates if its hinge occludes content (a dual-screen device).
- A flat fold with a seamless panel does not separate.

The state is still carried, for chapter 7, but it never decides the layout.

**Segments are cut at the hinge's bounds.** An occluding hinge 26 dp wide is excluded from both
sides. A hinge off-centre gives unequal segments. Hinges reported out of order still give
segments in reading order.

**Crossing folds are refused.** No hardware reports a vertical and a horizontal separating fold at
once. If one ever does, the model says `Flat` with one segment instead of guessing which split
wins.

### The cover surface

`Cover` is a window narrower than 480 dp with a compact height (under 480 dp): a flip phone's
outer screen at roughly 330 × 350 dp. It is decided from the window alone, so a small
split-screen window or a tiny Stage Manager window gets the cover surface too. That is deliberate:
the window is the only input, and a window that small cannot hold the fleet list either. A narrow
but tall window, such as 330 × 700, stays `Flat`.

## What went wrong on the way

**iOS measured the safe area, not the window.** The first `WindowPostureReader` measured a
background that ignored the safe area and expected the full window. On an iPhone 18 Pro it reported
402 × 778 pt, the safe area alone, instead of 402 × 874. The fix adds the proxy's
`safeAreaInsets` to its size, so the number matches what Android's window metrics report and the
content keeps its normal safe-area layout.

**Tabletop looked unsplit.** The first readout printed segment widths. In tabletop both segments
are the full width, so it read `883 | 883` and looked like nothing had happened. It now prints both
dimensions: `883 × 426 | 883 × 426`.

## Accessibility

- The posture line is a polite live region on Android and posts an announcement on iOS, so
  folding the device is heard as well as seen.
- The window size is deliberately not announced; it changes continuously during a resize.
- At 200% font scale and at the largest accessibility text size the readout wraps without clipping.

## Verify

```sh
cd android && ./gradlew :app:testDebugUnitTest      # 26 tests: fixture and posture
cd ios && xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPhone 18 Pro,OS=27.2' test   # 26 tests
```

Driving postures on the foldable emulator (`fold_api36`):

```sh
adb shell cmd device_state print-states
adb shell cmd device_state state 2      # OPENED
adb shell cmd device_state state 1      # HALF_OPENED
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 1   # rotate a half-open device into tabletop
adb shell cmd device_state state 0      # CLOSED, outer display
```

Observed:

| Device | State | Window | Classes | Posture | Segments |
| --- | --- | --- | --- | --- | --- |
| `fold_api36` | Opened | 852 × 883 dp | expanded · medium | flat | 852 × 883 |
| `fold_api36` | Half-opened | 852 × 883 dp | expanded · medium | book | 426 × 883 \| 426 × 883 |
| `fold_api36` | Half-opened, rotated | 883 × 852 dp | expanded · medium | tabletop | 883 × 426 \| 883 × 426 |
| `fold_api36` | Closed | 443 × 994 dp | compact · expanded | flat | 443 × 994 |
| iPhone 18 Pro, iOS 27.2 | Portrait | 402 × 874 pt | compact · medium | flat | 402 × 874 |
| iPad Pro 13" (M5), iOS 27.2 | Full screen | 1032 × 1376 pt | expanded · expanded | flat | 1032 × 1376 |

Not yet verified on hardware or an emulator, covered by unit tests only:

- **Tri-fold.** No tri-fold emulator image is installed.
- **Cover display.** No flip-phone emulator image is installed.
- **Occluding hinge.** No dual-screen emulator image is installed.

These rows are added when the devices on the shelf are run through the matrix.
