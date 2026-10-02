package com.umain.hylla

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.notify.OverdueWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val app = context as HyllaApplication
    private val manager = context.getSystemService(NotificationManager::class.java)

    @Before
    fun allowNotifications() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        app.notifications.set(true)
        manager.cancelAll()
    }

    @After
    fun clear() = manager.cancelAll()

    private fun titles() = manager.activeNotifications.map { it.notification.extras.getString("android.title") }

    @Test
    fun theOverdueCheckNotifiesForEachDeviceKeptTooLong() = runBlocking {
        // Noah has held the Mac mini since March and the Pixel 9 Pro Fold since August.
        app.me.set(PersonId("p-02"))

        val result = TestListenableWorkerBuilder<OverdueWorker>(context).build().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(titles().containsAll(listOf("Mac mini is overdue", "Pixel 9 Pro Fold is overdue")))
    }

    @Test
    fun aWatchedDeviceReturnedToTheShelfIsAnnounced() {
        val trifold = DeviceId("HYL-003")
        if (trifold !in app.watchList.watched.value) app.watchList.toggle(trifold)

        app.store.returnDevice(trifold)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(500)

        assertTrue(titles().contains("TriFold is back on the shelf"))
        assertTrue(trifold !in app.watchList.watched.value)
    }

    @Test
    fun nothingIsPostedWhenTheHolderTurnedNotificationsOff() = runBlocking {
        app.me.set(PersonId("p-02"))
        app.notifications.set(false)

        TestListenableWorkerBuilder<OverdueWorker>(context).build().doWork()

        assertTrue(titles().isEmpty())
    }
}
