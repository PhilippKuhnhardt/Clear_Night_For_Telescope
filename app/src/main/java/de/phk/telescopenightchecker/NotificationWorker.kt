package de.phk.telescopenightchecker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        //TODO: Send notification
        val CHANNEL_ID = "default"
        var builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("test")
            .setContentText("test")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = 0
        with(NotificationManagerCompat.from(this.applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }


        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}

