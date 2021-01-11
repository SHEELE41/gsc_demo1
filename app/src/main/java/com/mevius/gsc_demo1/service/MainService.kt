package com.mevius.gsc_demo1.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.mevius.gsc_demo1.R
import com.mevius.gsc_demo1.ui.MainActivity
import kotlinx.android.synthetic.main.overlay_view.view.*


class MainService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var mView: View? = null
    private var textView: TextView? = null
    private var okButton: Button? = null
    private var cancelButton: Button? = null
    private var reverseString = ""
    private val viewRunnable = Runnable {
        mView?.visibility = View.INVISIBLE
    }
    private var nodeInput:AccessibilityNodeInfo? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d("#######################", "Service Started")
    }

    @SuppressLint("InflateParams")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        // TODO FLAG 최적화
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or    // | 쓰면 안되는 듯
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        // TODO 일단 다 !! 붙여놓고 나중에 고치기
        params.gravity = Gravity.START or Gravity.TOP
        mView = inflater.inflate(R.layout.overlay_view, null)
        mView?.visibility = View.INVISIBLE  // 초기 상태 INVISIBLE, 그냥 XML 에서 정해놓을까?

        textView = mView!!.tv_overlay_view
        okButton = mView!!.btn_ok
        cancelButton = mView!!.btn_cancel

        cancelButton?.setOnClickListener {
            mView?.visibility = View.INVISIBLE
        }

        wm!!.addView(mView, params)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        var eventString = ""    // 웬만하면 전역변수로는 쓰면 안될듯.... 상주 서비스라서...
        Log.d("*******################", event?.className.toString())

        if (event?.className.toString() == "android.widget.EditText") {
            nodeInput = event?.source
            Log.d("##################", nodeInput.toString())
        }

        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            eventString = event.text.toString()

            reverseString = eventString.reversed() // 거꾸로 된 스트링

            Toast.makeText(this, eventString, Toast.LENGTH_SHORT).show()
            Log.d("#####################", eventString)
            // sendNotification(eventString)
            textView?.text = reverseString
            mView?.let {
                it.visibility = View.VISIBLE
                it.postDelayed(viewRunnable, 3000)
            }
        }

        val response = reverseString

        okButton?.setOnClickListener {
            Log.d("##################", nodeInput.toString())
            if (Build.VERSION_CODES.JELLY_BEAN_MR2 <= Build.VERSION.SDK_INT) {
                nodeInput?.refresh()
            }
            if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
                val bundle = Bundle()
                bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, response);

                //set the text
                nodeInput?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);
            } else {
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboardManager != null) {
                    var lastClip = "";
                    val clipData = clipboardManager.primaryClip;

                    if (clipData != null) {
                        // TODO 여기 this activity 자리임. service에서 activity 접근하는 방법
                        lastClip = clipData.getItemAt(0).coerceToText(this).toString();
                    }

                    clipboardManager.setPrimaryClip(ClipData.newPlainText("label", response))

                    if (Build.VERSION_CODES.JELLY_BEAN_MR2 <= Build.VERSION.SDK_INT) {
                        nodeInput?.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    } else {
                        nodeInput?.performAction(AccessibilityNodeInfoCompat.ACTION_PASTE);
                    }

                    clipboardManager.setPrimaryClip(ClipData.newPlainText(lastClip, lastClip));
                }
            }
        }
    }

    override fun onInterrupt() {}

    // 방법 - Head-up-Notification
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

        val copyIntent = Intent(this, MainActivity::class.java)
        copyIntent.action = "COPY"
        copyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingCopyIntent = PendingIntent.getActivity(
            this, 0, copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val copyAction = NotificationCompat.Action(
            R.drawable.ic_baseline_notification_important_24,
            "COPY", pendingCopyIntent
        )
        builder.addAction(copyAction)

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

    override fun onDestroy() {
        super.onDestroy()
        if(wm != null){
            if(mView != null) {
                wm!!.removeView(mView)
                mView = null
            }
            wm = null
        }
    }
}