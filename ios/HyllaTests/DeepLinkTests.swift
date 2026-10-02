import Foundation
import Testing
@testable import Hylla

struct DeepLinkTests {
    @Test func aDeviceLinkResolvesToItsShelfTag() {
        #expect(DeepLink.parse(URL(string: "hylla://device/HYL-003")!) == Device.ID(rawValue: "HYL-003"))
    }

    @Test(arguments: ["hylla://device/hyl-3", "HYLLA://Device/3", "hylla://device/HYL-003/"])
    func handWrittenLinksAreReadLikeTypedTags(link: String) {
        #expect(DeepLink.parse(URL(string: link)!) == Device.ID(rawValue: "HYL-003"))
    }

    @Test(arguments: ["https://device/HYL-003", "hylla://fleet/HYL-003", "hylla://device/", "hylla://device/HYL-003/history"])
    func otherSchemesHostsAndShapesAreNotHyllaLinks(link: String) {
        #expect(DeepLink.parse(URL(string: link)!) == nil)
    }

    @Test func linksRoundTrip() {
        #expect(DeepLink.parse(DeepLink.device(Device.ID(rawValue: "HYL-017"))) == Device.ID(rawValue: "HYL-017"))
    }
}
