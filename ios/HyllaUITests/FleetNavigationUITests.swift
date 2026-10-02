import XCTest

final class FleetNavigationUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testFleetToDetailAndBack() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.navigationBars["Fleet"].waitForExistence(timeout: 5))
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

    /// Landscape adds width, so the fleet gains a column; it stays one pane.
    @MainActor
    func testFleetInLandscape() {
        let app = XCUIApplication()
        app.launch()
        XCUIDevice.shared.orientation = .landscapeLeft
        defer { XCUIDevice.shared.orientation = .portrait }

        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))
        // Let the rotation settle. Simulator screenshots of a rotated window are cropped to the
        // portrait width, so the layout is checked through element frames, not the image.
        Thread.sleep(forTimeInterval: 1.5)
        let columns = columnsInFirstRow(app)
        XCTAssertGreaterThanOrEqual(columns, 2, "landscape should gain a column")
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
