package com.umain.hylla

import android.app.Application
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.MeStore
import com.umain.hylla.fleet.NotificationSettings
import com.umain.hylla.fleet.loadFleet
import com.umain.hylla.notify.Notices
import com.umain.hylla.notify.Notifier
import com.umain.hylla.notify.OverdueWorker
import com.umain.hylla.notify.WatchList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Owns the [FleetStore], so it outlives the Activity. A fold, a rotation or a density change
 * recreates the Activity; the claims made before it must still be there afterwards.
 */
class HyllaApplication : Application() {
    val store: FleetStore by lazy { FleetStore(assets.loadFleet()) }
    val me: MeStore by lazy { MeStore(getSharedPreferences("hylla", MODE_PRIVATE)) }
    val notifications: NotificationSettings by lazy { NotificationSettings(getSharedPreferences("hylla", MODE_PRIVATE)) }
    val watchList: WatchList by lazy { WatchList(getSharedPreferences("hylla", MODE_PRIVATE)) }
    val notifier: Notifier by lazy { Notifier(this, enabled = { notifications.enabled.value }) }

    /** Lives as long as the process, like the store it watches. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        notifier.createChannels()
        OverdueWorker.schedule(this)
        // A watched device that becomes claimable, by a return on this phone or (chapter 16) a
        // sync, is announced once and leaves the watch list.
        scope.launch {
            var before: Fleet? = null
            store.fleet.collect { after ->
                before?.let { previous ->
                    Notices.backOnTheShelf(previous, after, watchList.watched.value).forEach { device ->
                        notifier.backOnTheShelf(device)
                        watchList.remove(device.id)
                    }
                }
                before = after
            }
        }
    }
}
