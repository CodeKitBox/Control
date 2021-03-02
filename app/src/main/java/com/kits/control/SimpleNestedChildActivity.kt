package com.kits.control

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.kits.control.utils.Faker
import com.kits.control.utils.UiUtils
import com.kits.control.view.MyScrollNestedChildView
import com.kits.control.view.MyScrollView

class SimpleNestedChildActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_nested_child)
        // 父容器
        val parentView:ScrollView = findViewById(R.id.svContainer)
        // 设置父控件支持内嵌滑动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            parentView.isNestedScrollingEnabled = true
        }
        // 子控件 头部
        val llHeader:LinearLayout = findViewById(R.id.llHeader)
        // 头部控件初始化
        for (i in 0 .. 10){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} Header $i"
            item.setBackgroundResource(R.drawable.item_background)
            llHeader.addView(item)
        }
        // 子控件 尾部
        val llFooter:LinearLayout = findViewById(R.id.llFooter)
        for (i in 0 .. 10){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} Footer $i"
            item.setBackgroundResource(R.drawable.item_background)
            llFooter.addView(item)
        }
        // 子控件，内容，支持内嵌滑动
        val myScrollView: MyScrollNestedChildView = findViewById(R.id.myScrollView)
        // 设置子控件支持内嵌滑动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myScrollView.isNestedScrollingEnabled = true
        }
        for (i in 0 .. 50){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} $i"
            item.setBackgroundResource(R.drawable.item_background)
            myScrollView.addView(item)
        }


        // 系统scrollview子控件，内容，支持内嵌滑动
        val sysScrollView: ScrollView = findViewById(R.id.systemSv)
        val llContainer:LinearLayout = findViewById(R.id.llContainer)
        // 设置子控件支持内嵌滑动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sysScrollView.isNestedScrollingEnabled = true
        }
        for (i in 0 .. 50){
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

    }

}