package com.kits.control

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import com.kits.control.adapter.DefaultAdapter
import com.kits.control.items.FirstItem
import com.kits.control.utils.Faker
import com.kits.control.utils.UiUtils
import com.kits.control.view.MatchHeightRecyclerView
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlin.properties.Delegates

/**
 * 效果：当滑动 MatchHeightRecyclerView 的时候，当 MatchHeightRecyclerView 先让父控件滑动
 * 父控件为ScrollView 判断
 */
class NestedViewActivity : AppCompatActivity(){
    private var llContainer:LinearLayout   by Delegates.notNull()
    private var llBegin:LinearLayout by Delegates.notNull()
    private var llEnd:LinearLayout by Delegates.notNull()
    private var matchHeightRv by Delegates.notNull<MatchHeightRecyclerView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_view)
        llContainer = findViewById(R.id.llContainer)


        llBegin = findViewById(R.id.llBegin)
        llEnd = findViewById(R.id.llEnd)
        matchHeightRv = findViewById(R.id.rvMatchHeight)
        // 设置支持内嵌滑动
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
           // llContainer.isNestedScrollingEnabled = true
            matchHeightRv.isNestedScrollingEnabled = true
        }

        for (i in 0 .. 15){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} $i"
            item.setBackgroundResource(R.drawable.item_background)
            llBegin.addView(item)
        }

        val list = mutableListOf<AbstractFlexibleItem<*>>()
        for (i in 0 .. 100){
            list.add(FirstItem("first $i","${Faker.getRandomWord(1,5)}  first $i"))
        }
        matchHeightRv.layoutManager = LinearLayoutManager(this)
        val adapter = DefaultAdapter(list)
        val mItemDecoration = FlexibleItemDecoration(this)
                .withOffset(8)
                .withEdge(true)
        matchHeightRv.adapter = adapter
        matchHeightRv.addItemDecoration(mItemDecoration!!)

        for (i in 0 .. 15){
            val item = Button(this)
            val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(UiUtils.dip2px(this,10f))
            item.layoutParams = layoutParams
            item.text = "${Faker.getRandomWord(1,5)} $i"
            item.setBackgroundResource(R.drawable.item_background)
            llEnd.addView(item)
        }

    }

}

