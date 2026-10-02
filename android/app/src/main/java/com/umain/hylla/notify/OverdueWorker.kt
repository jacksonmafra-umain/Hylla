package com.umain.hylla.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.umain.hylla.HyllaApplication
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** Once a day, a notification for each device the phone holder has kept too long. */
class OverdueWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as HyllaApplication
        Notices.overdue(app.store.fleet.value, app.me.me.value, LocalDate.now()).forEach(app.notifier::overdue)
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "overdue",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<OverdueWorker>(1, TimeUnit.DAYS).build(),
            )
        }
    }
}
