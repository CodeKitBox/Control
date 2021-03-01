package com.kits.control

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.kits.control.utils.Faker
import com.kits.control.utils.UiUtils
import com.kits.control.view.MyScrollFlingView

class TestScrollViewActivity : AppCompatActivity(){
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_scroll)
        val myScrollView: ScrollView = findViewById(R.id.myScrollView)
        var llContainer :LinearLayout = findViewById(R.id.llContainer)
        for (i in 0 .. 200){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} $i"
            item.setBackgroundResource(R.drawable.item_background)
            llContainer.addView(item)
        }
        myScrollView.setOnScrollChangeListener(object: View.OnScrollChangeListener{
            override fun onScrollChange(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                println("====myScrollView===")
            }

        })

    }
}