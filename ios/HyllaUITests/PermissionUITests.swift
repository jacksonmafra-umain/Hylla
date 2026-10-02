import XCTest

final class PermissionUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// The camera is asked for on the scanner, with a reason, not at launch.
    @MainActor
    func testCameraIsAskedForOnlyWhenScanning() {
        let app = XCUIApplication()
        app.resetAuthorizationStatus(for: .camera)
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))

        // The scanner still explaining and offering to ask means nothing asked for the camera
        // at launch: an answered or pending request would not leave it "not asked".
        app.buttons["Scan"].tap()
        // Right after a permission reset the full-screen cover takes longer to come up.
        XCTAssertTrue(app.staticTexts["Hylla uses the camera only to read shelf tags. Nothing is recorded or stored."].waitForExistence(timeout: 10))
        app.buttons["Use the camera"].tap()

        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let allow = springboard.alerts.firstMatch.buttons.matching(identifier: "Allow").firstMatch
        if allow.waitForExistence(timeout: 5) { allow.tap() }

        // The simulator has no camera to scan with, so the scanner says so and keeps the typed path.
        XCTAssertTrue(app.staticTexts["This device cannot scan codes. Type the tag below."].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textFields["Shelf tag"].exists)
    }
}
