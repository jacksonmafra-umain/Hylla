import CoreGraphics
import Testing
@testable import Hylla

struct AdaptiveLayoutTests {
    /// Fleet tile columns for a container `width` wide, as the fleet screen computes them.
    func fleetColumns(_ width: CGFloat, _ widthClass: WidthClass, textScale: CGFloat = 1) -> Int {
        let margin = AdaptiveLayout.margin(widthClass)
        return AdaptiveLayout.columns(
            availableWidth: width - 2 * margin,
            minColumnWidth: AdaptiveLayout.fleetTileMinWidth,
            gutter: AdaptiveLayout.gutter(widthClass),
            textScale: textScale
        )
    }

    @Test func marginsAndGuttersWidenPastCompact() {
        #expect(AdaptiveLayout.margin(.compact) == 16)
        #expect(AdaptiveLayout.margin(.medium) == 24)
        #expect(AdaptiveLayout.gutter(.compact) == 12)
        #expect(AdaptiveLayout.gutter(.large) == 16)
    }

    @Test func phonePortraitHasOneColumn() {
        #expect(fleetColumns(402, .compact) == 1)
    }

    @Test func phoneLandscapeGainsAColumnButStaysOnePane() {
        #expect(fleetColumns(874, .expanded) == 2)
    }

    @Test func tabletPortraitAndLandscapeGainColumns() {
        #expect(fleetColumns(1032, .expanded) == 3)
        #expect(fleetColumns(1376, .large) == 4)
    }

    @Test func columnsNeverStretchPastTwiceTheirMinimum() {
        let gutter: CGFloat = 16
        for width in stride(from: CGFloat(300), through: 2400, by: 7) {
            let available = width - 48
            let count = AdaptiveLayout.columns(
                availableWidth: available, minColumnWidth: AdaptiveLayout.fleetTileMinWidth, gutter: gutter)
            let columnWidth = (available - gutter * CGFloat(count - 1)) / CGFloat(count)
            #expect(columnWidth < AdaptiveLayout.fleetTileMinWidth * 2 + gutter, "width \(width)")
        }
    }

    @Test func neverFewerThanOneColumn() {
        #expect(AdaptiveLayout.columns(availableWidth: 120, minColumnWidth: 280, gutter: 12) == 1)
    }

    @Test func largerTextNeedsWiderColumns() {
        #expect(fleetColumns(1032, .expanded, textScale: 1) == 3)
        #expect(fleetColumns(1032, .expanded, textScale: 1.3) == 2)
        #expect(fleetColumns(1032, .expanded, textScale: 2) == 1)
    }

    @Test func textScaleIsCappedAtTwo() {
        #expect(fleetColumns(1600, .large, textScale: 2) == fleetColumns(1600, .large, textScale: 3.1))
    }

    @Test func smallerTextDoesNotShrinkColumns() {
        #expect(fleetColumns(1032, .expanded, textScale: 0.82) == fleetColumns(1032, .expanded))
    }
}
