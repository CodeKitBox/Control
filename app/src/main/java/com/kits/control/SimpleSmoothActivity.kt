package com.kits.control

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.kits.control.utils.Faker
import com.kits.control.utils.UiUtils
import com.kits.control.view.MyScrollSmoothView
import com.kits.control.view.MyScrollView

class SimpleSmoothActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_smooth)
        val myScrollView:MyScrollSmoothView = findViewById(R.id.myScrollView)

        for (i in 0 .. 100){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} $i"
            item.setBackgroundResource(R.drawable.item_background)
            item.setOnClickListener {
               // 模拟滑动到顶顶部
               // myScrollView.scrollTo(0,0)
                myScrollView.smoothScrollTo(0,0)
            }
            myScrollView.addView(item)
        }

    }

}