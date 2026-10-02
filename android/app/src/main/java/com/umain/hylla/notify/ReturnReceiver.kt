package com.umain.hylla.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.umain.hylla.HyllaApplication
import com.umain.hylla.R
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.device

/**
 * The overdue notification's Return action. The app's UI is not on screen, so there is no
 * snackbar host to confirm in: this is the one place Hylla uses a toast.
 */
class ReturnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HyllaApplication
        val device = intent.getStringExtra(EXTRA_DEVICE)?.let { app.store.fleet.value.device(DeviceId(it)) } ?: return
        app.store.returnDevice(device.id).onSuccess {
            app.notifier.cancelOverdue(device)
            Toast.makeText(context, context.getString(R.string.scan_returned, device.deviceName), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_DEVICE = "device"
    }
}
