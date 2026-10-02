# 10 — Bottom sheets, modals, alerts, pickers

**Tag:** `chapter-10`

## The concept

Three sheet heights, each matched to one job. The height says how big the task is.

| Detent | Job | Android | iOS |
| --- | --- | --- | --- |
| **Small** | Quick claim: who takes it, one button | `ModalBottomSheet`, `skipPartiallyExpanded = true`, sized to its content | `.presentationDetents([.height(h)])`, `h` measured from the content |
| **Half** | Filters, with the list visible behind | `ModalBottomSheet`, `skipPartiallyExpanded = false`, opens partially expanded | `.presentationDetents([.medium, .large])` |
| **Full** | Edit a record | `ModalBottomSheet`, `skipPartiallyExpanded = true`, content `fillMaxHeight()` | `.presentationDetents([.large])` |

The small detent is the content's own height, not a fixed fraction, so it grows with Dynamic Type
and font scale instead of clipping. The half detent can be dragged to full height when large text
makes the filters long.

## Pickers

| Picker | Where | Android | iOS |
| --- | --- | --- | --- |
| Platform (any of) | Filters | `FilterChip`s in a `FlowRow` | A `Toggle` per platform in a `Form` |
| Device type | Filters, edit | `SingleChoiceSegmentedButtonRow` (filters), `ExposedDropdownMenuBox` (edit) | `Picker`, menu style |
| Person | Quick claim, *You* | `PersonPicker`: a radio group, `selectableGroup`, 48 dp rows | `Picker(.inline)` |
| Date (`since`) | Edit | `DatePickerDialog` with `SelectableDates` up to today | `DatePicker(in: ...Date.now)` |

**The date picker works in UTC.** Material 3's `DatePicker` selects UTC milliseconds. `since` is
a calendar date, so it maps to midnight UTC on that day and back, never through the local zone.
Otherwise a picker opened west of UTC shows the day before. iOS maps `CalendarDate` to local
midnight for display and reads the calendar day back from the picked `Date`. This is the
zoneless-date decision from chapter 1, showing up again in a picker.

## Alerts and modals

- **Returning asks first.** *Return Tab S10 FE?* names the device and the person who will no
  longer hold it.
  - Android: `AlertDialog`.
  - iOS: `.alert`, with *Return to the shelf* marked destructive.
- **Leaving an edit with unsaved changes asks first.**
  - Android intercepts every way out: `confirmValueChange` refuses `SheetValue.Hidden` while the
    form is dirty, and `onDismissRequest` (back, a tap outside) shows *Discard changes?* instead.
  - iOS uses `.interactiveDismissDisabled(dirty)`, so a swipe down springs back. *Cancel* opens a
    `.confirmationDialog`, the iOS idiom for a choice about the current task.
- **Only one modal at a time.** The detail screen holds `DetailSheet.None / Claim / Return / Edit`
  as one saved value, so a fold or rotation brings back the modal that was open, and two can never
  stack.

## Editing a record

`FleetStore.update` accepts everything except **who holds the device**, which only claiming and
returning change. Moving `since` on a held device moves the start of its open assignment with it.

Validation gained a rule while doing this: **assignments may not overlap**. Moving Fold7 Blue's
`since` to August would make Alva's period overlap Leo's, and the store rejects it and changes
nothing. That rule now covers the fixture too.

## Filters

`FleetFilter` combines its conditions with *and*; inside one condition the values combine with
*or*. *Only devices I can take* means available **and** in service, so the decommissioned Fold4
does not count even though its status is *Available*. The toolbar button shows *Filters (n)* with
the number of conditions set. An empty result says *No devices match these filters.* instead of
showing a blank grid.

On Android the filter is saved state, serialized as JSON, so it survives a fold and process
death.

## What went wrong on the way

**Tests that passed on a build that did not compile.** The first instrumented run for this
chapter reported 9 tests with 1 failure, and three attempts to fix that failure changed nothing.
The test sources did not compile (Espresso was not on the `androidTest` classpath), so every
"result" was the XML left over from an earlier run. The results directory is now cleared before a
run, and the Gradle output is checked, not only the report.

**Back did not reach the sheet.** The edit test sent back through the activity's
`OnBackPressedDispatcher`. A `ModalBottomSheet` has its own window and its own back handling, so
the test now sends back the way the system does, with `Espresso.pressBack()`. The first press only
puts the keyboard away, which a manual run on the emulator showed. The test closes the keyboard
first.

**An inline picker outside a form is a wheel.** iOS's quick-claim sheet first used
`Picker(.inline)` for the person. Inside a `Form` that is a list of rows, but in a plain stack it
renders as a wheel, which is hard to read at large Dynamic Type sizes and could not be driven by
the UI test either. `PersonPicker` is now rows with a checkmark, the same shape as Android's radio
group.

**A cancel-role button can be left out.** *Keep editing* was first `role: .cancel` in the
discard `confirmationDialog`. Recent iOS can leave a cancel button out of the dialog, with tapping
outside as the way to cancel, so the choice was invisible. It is now a plain button you can see.

**The button behind the sheet.** The quick-claim test tapped "the last *Claim* button". That was
the detail's own button, behind the small sheet, and tapping it only dismissed the sheet. The
sheet's button now has an accessibility identifier, `confirm-claim`.

**Lazy grids hide their tiles.** Tiles below the fold are not composed, so `performScrollTo()`
cannot find them. The tests scroll the grid with `performScrollToNode` first.

## Not verified

**Filter toggles on the iPad simulator.** Under XCUITest, neither the toggle's control nor its
row switches the *Android* filter on in the iPad simulator. The same test passes on the iPhone
simulator. The test runner hung twice while this was being tried, so on iPad the test now skips
that step with the reason, instead of asserting something it cannot reach. Presenting the sheet
from `FleetRootView` instead of from inside the split view's sidebar did not change it. A manual
check on an iPad is still owed.

## Verify

```sh
cd android && ./gradlew :app:testDebugUnitTest
ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest   # 9 tests
cd ios && xcodebuild -project Hylla.xcodeproj -scheme Hylla \
  -destination 'platform=iOS Simulator,name=iPhone 18 Pro,OS=27.2' -only-testing:HyllaUITests/SheetsUITests test
```
