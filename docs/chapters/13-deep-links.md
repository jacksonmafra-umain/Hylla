# 13 — Deep links

**Tag:** `chapter-13`

## The concept

`hylla://device/HYL-003` opens that device. A shelf tag's QR code, a message from a colleague
("the TriFold is back, `hylla://device/HYL-003`") and, in chapter 15, a notification all deliver
the same thing: a device id.

That is why chapter 3 addressed the detail by id from the start. A link needs no new
destination: it builds the back stack the fleet would have had anyway.

## Parsing

`DeepLink.parse` is a pure function on both platforms, and the tests are the same:

| Link | Result |
| --- | --- |
| `hylla://device/HYL-003` | `HYL-003` |
| `hylla://device/hyl-3`, `HYLLA://Device/3`, a trailing `/` | `HYL-003`; the tag is read like a typed one |
| `https://…`, `hylla://fleet/…`, no tag, extra path segments | Not a Hylla link |

A link to a tag that does not exist (`HYL-099`) opens the detail's *Not found* state. It is not
rejected at parse time, because whether a tag exists is the fleet's business, not the parser's.

## Android

- **Manifest:** an `intent-filter` for `VIEW` with `scheme="hylla"` and `host="device"`, marked
  `BROWSABLE` so links from other apps can open it.
- **`launchMode="singleTop"`:** a link that arrives while Hylla is in front is delivered to the
  running activity's `onNewIntent`. Without it, every link would stack another copy of the app on
  top of the first.
- **The link becomes navigation state.** `MainActivity` turns the intent into a `DeviceId?` and
  `HyllaNavigation` consumes it. The stack becomes `[Fleet, Device]` on the *Fleet* destination,
  so back from a linked device goes to the fleet, never to whatever screen happened to be open.
- **Not on recreation.** The link is read only when `savedInstanceState` is null. After a fold or
  rotation, the saved back stack already shows the linked device, and replaying the link would
  undo anything done since.

## iOS

- `CFBundleURLTypes` in the merged `Info.plist` registers `hylla`.
- `.onOpenURL` on `RootView` closes the scanner if it was open, switches to *Fleet* and sets
  `selection`. The one selection drives the stack or the split view, as in chapter 5, so a link
  opens correctly in one, two or three panes.
- **Restoration:** `@SceneStorage("selection")` saves the selected device, so a scene the system
  killed in the background comes back showing it. Android has had this since chapter 3 through
  `rememberNavBackStack`.

## Verify

```sh
adb shell am start -a android.intent.action.VIEW -d "hylla://device/HYL-003" com.umain.hylla
xcrun simctl openurl booted "hylla://device/HYL-003"
```

Observed on `fold_api36`:

| Link | App state | Result |
| --- | --- | --- |
| `HYL-003` | Not running | Cold start straight into the TriFold |
| `hyl-17` | In front | Delivered to the running instance; shows the Mac mini |
| Back after it | | The fleet |
| `HYL-099` | | *No device with shelf tag HYL-099.* |

Tests:

- **Android:** `DeepLinkTest` (unit, 4) and `DeepLinkTest` (instrumented, 2).
- **iOS:** `DeepLinkTests` (unit) and `testDeviceLinkOpensThatDevice` (UI), which uses
  `XCUIApplication.open(_:)`.
