import SwiftUI

/// What the app knows about the device it runs on. Chapter 17 adds registering it.
struct ThisDeviceView: View {
    let posture: WindowPosture

    var body: some View {
        NavigationStack {
            ScrollView {
                PostureReadout(posture: posture)
                    .frame(maxWidth: .infinity)
                    .padding()
            }
            .navigationTitle("This device")
        }
    }
}
