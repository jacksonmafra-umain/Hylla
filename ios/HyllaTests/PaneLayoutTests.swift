import CoreGraphics
import Testing
@testable import Hylla

struct PaneLayoutTests {
    func hinge(at x: CGFloat, height: CGFloat, width: CGFloat = 0, vertical: Bool = true) -> Fold {
        Fold(
            frame: vertical ? CGRect(x: x, y: 0, width: width, height: height) : CGRect(x: 0, y: x, width: height, height: width),
            orientation: vertical ? .vertical : .horizontal,
            isSeparating: true,
            occludes: width > 0
        )
    }

    func panes(_ width: CGFloat, _ height: CGFloat, _ folds: Fold...) -> PaneLayout {
        PaneLayout.compute(WindowPosture(size: CGSize(width: width, height: height), folds: folds))
    }

    @Test func phonePortraitIsOnePane() {
        #expect(panes(402, 874).paneCount == 1)
    }

    @Test func phoneLandscapeStaysOnePaneBecauseTheHeightIsCompact() {
        #expect(panes(874, 402).paneCount == 1)
    }

    @Test func mediumWindowTooNarrowForTwoLegiblePanesCollapses() {
        #expect(panes(700, 900).paneCount == 1)
    }

    @Test func iPadPortraitSplitsWithTheListAtFortyPercent() {
        let layout = panes(1032, 1376)
        #expect(layout.panes.map(\.width) == [412.8, 619.2])
    }

    @Test func listPaneStopsGrowingAtItsMaximum() {
        #expect(panes(1376, 1032).panes.first?.width == 480)
    }

    @Test func bookPostureSplitsAtTheHinge() {
        #expect(panes(852, 883, hinge(at: 400, height: 883)).panes.map(\.width) == [400, 452])
    }

    @Test func occludingHingeLeavesAGap() {
        let layout = panes(826, 720, hinge(at: 400, height: 720, width: 26))
        #expect(layout.panes[0].maxX == 400)
        #expect(layout.panes[1].minX == 426)
    }

    @Test func paneBelowTheMinimumCollapsesRatherThanSqueeze() {
        #expect(panes(700, 883, hinge(at: 300, height: 883)).paneCount == 1)
    }

    @Test func tabletopAndCoverKeepTheirOwnSurface() {
        #expect(panes(883, 852, hinge(at: 426, height: 883, vertical: false)).paneCount == 1)
        #expect(panes(332, 348).paneCount == 1)
    }
}
