package com.mevius.gsc_demo1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mevius.gsc_demo1.R
import com.mevius.gsc_demo1.service.MainService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}