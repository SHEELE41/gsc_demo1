package com.mevius.gsc_demo1.ui

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mevius.gsc_demo1.R
import com.mevius.gsc_demo1.service.MainService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val actionManageOverlayPermissionRequestCode = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, actionManageOverlayPermissionRequestCode)
            } else {
                startService(Intent(this@MainActivity, MainService::class.java))
            }
        } else {
            startService(Intent(this@MainActivity, MainService::class.java))
        }

        btn_start_service.setOnClickListener {
            val mIntent = Intent(this, MainService::class.java)
            Toast.makeText(this, "서비스 시작", Toast.LENGTH_SHORT).show()
            startService(mIntent)
        }

        btn_stop_service.setOnClickListener {
            val mIntent = Intent(this, MainService::class.java)
            Toast.makeText(this, "서비스 중지", Toast.LENGTH_SHORT).show()
            stopService(mIntent)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == actionManageOverlayPermissionRequestCode) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
            } else {
                startService(Intent(this@MainActivity, MainService::class.java))
            }
        }
    }
}