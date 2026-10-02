#if DEBUG
import CoreGraphics
import Testing
@testable import Hylla

/// Each test shows the wrong rule producing the wrong layout, next to the right rule on the same
/// input. They pass because the bug is real.
struct AntiPatternsTests {
    func panes(_ width: CGFloat, _ height: CGFloat) -> Int {
        PaneLayout.compute(WindowPosture(size: CGSize(width: width, height: height))).paneCount
    }

    @Test func orientationIsNotSizeAPhoneOnItsSideIsNotATablet() {
        #expect(AntiPatterns.panesByOrientation(CGSize(width: 874, height: 402)) == 2)
        #expect(panes(874, 402) == 1)
    }

    @Test func orientationIsNotSizeAnUprightIPadHasRoomForTwo() {
        #expect(AntiPatterns.panesByOrientation(CGSize(width: 1032, height: 1376)) == 1)
        #expect(panes(1032, 1376) == 2)
    }

    @Test func theWindowIsNotTheScreenSlideOverOnALargeIPad() {
        // A 320 pt Slide Over window on a 1376 pt screen.
        #expect(AntiPatterns.panesByScreen(screenWidth: 1376, windowHeight: 1000) == 3)
        #expect(panes(320, 1000) == 1)
    }

    @Test func oneHingeIsAnAssumptionATriFoldsSecondHingeRunsThroughAPane() {
        let hinges = [370.0, 740.0].map {
            Fold(frame: CGRect(x: $0, y: 0, width: 0, height: 900), orientation: .vertical, isSeparating: true, occludes: false)
        }
        let wrong = AntiPatterns.segmentsFromFirstHinge(CGRect(x: 0, y: 0, width: 1110, height: 900), folds: hinges)

        #expect(wrong.count == 2)
        #expect(wrong[1].minX < 740 && 740 < wrong[1].maxX)
        #expect(WindowPosture(size: CGSize(width: 1110, height: 900), folds: hinges).segments.map(\.width) == [370, 370, 370])
    }
}
#endif
