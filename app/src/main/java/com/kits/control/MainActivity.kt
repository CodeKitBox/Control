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

        findViewById<Button>(R.id.btnSimpleSmooth).setOnClickListener {
            startActivity(Intent(this,SimpleSmoothActivity::class.java))
        }
        findViewById<Button>(R.id.btnSimpleNestChild).setOnClickListener {
            startActivity(Intent(this,SimpleNestedChildActivity::class.java))
        }
        findViewById<Button>(R.id.btnSimpleNestParent).setOnClickListener {
            startActivity(Intent(this,NestedViewActivity::class.java))
            //startActivity(Intent(this,NestedActivity::class.java))
        }
        findViewById<Button>(R.id.btnSystemNestChild).setOnClickListener {
            startActivity(Intent(this,SystemNestedActivity::class.java))
        }
    }
}