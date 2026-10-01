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
