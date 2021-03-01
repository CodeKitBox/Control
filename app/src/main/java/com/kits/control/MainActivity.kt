package com.kits.control

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

/**
 * 滑动控件测试demo
 * 1. 简单的滑动控件
 * 2. 作为子控件的内嵌滑动
 * 3. 作为符控件的内嵌滑动
 * 4. API 区别
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnSysSimple).setOnClickListener {
            startActivity(Intent(this,TestScrollViewActivity::class.java))
        }
        findViewById<Button>(R.id.btnSimple).setOnClickListener {
            startActivity(Intent(this,SimpleActivity::class.java))
        }
        findViewById<Button>(R.id.btnSimpleFling).setOnClickListener {
            startActivity(Intent(this,SimpleFlingActivity::class.java))
        }
    }
}