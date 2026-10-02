import XCTest

final class ScanUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testClaimByTypedTagShowsInTheFleet() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["Fold7 Blue"].waitForExistence(timeout: 5))

        app.buttons["Scan"].tap()
        let field = app.textFields["Shelf tag"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("2\n")

        XCTAssertTrue(app.staticTexts["Flip7 Black"].waitForExistence(timeout: 5))
        app.buttons["Claim"].tap()
        XCTAssertTrue(app.staticTexts["Flip7 Black is now with Alva Berg."].waitForExistence(timeout: 5))

        app.buttons["Close"].tap()
        let tile = app.buttons.containing(.staticText, identifier: "Flip7 Black").firstMatch
        XCTAssertTrue(tile.waitForExistence(timeout: 5))
        XCTAssertTrue(tile.staticTexts["In use · Alva Berg"].exists)
    }

    @MainActor
    func testReturningPutsTheDeviceBackOnTheShelf() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.buttons["Scan"].waitForExistence(timeout: 5))

        app.buttons["Scan"].tap()
        let field = app.textFields["Shelf tag"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("hyl-1\n")

        app.buttons["Return to the shelf"].tap()
        XCTAssertTrue(app.staticTexts["Fold7 Blue is back on the shelf."].waitForExistence(timeout: 5))
    }
}
