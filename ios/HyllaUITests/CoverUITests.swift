import XCTest

final class CoverUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    private func launchCover(me: String?) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-HyllaWindowOverride", "330x350", "-me", me ?? ""]
        app.launchArguments += ["-HyllaResetDefaults", "YES"]
        app.launch()
        return app
    }

    @MainActor
    func testCoverShowsOneActionAndTheDevicesYouHold() {
        let app = launchCover(me: "p-01")

        XCTAssertTrue(app.buttons["Scan to check out"].waitForExistence(timeout: 5))
        let held = app.staticTexts.matching(NSPredicate(format: "label BEGINSWITH %@", "Fold7 Blue, since ")).firstMatch
        XCTAssertTrue(held.exists)
        XCTAssertFalse(app.navigationBars.firstMatch.exists, "no navigation chrome on the cover")
        attach(app, "cover")

        app.buttons["Scan to check out"].tap()
        XCTAssertTrue(app.textFields["Shelf tag"].waitForExistence(timeout: 5))
        attach(app, "cover-scan")
    }

    @MainActor
    func testCoverWithoutMeAsksWhoYouAre() {
        let app = launchCover(me: nil)

        XCTAssertTrue(app.staticTexts["Open the full app and go to You to choose who you are."].waitForExistence(timeout: 5))
    }

    @MainActor
    private func attach(_ app: XCUIApplication, _ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
