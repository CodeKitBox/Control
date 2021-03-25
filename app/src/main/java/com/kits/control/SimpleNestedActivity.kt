package com.kits.control


import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kits.control.adapter.DefaultAdapter
import com.kits.control.items.FirstItem
import com.kits.control.items.SecondItem
import com.kits.control.items.ThirdItem
import com.kits.control.utils.Faker
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem


/**
 * 希望达到的效果
 */

class SimpleNestedActivity : AppCompatActivity(){
    private lateinit var svContainer:ScrollView
    private lateinit var firstRv:RecyclerView
    private lateinit var secondRv:RecyclerView
    private lateinit var thirdRv:RecyclerView

    private var mItemDecoration: FlexibleItemDecoration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_nested)
        svContainer = findViewById(R.id.svContainer)
        firstRv = findViewById(R.id.firstRv)
        secondRv = findViewById(R.id.secondRv)
        thirdRv = findViewById(R.id.thirdRv)
        initFirst()
        initSecond()
        initThird()
    }


    private fun initFirst(){
        val list = mutableListOf<AbstractFlexibleItem<*>>()
        for (i in 0 .. 50){
            list.add(FirstItem("first $i","${Faker.getRandomWord(1,5)}  first $i"))
        }
        firstRv.layoutManager = LinearLayoutManager(this)
        val adapter = DefaultAdapter(list)
        mItemDecoration = FlexibleItemDecoration(this)
                .withOffset(8)
                .withEdge(true)
        firstRv.adapter = adapter
        firstRv.addItemDecoration(mItemDecoration!!)
    }

    private fun initSecond(){
        val list = mutableListOf<AbstractFlexibleItem<*>>()
        for (i in 0 .. 50){
            list.add(SecondItem("second $i","${Faker.getRandomWord(1,5)}  second $i"))
        }
        secondRv.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        val adapter = DefaultAdapter(list)
        mItemDecoration = FlexibleItemDecoration(this)
                .withOffset(8)
                .withEdge(true)
        secondRv.adapter = adapter
        secondRv.addItemDecoration(mItemDecoration!!)
    }

    private fun initThird(){
        val list = mutableListOf<AbstractFlexibleItem<*>>()
        for (i in 0 .. 50){
            list.add(ThirdItem("third $i","${Faker.getRandomWord(1,5)}  third $i"))
        }
        thirdRv.layoutManager = LinearLayoutManager(this)
        val adapter = DefaultAdapter(list)
        mItemDecoration = FlexibleItemDecoration(this)
                .withOffset(8)
                .withEdge(true)
        thirdRv.adapter = adapter
        thirdRv.addItemDecoration(mItemDecoration!!)
    }


}



