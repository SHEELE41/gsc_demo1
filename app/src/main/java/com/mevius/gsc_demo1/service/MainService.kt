package com.mevius.gsc_demo1.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.mevius.gsc_demo1.R
import com.mevius.gsc_demo1.ui.MainActivity

class MainService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("#######################", "Service Started")
        val info = serviceInfo
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        }
        this.serviceInfo = info
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        var eventString = ""    // 웬만하면 전역변수로는 쓰면 안될듯.... 상주 서비스라서...

        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            eventString = event.text.toString()
            Toast.makeText(this, eventString, Toast.LENGTH_SHORT).show()
            Log.d("#####################", eventString)
            sendNotification(eventString)
        }
    }

    override fun onInterrupt() {}

    private fun sendNotification(eventString: String) {
        var notifyManager: NotificationManager? = null
        val NOTIFY_ID = 1002

        val name = "KotlinApplication"
        val id = "kotlin_app"
        val description = "kotlin_app_first_channel"

        val intent: Intent
        val pendingIntent: PendingIntent
        val builder: NotificationCompat.Builder

        if (notifyManager == null) {
            notifyManager = application!!.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = notifyManager.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description
                mChannel.enableVibration(true)
                mChannel.lightColor = Color.GREEN
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notifyManager.createNotificationChannel(mChannel)
            }
        }

        builder = NotificationCompat.Builder(this, id)

        intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        builder.setContentTitle("Heads Up Notification")  // required
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
            .setContentText(eventString)  // required
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setTicker("Notification")
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

        val dismissIntent = Intent(this, MainActivity::class.java)
        dismissIntent.action = "DISMISS"
        dismissIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingDismissIntent = PendingIntent.getActivity(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dismissAction = NotificationCompat.Action(
            R.drawable.ic_baseline_notification_important_24,
            "DISMISS", pendingDismissIntent
        )
        builder.addAction(dismissAction)

        val notification = builder.build()
        notifyManager.notify(NOTIFY_ID, notification)
    }
}