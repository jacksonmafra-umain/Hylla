import AVFoundation
import Testing
import UserNotifications
@testable import Hylla

struct PermissionStateTests {
    @Test func cameraStatusesMapToStates() {
        #expect(PermissionState(camera: .authorized) == .granted)
        #expect(PermissionState(camera: .notDetermined) == .notAsked)
        #expect(PermissionState(camera: .denied) == .blocked)
        #expect(PermissionState(camera: .restricted) == .blocked)
    }

    @Test func notificationStatusesMapToStates() {
        #expect(PermissionState(notifications: .authorized) == .granted)
        #expect(PermissionState(notifications: .provisional) == .granted)
        #expect(PermissionState(notifications: .notDetermined) == .notAsked)
        #expect(PermissionState(notifications: .denied) == .blocked)
    }
}

struct ScannedCodeTests {
    @Test func aTagsQRCodeHoldsItsDeviceLink() {
        #expect(ScannedCode.parse("hylla://device/HYL-003") == Device.ID(rawValue: "HYL-003"))
    }

    @Test func aBareTagStillScans() {
        #expect(ScannedCode.parse("HYL-003") == Device.ID(rawValue: "HYL-003"))
    }

    @Test func otherCodesAreIgnored() {
        #expect(ScannedCode.parse("https://example.com/HYL-003") == nil)
        #expect(ScannedCode.parse("4006381333931") == nil)
    }
}
