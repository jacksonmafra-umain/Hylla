import CoreGraphics
import Testing
@testable import Hylla

struct WindowPostureTests {
    func verticalFold(x: CGFloat, height: CGFloat, width: CGFloat = 0, separating: Bool = true) -> Fold {
        Fold(frame: CGRect(x: x, y: 0, width: width, height: height), orientation: .vertical,
             isSeparating: separating, occludes: width > 0)
    }

    func horizontalFold(y: CGFloat, width: CGFloat) -> Fold {
        Fold(frame: CGRect(x: 0, y: y, width: width, height: 0), orientation: .horizontal,
             isSeparating: true, occludes: false)
    }

    func posture(_ width: CGFloat, _ height: CGFloat, _ folds: [Fold] = []) -> WindowPosture {
        WindowPosture(size: CGSize(width: width, height: height), folds: folds)
    }

    // Size classes

    @Test(arguments: [
        (599.0, WidthClass.compact), (600, .medium), (839, .medium),
        (840, .expanded), (1199, .expanded), (1200, .large), (1600, .large),
    ])
    func widthBreakpoints(width: Double, expected: WidthClass) {
        #expect(posture(width, 800).widthClass == expected)
    }

    @Test(arguments: [(479.0, HeightClass.compact), (480, .medium), (899, .medium), (900, .expanded)])
    func heightBreakpoints(height: Double, expected: HeightClass) {
        #expect(posture(700, height).heightClass == expected)
    }

    @Test func systemSizeClassesAreCarriedButNotMeasured() {
        // An iPad window at 700 pt can be reported .compact by the system in Split View.
        let window = WindowPosture(size: CGSize(width: 700, height: 1000), horizontalSizeClass: .compact)

        #expect(window.horizontalSizeClass == .compact)
        #expect(window.widthClass == .medium)
    }

    // Flat

    @Test func phonePortraitIsCompactAndFlatWithOneSegment() {
        let window = posture(402, 874)

        #expect(window.widthClass == .compact)
        #expect(window.posture == .flat)
        #expect(window.segments == [CGRect(x: 0, y: 0, width: 402, height: 874)])
    }

    @Test func phoneLandscapeIsExpandedWidthWithCompactHeightAndNotACover() {
        let window = posture(874, 402)

        #expect(window.widthClass == .expanded)
        #expect(window.heightClass == .compact)
        #expect(window.posture == .flat)
    }

    @Test func nonSeparatingFoldDoesNotSplit() {
        let fold = verticalFold(x: 336, height: 841, separating: false)
        let window = posture(673, 841, [fold])

        #expect(window.posture == .flat)
        #expect(window.folds == [fold])
        #expect(window.hinges.isEmpty)
        #expect(window.segments.count == 1)
    }

    // Book

    @Test func bookPostureSplitsAtTheHingeNotAtHalf() {
        let window = posture(673, 841, [verticalFold(x: 300, height: 841)])

        #expect(window.posture == .book)
        #expect(window.segments == [
            CGRect(x: 0, y: 0, width: 300, height: 841),
            CGRect(x: 300, y: 0, width: 373, height: 841),
        ])
    }

    @Test func occludingHingeIsExcludedFromBothSegments() {
        let window = posture(826, 720, [verticalFold(x: 400, height: 720, width: 26)])

        #expect(window.segments == [
            CGRect(x: 0, y: 0, width: 400, height: 720),
            CGRect(x: 426, y: 0, width: 400, height: 720),
        ])
    }

    // Tabletop

    @Test func horizontalHingeIsTabletop() {
        let window = posture(673, 841, [horizontalFold(y: 420, width: 673)])

        #expect(window.posture == .tabletop)
        #expect(window.segments == [
            CGRect(x: 0, y: 0, width: 673, height: 420),
            CGRect(x: 0, y: 420, width: 673, height: 421),
        ])
    }

    // Tri-fold

    @Test func triFoldReportsBothHingesAndThreeSegments() {
        let window = posture(1020, 900, [verticalFold(x: 340, height: 900), verticalFold(x: 680, height: 900)])

        #expect(window.posture == .triFold)
        #expect(window.hinges.count == 2)
        #expect(window.segments.map(\.width) == [340, 340, 340])
    }

    @Test func hingesOutOfOrderStillGiveSegmentsInReadingOrder() {
        let window = posture(1020, 900, [verticalFold(x: 680, height: 900), verticalFold(x: 340, height: 900)])

        #expect(window.segments.map(\.minX) == [0, 340, 680])
    }

    @Test func onlySeparatingFoldsBecomeHinges() {
        let window = posture(1020, 900, [
            verticalFold(x: 340, height: 900),
            verticalFold(x: 680, height: 900, separating: false),
        ])

        #expect(window.posture == .book)
        #expect(window.segments.count == 2)
        #expect(window.folds.count == 2)
    }

    @Test func crossingFoldsAreRefusedRatherThanGuessed() {
        let window = posture(800, 800, [verticalFold(x: 400, height: 800), horizontalFold(y: 400, width: 800)])

        #expect(window.posture == .flat)
        #expect(window.segments.count == 1)
    }

    // Cover

    @Test func coverSizedWindowIsACoverSurface() {
        #expect(posture(332, 348).posture == .cover)
    }

    @Test func smallStageManagerWindowIsACoverSurfaceBecauseTheWindowIsTheOnlyInput() {
        #expect(posture(411, 420).posture == .cover)
    }

    @Test func narrowButTallWindowIsNotACoverSurface() {
        #expect(posture(320, 700).posture == .flat)
    }
}
