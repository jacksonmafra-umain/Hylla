# 06 — Three panes and the tri-fold

**Tag:** `chapter-06`

## The concept

A window with room for three legible panes shows the fleet, the selected device and that
device's assignment history side by side. A tri-fold reports **two** hinges, and each segment
they leave becomes a pane.

| Posture | Panes |
| --- | --- |
| Tri-fold, two separating vertical hinges | One pane per segment, if all three are at least 360 wide. |
| Flat, `Large` width (1200+), height not compact | List: 28% of the width, 360–420. History: 360. Detail: the rest, at least 360. |
| Otherwise | Chapter 5's rules: two panes or one. |

The fall-back is always *fewer* panes, never narrower ones:

- A tri-fold whose segments are too narrow collapses to one pane. It does not merge two
  segments, because that would put content across a hinge.
- A large window whose detail would drop below 360 gets two panes.

## The history

Chapter 6 needed data the fixture did not have. `devices.json` now has an `assignments` list:
who held which device, from when to when, and `to: null` while it is still held.

Both models parse it strictly and validate it:

- every assignment references a real device and person, and ends on or after it starts;
- an `inUse` device has exactly one open assignment, held by its `currentUser`, starting on its
  `since`;
- every other device has none.

The fixture, both models and both test suites changed in one commit, because Android's strict
decoding rejects the new key until its model knows it. A commit with only the fixture would not
build.

**One history, two places.** History is not a navigation destination; nothing navigates to it.

- With three panes, it is the third pane, beside the detail.
- With one or two panes, the same rows end the detail screen as a *History* section.

## Folds as a list, all the way down

The tri-fold is where a `firstOrNull()` on display features would have bitten. Nothing in this
chapter needed new fold handling:

- Chapter 2's posture model already returns every separating hinge and the segments between them.
- `PaneLayout` takes the segments as they are, two or three.
- The tests feed the hinges in reverse order and still get the panes left to right.

## Android

`TwoPaneSceneStrategy` became `MultiPaneSceneStrategy` and lays out however many panes
`PaneLayout` returns:

- The list entry goes in the first pane, the top detail entry in the second.
- The third pane is a **supporting** pane: a composable fed with the detail's subject. The
  subject is the device id the detail entry carries in its metadata,
  `PaneRole.Detail.metadata(subject = route.id)`.

Panes read `LocalPaneCount`:

- The detail drops its up button when the count is above 1.
- The detail drops its history section when the count is 3.

## iOS

`NavigationSplitView(sidebar:content:detail:)`: fleet, device and history. The three-column
initialiser is chosen only when `PaneLayout` says three, for the same reason as in chapter 5:
the split view's own collapsing follows the system size class, not the measured width.

## Observed

| Window | Panes |
| --- | --- |
| Android, 1333 × 933 dp (`wm size 2000x1400`, `wm density 240` on `pixel6pro_api35`) | 373 \| 600 \| 360 |
| iPad Pro 13" landscape, 1376 pt | 385 \| 631 \| 360 |
| iPad Pro 13" portrait, 1032 pt | two panes, history in the detail |
| Phones | one pane, history in the detail |

**Not run on hardware or an emulator: the tri-fold.** No tri-fold emulator image is available.
The tri-fold layouts are covered by unit tests on both platforms, which feed two hinges at 370 and
740 in a 1110-wide window.

## Verify

```sh
cd android && ./gradlew :app:testDebugUnitTest      # 56 unit tests
cd ios && xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPad Pro 13-inch (M5),OS=27.2' test   # 52 unit + 5 UI
```
