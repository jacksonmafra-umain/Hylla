import XCTest

final class ShellUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testTopLevelDestinations() {
        let app = XCUIApplication()
        // Not `-me ""`: a launch argument shadows what the app writes to the same key.
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))

        app.buttons["This device"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Posture: flat"].waitForExistence(timeout: 5))

        app.buttons["You"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Nothing right now."].waitForExistence(timeout: 5))
        app.buttons["Alva Berg"].tap()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5), "Alva holds the Fold7")

        app.buttons["Fleet"].firstMatch.tap()
        XCTAssertTrue(app.buttons.containing(.staticText, identifier: "Flip7 Black").firstMatch.waitForExistence(timeout: 5))
    }
}
