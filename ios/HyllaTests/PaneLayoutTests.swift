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

    @Test func listPaneNeverGrowsPastItsMaximum() {
        for width in stride(from: CGFloat(720), through: 2400, by: 13) {
            let list = panes(width, 1000).panes[0].width
            #expect(list <= PaneLayout.listMaxWidth, "width \(width)")
        }
    }

    @Test func largeFlatWindowShowsListDetailAndHistory() {
        let layout = panes(1376, 1032)
        #expect(layout.paneCount == 3)
        #expect(abs(layout.panes[0].width - 385.28) < 0.01)
        #expect(layout.panes[2].width == 360)
    }

    @Test func atTheLargeBreakpointTheDetailStillGetsItsMinimum() {
        #expect(panes(1200, 900).panes.map(\.width) == [360, 480, 360])
    }

    @Test func triFoldGivesOnePanePerSegment() {
        let layout = panes(1110, 900, hinge(at: 370, height: 900), hinge(at: 740, height: 900))
        #expect(layout.panes.map(\.width) == [370, 370, 370])
    }

    @Test func triFoldHingesInReverseStillGivePanesLeftToRight() {
        let layout = panes(1110, 900, hinge(at: 740, height: 900), hinge(at: 370, height: 900))
        #expect(layout.panes.map(\.minX) == [0, 370, 740])
    }

    @Test func triFoldWithSegmentsBelowTheMinimumCollapses() {
        #expect(panes(1020, 900, hinge(at: 340, height: 900), hinge(at: 680, height: 900)).paneCount == 1)
    }

    @Test func bookPostureSplitsAtTheHinge() {
        #expect(panes(852, 883, hinge(at: 400, height: 883)).panes.map(\.width) == [400, 452])
    }

    @Test func anUnfoldedIPhoneDuoSplitsAtTheHingeNotAt40Percent() {
        let layout = panes(951, 669, hinge(at: 475.5, height: 669))
        #expect(layout.panes.map(\.width) == [475.5, 475.5])
        // Without the fold the same window is a flat 40% split: the list would end 95 pt short of
        // the hinge and the detail would straddle it.
        #expect(abs((panes(951, 669).panes.first?.width ?? 0) - 380.4) < 0.01)
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
