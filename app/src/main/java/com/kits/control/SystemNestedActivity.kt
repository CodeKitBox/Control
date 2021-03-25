package com.kits.control

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.kits.control.utils.Faker
import com.kits.control.utils.UiUtils

/**
 * 系统的内嵌滑动
 */
class SystemNestedActivity :  AppCompatActivity(){
    private lateinit var llFirst:LinearLayout
    private lateinit var listView:ListView
    private lateinit var llSecond:LinearLayout
    private lateinit var svParent:ScrollView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_nested)
        llFirst = findViewById(R.id.llFirst)
        listView = findViewById(R.id.listView)
        llSecond = findViewById(R.id.llSecond)
        /**
         * 1. 只设置父控件支持内嵌滑动
         */
//        svParent = findViewById(R.id.svParent)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            svParent.isNestedScrollingEnabled = true
//        }
        /**
         * 2. 只设置子控件支持内嵌滑动
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.isNestedScrollingEnabled = true
        }

        initFirst()
        initSecond()
        initList()

    }

    private fun initFirst(){
        for (i in 0 .. 30){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} first $i"
            item.setBackgroundResource(R.drawable.item_background)
            llFirst.addView(item)
        }

    }

    private fun initSecond(){
        for (i in 0 .. 30){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            //layoutParams.setMargins(UiUtils.dip2px(requireContext(),10f),UiUtils.dip2px(requireContext(),10f),0,0)
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} second $i"
            item.setBackgroundResource(R.drawable.item_background)
            llSecond.addView(item)
        }

    }

    private fun initList(){
        val adapter:ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_list_item_1,initData())
        listView.adapter = adapter
    }

    private fun initData():MutableList<String>{
        val list = mutableListOf<String>()
        for ( i in 0 .. 100){
            list.add("${Faker.getRandomWord(1,5)} list item  $i")
        }
        return list
    }
}