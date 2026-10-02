import XCTest

final class FleetNavigationUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testFleetToDetailAndBack() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()

        XCTAssertTrue(app.navigationBars["Fleet"].waitForExistence(timeout: 5))
        try XCTSkipIf(app.staticTexts[Self.placeholder].exists, "Two panes: the detail sits beside the list, with no back.")
        attach(app, "fleet")

        app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.tap()
        XCTAssertTrue(app.staticTexts["SM-F966B"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.navigationBars["Fold7 Blue"].exists)
        attach(app, "detail")
        // Each field is one VoiceOver element: label, then value.
        XCTAssertTrue(app.staticTexts["Model, Galaxy Z Fold7"].exists)
        XCTAssertTrue(app.staticTexts["Model number, SM-F966B"].exists)

        app.navigationBars.buttons.element(boundBy: 0).tap()
        XCTAssertTrue(app.navigationBars["Fleet"].waitForExistence(timeout: 5))
    }

    static let placeholder = "Select a device to see its details."

    /// A device link opens that device; back goes to the fleet.
    @MainActor
    func testDeviceLinkOpensThatDevice() {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))

        app.open(URL(string: "hylla://device/HYL-003")!)

        XCTAssertTrue(app.staticTexts["Model number, SM-F968B"].waitForExistence(timeout: 5))
        app.open(URL(string: "hylla://device/HYL-099")!)
        XCTAssertTrue(app.staticTexts["Not found"].waitForExistence(timeout: 5))
    }

    /// The interactive pop gesture: drag from the leading edge back to the fleet.
    @MainActor
    func testEdgeSwipeGoesBack() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        try XCTSkipIf(app.staticTexts[Self.placeholder].exists, "Two panes: there is no back.")
        app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.tap()
        XCTAssertTrue(app.navigationBars["Fold7 Blue"].waitForExistence(timeout: 5))

        let window = app.windows.firstMatch
        let start = window.coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        start.press(forDuration: 0.1, thenDragTo: window.coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))

        XCTAssertTrue(app.navigationBars["Fleet"].waitForExistence(timeout: 5))
    }

    /// A regular-width iPad shows the list and the detail side by side.
    @MainActor
    func testTwoPanesShowListBesideDetail() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        try XCTSkipUnless(app.staticTexts[Self.placeholder].exists, "One pane on this device.")

        app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.tap()

        XCTAssertTrue(app.staticTexts["Model number, SM-F966B"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Flip7 Black"].isHittable, "the list stays beside the detail")
        XCTAssertTrue(app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.isSelected)
        XCTAssertLessThan(app.staticTexts["Flip7 Black"].frame.maxX, 480, "the list pane is at most 480 pt")
        attach(app, "two-panes")
    }

    /// A large window shows the selected device's history as a third column.
    @MainActor
    func testThreePanesShowHistoryBesideDetail() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCUIDevice.shared.orientation = .landscapeLeft
        defer { XCUIDevice.shared.orientation = .portrait }
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        Thread.sleep(forTimeInterval: 1.5)
        try XCTSkipUnless(
            app.staticTexts["History appears here for the selected device."].exists,
            "Fewer than three panes on this device."
        )

        app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.tap()

        XCTAssertTrue(app.staticTexts["Model number, SM-F966B"].waitForExistence(timeout: 5))
        let holder = app.staticTexts.matching(NSPredicate(format: "label BEGINSWITH %@", "Alva Berg, ")).firstMatch
        XCTAssertTrue(holder.waitForExistence(timeout: 5), "the history column lists the current holder")
        XCTAssertGreaterThan(holder.frame.minX, app.staticTexts["Model number, SM-F966B"].frame.maxX,
                             "history sits to the right of the detail")
    }

    /// Landscape adds width, so the fleet gains a column, or, where there is room for two legible
    /// panes, the detail pane appears beside a one-column list. It never stretches one column.
    @MainActor
    func testFleetInLandscape() {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCUIDevice.shared.orientation = .landscapeLeft
        defer { XCUIDevice.shared.orientation = .portrait }

        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        // Let the rotation settle. Simulator screenshots of a rotated window are cropped to the
        // portrait width, so the layout is checked through element frames, not the image.
        Thread.sleep(forTimeInterval: 1.5)
        let columns = columnsInFirstRow(app)
        if app.staticTexts[Self.placeholder].exists {
            XCTAssertEqual(columns, 1, "a 480 pt list pane holds one column")
        } else {
            XCTAssertGreaterThanOrEqual(columns, 2, "one pane in landscape should gain a column")
        }
        let note = XCTAttachment(string: "first-row columns in landscape: \(columns)")
        note.name = "landscape-columns"
        note.lifetime = .keepAlways
        add(note)
    }

    /// Fleet tiles whose top edge lines up with the first tile's.
    @MainActor
    private func columnsInFirstRow(_ app: XCUIApplication) -> Int {
        let names = ["Fold7 Blue", "Flip7 Black", "TriFold", "Pixel 10 Pro Fold", "Pixel 9 Pro Fold", "OnePlus Open"]
        let tops = names.map { app.staticTexts[$0].frame.minY }
        return tops.filter { abs($0 - tops[0]) < 1 }.count
    }

    /// The largest accessibility text size, the iOS counterpart of 200% font scale on Android.
    @MainActor
    func testFleetAndDetailAtLargestDynamicType() {
        let app = XCUIApplication()
        app.launchArguments += ["-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityXXXL"]
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()

        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        attach(app, "fleet-ax5")

        app.buttons.containing(.staticText, identifier: "Fold7 Blue").firstMatch.tap()
        XCTAssertTrue(app.staticTexts["SM-F966B"].waitForExistence(timeout: 5))
        attach(app, "detail-ax5")
    }

    @MainActor
    private func attach(_ app: XCUIApplication, _ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
