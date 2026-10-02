import XCTest

final class NotificationUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Waiting for a device, then seeing it returned, posts "back on the shelf".
    @MainActor
    func testAWatchedDeviceReturnedIsAnnounced() {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")

        app.buttons["You"].firstMatch.tap()
        let toggle = app.switches["Notify me about devices"]
        XCTAssertTrue(toggle.waitForExistence(timeout: 5))
        toggle.switches.firstMatch.tap()
        let allow = springboard.alerts.firstMatch.buttons.matching(identifier: "Allow").firstMatch
        if allow.waitForExistence(timeout: 5) { allow.tap() }

        app.buttons["Fleet"].firstMatch.tap()
        app.buttons.containing(.staticText, identifier: "TriFold").firstMatch.tap()
        app.buttons["Notify me when it is back"].tap()
        XCTAssertTrue(app.buttons["Stop waiting for it"].waitForExistence(timeout: 5))
        app.buttons["Return to the shelf"].tap()
        app.alerts.firstMatch.buttons["Return to the shelf"].tap()

        let banner = springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "TriFold is back on the shelf")).firstMatch
        XCTAssertTrue(banner.waitForExistence(timeout: 10))
        XCTAssertFalse(app.buttons["Stop waiting for it"].exists, "the device left the watch list")
    }
}
