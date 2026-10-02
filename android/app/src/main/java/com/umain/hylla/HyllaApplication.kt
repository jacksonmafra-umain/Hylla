package com.umain.hylla

import android.app.Application
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.MeStore
import com.umain.hylla.fleet.NotificationSettings
import com.umain.hylla.fleet.loadFleet

/**
 * Owns the [FleetStore], so it outlives the Activity. A fold, a rotation or a density change
 * recreates the Activity; the claims made before it must still be there afterwards.
 */
class HyllaApplication : Application() {
    val store: FleetStore by lazy { FleetStore(assets.loadFleet()) }
    val me: MeStore by lazy { MeStore(getSharedPreferences("hylla", MODE_PRIVATE)) }
    val notifications: NotificationSettings by lazy { NotificationSettings(getSharedPreferences("hylla", MODE_PRIVATE)) }
}
